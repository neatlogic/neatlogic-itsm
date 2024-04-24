CREATE TABLE `processtask_extend_formattribute` (
    `processtask_id` bigint NOT NULL COMMENT '工单id',
    `form_attribute_data_id` bigint NOT NULL COMMENT '表单属性值id',
    `tag` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '标签',
    PRIMARY KEY (`processtask_id`,`form_attribute_data_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='工单与表单扩展属性值关系表';

CREATE TABLE `processtask_form_extend_attribute` (
    `processtask_id` bigint NOT NULL COMMENT '工单id',
    `form_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '表单uuid',
    `parent_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '属性父级uuid',
    `tag` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '标签',
    `key` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '属性key',
    `uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '属性uuid',
    `label` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '属性名',
    `type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '属性类型，系统属性不允许修改',
    `handler` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '属性处理器',
    `config_hash` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '属性配置Hash值',
    PRIMARY KEY (`processtask_id`,`form_uuid`,`tag`,`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='工单表单版本扩展属性';
