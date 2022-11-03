CREATE TABLE IF NOT EXISTS simple_lock (
    id        VARCHAR(36)   PRIMARY KEY  NOT NULL,
    lock_key  VARCHAR(255)  UNIQUE       NOT NULL,
    token     VARCHAR(36)                NOT NULL
);
