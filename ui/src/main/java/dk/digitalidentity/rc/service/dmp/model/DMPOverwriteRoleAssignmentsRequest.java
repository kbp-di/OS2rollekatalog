package dk.digitalidentity.rc.service.dmp.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class DMPOverwriteRoleAssignmentsRequest {
	public List<DMPOverwriteRoleAssignments> userRoleAssignments;
}
