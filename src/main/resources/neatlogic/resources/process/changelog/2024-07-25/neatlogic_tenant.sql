CREATE TABLE `processtask_step_cost` (
    `id` bigint NOT NULL COMMENT 'ID',
    `processtask_id` bigint NOT NULL COMMENT '工单id',
    `processtask_step_id` bigint DEFAULT NULL COMMENT '步骤id',
    `start_action` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '开始操作类型',
    `start_status` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '开始操作状态',
    `start_time` timestamp(3) NOT NULL COMMENT '开始操作时间',
    `start_user_uuid` char(32) COLLATE utf8mb4_general_ci NOT NULL COMMENT '开始操作人',
    `end_action` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '结束操作类型',
    `end_status` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '结束操作状态',
    `end_time` timestamp(3) NULL DEFAULT NULL COMMENT '结束操作时间',
    `end_user_uuid` char(32) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '结束操作人',
    `time_cost` bigint DEFAULT NULL COMMENT '工作时间耗时',
    `realtime_cost` bigint DEFAULT NULL COMMENT '自然时间耗时',
    PRIMARY KEY (`id`),
    KEY `idx_processtask_id_processtask_step_id` (`processtask_id`,`processtask_step_id`),
    KEY `idx_processtask_step_id` (`processtask_step_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='工单步骤操作耗时表';

CREATE TABLE `processtask_step_cost_worker` (
    `id` bigint NOT NULL COMMENT 'ID',
    `action_type` enum('start','end') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '起止操作',
    `cost_id` bigint NOT NULL COMMENT '耗时id',
    `type` enum('user','team','role') COLLATE utf8mb4_general_ci NOT NULL COMMENT '类型',
    `uuid` char(32) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'UUID',
    PRIMARY KEY (`id`),
    KEY `idx_cost_id` (`cost_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='工单步骤操作耗时处理人表';