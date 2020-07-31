package dk.digitalidentity.rc.task;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import dk.digitalidentity.rc.dao.model.ConstraintTypeValueSet;
import dk.digitalidentity.rc.dao.model.PendingKOMBITUpdate;
import dk.digitalidentity.rc.dao.model.SystemRoleAssignment;
import dk.digitalidentity.rc.dao.model.SystemRoleAssignmentConstraintValue;
import dk.digitalidentity.rc.dao.model.UserRole;
import dk.digitalidentity.rc.service.PendingKOMBITUpdateService;
import dk.digitalidentity.rc.service.UserRoleService;
import dk.digitalidentity.rc.service.kombit.dto.KOMBITBrugersystemrolleDataafgraensningerDTO;
import dk.digitalidentity.rc.service.kombit.dto.KOMBITDataafgraensningsVaerdier;
import dk.digitalidentity.rc.service.kombit.dto.KOMBITUserRoleDTO;
import dk.digitalidentity.rc.util.IdentifierGenerator;
import lombok.extern.log4j.Log4j;

@Log4j
@Component
@EnableScheduling
@Transactional
public class UpdateKOMBITTask {

	@Value("${kombit.url}")
	private String baseUrl;

	@Value("${kombit.domain}")
	private String domain;

	@Value("${kombit.enabled}")
	private boolean kombitEnabled;
	
	@Value("${customer.cvr}")
	private String cvr;

	@Value("${scheduled.enabled:false}")
	private boolean runScheduled;

	@Autowired
	@Qualifier("kombitRestTemplate")
	private RestTemplate restTemplate;

	@Autowired
	private PendingKOMBITUpdateService pendingKOMBITUpdateService;

	@Autowired
	private UserRoleService userRoleService;

	private boolean initialized = false;

	@PostConstruct
	public void init() {
		if (runScheduled && kombitEnabled) {
			initialized = true;
			log.info("KOMBIT synchronization enabled on this instance!");
		}
		else {
			log.info("KOMBIT synchronization disabled on this instance!");
		}
	}

	// we have had some issues with this task not running, so just check every weekday
	@Scheduled(cron = "0 0 10 * * MON-FRI")
	public void verifyTaskIsRunning() {
		if (!runScheduled) {
			return;
		}

    	Calendar cal = Calendar.getInstance();
    	cal.add(Calendar.DATE, -2);
    	Date twoDaysAgo = cal.getTime();

		List<PendingKOMBITUpdate> pendingUpdates = pendingKOMBITUpdateService.findAll();
		for (PendingKOMBITUpdate pending : pendingUpdates) {
			if (pending.getTimestamp().before(twoDaysAgo)) {
				log.error("KOMBIT synchronization has stopped!");
				break;
			}
		}
	}

	@Scheduled(fixedRate = 5 * 60 * 1000)
	public void processUserRolesFromUpdateQueue() {
		if (!runScheduled || !initialized) {
			log.debug("Scheduled jobs are disabled on this instance");
			return;
		}

		List<PendingKOMBITUpdate> pendingUserRoleUpdates = pendingKOMBITUpdateService.findAll();

		for (Iterator<PendingKOMBITUpdate> iterator = pendingUserRoleUpdates.iterator(); iterator.hasNext();) {
			PendingKOMBITUpdate pendingKOMBITUpdate = iterator.next();
			boolean success = false;

			switch (pendingKOMBITUpdate.getEventType()) {
				case DELETE:
					success = deleteUserRole(pendingKOMBITUpdate);
					break;
				case UPDATE:
					if (pendingKOMBITUpdate.getUserRoleUuid() == null) {
						success = createUserRole(pendingKOMBITUpdate);
					}
					else {
						success = updateUserRole(pendingKOMBITUpdate);
					}

					break;
			}

			if (success) {
				pendingKOMBITUpdateService.delete(pendingKOMBITUpdate);
			}
		}
	}

	private boolean createUserRole(PendingKOMBITUpdate pendingKOMBITUpdate) {		
		UserRole userRole = userRoleService.getById(pendingKOMBITUpdate.getUserRoleId());
		if (userRole == null) {
			log.warn("Attempted to synchronize UserRole with id " + pendingKOMBITUpdate.getUserRoleId() + " but it does not exist anymore");
			
			// we return true to get it removed from the queue
			return true;
		}
		
		if (userRole.getDelegatedFromCvr() != null) {
			log.warn("Attempting to synchronize UserRole with id " + userRole.getId() + " which is a delegated role");
			return true;
		}

		String entityId = IdentifierGenerator.buildKombitIdentifier(userRole.getIdentifier(), domain);
		List<KOMBITBrugersystemrolleDataafgraensningerDTO> brugersystemrolleDataafgraensninger = new ArrayList<>();

		KOMBITUserRoleDTO userRoleDTO = new KOMBITUserRoleDTO();
		userRoleDTO.setNavn(userRole.getName());
		userRoleDTO.setEntityId(entityId);
		userRoleDTO.setOrganisationCvr(cvr);
		userRoleDTO.setBeskrivelse(userRole.getDescription());
		userRoleDTO.setBrugersystemrolleDataafgraensninger(brugersystemrolleDataafgraensninger);

		// map all assigned systemroles and constraints to KOMBIT data-structure
		mapRoleAssignments(userRole, brugersystemrolleDataafgraensninger);

		// perform create (POST)
		try {
			String url = baseUrl + "/jobfunktionsroller";

			HttpEntity<KOMBITUserRoleDTO> userRoleResponse = restTemplate.postForEntity(url, userRoleDTO, KOMBITUserRoleDTO.class);

			KOMBITUserRoleDTO userRoleResponseDTO = userRoleResponse.getBody();
			userRole.setUuid(userRoleResponseDTO.getUuid());

			userRoleService.save(userRole);
			
			return true;
		}
		catch (HttpClientErrorException ex) {
			log.error("Failed to create UserRole: " + userRole.getId() + " / " + ex.getResponseBodyAsString());
		}
		
		return false;
	}

	private boolean updateUserRole(PendingKOMBITUpdate pendingKOMBITUpdate) {
		UserRole userRole = userRoleService.getById(pendingKOMBITUpdate.getUserRoleId());
		if (userRole == null) {
			log.warn("Attempted to synchronize UserRole with id " + pendingKOMBITUpdate.getUserRoleId() + " but it does not exist anymore");
			
			// we return true to get it removed from the queue
			return true;
		}

		if (userRole.getDelegatedFromCvr() != null) {
			log.warn("Attempting to synchronize UserRole with id " + userRole.getId() + " which is a delegated role");
			return true;
		}

		List<KOMBITBrugersystemrolleDataafgraensningerDTO> brugersystemrolleDataafgraensninger = new ArrayList<>();
		String url = baseUrl + "/jobfunktionsroller/" + pendingKOMBITUpdate.getUserRoleUuid();

		// read existing UserRole from KOMBIT (fallback to create-case if it does not exist)
		ResponseEntity<KOMBITUserRoleDTO> userRoleResponse = restTemplate.exchange(url, HttpMethod.GET, null, KOMBITUserRoleDTO.class);
		if (userRoleResponse.getStatusCodeValue() > 299) {
			return createUserRole(pendingKOMBITUpdate);
		}

		KOMBITUserRoleDTO userRoleDTO = userRoleResponse.getBody();
		userRoleDTO.setBeskrivelse(userRole.getDescription());
		userRoleDTO.setNavn(userRole.getName());
		userRoleDTO.setBrugersystemrolleDataafgraensninger(brugersystemrolleDataafgraensninger);

		mapRoleAssignments(userRole, brugersystemrolleDataafgraensninger);

		// write back
		try {
			restTemplate.put(url, userRoleDTO);

			return true;
		}
		catch (HttpClientErrorException ex) {
			log.error("Failed to update UserRole: " + userRole.getId() + " / " + ex.getResponseBodyAsString());
		}
				
		return false;
	}
	
	private boolean deleteUserRole(PendingKOMBITUpdate pendingKOMBITUpdate) {
		if (pendingKOMBITUpdate.getUserRoleUuid() == null) {
			log.error("Cannot delete UserRole without UUID.");

			return true;
		}

		String url = baseUrl + "/jobfunktionsroller/" + pendingKOMBITUpdate.getUserRoleUuid();

		try {
			restTemplate.delete(url);

			return true;
		}
		catch (HttpClientErrorException ex) {
			log.error("Failed to delete UserRole: " + pendingKOMBITUpdate.getUserRoleUuid() + " / " + ex.getResponseBodyAsString());
		}

		return false;
	}
	
	private void mapRoleAssignments(UserRole userRole, List<KOMBITBrugersystemrolleDataafgraensningerDTO> brugersystemrolleDataafgraensninger) {
		for (SystemRoleAssignment systemRoleAssignment : userRole.getSystemRoleAssignments()) {
			KOMBITBrugersystemrolleDataafgraensningerDTO systemRoleAssignmentDTO = new KOMBITBrugersystemrolleDataafgraensningerDTO();
			systemRoleAssignmentDTO.setBrugersystemrolleUuid(systemRoleAssignment.getSystemRole().getUuid());

			List<KOMBITDataafgraensningsVaerdier> dataafgraensningsVaerdier = new ArrayList<>();
			for (SystemRoleAssignmentConstraintValue constraintValue : systemRoleAssignment.getConstraintValues()) {
				KOMBITDataafgraensningsVaerdier dataConstraintValue = new KOMBITDataafgraensningsVaerdier();

				switch (constraintValue.getConstraintValueType()) {
					case EXTENDED_INHERITED:
					case INHERITED:
					case LEVEL_1:
					case LEVEL_2:
					case LEVEL_3:
					case LEVEL_4:
						dataConstraintValue.setDynamisk(true);
						dataConstraintValue.setVaerdi(constraintValue.getConstraintIdentifier());
						break;
					case VALUE:
						dataConstraintValue.setDynamisk(false);
						dataConstraintValue.setVaerdi(fixConstraintValue(userRole, constraintValue));
						break;
				}

				dataConstraintValue.setDataafgraensningstypeEntityId(constraintValue.getConstraintType().getEntityId());
				dataConstraintValue.setDataafgraensningstypeNavn(constraintValue.getConstraintType().getName());

				dataafgraensningsVaerdier.add(dataConstraintValue);
			}

			systemRoleAssignmentDTO.setDataafgraensningsVaerdier(dataafgraensningsVaerdier);
			brugersystemrolleDataafgraensninger.add(systemRoleAssignmentDTO);
		}
	}

	// We want to perform a bit of validation before we send the value,
	// as the vendor of the it-system might have changed the validation rules,
	// and the change that triggered a re-provision might not have affected
	// the constraint values. We cannot catch everything, but some cases we
	// might be able to cleanup combined with a bit of WARN logging
	private String fixConstraintValue(UserRole userRole, SystemRoleAssignmentConstraintValue constraintValue) {
		if (constraintValue.getConstraintValue() == null) {
			return null;
		}

		String value = constraintValue.getConstraintValue();
		switch (constraintValue.getConstraintType().getUiType()) {
			case COMBO_MULTI:
				String[] values = value.split(",");
				StringBuilder builder = new StringBuilder();
				
				for (String val : values) {
					boolean found = false;

					for (ConstraintTypeValueSet ctvs : constraintValue.getConstraintType().getValueSet()) {
						if (ctvs.getConstraintKey().equals(val)) {
							found = true;
							break;
						}
					}
					
					if (!found) {
						log.warn("Skipping constraint value '" + val + "' on userRole " + userRole.getId() + " because it is no longer a valid constraint value");
					}
					else {
						if (builder.length() > 0) {
							builder.append(",");
						}

						builder.append(val);
					}
				}
				
				value = builder.toString();
				break;
			case COMBO_SINGLE:
			case REGEX:
				// nothing we can do for these
				break;
		}
		
		return value;
	}
}
