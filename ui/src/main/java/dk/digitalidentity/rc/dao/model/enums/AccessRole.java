package dk.digitalidentity.rc.dao.model.enums;

public enum AccessRole {
	ORGANISATION("html.enum.accessrole.organisation"),
	READ_ACCESS("html.enum.accessrole.readAccess"),
	ROLE_MANAGEMENT("html.enum.accessrole.roleManagement"),
	ADMINISTRATOR("html.enum.accessrole.administrator");

	private String messageId;

	private AccessRole(String messageId) {
		this.messageId = messageId;
	}

	public String getMessageId() {
		return messageId;
	}
}