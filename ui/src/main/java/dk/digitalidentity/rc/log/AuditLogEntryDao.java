package dk.digitalidentity.rc.log;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dk.digitalidentity.rc.dao.model.AuditLog;

public interface AuditLogEntryDao extends JpaRepository<AuditLog, Long> {
	void deleteByTimestampBefore(Date before);

	@Query(value = "SELECT max(id) FROM audit_log", nativeQuery = true)
	long getMaxId();

	@Query(value = "SELECT a.* FROM audit_log a WHERE a.id > ?1 ORDER BY a.id ASC LIMIT ?2", nativeQuery = true)
	public List<AuditLog> findAllWithOffsetAndSize(long offset, long size);

}
