-- DROP TABLE IF EXISTS
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS friendships;
DROP TABLE IF EXISTS user_statistics;
--

-- CREATE TABLE
CREATE TABLE users
(
    user_id    BIGSERIAL                           NOT NULL
        CONSTRAINT users_pk
            PRIMARY KEY,
    username   VARCHAR(32) UNIQUE                  NOT NULL,
    password   VARCHAR(128)                        NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

create table friendships
(
    id         BIGSERIAL                           not null
        constraint friendships_pk
            primary key,
    username_1 VARCHAR(32)                         not null
        constraint friendships_users_username_fk
            references users (username),
    username_2 VARCHAR(32)                         not null
        constraint friendships_users_username_fk_2
            references users (username),
    status     VARCHAR(256)                        not null,
    updated_at timestamp DEFAULT CURRENT_TIMESTAMP not null,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP not null
);

CREATE TABLE user_statistics (
    user_id BIGINT NOT NULL
        CONSTRAINT user_statistics_pk PRIMARY KEY
        CONSTRAINT user_statistics_users_fk REFERENCES users(user_id)
        ON DELETE CASCADE,
    wins INT DEFAULT 0 NOT NULL,
    losses INT DEFAULT 0 NOT NULL,
    draws INT DEFAULT 0 NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);
--
