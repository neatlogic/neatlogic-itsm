CREATE TABLE IF NOT EXISTS `processtask_action` (
    `id` bigint NOT NULL COMMENT '主键ID',
    `processtask_id` bigint NOT NULL COMMENT '工单ID',
    `processtask_step_id` bigint DEFAULT NULL COMMENT '步骤ID',
    `trigger` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '触发点',
    `trigger_time` timestamp(3) NOT NULL COMMENT '触发时间',
    `integration_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '集成UUID',
    `status` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '状态',
    `error` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '失败信息',
    `config` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '配置信息',
    PRIMARY KEY (`id`),
    KEY `idx_processtask_step_id` (`processtask_step_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT = '工单动作表';