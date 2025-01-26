INSERT INTO roles(name)
VALUES ('ADMIN');
INSERT INTO roles(name)
VALUES ('TEST_TAKER');

INSERT INTO permissions(name, description)
VALUES ('VIEW_PROFILE', 'Ability to view own profile.'),
       ('MANAGE_PROFILE', 'Ability to update own profile details.'),
       ('VIEW_ASSIGNED_TEST', 'Ability to access assigned test to the user.'),
       ('VIEW_ASSIGNED_TEST_RESULTS', 'Ability to view (past) assigned test results to the user.'),

       ('VIEW_ASSIGNED_TEST_TAKERS', 'Ability to view, search all assigned test takers.'),
       ('VIEW_ALL_TEST_TAKERS', 'Ability to view, search all test takers.'),
       ('MANAGE_TEST_TAKERS', 'Ability to add, update, and deactivate test takers.'),
       ('MANAGE_ALL_USERS', 'Ability to add, update, and deactivate all users.'), -- id: 8

       ('VIEW_ALL_TESTS', 'Ability to view all tests (inc. questions and answers).'),
       ('MANAGE_OWN_TESTS', 'Ability to create, update, or delete only owned tests.'),
       ('MANAGE_ALL_TESTS', 'Ability to create, update, or delete tests.'),
       ('VIEW_ALL_TEST_RESULTS', 'Ability to view all (past) test results.'),     -- id: 12

       ('MANAGE_TEST_EVENTS', '(Assign and) manage test events assigned to test takers.'),
       ('MONITOR_TESTS', 'Monitor test takers during tests.');

INSERT INTO role_permission(role_id, permission_id)
VALUES (1, 1),
       (1, 2),
       (1, 5),
       (1, 6),
       (1, 7),
       (1, 8),
       (1, 9),
       (1, 10),
       (1, 11),
       (1, 12),
       (1, 13),
       (1, 14),

       (2, 1),
       (2, 2),
       (2, 3),
       (2, 4);

INSERT INTO users (email, password, role_id, fname, lname, gender, status, created_at)
VALUES ('john.doe@gmail.com', '$2a$10$EF1x/Pd5OxIASv6Ui9aP2.fhyaMV1akRlnkfCu6om6OtMmcHo6ZNy', 2,
        'John', 'Doe', 'MALE', 'ACTIVE', '2025-01-19 12:03:50.811000'),
       ('maryam@gmail.com', '$2a$10$2cvUcn8pQ/foOboU.egQgegyt44ziaAmC4aq8KTgfcnEZ/kINRr/.', 2,
        'Maryam', 'Imran', 'FEMALE', 'ACTIVE', '2025-01-18 19:33:11.202000'),
       ('info44@mirodil.dev', '$2a$10$KiRDi5QT59fQxP7eSEsfYeY11JVl/qHwcorVKLpWrEx7o1NJYtLwC', 2,
        'Mirodil', 'Kamilov', 'MALE', 'ACTIVE', '2025-01-18 18:02:58.773000'),
       ('mirjalol@gmail.nl', '$2a$10$NTMyNaW3aWYy3AUqRs2rfuCmP1aMHJfb3c0R3MgGCdIuCj1AyWz/O', 2,
        'Mirjalol', 'Kamilov', 'MALE', 'ACTIVE', '2025-01-15 12:50:09.620000'),
       ('whatever@mirodil.dev', '$2a$10$Dvq4/DEIqsNGAS9HsWxpFuif/VHyHWWlX3je2CNRhqz.X/8iBTQrW', 2,
        'Mirodil', 'Kamilov', 'MALE', 'ACTIVE', '2025-01-14 15:52:57.844000'),
       ('info@mirodil.dev', '$2a$10$z9LZxrMJm/JgovAYdR/qvODExHSoBeH9H3yS0K5fUsb6il8s4EiRu', 1, 'Mirodil',
        'Kamilov', 'MALE', 'ACTIVE', '2025-01-14 15:12:27.407000'),
       ('mirodig@lcc.com', '$2a$10$RWz46v.NmrpmD4TZO0N0Nex8BjQB9a.clUq96rkhYjAoyt9gtoyIa', 2, 'Mirodil',
        'Kamilov', 'MALE', 'ACTIVE', '2025-01-14 14:45:00.436000'),
       ('test2@lcc.com', '$2a$10$ZEji2CcB.y7XfjSVGGzxzelkp3gyY5oK2lZZn5mHYezopWm.xS7Wq', 2, 'Mirodil',
        'Kamilov', 'MALE', 'ACTIVE', '2025-01-14 13:57:38.674000'),
       ('mirodil@cc.com', '$2a$10$9blp1VSeaH/HmHFAFLKZlOg4n5lb7FVlmIO5MA/6sJsPVKGK0HM6i', 2, 'Mirodil',
        'Kamilov', 'MALE', 'ACTIVE', '2025-01-14 12:41:03.940000'),
       ('mirodil@www.com', '$2a$10$r2VqD7QrtTzaxbDWzYSmz.Jy/xo3jCKcKbDhRs3sWdwRW/Hc.EERG', 2, 'Mirodil',
        'Kamilov', 'MALE', 'ACTIVE', '2025-01-14 12:16:31.864000');

INSERT INTO test_events (test_taker_id, test_id, event_datetime, status, score, is_passed, started_at, finished_at,
                         test_attempt)
VALUES (1, 1, '2025-02-14 14:00:00', 'SCHEDULED', NULL, NULL, NULL, NULL, NULL),
       (2, 1, '2025-02-10 14:00:00', 'SCHEDULED', NULL, NULL, NULL, NULL, NULL),
       (3, 2, '2025-02-09 14:00:00', 'SCHEDULED', NULL, NULL, NULL, NULL, NULL),
       (3, 3, '2025-02-10 14:00:00', 'SCHEDULED', NULL, NULL, NULL, NULL, NULL),
       (4, 3, '2025-02-15 09:00:00', 'SCHEDULED', NULL, NULL, NULL, NULL, NULL);

