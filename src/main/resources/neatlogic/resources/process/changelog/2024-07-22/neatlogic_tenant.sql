ALTER TABLE `channel`
    ADD COLUMN `is_active_priority` TINYINT(1) DEFAULT 1  NOT NULL   COMMENT '是否启用优先级' AFTER `is_active`,
    ADD COLUMN `is_display_priority` TINYINT(1) DEFAULT 1  NOT NULL   COMMENT '是否显示优先级' AFTER `is_active_priority`;