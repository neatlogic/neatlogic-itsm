TRUNCATE `process_workcenter_thead` ;

ALTER TABLE `process_workcenter_thead` 
DROP COLUMN `name`,
DROP COLUMN `sort`,
DROP COLUMN `is_show`,
DROP COLUMN `width`,
DROP COLUMN `type`,
DROP COLUMN `disabled`,
ADD COLUMN `config_hash` char(32) NULL AFTER `user_uuid`,
DROP PRIMARY KEY,
ADD PRIMARY KEY (`workcenter_uuid`, `user_uuid`),
ADD INDEX `idx_hash`(`config_hash`) USING HASH;

ALTER TABLE `process_workcenter` 
ADD COLUMN `thead_config_hash` char(32) NULL COMMENT '默认表头配置hash' AFTER `is_show_total`,
ADD INDEX `idx_thead_config_hash`(`thead_config_hash`) USING HASH;

CREATE TABLE IF NOT EXISTS `process_workcenter_thead_config` (
  `hash` char(32) COLLATE utf8mb4_general_ci NOT NULL COMMENT '配置hash',
  `config` text COLLATE utf8mb4_general_ci COMMENT '配置',
  PRIMARY KEY (`hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;