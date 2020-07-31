package dk.digitalidentity.rc.dao.model;

import java.util.Date;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import lombok.Getter;
import lombok.Setter;

@Entity(name = "title_rolegroups")
@Getter
@Setter
public class TitleRoleGroupAssignment {

    @Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "title_uuid")
	private Title title;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "rolegroup_id")
	private RoleGroup roleGroup;
	
	@Column
	private String assignedByUserId;
	
	@Column
	private String assignedByName;
	
	@Column
	private Date assignedTimestamp;
	
	@ElementCollection(targetClass = String.class)
	@CollectionTable(name = "title_rolegroups_ous", joinColumns = @JoinColumn(name = "title_rolegroups_id"))
	@Column(name = "ou_uuid")
	private List<String> ouUuids;
}
