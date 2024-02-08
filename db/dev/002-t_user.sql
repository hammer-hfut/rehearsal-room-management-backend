create table if not exists public.t_user
(
    id          bigserial
        constraint t_user_pk
            primary key,
    username    text      not null,
    realname    text      not null,
    password    text      not null,
    create_time timestamp not null
);

create unique index t_user_username_uindex
    on public.t_user (username);