CREATE TABLE IF NOT EXISTS `processtask_approve_entity` (
  `processtask_id` bigint NOT NULL COMMENT '工单id',
  `type` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '审批处理器类型',
  `config_hash` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '审批配置hash值',
  PRIMARY KEY (`processtask_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='工单审批实体信息表';

CREATE TABLE IF NOT EXISTS `processtask_approve_entity_config` (
  `hash` char(32) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'hash值',
  `config` longtext COLLATE utf8mb4_general_ci NOT NULL COMMENT '配置信息',
  PRIMARY KEY (`hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='工单审批实体信息hash表';

CREATE TABLE IF NOT EXISTS `processtask_approve_status` (
  `processtask_id` bigint NOT NULL COMMENT '工单id',
  `approve_status` enum('accept','neutral','deny') COLLATE utf8mb4_general_ci NOT NULL COMMENT '状态',
  PRIMARY KEY (`processtask_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='工单审批状态表';
