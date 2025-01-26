CREATE TABLE roles
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE
);

CREATE TABLE permissions
(
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(256)       NULL
);

CREATE TABLE role_permission
(
    role_id       BIGINT REFERENCES roles (id),
    permission_id BIGINT REFERENCES permissions (id),
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE users
(
    id         SERIAL PRIMARY KEY,
    email      VARCHAR(100) UNIQUE                             NOT NULL,
    password   VARCHAR(256)                                    NOT NULL,
    role_id    BIGINT REFERENCES roles (id),
    fname      VARCHAR(100)                                    NOT NULL,
    lname      VARCHAR(100)                                    NOT NULL,
    gender     VARCHAR(10)                                     NOT NULL,
    status     VARCHAR(50) DEFAULT 'ACTIVE'::CHARACTER VARYING NOT NULL,
    created_at TIMESTAMP   DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE test_events
(
    id             SERIAL PRIMARY KEY,
    test_taker_id  BIGINT REFERENCES users (id),
    test_id        BIGINT                                             NULL, -- REFERENCES tests (id)
    event_datetime TIMESTAMP                                          NOT NULL,
    status         VARCHAR(50) DEFAULT 'SCHEDULED'::CHARACTER VARYING NOT NULL,
    score          DOUBLE PRECISION                                   NULL,
    is_passed      BOOLEAN                                            NULL,
    started_at     TIMESTAMP                                          NULL,
    finished_at    TIMESTAMP                                          NULL,
    created_at     TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    test_attempt   jsonb                                              NULL
);
