--liquibase formatted sql

--changeset ilya:add-service-stackoverflow
insert into supported_services(name) values ('stackoverflow');

--changeset ilya:add-service-github
insert into supported_services(name) values ('github');
