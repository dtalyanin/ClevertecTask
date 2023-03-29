CREATE TABLE IF NOT EXISTS gift_certificates
(
    id               BIGSERIAL PRIMARY KEY,
    name             VARCHAR(100) NOT NULL CHECK (length(name) > 0),
    description      VARCHAR(300) NOT NULL CHECK (length(name) > 0),
    price            BIGINT       NOT NULL CHECK (price > 0),
    duration         INTEGER      NOT NULL CHECK (duration >= 1),
    create_date      TIMESTAMP    NOT NULL,
    last_update_date TIMESTAMP    NOT NULL CHECK (create_date <= last_update_date),
    UNIQUE (name, description, price, duration)
);

CREATE TABLE IF NOT EXISTS tags
(
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE CHECK (length(name) > 0)
);

CREATE TABLE IF NOT EXISTS gift_certificates_tags
(
    certificate_id BIGINT NOT NULL REFERENCES gift_certificates (id) ON DELETE CASCADE,
    tag_id         BIGINT NOT NULL REFERENCES tags (id) ON DELETE CASCADE,
    PRIMARY KEY (certificate_id, tag_id)
)