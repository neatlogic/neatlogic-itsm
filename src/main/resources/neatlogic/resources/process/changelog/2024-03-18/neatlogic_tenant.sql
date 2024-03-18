CREATE TABLE IF NOT EXISTS `processtask_invoke` (
  `processtask_id` bigint NOT NULL COMMENT '工单步骤id',
  `source` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '来源',
  `type` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '来源类型',
  `invoke_id` bigint DEFAULT NULL COMMENT '来源id',
  PRIMARY KEY (`processtask_id`),
  KEY `idx_invokeid` (`invoke_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='工单来源';