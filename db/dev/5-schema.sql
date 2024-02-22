create table if not exists t_user
(
    id          bigserial
        constraint t_user_pk
            primary key,
    username    text                     not null,
    realname    text                     not null,
    password    text                     not null,
    create_time timestamp with time zone not null,
    join_time   timestamp with time zone,
    contact        jsonb not null,
    expire_time    timestamp with time zone default null,
    expire_comment text  not null           default ''
);

comment on column t_user.join_time is '加入大艺团的时间';

comment on column t_user.contact is '联系方式，结构为 map<name: str, contact: str>';

alter table t_user
    owner to postgres;

create unique index IF NOT EXISTS t_user_username_uindex
    on t_user (username);

create table if not exists announcement
(
    id          bigserial
        constraint announcement_pk
            primary key,
    content     text      not null,
    is_pinned   boolean   not null,
    create_time timestamp not null,
    author_id   bigint    not null
        constraint announcement_t_user_id_fk
            references t_user
);

alter table announcement
    owner to postgres;

create table if not exists band
(
    id        serial
        constraint band_pk
            primary key,
    name      text   not null,
    leader_id bigint not null
        constraint band_t_user_id_fk
            references t_user,
    constraint band_name_leader_unique
        unique (name, leader_id)
);

alter table band
    owner to postgres;

create table if not exists place
(
    id   serial
        constraint place_pk
            primary key,
    name text not null
        constraint place_pk_2
            unique
);

alter table place
    owner to postgres;

create table if not exists appointment
(
    id         bigserial
        constraint appointment_pk
            primary key,
    name       text                     not null,
    start_time timestamp with time zone not null,
    end_time   timestamp with time zone not null,
    place_id   integer                  not null
        constraint appointment_place_id_fk
            references place,
    band_id    integer                  not null
        constraint appointment_band_id_fk
            references band,
    constraint time_range_check
        check ((end_time > start_time) AND ((end_time)::date = (start_time)::date))
);

alter table appointment
    owner to postgres;

create table if not exists operation_log
(
    id                 uuid                     not null
        constraint operate_log_pk
            primary key,
    target_module_name text                     not null,
    data_before        text,
    data_after         text,
    create_time        timestamp with time zone not null,
    operator_id        bigint                   not null
        constraint operate_log_t_user_id_fk
            references t_user
);

alter table operation_log
    owner to postgres;

create index IF NOT EXISTS operate_log_create_time_index
    on operation_log (create_time desc);

create index IF NOT EXISTS operate_log_target_module_name_index
    on operation_log using hash (target_module_name);

create table if not exists band_user_mapping
(
    band_id integer not null
        constraint band_user_mapping_band_id_fk
            references band,
    user_id bigint  not null
        constraint band_user_mapping_t_user_id_fk
            references t_user,
    constraint band_user_mapping_pk
        primary key (user_id, band_id)
);

alter table band_user_mapping
    owner to postgres;

create table if not exists log_notice
(
    id          uuid    not null
        constraint log_notice_pk_2
            primary key,
    log_id      uuid    not null
        constraint log_notice_operation_log_id_fk
            references operation_log,
    receiver_id bigint  not null
        constraint log_notice_t_user_id_fk
            references t_user,
    is_read     boolean not null,
    constraint log_notice_pk
        unique (receiver_id, log_id)
);

alter table log_notice
    owner to postgres;

create table if not exists equipment
(
    id               bigserial
        constraint equipment_pk
            primary key,
    name             text   not null,
    owner_id         bigint not null
        constraint equipment_t_user_id_fk
            references t_user,
    requirement_text text   not null,
    comment          text   not null
);

alter table equipment
    owner to postgres;

create table if not exists equipment_tag
(
    id   bigserial
        constraint equipment_tag_pk
            primary key,
    name text not null
        constraint equipment_tag_pk_2
            unique
);

alter table equipment_tag
    owner to postgres;

create table if not exists equipment_tag_mapping
(
    equipment_id bigint not null
        constraint equipment_tag_mapping_equipment_id_fk
            references equipment,
    tag_id       bigint not null
        constraint equipment_tag_mapping_equipment_tag_id_fk
            references equipment_tag,
    constraint equipment_tag_mapping_pk
        primary key (tag_id, equipment_id)
);

alter table equipment_tag_mapping
    owner to postgres;

create table if not exists role_group
(
    name          text                 not null,
    id            bigint               not null
    constraint role_group_ky
    primary key
);

create unique index if not exists role_group_name_uindex
    on role_group (name);

alter table role_group
    owner to postgres;

create table if not exists role
(
    name          text                 not null,
    remark        text                 not null,
    id            serial               not null
        constraint role_ky
            primary key,
    editable      boolean default true not null,
    role_group_id      bigint               not null
        constraint role_role_group_id_fk
            references role_group
);

create unique index IF NOT EXISTS role_name_role_group_id_uindex
    on role (name, role_group_id);


comment on column role.name is '英文的命名空间，如appoint';

alter table role
    owner to postgres;

create table if not exists united_role
(
    role_id       bigint not null
        constraint united_role_role_id_fk
            references role,
    child_role_id bigint not null
        constraint united_role_role_id_fk2
            references role,
    constraint united_role_pk
        primary key (role_id, child_role_id)
);

alter table united_role
    owner to postgres;

create table if not exists user_role_band
(
    user_id bigint not null
        constraint user_role_band_t_user_id_fk
            references t_user,
    role_id bigint not null
        constraint user_role_band_role_id_fk
            references role,
    band_id integer
        constraint user_role_band_band_id_fk
            references band,
    id      bigserial
        constraint user_role_band_pk
            primary key,
    constraint user_role_band_pk_2
        unique (user_id, role_id, band_id)
);

alter table user_role_band
    owner to postgres;

INSERT INTO "public"."t_user" ("username", "realname", "password", "create_time", "contact")
VALUES
    ('s114514', '超只因管理员', '$2a$12$qtTvimr/kiqX7z.KAtBrB.XGYaBj71dexWSlg8ijhFhbiprNrHPve', '2024-02-12 17:54:27', '{}'),
    ('a1919810', '普通管理员', '$2a$12$DYV6uMcFopGVr5BPvoML4.CFknvCRPAIzToJm6JJhTcU8QdAXU4ie', '2024-02-12 17:58:48', '{}'),
    ('u12138', '平凡的用户', '$2a$12$4n8F7/GX/fdoeFLf0TvoDuXypo9Wgd9w1KNr/ddZxcdTC10eAUpl6', '2024-02-12 18:00:20', '{}')
    ON conflict(username)
    DO NOTHING;

INSERT INTO "public"."role_group" ("id", "name")
VALUES
    (1, 'admin'),
    (2, 'member'),
    (3, 'other')
    ON conflict(id)
    DO NOTHING;

INSERT INTO "public"."role" ("name", "remark", "editable", "role_group_id")
VALUES
    ('announcement', '', false, 1),
    ('hrm', 'this is hrm', false, 1),
    ('appointment', '', false, 1),
    ('equipment', '', false, 1),
    ('band', '', false, 1),
    ('place', '', false, 1),
    ('system', '', false, 1),
    ('room', '', false, 2),
    ('super admin', '', false, 3),
    ('leader', '', false, 3),
    ('union', '', false, 3)
    ON conflict(name, role_group_id)
    DO NOTHING;

INSERT INTO "public"."united_role" ("role_id", "child_role_id")
VALUES
    (9, 1),
    (9, 2),
    (9, 3),
    (9, 4),
    (9, 5),
    (9, 6),
    (9, 7),
    (9, 8),
    (10, 1),
    (10, 2),
    (10, 3),
    (10, 4),
    (11, 10),
    (11, 6),
    (11, 7)
ON conflict(role_id, child_role_id)
    DO NOTHING;

INSERT INTO "public"."band" ("id", "name", "leader_id")
VALUES
    (1, 'King Crimson', 1),
    (2, 'Yes', 2),
    (3, 'Pink Floyd', 3)
    ON conflict(name, leader_id)
    DO NOTHING;

INSERT INTO "public"."user_role_band" ("user_id", "role_id", "band_id")
VALUES
    (1, 10, null),
    (2, 11, null),
    (1, 3, 1),
    (3, 9, null),
    (3, 5, null),
    (3, 6, null)
    ON conflict(user_id, role_id, band_id)
    DO NOTHING;
