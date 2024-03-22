--liquibase formatted sql

--changeset ilya:init-table-supported-services
create table supported_services
(
    id  bigint generated always as identity primary key,
    name text not null
);

--changeset ilya:init-table-links
create table links
(
    id  bigint generated always as identity primary key,
    url text not null,
    last_check_time timestamp with time zone not null,
    last_update_time timestamp with time zone not null,
    service bigint not null,
    snapshot json not null,
    foreign key (service) references supported_services(id),
    unique (url)
);

--changeset ilya:init-table-telegram-chat
create table telegram_chat
(
    id      bigint generated always as identity primary key,
    chat_id bigint not null,
    unique (chat_id)
);

--changeset ilya:init-relation-table-track-info
create table track_info
(
    id               bigint generated always as identity primary key,
    telegram_chat_id bigint not null,
    link_id          bigint not null,
    foreign key (link_id) references links (id),
    foreign key (telegram_chat_id) references telegram_chat (id) on delete cascade,
    unique (telegram_chat_id, link_id)
)
