package dk.digitalidentity.rc.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import dk.digitalidentity.rc.dao.KleDao;
import dk.digitalidentity.rc.dao.model.Kle;
import dk.kleonline.xml.EmneKomponent;
import dk.kleonline.xml.GruppeKomponent;
import dk.kleonline.xml.HovedgruppeKomponent;
import dk.kleonline.xml.KLEEmneplanKomponent;
import lombok.extern.log4j.Log4j;

@Component
@EnableScheduling
@EnableAsync
@Log4j
public class ReadKleTask {
	private static Map<String, String> kleCacheMap = new HashMap<>();

	@Value("${scheduled.enabled:false}")
	private boolean runScheduled;

	@Autowired
	@Qualifier("defaultRestTemplate")
	private RestTemplate restTemplate;

	@Autowired
	private KleDao kleDao;
	
	@Async
	public void init() {
		if (runScheduled && kleDao.countByActiveTrue() == 0) {
			parse();
		}
		else {
			// even non scheduled instances should populate the cache
			loadCache();
		}
	}

	private void loadCache() {
		Map<String, String> newKleCacheMap = new HashMap<>();

		List<Kle> kleList = kleDao.findAll();		
		for (Kle kle : kleList) {
			newKleCacheMap.put(kle.getCode(), kle.getName());
		}
		
		kleCacheMap = newKleCacheMap;
	}

	// Run every Saturday at 22:00
	@Scheduled(cron = "${kle.cron.refresh:0 0 22 * * SAT}")
	@Transactional(rollbackFor = Exception.class)
	public synchronized void reloadCache() {
		if (runScheduled) {
			return; // do not reload cache on the instance that is running the scheduled task
		}
		
		log.info("Refreshing KLE cache");

		loadCache();
	}

	// Run every Saturday at 21:00
	@Scheduled(cron = "${kle.cron:0 0 21 * * SAT}")
	@Transactional(rollbackFor = Exception.class)
	public synchronized void parse() {
		if (!runScheduled) {
			return;
		}

		log.info("Fetching KLE from kle-online and refreshing cache");

		Map<String, String> newCacheMap = new HashMap<>();
		List<Kle> updatedKleList = getUpdatedKleList();
		List<Kle> kleList = kleDao.findAll();

		for (Kle updatedKle : updatedKleList) {
			boolean found = false;
			boolean nameChange = false;
			boolean activate = false;

			for (Iterator<Kle> iterator = kleList.iterator(); iterator.hasNext();) {
				Kle kle = iterator.next();

				if (kle.getCode().equals(updatedKle.getCode())) {
					found = true;

					if (!kle.getName().equals(updatedKle.getName())) {
						nameChange = true;
					}

					if (!kle.isActive()) {
						activate = true;
					}

					iterator.remove();
					break;
				}
			}

			if (found && (nameChange || activate)) {
				Kle kle = kleDao.getByCode(updatedKle.getCode());
				kle.setName(updatedKle.getName());
				kle.setActive(true);

				kleDao.save(kle);
			}
			else if (!found) {
				kleDao.save(updatedKle);
			}
			
			newCacheMap.put(updatedKle.getCode(), updatedKle.getName());
		}

		// Deactivate whatever is left in the list
		for (Kle inactiveKle : kleList) {
			Kle kle = kleDao.getByCode(inactiveKle.getCode());
			kle.setActive(false);

			kleDao.save(kle);
		}
		
		kleCacheMap = newCacheMap;
	}

	public List<Kle> getUpdatedKleList() {
		List<Kle> updatedKleList = new ArrayList<>();

		HttpEntity<KLEEmneplanKomponent> responseRootEntity = restTemplate.getForEntity("https://www.klxml.dk/download/XML-ver2-0/KLE-Emneplan_Version2-0.xml", KLEEmneplanKomponent.class);

		KLEEmneplanKomponent kleKLEEmneplan = responseRootEntity.getBody();

		for (HovedgruppeKomponent hg : kleKLEEmneplan.getHovedgruppe()) {
			Kle kle = new Kle();
			kle.setCode(hg.getHovedgruppeNr());
			kle.setName(hg.getHovedgruppeTitel());
			kle.setActive(true);
			kle.setParent("0");
			
			updatedKleList.add(kle);
			
			for (GruppeKomponent group : hg.getGruppe()) {
				Kle groupKle = new Kle();
				groupKle.setCode(group.getGruppeNr());
				groupKle.setName(group.getGruppeTitel());
				groupKle.setActive(true);			
				groupKle.setParent(group.getGruppeNr().substring(0, 2));

				updatedKleList.add(groupKle);
				
				for (EmneKomponent subject : group.getEmne()) {
					Kle emneKle = new Kle();
					emneKle.setCode(subject.getEmneNr());
					emneKle.setName(subject.getEmneTitel());
					emneKle.setActive(true);
					emneKle.setParent(subject.getEmneNr().substring(0, 5));

					updatedKleList.add(emneKle);
				}

			}

		}

		return updatedKleList;
	}
	
	public static String getName(String code) {
		return kleCacheMap.get(code);
	}
}
