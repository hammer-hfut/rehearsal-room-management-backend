create table if not exists public.announcement
(
    id          bigserial
        constraint announcement_pk
            primary key,
    content     text      not null,
    is_pinned   boolean   not null,
    create_time timestamp not null,
    author_id   bigint    not null
        constraint announcement_t_user_id_fk
            references public.t_user,
    test        integer
);