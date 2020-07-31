-- updating to spring boot 2.1 requires new session tables
IF OBJECT_ID('SPRING_SESSION_ATTRIBUTES', 'U') IS NOT NULL DROP TABLE SPRING_SESSION_ATTRIBUTES;
IF OBJECT_ID('SPRING_SESSION', 'U') IS NOT NULL DROP TABLE SPRING_SESSION;
