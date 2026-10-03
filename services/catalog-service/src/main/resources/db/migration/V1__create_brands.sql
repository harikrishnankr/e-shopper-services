CREATE TABLE brands (
                        id          UUID PRIMARY KEY,
                        name        VARCHAR(120) NOT NULL,
                        slug        VARCHAR(140) NOT NULL UNIQUE,
                        created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
                        version     BIGINT       NOT NULL DEFAULT 0
);