DROP TABLE IF EXISTS `SYS_ROLE`;
DROP TABLE IF EXISTS `SYS_USER_ROLE`;
DROP TABLE IF EXISTS `SYS_USER`;

create table SYS_ROLE
(
    ID   INT not null primary key,
    NAME VARCHAR(255) not null
);

create table SYS_USER
(
    ID       INT not null primary key,
    NAME     VARCHAR(255) not null,
    PASSWORD VARCHAR(255) not null
);

create table SYS_USER_ROLE
(
    USER_ID INT not null,
    ROLE_ID INT not null,
    constraint pk_1 primary key (ROLE_ID, USER_ID),
    constraint fk_1 foreign key (ROLE_ID) references SYS_ROLE (ID) on update cascade on delete cascade,
    constraint fk_2 foreign key (USER_ID) references SYS_USER (ID) on update cascade on delete cascade
);

CREATE TABLE persistent_logins (
	`username` VARCHAR(64) NOT NULL,
	`series` VARCHAR(64) PRIMARY KEY,
	`token` VARCHAR(64) NOT NULL,
	`last_used` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
)ENGINE=INNODB DEFAULT CHARSET=utf8;