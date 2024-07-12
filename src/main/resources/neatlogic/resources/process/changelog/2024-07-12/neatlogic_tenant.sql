ALTER TABLE `neatlogic_develop`.`channel`
DROP COLUMN `allow_desc`,
  DROP COLUMN `is_active_help`,
  CHANGE `help` `content_help` LONGTEXT CHARSET utf8mb4 COLLATE utf8mb4_general_ci NULL   COMMENT '工单上报页描述文本占位符';