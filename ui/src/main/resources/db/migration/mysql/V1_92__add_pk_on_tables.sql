ALTER TABLE constraint_type_value_sets ADD COLUMN id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT FIRST;
ALTER TABLE ous_itsystems ADD COLUMN id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT FIRST;
ALTER TABLE report_template_user ADD COLUMN id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT FIRST;
ALTER TABLE system_role_supported_constraints ADD COLUMN id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT FIRST;
ALTER TABLE title_rolegroups_ous ADD COLUMN id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT FIRST;
ALTER TABLE title_roles_ous ADD COLUMN id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT FIRST;
