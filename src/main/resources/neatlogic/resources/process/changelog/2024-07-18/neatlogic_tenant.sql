ALTER TABLE `catalog_authority`
    ADD COLUMN `action` ENUM('report','view') NOT NULL   COMMENT '授权类型' AFTER `uuid`,
    DROP PRIMARY KEY,
    ADD PRIMARY KEY (`catalog_uuid`, `type`, `uuid`, `action`);