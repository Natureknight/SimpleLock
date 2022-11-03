CREATE TABLE IF NOT EXISTS `simple_lock` (
    `id`        VARCHAR(36)   NOT NULL,
    `lock_key`  VARCHAR(255)  NOT NULL,
    `token`     VARCHAR(36)   NOT NULL,
    PRIMARY KEY(`id`),
    UNIQUE KEY `uk_lock_key` (`lock_key`)
);