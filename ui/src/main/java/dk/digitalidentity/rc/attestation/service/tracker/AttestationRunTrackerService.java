package dk.digitalidentity.rc.attestation.service.tracker;

import dk.digitalidentity.rc.attestation.dao.AttestationDao;
import dk.digitalidentity.rc.attestation.dao.AttestationRunDao;
import dk.digitalidentity.rc.attestation.model.entity.Attestation;
import dk.digitalidentity.rc.attestation.model.entity.AttestationRun;
import dk.digitalidentity.rc.config.RoleCatalogueConfiguration;
import dk.digitalidentity.rc.dao.model.enums.CheckupIntervalEnum;
import dk.digitalidentity.rc.service.SettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

import static dk.digitalidentity.rc.attestation.AttestationConstants.FINISHED_DAYS_AFTER_DEADLINE;

@Service
public class AttestationRunTrackerService {
    @Autowired
    private RoleCatalogueConfiguration configuration;
    @Autowired
    private SettingsService settingsService;
    @Autowired
    private AttestationRunDao attestationRunDao;
    @Autowired
    private AttestationDao attestationDao;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateRuns(final LocalDate when) {
        // Check if any runs have expired
        attestationRunDao.findByFinishedFalse().stream()
                .filter(r -> r.getDeadline().isBefore(LocalDate.now().minusDays(FINISHED_DAYS_AFTER_DEADLINE)))
                .forEach(r -> r.setFinished(true));
        // Now check if we need to create a new run
        final LocalDate deadlineNormal = findNextAttestationDate(when, false);
        final LocalDate deadlineSensitive = findNextAttestationDate(when, true);
        // Check if we already have an attestation run active
        final boolean normalRun = shouldCreateAttestationRun(when, deadlineNormal);
        final boolean sensitiveRun = !normalRun && shouldCreateAttestationRun(when, deadlineSensitive);
        if (normalRun || sensitiveRun) {
            getAttestationRun(when)
                    .orElseGet(() -> createNewAttestationRun(sensitiveRun ? deadlineSensitive : deadlineNormal, sensitiveRun));
        }
    }

    /**
     * This method will create {@link AttestationRun}s for the old {@link dk.digitalidentity.rc.attestation.model.entity.Attestation}s
     * which was created before the {@link AttestationRun} where introduced.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void migrateAttestationsWithoutRun() {
        // TODO Remove this method when all have been migrated - probably 2024r3
        attestationDao.findByAttestationRunIsNull().forEach(this::findOrCreateRunFor);
    }

    @SuppressWarnings("deprecation")
    private void findOrCreateRunFor(final Attestation att) {
        if (att.getAttestationRun() == null) {
            attestationRunDao.findByDeadlineIs(att.getDeadline())
                    .ifPresentOrElse(att::setAttestationRun,
                            () -> att.setAttestationRun(createNewAttestationRun(att.getDeadline(), att.isSensitive())));
        }
    }

    public Optional<AttestationRun> getAttestationRun(final LocalDate when) {
        return attestationRunDao.findFirstByFinishedFalseAndDeadlineGreaterThanEqual(when);
    }

    private AttestationRun createNewAttestationRun(final LocalDate deadline, final boolean sensitive) {
        final AttestationRun attestationRun = AttestationRun.builder()
                .createdAt(LocalDate.now())
                .superSensitive(false) // TODO
                .sensitive(sensitive)
                .finished(false)
                .deadline(deadline)
                .build();
        return attestationRunDao.save(attestationRun);
    }

    private LocalDate findNextAttestationDate(final LocalDate when, final boolean sensitive) {
        final CheckupIntervalEnum interval = settingsService.getScheduledAttestationInterval();
        LocalDate deadline = settingsService.getFirstAttestationDate();
        while (deadline.isBefore(when)) {
            deadline = deadline.plusMonths(sensitive
                    ? sensitiveIntervalToMonths(interval)
                    : intervalToMonths(interval));
        }
        return deadline;
    }

    private boolean shouldCreateAttestationRun(final LocalDate now, final LocalDate deadline) {
        return !deadline.minusDays(configuration.getAttestation().getDaysForAttestation()).isAfter(now);
    }

    /**
     * Intervals are halved for sensitive roles.
     */
    private static int sensitiveIntervalToMonths(final CheckupIntervalEnum intervalEnum) {
        return switch (intervalEnum) {
            case YEARLY -> 6;
            case EVERY_HALF_YEAR -> 3;
        };
    }

    private static int intervalToMonths(final CheckupIntervalEnum intervalEnum) {
        return switch (intervalEnum) {
            case YEARLY -> 12;
            case EVERY_HALF_YEAR -> 6;
        };
    }

}
