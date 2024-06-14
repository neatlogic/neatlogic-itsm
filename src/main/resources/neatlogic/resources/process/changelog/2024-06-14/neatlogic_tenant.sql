CREATE TABLE IF NOT EXISTS `processtask_step_content_target` (
    `content_id` bigint NOT NULL COMMENT '工单步骤回复内容ID',
    `type` enum('user','team','role') COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '对象类型',
    `uuid` char(32) COLLATE utf8mb4_general_ci NOT NULL COMMENT '对象UUID',
    PRIMARY KEY (`content_id`,`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='工单步骤回复目标对象表';
