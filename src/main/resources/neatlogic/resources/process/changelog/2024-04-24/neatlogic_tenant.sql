CREATE TABLE `processtask_extend_formattribute` (
    `processtask_id` bigint NOT NULL COMMENT '工单id',
    `form_attribute_data_id` bigint NOT NULL COMMENT '表单属性值id',
    `tag` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '标签',
    PRIMARY KEY (`processtask_id`,`form_attribute_data_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='工单与表单扩展属性值关系表';

