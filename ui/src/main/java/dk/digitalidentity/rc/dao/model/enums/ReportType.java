package dk.digitalidentity.rc.dao.model.enums;
import lombok.Getter;

@Getter
public enum ReportType {
	USER_ROLE_WITH_SYSTEM_ROLE_THAT_COULD_BE_CONSTRAINT_BUT_ISNT("html.report.user_roles_w_sys_role_but_no_constraint");

	private String title;

	private ReportType(String title) {
		this.title = title;
	}
}
