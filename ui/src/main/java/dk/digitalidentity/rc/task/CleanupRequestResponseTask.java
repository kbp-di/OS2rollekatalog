package dk.digitalidentity.rc.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import dk.digitalidentity.rc.service.RequestApproveService;
import dk.digitalidentity.rc.service.SettingsService;
import lombok.extern.log4j.Log4j;

@Component
@EnableScheduling
@Log4j
public class CleanupRequestResponseTask {

	@Autowired
	private RequestApproveService requestApproveService;

	@Autowired
	private SettingsService settingsService;

	@Value("${scheduled.enabled:false}")
	private boolean runScheduled;
		
	// Run daily at 04:00
	@Scheduled(cron = "0 0 4 * * ?")
	public void sendNotifications() {
		if (!runScheduled) {
			log.debug("Scheduled jobs are disabled on this instance");
			return;
		}

		if (!settingsService.isRequestApproveEnabled()) {
			return;
		}

		requestApproveService.deleteOld();
	}
}
