ALTER TABLE user_roles MODIFY COLUMN name VARCHAR(128) NOT NULL;
ALTER TABLE history_user_roles MODIFY COLUMN user_role_name VARCHAR(128);