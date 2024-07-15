CREATE TABLE IF NOT EXISTS `processtask_step_sla_delay` (
    `id` bigint NOT NULL COMMENT 'ID',
    `processtask_id` bigint NOT NULL COMMENT '工单ID',
    `target_processtask_id` bigint NOT NULL COMMENT '目标工单ID',
    `target_processtask_step_id` bigint NOT NULL COMMENT '目标步骤ID',
    `sla_id` bigint NOT NULL COMMENT '时效ID',
    `time` bigint NOT NULL COMMENT '追加时间',
    `fcu` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '创建人',
    `fcd` timestamp(3) NOT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_unique` (`target_processtask_step_id`,`sla_id`,`processtask_id`),
    KEY `idx_sla_id` (`sla_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='工单时效延迟表';