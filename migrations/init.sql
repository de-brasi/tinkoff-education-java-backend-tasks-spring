--liquibase formatted sql

-- TODO:
--  Понять зачем в задаче дали ссылку на дизайн liquibase-проекта
--      (https://docs.liquibase.com/start/design-liquibase-project.html)

--changeset ilya:init-table-links
create table links
(
    id  bigint generated always as identity primary key,
    url text not null
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
    foreign key (telegram_chat_id) references telegram_chat (id)
)
