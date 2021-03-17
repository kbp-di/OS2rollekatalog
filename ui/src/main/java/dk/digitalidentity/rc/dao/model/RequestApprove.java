package dk.digitalidentity.rc.dao.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import dk.digitalidentity.rc.dao.model.enums.EntityType;
import dk.digitalidentity.rc.dao.model.enums.RequestApproveStatus;
import dk.digitalidentity.rc.log.AuditLoggable;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "request_approve")
@Getter
@Setter
public class RequestApprove implements AuditLoggable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "requester_uuid")
	private User requester;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "manager_uuid")
	private User manager;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "assigner_uuid")
	private User assigner;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private EntityType roleType;

	@Column
	private Long roleId;
	
	@Column
	private String reason;
	
	@Column
	private String rejectReason;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private RequestApproveStatus status;
	
	@Column
	private boolean roleAssignerNotified;
	
	@Column
	private Date requestTimestamp;

	@Column
	private Date statusTimestamp;

	@JsonIgnore
	@Override
	public String getEntityId() {
		return Long.toString(id);
	}

	@Override
	public String getEntityName() {
		return "Request"; // TODO: anything better to put in this column?
	}
}