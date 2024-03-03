INSERT INTO "public"."t_user" ("username", "realname", "password", "create_time", "contact")
VALUES
    ('s114514', '超只因管理员', '$2a$12$qtTvimr/kiqX7z.KAtBrB.XGYaBj71dexWSlg8ijhFhbiprNrHPve', '2024-02-12 17:54:27', '{}'),
    ('a1919810', '普通管理员', '$2a$12$DYV6uMcFopGVr5BPvoML4.CFknvCRPAIzToJm6JJhTcU8QdAXU4ie', '2024-02-12 17:58:48', '{}'),
    ('u12138', '平凡的用户', '$2a$12$4n8F7/GX/fdoeFLf0TvoDuXypo9Wgd9w1KNr/ddZxcdTC10eAUpl6', '2024-02-12 18:00:20', '{}')
    ON conflict(username)
    DO NOTHING;

INSERT INTO "public"."role_group" ("name")
VALUES
    ('admin'),
    ('member'),
    ('other')
    ON conflict(name)
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
    (1, '原神', 1),
    (2, '星穹铁道', 2),
    (3, '明日方舟', 3)
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
