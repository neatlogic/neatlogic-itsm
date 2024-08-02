CREATE TABLE IF NOT EXISTS `processtask_async_create` (
    `id` bigint NOT NULL COMMENT '主键ID',
    `processtask_id` bigint NOT NULL COMMENT '工单ID',
    `title` varchar(255) COLLATE utf8mb4_general_ci NOT NULL COMMENT '标题',
    `status` enum('doing','done','failed','aborted','redo') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '状态',
    `config` longtext COLLATE utf8mb4_general_ci NOT NULL COMMENT '配置信息',
    `error` longtext COLLATE utf8mb4_general_ci COMMENT '异常信息',
    `try_count` int NOT NULL DEFAULT '0' COMMENT '尝试次数',
    `server_id` int NOT NULL COMMENT '服务器ID',
    `fcu` char(32) COLLATE utf8mb4_general_ci NOT NULL COMMENT '创建人',
    `fcd` timestamp(3) NOT NULL COMMENT '创建时间',
    `lcd` timestamp(3) NULL DEFAULT NULL COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_processtask_id` (`processtask_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT = '异步创建工单表';