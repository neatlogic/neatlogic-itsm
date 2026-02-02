CREATE TABLE `processtask_history_confighash` (
    `processtask_id` BIGINT NOT NULL COMMENT '工单ID',
    `config_hash` CHAR(32) NOT NULL COMMENT '流程配置md5散列值',
    `fcu` CHAR(32) NOT NULL COMMENT '创建人',
    `fcd` TIMESTAMP(3) NOT NULL COMMENT '创建时间',
    PRIMARY KEY (`processtask_id`, `config_hash`)
) ENGINE = INNODB CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工单流程图配置历史数据';
