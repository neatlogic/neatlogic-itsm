-- ----------------------------
-- Table structure for catalog
-- ----------------------------
CREATE TABLE IF NOT EXISTS `catalog`  (
  `uuid` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '全局唯一id，跨环境导入用',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '名称',
  `parent_uuid` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '父级uuid',
  `is_active` int NULL DEFAULT 1 COMMENT '是否启用',
  `icon` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '图标',
  `color` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '颜色',
  `desc` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '描述',
  `lft` int NULL DEFAULT NULL COMMENT '左编码',
  `rht` int NULL DEFAULT NULL COMMENT '右编码',
  PRIMARY KEY (`uuid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '服务目录表';

-- ----------------------------
-- Table structure for catalog_authority
-- ----------------------------
CREATE TABLE IF NOT EXISTS `catalog_authority`  (
  `catalog_uuid` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '目录uuid',
  `type` enum('common','user','team','role') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '类型',
  `uuid` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'uuid',
  `action` enum('report','view') COLLATE utf8mb4_general_ci NOT NULL COMMENT '授权类型',
  PRIMARY KEY (`catalog_uuid`, `type`, `uuid`,`action`) USING BTREE,
  INDEX `idx_uuid`(`uuid`) USING BTREE,
  INDEX `idx_catalog_uuid`(`catalog_uuid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '目录授权表';



-- ----------------------------
-- Table structure for channel
-- ----------------------------
CREATE TABLE IF NOT EXISTS `channel`  (
  `uuid` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '全局唯一id，跨环境导入用',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '名称',
  `parent_uuid` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'catalog的uuid',
  `is_active` int NOT NULL COMMENT '是否启用',
  `is_active_priority` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用优先级',
  `is_display_priority` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否显示优先级',
  `icon` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '图标',
  `color` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '颜色',
  `desc` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '描述',
  `sort` int NOT NULL COMMENT '排序',
  `sla` int NULL DEFAULT NULL COMMENT 'sla',
  `content_help` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '描述帮助',
  `channel_type_uuid` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '服务类型uuid',
  `support` enum('all','pc','mobile') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'all' COMMENT '使用范围',
  `config` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '配置信息',
  PRIMARY KEY (`uuid`) USING BTREE,
  INDEX `idx_channeltype_uuid`(`channel_type_uuid`) USING BTREE,
  INDEX `idx_parent_uuid`(`parent_uuid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '服务表';

-- ----------------------------
-- Table structure for channel_authority
-- ----------------------------
CREATE TABLE IF NOT EXISTS `channel_authority`  (
  `channel_uuid` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '服务uuid',
  `type` enum('common','user','team','role') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '类型',
  `uuid` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'uuid',
  `action` enum('report','view') COLLATE utf8mb4_general_ci NOT NULL COMMENT '授权类型',
  PRIMARY KEY (`channel_uuid`, `type`, `uuid`, `action`) USING BTREE,
  INDEX `idx_channel_uuid`(`channel_uuid`) USING BTREE,
  INDEX `idx_uuid`(`uuid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '服务授权表';

-- ----------------------------
-- Table structure for channel_priority
-- ----------------------------
CREATE TABLE IF NOT EXISTS `channel_priority`  (
  `channel_uuid` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'channel表的uuid',
  `priority_uuid` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'priority表的uuid',
  `is_default` tinyint(1) NOT NULL DEFAULT 0 COMMENT '1:默认优先级,0:否',
  PRIMARY KEY (`channel_uuid`, `priority_uuid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '服务引用优先级关系表';

-- ----------------------------
-- Table structure for channel_process
-- ----------------------------
CREATE TABLE IF NOT EXISTS `channel_process`  (
  `channel_uuid` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'channel表的uuid',
  `process_uuid` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'process表的uuid',
  PRIMARY KEY (`channel_uuid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '服务引用流程关系表';

-- ----------------------------
-- Table structure for channel_relation
-- ----------------------------
CREATE TABLE IF NOT EXISTS `channel_relation`  (
  `source` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '来源服务uuid',
  `channel_type_relation_id` bigint NOT NULL COMMENT '关系类型id',
  `type` enum('channel','catalog') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '标识目标是服务还是目录',
  `target` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '目标服务或目录uuid',
  PRIMARY KEY (`source`, `channel_type_relation_id`, `type`, `target`) USING BTREE,
  INDEX `idx_source`(`source`) USING BTREE,
  INDEX `idx_channel_type_relation_id`(`channel_type_relation_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '服务转报设置表';

-- ----------------------------
-- Table structure for channel_relation_authority
-- ----------------------------
CREATE TABLE IF NOT EXISTS `channel_relation_authority`  (
  `source` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '服务uuid',
  `channel_type_relation_id` bigint NOT NULL COMMENT '关系类型id',
  `type` char(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '授权对象类型',
  `uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '授权对象uuid',
  PRIMARY KEY (`source`, `channel_type_relation_id`, `type`, `uuid`) USING BTREE,
  INDEX `idx_source`(`source`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '服务转报设置授权表';

-- ----------------------------
-- Table structure for channel_relation_isusepreowner
-- ----------------------------
CREATE TABLE IF NOT EXISTS `channel_relation_isusepreowner`  (
  `source` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '服务uuid',
  `channel_type_relation_id` bigint NOT NULL COMMENT '关系类型id',
  `is_use_pre_owner` tinyint(1) NOT NULL COMMENT '是否使用原用户上报',
  PRIMARY KEY (`source`, `channel_type_relation_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '服务转报设置是否使用原用户表';

-- ----------------------------
-- Table structure for channel_type
-- ----------------------------
CREATE TABLE IF NOT EXISTS `channel_type`  (
  `uuid` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '全局唯一id，跨环境导入用',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '名称',
  `is_active` tinyint(1) NULL DEFAULT NULL COMMENT '是否激活',
  `icon` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '图标',
  `color` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '颜色',
  `description` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '描述',
  `sort` int NULL DEFAULT NULL COMMENT '排序',
  `prefix` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '工单号前缀',
  PRIMARY KEY (`uuid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '服务类型信息表';

-- ----------------------------
-- Table structure for channel_type_relation
-- ----------------------------
CREATE TABLE IF NOT EXISTS `channel_type_relation`  (
  `id` bigint NOT NULL COMMENT '唯一主键',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '名称',
  `is_active` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否激活',
  `is_delete` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工单关联关系类型表';

-- ----------------------------
-- Table structure for channel_type_relation_source
-- ----------------------------
CREATE TABLE IF NOT EXISTS `channel_type_relation_source`  (
  `channel_type_relation_id` bigint NOT NULL COMMENT '关系类型id',
  `channel_type_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '服务类型uuid',
  PRIMARY KEY (`channel_type_relation_id`, `channel_type_uuid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工单关联关系来源服务类型表';

-- ----------------------------
-- Table structure for channel_type_relation_target
-- ----------------------------
CREATE TABLE IF NOT EXISTS `channel_type_relation_target`  (
  `channel_type_relation_id` bigint NOT NULL COMMENT '关系类型id',
  `channel_type_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '服务类型uuid',
  PRIMARY KEY (`channel_type_relation_id`, `channel_type_uuid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工单关联关系目标服务类型表';

-- ----------------------------
-- Table structure for channel_user
-- ----------------------------
CREATE TABLE IF NOT EXISTS `channel_user`  (
  `user_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'user的uuid',
  `channel_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'channel的uuid',
  `insert_time` timestamp NULL DEFAULT NULL COMMENT '创建时间',
  UNIQUE INDEX `user_channel_index`(`user_uuid`, `channel_uuid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户收藏服务关系表';

-- ----------------------------
-- Table structure for channel_worktime
-- ----------------------------
CREATE TABLE IF NOT EXISTS `channel_worktime`  (
  `channel_uuid` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'channel表的uuid',
  `worktime_uuid` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'worktime表的uuid',
  PRIMARY KEY (`channel_uuid`) USING BTREE,
  INDEX `idx_wt_uuid`(`worktime_uuid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '服务引用服务窗口关系表';

-- ----------------------------
-- Table structure for priority
-- ----------------------------
CREATE TABLE IF NOT EXISTS `priority`  (
  `uuid` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '全局唯一id，跨环境导入用',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '名称',
  `is_active` tinyint(1) NULL DEFAULT 1 COMMENT '是否激活，1:激活,0:禁用',
  `desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '描述',
  `icon` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '图标',
  `color` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '颜色',
  `sort` int NULL DEFAULT NULL COMMENT '排序',
  PRIMARY KEY (`uuid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '优先级信息表';

-- ----------------------------
-- Table structure for process
-- ----------------------------
CREATE TABLE IF NOT EXISTS `process`  (
  `uuid` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '全局唯一id，跨环境导入用',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '名称',
  `is_active` tinyint NULL DEFAULT NULL COMMENT '是否激活',
  `config` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '流程图配置',
  `fcd` timestamp(3) NULL DEFAULT NULL COMMENT '创建时间',
  `fcu` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '创建用户',
  `lcd` timestamp(3) NULL DEFAULT NULL COMMENT '修改时间',
  `lcu` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '修改用户',
  PRIMARY KEY (`uuid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '流程信息表';

-- ----------------------------
-- Table structure for process_comment_template
-- ----------------------------
CREATE TABLE IF NOT EXISTS `process_comment_template`  (
  `id` bigint NOT NULL COMMENT '主键',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '名称',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '回复内容',
  `type` enum('system','custom') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'system:系统模版；custom:自定义模版',
  `fcd` timestamp(3) NULL DEFAULT NULL COMMENT '创建时间',
  `fcu` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建人',
  `lcd` timestamp(3) NULL DEFAULT NULL COMMENT '修改时间',
  `lcu` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '修改人',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_name`(`name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统回复模版表';

-- ----------------------------
-- Table structure for process_comment_template_authority
-- ----------------------------
CREATE TABLE IF NOT EXISTS `process_comment_template_authority`  (
  `comment_template_id` bigint NOT NULL COMMENT '回复模版ID',
  `type` enum('common','user','team','role') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '授权对象类型',
  `uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '授权对象UUID',
  PRIMARY KEY (`comment_template_id`, `type`, `uuid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统回复模版授权表';

-- ----------------------------
-- Table structure for process_comment_template_usecount
-- ----------------------------
CREATE TABLE IF NOT EXISTS `process_comment_template_usecount`  (
  `comment_template_id` bigint NOT NULL COMMENT '回复模版ID',
  `user_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户uuid',
  `count` int NULL DEFAULT NULL COMMENT '使用次数',
  PRIMARY KEY (`comment_template_id`, `user_uuid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '回复模版使用次数表';

-- ----------------------------
-- Table structure for process_draft
-- ----------------------------
CREATE TABLE IF NOT EXISTS `process_draft`  (
  `uuid` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '全局唯一id，跨环境导入用',
  `process_uuid` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '流程uuid',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '名称',
  `config` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '流程图配置',
  `fcd` timestamp(3) NULL DEFAULT NULL COMMENT '创建时间',
  `fcu` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建用户',
  `md5` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'md5',
  PRIMARY KEY (`uuid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '流程草稿信息表';

-- ----------------------------
-- Table structure for process_form
-- ----------------------------
CREATE TABLE IF NOT EXISTS `process_form`  (
  `process_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '流程uuid',
  `form_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '关联的表单uuid',
  PRIMARY KEY (`process_uuid`, `form_uuid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '流程表单';

-- ----------------------------
-- Table structure for process_integration
-- ----------------------------
CREATE TABLE IF NOT EXISTS `process_integration`  (
  `process_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '流程uuid',
  `integration_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '集成uuid',
  PRIMARY KEY (`process_uuid`, `integration_uuid`) USING BTREE,
  INDEX `idx_integration_uuid`(`integration_uuid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '流程设置引用集成表';

-- ----------------------------
-- Table structure for process_notify_policy
-- ----------------------------
CREATE TABLE IF NOT EXISTS `process_notify_policy`  (
  `process_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '流程uuid',
  `notify_policy_id` bigint NOT NULL COMMENT '通知策略id',
  PRIMARY KEY (`process_uuid`) USING BTREE,
  INDEX `idx_notify_policy_id`(`notify_policy_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '流程设置引用通知策略表';

-- ----------------------------
-- Table structure for process_score_template
-- ----------------------------
CREATE TABLE IF NOT EXISTS `process_score_template`  (
  `score_template_id` bigint NOT NULL COMMENT '评分模版ID',
  `process_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '流程UUID',
  `is_active` tinyint NULL DEFAULT NULL COMMENT '是否启用评分（0：否，1：是）',
  `is_auto` tinyint(1) NULL DEFAULT NULL COMMENT '是否自动评分',
  `config` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '评分设置',
  PRIMARY KEY (`score_template_id`, `process_uuid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '评分模版-流程关联表';

-- ----------------------------
-- Table structure for process_sla
-- ----------------------------
CREATE TABLE IF NOT EXISTS `process_sla`  (
  `uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'uuid',
  `process_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '流程uuid',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '名称',
  `config` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '配置规则,json格式',
  PRIMARY KEY (`uuid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '流程时效表';

-- ----------------------------
-- Table structure for process_sla_notify_policy
-- ----------------------------
CREATE TABLE IF NOT EXISTS `process_sla_notify_policy`  (
  `sla_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '时效uuid',
  `notify_policy_id` bigint NOT NULL COMMENT '通知策略id',
  PRIMARY KEY (`sla_uuid`, `notify_policy_id`) USING BTREE,
  INDEX `idx_notify_policy_id`(`notify_policy_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '流程时效引用通知策略表';

-- ----------------------------
-- Table structure for process_step
-- ----------------------------
CREATE TABLE IF NOT EXISTS `process_step`  (
  `process_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '流程uuid',
  `uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'uuid',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '名称',
  `type` enum('start','end','process','converge','auto','timer') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '类型',
  `handler` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '处理器',
  `config` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '额外配置,json格式',
  `description` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '描述',
  PRIMARY KEY (`uuid`) USING BTREE,
  INDEX `idx_process_step`(`process_uuid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '流程步骤信息表';

-- ----------------------------
-- Table structure for process_step_comment_template
-- ----------------------------
CREATE TABLE IF NOT EXISTS `process_step_comment_template`  (
  `process_step_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '流程步骤uuid',
  `comment_template_id` bigint NOT NULL COMMENT '回复模版ID',
  PRIMARY KEY (`process_step_uuid`, `comment_template_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '流程步骤-回复模版关联表';

-- ----------------------------
-- Table structure for process_step_formattribute
-- ----------------------------
CREATE TABLE IF NOT EXISTS `process_step_formattribute`  (
  `process_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '流程uuid',
  `process_step_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '步骤uuid',
  `form_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '表单uuid',
  `attribute_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '属性uuid',
  `action` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '授权类型',
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '类型',
  PRIMARY KEY (`process_uuid`, `process_step_uuid`, `form_uuid`, `attribute_uuid`, `action`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '流程步骤表单组件授权表';

-- ----------------------------
-- Table structure for process_step_handler
-- ----------------------------
CREATE TABLE IF NOT EXISTS `process_step_handler`  (
  `handler` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '节点组件',
  `config` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '组件配置信息',
  PRIMARY KEY (`handler`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '流程组件全局配置信息表';

-- ----------------------------
-- Table structure for process_step_handler_integration
-- ----------------------------
CREATE TABLE IF NOT EXISTS `process_step_handler_integration`  (
  `handler` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '节点组件类型',
  `integration_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '集成uuid',
  PRIMARY KEY (`handler`, `integration_uuid`) USING BTREE,
  INDEX `idx_integration_uuid`(`integration_uuid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '流程组件引用集成表';

-- ----------------------------
-- Table structure for process_step_handler_notify_policy
-- ----------------------------
CREATE TABLE IF NOT EXISTS `process_step_handler_notify_policy`  (
  `handler` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '节点组件类型',
  `notify_policy_id` bigint NOT NULL COMMENT '通知策略id',
  PRIMARY KEY (`handler`) USING BTREE,
  INDEX `idx_notify_policy_id`(`notify_policy_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '流程组件引用通知策略表';

-- ----------------------------
-- Table structure for process_step_integration
-- ----------------------------
CREATE TABLE IF NOT EXISTS `process_step_integration`  (
  `process_step_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '步骤uuid',
  `integration_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '集成配置uuid',
  PRIMARY KEY (`process_step_uuid`, `integration_uuid`) USING BTREE,
  INDEX `idx_integration_uuid`(`integration_uuid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '流程步骤引用集成表';

-- ----------------------------
-- Table structure for process_step_notify_policy
-- ----------------------------
CREATE TABLE IF NOT EXISTS `process_step_notify_policy`  (
  `process_step_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '流程步骤uuid',
  `notify_policy_id` bigint NOT NULL COMMENT '通知策略id',
  PRIMARY KEY (`process_step_uuid`) USING BTREE,
  INDEX `idx_notify_policy_id`(`notify_policy_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '流程步骤引用通知策略表';

-- ----------------------------
-- Table structure for process_step_rel
-- ----------------------------
CREATE TABLE IF NOT EXISTS `process_step_rel`  (
  `process_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '流程uuid',
  `uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '全局唯一id',
  `from_step_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '源步骤uuid',
  `to_step_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '目标步骤uuid',
  `condition` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '条件',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '名称',
  `type` enum('forward','backward') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '类型',
  PRIMARY KEY (`uuid`) USING BTREE,
  INDEX `idx_process_uuid`(`process_uuid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '流程步骤之间连线表';

-- ----------------------------
-- Table structure for process_step_sla
-- ----------------------------
CREATE TABLE IF NOT EXISTS `process_step_sla`  (
  `sla_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'sla uuid',
  `step_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '步骤uuid',
  PRIMARY KEY (`sla_uuid`, `step_uuid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '流程步骤时效表';

-- ----------------------------
-- Table structure for process_step_tag
-- ----------------------------
CREATE TABLE IF NOT EXISTS `process_step_tag`  (
  `process_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '流程uuid',
  `process_step_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '步骤uuid',
  `tag_id` bigint NOT NULL COMMENT '标签id',
  PRIMARY KEY (`process_step_uuid`, `tag_id`) USING BTREE,
  INDEX `idx_process_uuid`(`process_uuid`) USING BTREE,
  INDEX `idx_tag_id`(`tag_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '流程步骤打标签表';

-- ----------------------------
-- Table structure for process_step_task_config
-- ----------------------------
CREATE TABLE IF NOT EXISTS `process_step_task_config`  (
  `process_step_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '流程步骤uuid',
  `task_config_id` bigint NOT NULL COMMENT '任务id',
  PRIMARY KEY (`process_step_uuid`, `task_config_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '流程步骤子任务表';

-- ----------------------------
-- Table structure for process_step_worker_dispatcher
-- ----------------------------
CREATE TABLE IF NOT EXISTS `process_step_worker_dispatcher`  (
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '名称',
  `handler` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '处理器',
  `config` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '配置',
  `is_active` tinyint(1) NULL DEFAULT NULL COMMENT '是否激活',
  `help` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '帮助',
  PRIMARY KEY (`name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '流程步骤分派器表';

-- ----------------------------
-- Table structure for process_step_worker_policy
-- ----------------------------
CREATE TABLE IF NOT EXISTS `process_step_worker_policy`  (
  `process_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '流程uuid',
  `process_step_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '步骤uuid',
  `policy` enum('manual','automatic','assign','copy','fromer','form','attribute','prestepassign') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '策略',
  `sort` int NULL DEFAULT NULL COMMENT '排序',
  `config` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '策略配置，一段json',
  PRIMARY KEY (`process_step_uuid`, `policy`) USING BTREE,
  INDEX `idx_process_uuid`(`process_uuid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '流程步骤分配策略表';

-- ----------------------------
-- Table structure for process_tag
-- ----------------------------
CREATE TABLE IF NOT EXISTS `process_tag`  (
  `id` bigint NOT NULL COMMENT 'id',
  `name` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '标签名',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_name`(`name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'ITSM模块标签表';

-- ----------------------------
-- Table structure for process_workcenter
-- ----------------------------
CREATE TABLE IF NOT EXISTS `process_workcenter`  (
  `uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '分类唯一标识',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '分类名',
  `type` enum('factory','system','custom') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'default:默认出厂  system：系统分类  custom：自定义分类',
  `sort` int NOT NULL AUTO_INCREMENT COMMENT '分类排序，越小越靠前',
  `condition_config` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '分类条件配置',
  `support` enum('all','mobile','pc') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'all' COMMENT '使用显示范围',
  `catalog_id` bigint NULL DEFAULT NULL COMMENT '菜单id',
  `is_show_total` tinyint(1) NULL DEFAULT NULL COMMENT '是否显示总数,默认显示待办数',
  PRIMARY KEY (`uuid`) USING BTREE,
  INDEX `idx_sort`(`sort`) USING BTREE,
  INDEX `idx_support`(`support`) USING BTREE,
  INDEX `idx_type`(`type`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 305 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工单中心分类表';

-- ----------------------------
-- Table structure for process_workcenter_authority
-- ----------------------------
CREATE TABLE IF NOT EXISTS `process_workcenter_authority`  (
  `workcenter_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '工单分类uuid',
  `type` enum('common','user','role','team') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '类型',
  `uuid` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'uuid',
  PRIMARY KEY (`workcenter_uuid`, `type`, `uuid`) USING BTREE,
  INDEX `index_role`(`uuid`) USING BTREE,
  INDEX `index_user`(`type`, `uuid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工单中心分类授权表';

-- ----------------------------
-- Table structure for process_workcenter_catalog
-- ----------------------------
CREATE TABLE IF NOT EXISTS `process_workcenter_catalog`  (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '菜单类型唯一标识',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '菜单名称',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_name`(`name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工单中心菜单类型表';

-- ----------------------------
-- Table structure for process_workcenter_owner
-- ----------------------------
CREATE TABLE IF NOT EXISTS `process_workcenter_owner`  (
  `workcenter_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '分类唯一标识uuid',
  `user_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '如果属于私人分类，分类所属人',
  PRIMARY KEY (`workcenter_uuid`) USING BTREE,
  INDEX `idx_user_uuid`(`user_uuid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工单中心分类所属人表';

-- ----------------------------
-- Table structure for process_workcenter_thead
-- ----------------------------
CREATE TABLE IF NOT EXISTS `process_workcenter_thead`  (
  `workcenter_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '分类唯一标识',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '字段名（表单属性则存属性uuid）',
  `sort` int NOT NULL COMMENT '字段排序',
  `user_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '所属用户',
  `is_show` bigint NOT NULL COMMENT '字段是否展示',
  `width` int NOT NULL DEFAULT 1 COMMENT '字段宽度',
  `type` enum('common','form') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '字段类型，common:工单固有字段  form：表单属性',
  `disabled` tinyint NULL DEFAULT NULL COMMENT '字段是否禁用(1：是，0：否)',
  PRIMARY KEY (`workcenter_uuid`, `name`, `user_uuid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户自定义thead 显示排序';

-- ----------------------------
-- Table structure for process_workcenter_user_profile
-- ----------------------------
CREATE TABLE IF NOT EXISTS `process_workcenter_user_profile`  (
  `user_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户uuid',
  `config` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '用户个性化配置，如排序等',
  PRIMARY KEY (`user_uuid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'process_workcenter_user_profile';

-- ----------------------------
-- Table structure for processtask
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask`  (
  `id` bigint NOT NULL COMMENT '工单id',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '标题',
  `process_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '流程uuid',
  `channel_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '服务uuid',
  `config_hash` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '流程配置md5散列值',
  `priority_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '优先级uid',
  `status` enum('pending','draft','running','aborted','succeed','failed','hang','scored') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '工单状态',
  `start_time` timestamp(3) NULL DEFAULT NULL COMMENT '上报时间',
  `end_time` timestamp(3) NULL DEFAULT NULL COMMENT '关单时间',
  `owner` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '上报人',
  `reporter` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '代报人',
  `expire_time` timestamp(3) NULL DEFAULT NULL COMMENT '超时时间',
  `worktime_uuid` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '工作时间窗口uuid',
  `error` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '流程级异常',
  `is_show` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否显示 1：显示；0：隐藏',
  `serial_number` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '工单序列号',
  `source` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'pc' COMMENT '来源',
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '是否已删除',
  `need_score` tinyint(1) NULL DEFAULT NULL COMMENT '是否启用评分',
  `region_id` bigint NULL DEFAULT NULL COMMENT '地域id',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_processuuid`(`process_uuid`) USING BTREE,
  INDEX `idx_channel`(`channel_uuid`) USING BTREE,
  INDEX `idx_isshow`(`is_show`) USING BTREE,
  INDEX `idx_owner`(`owner`) USING BTREE,
  INDEX `idx_priority`(`priority_uuid`) USING BTREE,
  INDEX `idx_reporter`(`reporter`) USING BTREE,
  INDEX `idx_serialnum`(`serial_number`) USING BTREE,
  INDEX `idx_starttime`(`start_time`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_worktime`(`worktime_uuid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工单信息表';

-- ----------------------------
-- Table structure for processtask_action
-- ----------------------------
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

-- ----------------------------
-- Table structure for processtask_agent
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_agent`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `from_user_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '来源用户uuid',
  `to_user_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '目标用户uuid',
  `begin_time` timestamp(3) NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` timestamp(3) NULL DEFAULT NULL COMMENT '结束时间',
  `is_active` tinyint(1) NOT NULL COMMENT '启用',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_from_to_user_uuid`(`from_user_uuid`, `to_user_uuid`) USING BTREE,
  INDEX `idx_to_user_uuid`(`to_user_uuid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工单代理';

-- ----------------------------
-- Table structure for processtask_agent_target
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_agent_target`  (
  `processtask_agent_id` bigint NOT NULL COMMENT '代办id',
  `target` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '目标uuid,目录或服务',
  `type` enum('catalog','channel') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '目标类型',
  `path_list` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '路径',
  PRIMARY KEY (`processtask_agent_id`, `target`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工单代理目标';

-- ----------------------------
-- Table structure for processtask_assignworker
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_assignworker`  (
  `processtask_id` bigint NOT NULL COMMENT '工单id',
  `processtask_step_id` bigint NOT NULL COMMENT '被指派步骤id',
  `from_processtask_step_id` bigint NOT NULL COMMENT '指派步骤id',
  `from_process_step_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '指派步骤流程步骤uuid',
  `type` enum('user','team','role') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '指派对象类型',
  `uuid` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '指派对象',
  `fcu` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建人',
  `fcd` timestamp(3) NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`processtask_id`, `processtask_step_id`, `from_processtask_step_id`, `uuid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工单指派处理人表';

-- ----------------------------
-- Table structure for processtask_auto_score
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_auto_score`  (
  `processtask_id` bigint NOT NULL COMMENT '工单ID',
  `trigger_time` timestamp NULL DEFAULT NULL COMMENT '自动评分时间',
  `config` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '配置',
  PRIMARY KEY (`processtask_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工单自动评分表';

-- ----------------------------
-- Table structure for processtask_async_create
-- ----------------------------
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

-- ----------------------------
-- Table structure for processtask_config
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_config`  (
  `hash` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '通过process表中config生成的md5唯一标识',
  `config` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '历史流程配置',
  PRIMARY KEY (`hash`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工单配置信息压缩表';

-- ----------------------------
-- Table structure for processtask_content
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_content`  (
  `hash` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'hash',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '内容',
  PRIMARY KEY (`hash`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工单活动内容表';

-- ----------------------------
-- Table structure for processtask_converge
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_converge`  (
  `converge_id` bigint NOT NULL COMMENT '汇集',
  `processtask_step_id` bigint NOT NULL COMMENT '作业步骤id',
  `processtask_id` bigint NOT NULL COMMENT '作业id',
  `is_check` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否确认',
  PRIMARY KEY (`processtask_id`, `processtask_step_id`, `converge_id`) USING BTREE,
  INDEX `idx_convergeid`(`converge_id`) USING BTREE,
  INDEX `idx_stepid`(`processtask_step_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工单步骤汇合表';

-- ----------------------------
-- Table structure for processtask_extend_formattribute
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_extend_formattribute`  (
  `processtask_id` bigint NOT NULL COMMENT '工单id',
  `form_attribute_data_id` bigint NOT NULL COMMENT '表单属性值id',
  `tag` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '标签',
  PRIMARY KEY (`processtask_id`, `form_attribute_data_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工单与表单扩展属性值关系表';

-- ----------------------------
-- Table structure for processtask_file
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_file`  (
  `processtask_id` bigint NOT NULL COMMENT '工单ID',
  `processtask_step_id` bigint NOT NULL COMMENT '步骤ID',
  `file_id` bigint NOT NULL COMMENT '文件ID',
  `content_id` bigint NOT NULL COMMENT '内容ID',
  PRIMARY KEY (`processtask_id`, `processtask_step_id`, `file_id`, `content_id`) USING BTREE,
  INDEX `idx_content_id`(`content_id`) USING BTREE,
  INDEX `idx_processtask_step_id`(`processtask_step_id`) USING BTREE,
  INDEX `idx_file_id`(`file_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工单文件表';

-- ----------------------------
-- Table structure for processtask_focus
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_focus`  (
  `processtask_id` bigint NOT NULL COMMENT '工单ID',
  `user_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户UUID',
  PRIMARY KEY (`processtask_id`, `user_uuid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工单关注人表';

-- ----------------------------
-- Table structure for processtask_form
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_form`  (
  `processtask_id` bigint NOT NULL COMMENT '工单id',
  `form_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '工单绑定的表单uuid',
  `form_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '工单绑定的表单名',
  `form_content_hash` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '工单绑定的表单配置',
  PRIMARY KEY (`processtask_id`, `form_uuid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工单关联的表单';

-- ----------------------------
-- Table structure for processtask_form_content
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_form_content`  (
  `hash` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'hash',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '内容',
  PRIMARY KEY (`hash`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工单表单内容表';

-- ----------------------------
-- Table structure for processtask_form_extend_attribute
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_form_extend_attribute`  (
  `processtask_id` bigint NOT NULL COMMENT '工单id',
  `form_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '表单uuid',
  `parent_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '属性父级uuid',
  `tag` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '标签',
  `key` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '属性key',
  `uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '属性uuid',
  `label` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '属性名',
  `type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '属性类型，系统属性不允许修改',
  `handler` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '属性处理器',
  `config_hash` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '属性配置Hash值',
  PRIMARY KEY (`processtask_id`, `form_uuid`, `tag`, `uuid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工单表单版本扩展属性';

-- ----------------------------
-- Table structure for processtask_formattribute
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_formattribute`  (
  `processtask_id` bigint NOT NULL COMMENT '工单id',
  `form_attribute_data_id` bigint NOT NULL COMMENT '表单属性值id',
  PRIMARY KEY (`processtask_id`, `form_attribute_data_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工单与表单属性值关系表';

-- ----------------------------
-- Table structure for processtask_formattribute_data
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_formattribute_data`  (
  `processtask_id` bigint NOT NULL COMMENT '工单id',
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '插件类型',
  `attribute_label` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '属性名',
  `attribute_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '属性uuid',
  `data` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '工单属性值,json格式',
  `sort` int NULL DEFAULT NULL COMMENT '排序',
  PRIMARY KEY (`processtask_id`, `attribute_uuid`) USING BTREE,
  INDEX `idx_attribute_uuid`(`attribute_uuid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工单关联的属性当前值';

-- ----------------------------
-- Table structure for processtask_import_audit
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_import_audit`  (
  `id` bigint NOT NULL COMMENT '主键',
  `processtask_id` bigint NULL DEFAULT NULL COMMENT '工单ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '工单标题',
  `channel_uuid` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '服务UUID',
  `status` tinyint NOT NULL COMMENT '是否上报成功（是：1，否：0）',
  `error_reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '上报失败原因',
  `owner` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '上报人',
  `import_time` timestamp(3) NULL DEFAULT NULL COMMENT '导入时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工单批量导入审计表';

-- ----------------------------
-- Table structure for processtask_invoke
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_invoke`  (
  `processtask_id` bigint NOT NULL COMMENT '工单步骤id',
  `source` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '来源',
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '来源类型',
  `invoke_id` bigint NULL DEFAULT NULL COMMENT '来源id',
  PRIMARY KEY (`processtask_id`) USING BTREE,
  INDEX `idx_invokeid`(`invoke_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工单来源';

-- ----------------------------
-- Table structure for processtask_old_form_prop
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_old_form_prop`  (
  `processtask_id` bigint NOT NULL COMMENT '工单id',
  `form` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '工单表单html',
  `prop` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '工单自定义属性json',
  PRIMARY KEY (`processtask_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'processtask_old_form_prop';

-- ----------------------------
-- Table structure for processtask_operation_content
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_operation_content`  (
  `id` bigint NOT NULL COMMENT 'id',
  `processtask_id` bigint NULL DEFAULT NULL COMMENT '工单ID',
  `content_hash` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'hash',
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '类型',
  `source` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '来源',
  `fcd` timestamp(3) NULL DEFAULT NULL COMMENT '创建时间',
  `fcu` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建人',
  `lcd` timestamp(3) NULL DEFAULT NULL COMMENT '修改时间',
  `lcu` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '修改人',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_contentid`(`content_hash`) USING BTREE,
  INDEX `idx_processtask_id`(`processtask_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工单操作内容表';

-- ----------------------------
-- Table structure for processtask_relation
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_relation`  (
  `id` bigint NOT NULL COMMENT '唯一标识',
  `channel_type_relation_id` bigint NOT NULL COMMENT '关系类型',
  `source` bigint NOT NULL COMMENT '来源工单id',
  `target` bigint NOT NULL COMMENT '目标工单id',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_channel_type_relation_id`(`channel_type_relation_id`) USING BTREE,
  INDEX `idx_source`(`source`) USING BTREE,
  INDEX `idx_target`(`target`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工单关联关系表';

-- ----------------------------
-- Table structure for processtask_repeat
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_repeat`  (
  `processtask_id` bigint NOT NULL COMMENT '工单id',
  `repeat_group_id` bigint NOT NULL COMMENT '重复组id',
  PRIMARY KEY (`processtask_id`) USING BTREE,
  INDEX `idx_repeat_group_id`(`repeat_group_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '重复工单表';

-- ----------------------------
-- Table structure for processtask_score
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_score`  (
  `processtask_id` bigint NOT NULL COMMENT '工单ID',
  `score_template_id` bigint NOT NULL COMMENT '评分模版ID',
  `score_dimension_id` bigint NOT NULL COMMENT '评分维度ID',
  `score` int NOT NULL COMMENT '分数',
  `fcu` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '评分人',
  `fcd` timestamp(3) NULL DEFAULT NULL COMMENT '评分时间',
  `is_auto` tinyint NOT NULL COMMENT '是否自动评分（0：否，1：是）',
  PRIMARY KEY (`processtask_id`, `score_template_id`, `score_dimension_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工单评分表';

-- ----------------------------
-- Table structure for processtask_score_content
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_score_content`  (
  `processtask_id` bigint NOT NULL COMMENT '工单ID',
  `content_hash` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '评价内容hash',
  `fcu` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '评价人',
  `fcd` timestamp(3) NULL DEFAULT NULL COMMENT '评价时间',
  PRIMARY KEY (`processtask_id`, `content_hash`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工单评价内容表';

-- ----------------------------
-- Table structure for processtask_score_template
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_score_template`  (
  `processtask_id` bigint NOT NULL COMMENT '工单ID',
  `score_template_id` bigint NULL DEFAULT NULL COMMENT '评分模版ID',
  `is_auto` tinyint(1) NULL DEFAULT NULL COMMENT '是否自动评分',
  `config_hash` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '配置hash',
  PRIMARY KEY (`processtask_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工单评分模版表';

-- ----------------------------
-- Table structure for processtask_score_template_config
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_score_template_config`  (
  `hash` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '内容hash',
  `config` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '配置',
  PRIMARY KEY (`hash`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工单评分模版配置表';

-- ----------------------------
-- Table structure for processtask_serial_number
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_serial_number`  (
  `processtask_id` bigint NOT NULL COMMENT '工单id',
  `serial_number` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '工单序列号',
  PRIMARY KEY (`processtask_id`, `serial_number`) USING BTREE,
  INDEX `idx_serial_number`(`serial_number`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工单号表';

-- ----------------------------
-- Table structure for processtask_serial_number_policy
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_serial_number_policy`  (
  `channel_type_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '服务类型uuid',
  `handler` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '工单号生成策略类名',
  `config` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '配置信息',
  `serial_number_seed` bigint NULL DEFAULT NULL COMMENT 'serial_number_seed',
  `start_time` timestamp(3) NULL DEFAULT NULL COMMENT '最后一次更新工单号开始时间',
  `end_time` timestamp(3) NULL DEFAULT NULL COMMENT '最后一次更新工单号结束时间',
  PRIMARY KEY (`channel_type_uuid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工单号生成策略表';

-- ----------------------------
-- Table structure for processtask_sla
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_sla`  (
  `processtask_id` bigint NOT NULL COMMENT '工单ID',
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '名称',
  `is_active` tinyint(1) NOT NULL COMMENT '是否激活',
  `config` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT 'config',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_pt_id`(`processtask_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 394 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工单sla表';

-- ----------------------------
-- Table structure for processtask_sla_notify
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_sla_notify`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `sla_id` bigint NOT NULL COMMENT 'sla id',
  `hash` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'hash',
  `trigger_time` timestamp(3) NULL DEFAULT NULL COMMENT '触发时间',
  `config` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT 'config',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_sla_id`(`sla_id`, `hash`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 151 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'sla通知表';

-- ----------------------------
-- Table structure for processtask_sla_time
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_sla_time`  (
  `sla_id` bigint NOT NULL COMMENT 'id',
  `time_sum` bigint NULL DEFAULT NULL COMMENT '总耗时，毫秒',
  `expire_time` timestamp(3) NULL DEFAULT NULL COMMENT '超时时间（根据工作日历计算）',
  `realexpire_time` timestamp(3) NULL DEFAULT NULL COMMENT '超时时间（直接计算）',
  `time_left` bigint NULL DEFAULT NULL COMMENT '剩余时间，毫秒，根据工作日历计算',
  `realtime_left` bigint NULL DEFAULT NULL COMMENT '剩余时间，毫秒，直接计算',
  `status` enum('doing','pause','done') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'doing' COMMENT '状态',
  `calculation_time` timestamp(3) NULL DEFAULT NULL COMMENT '上次耗时计算时间点',
  PRIMARY KEY (`sla_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'sla时间表';

-- ----------------------------
-- Table structure for processtask_sla_transfer
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_sla_transfer`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `sla_id` bigint NOT NULL COMMENT 'sla id',
  `hash` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'hash',
  `trigger_time` timestamp(3) NULL DEFAULT NULL COMMENT '触发时间',
  `config` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT 'config',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `sla_id_hash_idx`(`sla_id`, `hash`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工单步骤时效转交设置表';

-- ----------------------------
-- Table structure for processtask_step_sla_delay
-- ----------------------------
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

-- ----------------------------
-- Table structure for processtask_step
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_step`  (
  `id` bigint NOT NULL COMMENT 'id',
  `processtask_id` bigint NOT NULL COMMENT '工单ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '步骤名称',
  `process_step_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '步骤uuid',
  `status` enum('pending','running','succeed','failed','back','hang','draft') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '状态',
  `result` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '处理结果',
  `type` enum('start','process','end','converge','timer') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '节点类型，不同类型节点有不同的流转行为',
  `handler` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '处理器',
  `is_active` tinyint(1) NOT NULL COMMENT '是否激活，终止：-1,未处理过：0，正在处理：1，处理完毕：2',
  `active_time` timestamp(3) NULL DEFAULT NULL COMMENT '激活时间',
  `start_time` timestamp(3) NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` timestamp(3) NULL DEFAULT NULL COMMENT '结束时间',
  `error` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '错误内容',
  `expire_time` timestamp(3) NULL DEFAULT NULL COMMENT '超时时间',
  `config_hash` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '配置散列值',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_processtask_id_status`(`processtask_id`, `status`) USING BTREE,
  INDEX `idx_processtask_id_active`(`processtask_id`, `is_active`) USING BTREE,
  INDEX `idx_active_time`(`active_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工单步骤表';

-- ----------------------------
-- Table structure for processtask_step_agent
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_step_agent`  (
  `processtask_id` bigint NULL DEFAULT NULL COMMENT '工单ID',
  `processtask_step_id` bigint NOT NULL COMMENT '步骤ID',
  `user_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户uuid',
  `agent_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '代理人uuid',
  PRIMARY KEY (`processtask_step_id`) USING BTREE,
  INDEX `idx_processtask_id`(`processtask_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工单步骤代理表';

-- ----------------------------
-- Table structure for processtask_step_audit
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_step_audit`  (
  `id` bigint NOT NULL COMMENT 'id',
  `processtask_id` bigint NOT NULL COMMENT '工单ID',
  `processtask_step_id` bigint NULL DEFAULT NULL COMMENT '步骤ID',
  `user_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户uuid',
  `action_time` timestamp(3) NULL DEFAULT NULL COMMENT '动作触发时间',
  `action` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '动作',
  `step_status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '步骤状态',
  `original_user` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '原始处理人',
  `description_hash` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '描述hash',
  `source` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '来源',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_processtask_id`(`processtask_id`) USING BTREE,
  INDEX `idx_action`(`action`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工单步骤审计表';

-- ----------------------------
-- Table structure for processtask_step_audit_detail
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_step_audit_detail`  (
  `audit_id` bigint NOT NULL COMMENT 'id',
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '类型',
  `old_content` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT 'old content',
  `new_content` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT 'new content',
  PRIMARY KEY (`audit_id`, `type`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工单步骤活动详细表';

-- ----------------------------
-- Table structure for processtask_step_automatic_request
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_step_automatic_request`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `processtask_id` bigint NOT NULL COMMENT '工单id',
  `processtask_step_id` bigint NOT NULL COMMENT '步骤id',
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '请求或回调',
  `trigger_time` timestamp NULL DEFAULT NULL COMMENT '下次触发时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 47 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工单步骤自动节点请求';

-- ----------------------------
-- Table structure for processtask_step_change_create
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_step_change_create`  (
  `processtask_id` bigint NOT NULL COMMENT '工单id',
  `processtask_step_id` bigint NOT NULL COMMENT '步骤id',
  `change_id` bigint NOT NULL COMMENT '变更id',
  `config` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '变更创建时配置信息',
  PRIMARY KEY (`change_id`) USING BTREE,
  UNIQUE INDEX `idx_processtask_step_id`(`processtask_step_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工单步骤与变更创建关系表';

-- ----------------------------
-- Table structure for processtask_step_change_handle
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_step_change_handle`  (
  `processtask_id` bigint NOT NULL COMMENT '工单id',
  `processtask_step_id` bigint NOT NULL COMMENT '步骤id',
  `change_id` bigint NOT NULL COMMENT '变更id',
  PRIMARY KEY (`change_id`) USING BTREE,
  UNIQUE INDEX `idx_processtask_step_id`(`processtask_step_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工单步骤与变更处理关系表';

-- ----------------------------
-- Table structure for processtask_step_config
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_step_config`  (
  `hash` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'hash',
  `config` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT 'config',
  PRIMARY KEY (`hash`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工单配置表';

-- ----------------------------
-- Table structure for processtask_step_content
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_step_content`  (
  `id` bigint NOT NULL COMMENT 'id',
  `processtask_id` bigint NULL DEFAULT NULL COMMENT '工单ID',
  `processtask_step_id` bigint NULL DEFAULT NULL COMMENT '步骤ID',
  `content_hash` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'hash',
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '类型',
  `source` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '来源',
  `fcd` timestamp(3) NULL DEFAULT NULL COMMENT '创建时间',
  `fcu` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建人',
  `lcd` timestamp(3) NULL DEFAULT NULL COMMENT '修改时间',
  `lcu` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '修改人',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_contentid`(`content_hash`) USING BTREE,
  INDEX `idx_processtask_id`(`processtask_id`) USING BTREE,
  INDEX `idx_processtaskstepid`(`processtask_step_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工单步骤内容表';

-- ----------------------------
-- Table structure for processtask_step_content_target
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_step_content_target` (
  `content_id` bigint NOT NULL COMMENT '工单步骤回复内容ID',
  `type` enum('user','team','role') COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '对象类型',
  `uuid` char(32) COLLATE utf8mb4_general_ci NOT NULL COMMENT '对象UUID',
  PRIMARY KEY (`content_id`,`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='工单步骤回复目标对象表';

-- ----------------------------
-- Table structure for processtask_step_data
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_step_data`  (
  `id` bigint NOT NULL COMMENT '唯一标识',
  `processtask_id` bigint NOT NULL COMMENT '工单id',
  `processtask_step_id` bigint NOT NULL COMMENT '工单步骤id',
  `data` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '数据',
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '功能类型',
  `fcu` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建人',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `task_step_type_fcu_idx`(`processtask_id`, `processtask_step_id`, `type`, `fcu`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工单步骤数据表';

-- ----------------------------
-- Table structure for processtask_step_diagram
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_step_diagram`  (
  `processtask_id` bigint NOT NULL COMMENT '工单id',
  `processtask_step_id` bigint NOT NULL COMMENT '工单步骤id',
  `catalog_id` bigint NOT NULL COMMENT '目录id',
  `template_id` bigint NOT NULL COMMENT '模板id',
  `cientity_id` bigint NOT NULL COMMENT '配置项id',
  `diagram_id` bigint NOT NULL COMMENT '架构图id',
  `diagram_version_id` bigint NOT NULL COMMENT '架构图版本id',
  `request` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '关联需求',
  PRIMARY KEY (`processtask_id`, `processtask_step_id`, `diagram_version_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工单与架构图关系';

-- ----------------------------
-- Table structure for processtask_step_eoa
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_step_eoa`  (
  `processtask_id` bigint NOT NULL COMMENT '工单的id',
  `processtask_step_id` bigint NOT NULL COMMENT '工单步骤的id',
  `eoa_id` bigint NOT NULL COMMENT 'eoa的id',
  `template_id` bigint NULL DEFAULT NULL COMMENT 'eoa模板的id',
  `config` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '配置信息',
  PRIMARY KEY (`processtask_id`, `processtask_step_id`, `eoa_id`) USING BTREE,
  UNIQUE INDEX `idx_eoa_id`(`eoa_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工单步骤与eoa关系表';

-- ----------------------------
-- Table structure for processtask_step_event
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_step_event`  (
  `processtask_id` bigint NULL DEFAULT NULL COMMENT '工单ID',
  `processtask_step_id` bigint NOT NULL COMMENT '步骤ID',
  `event_id` bigint NULL DEFAULT NULL COMMENT '事件ID',
  PRIMARY KEY (`processtask_step_id`) USING BTREE,
  UNIQUE INDEX `idx_event_id`(`event_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工单步骤-事件关联表';

-- ----------------------------
-- Table structure for processtask_step_formattribute
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_step_formattribute`  (
  `processtask_id` bigint NOT NULL COMMENT '工单id',
  `processtask_step_id` bigint NOT NULL COMMENT '工单步骤id',
  `attribute_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '属性uuid',
  `action` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '授权类型',
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '类型',
  PRIMARY KEY (`processtask_id`, `processtask_step_id`, `attribute_uuid`, `action`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工单步骤可查看的表单属性';

-- ----------------------------
-- Table structure for processtask_step_in_operation
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_step_in_operation`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `processtask_id` bigint NOT NULL COMMENT '工单ID',
  `processtask_step_id` bigint NOT NULL COMMENT '步骤ID',
  `operation_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '操作类型',
  `operation_time` timestamp(3) NULL DEFAULT NULL COMMENT '操作时间',
  `expire_time` timestamp(3) NULL DEFAULT NULL COMMENT '过期时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_processtask_id`(`processtask_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 17518 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工单步骤正在后台执行操作记录表';

-- ----------------------------
-- Table structure for processtask_step_notify_policy
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_step_notify_policy`  (
  `processtask_step_id` bigint NOT NULL COMMENT '步骤ID',
  `policy_id` bigint NOT NULL COMMENT '通知策略ID',
  `policy_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '通知策略名称',
  `policy_config_hash` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '通知策略配置hash',
  `policy_handler` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '通知策略处理器',
  PRIMARY KEY (`processtask_step_id`, `policy_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工单步骤通知策略表';

-- ----------------------------
-- Table structure for processtask_step_notify_policy_config
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_step_notify_policy_config`  (
  `hash` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'hash',
  `config` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT 'config',
  PRIMARY KEY (`hash`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工单步骤通知策略配置表';

-- ----------------------------
-- Table structure for processtask_step_reapproval_restore_backup
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_step_reapproval_restore_backup`  (
  `processtask_id` bigint NOT NULL COMMENT '工单id',
  `processtask_step_id` bigint NOT NULL COMMENT '步骤id',
  `backup_step_id` bigint NOT NULL COMMENT '备份步骤id',
  `config` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '备份信息',
  `sort` int NOT NULL COMMENT '顺序',
  PRIMARY KEY (`backup_step_id`, `processtask_step_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工单步骤审批恢复备份';

-- ----------------------------
-- Table structure for processtask_step_rel
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_step_rel`  (
  `processtask_id` bigint NOT NULL COMMENT '工单ID',
  `from_process_step_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '源步骤uuid',
  `to_process_step_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '目标步骤uuid',
  `from_processtask_step_id` bigint NOT NULL COMMENT '源步骤ID',
  `to_processtask_step_id` bigint NOT NULL COMMENT '目标步骤ID',
  `condition` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '条件',
  `is_hit` tinyint(1) NULL DEFAULT 0 COMMENT '0：没有触发流转，-1：触发了流转但条件不满足，1：触发了流转条件满足',
  `uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'uuid',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '名称',
  `type` enum('forward','backward') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '类型',
  PRIMARY KEY (`from_processtask_step_id`, `to_processtask_step_id`) USING BTREE,
  INDEX `idx_to_processtask_step_uuid`(`to_processtask_step_id`) USING BTREE,
  INDEX `idx_from_process_step_uuid`(`from_process_step_uuid`) USING BTREE,
  INDEX `idx_to_process_step_uuid`(`to_process_step_uuid`) USING BTREE,
  INDEX `idx_process_task_uuid`(`processtask_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工单步骤之间的连线表';

-- ----------------------------
-- Table structure for processtask_step_remind
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_step_remind`  (
  `processtask_id` bigint NULL DEFAULT NULL COMMENT '工单ID',
  `processtask_step_id` bigint NOT NULL COMMENT '步骤ID',
  `action` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '动作',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '标题',
  `fcu` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建人',
  `fcd` timestamp(3) NULL DEFAULT NULL COMMENT '创建时间',
  `content_hash` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '内容hash',
  PRIMARY KEY (`processtask_step_id`, `action`) USING BTREE,
  INDEX `idx_processtask_id`(`processtask_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工单步骤特殊提醒信息表';

-- ----------------------------
-- Table structure for processtask_step_sla
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_step_sla`  (
  `processtask_step_id` bigint NOT NULL COMMENT '步骤ID',
  `sla_id` bigint NOT NULL COMMENT 'sla id',
  PRIMARY KEY (`processtask_step_id`, `sla_id`) USING BTREE,
  INDEX `idx_slaid`(`sla_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工单步骤时效表';

-- ----------------------------
-- Table structure for processtask_step_sla_time
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_step_sla_time`  (
  `processtask_id` bigint NOT NULL COMMENT '工单id',
  `processtask_step_id` bigint NOT NULL COMMENT '步骤id',
  `sla_id` bigint NOT NULL COMMENT '时效id',
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '类型，响应、处理',
  `time_sum` bigint NOT NULL COMMENT '时效总时长',
  `time_cost` bigint NOT NULL COMMENT '耗时，毫秒，根据工作日历计算',
  `realtime_cost` bigint NOT NULL COMMENT '耗时，毫秒，直接计算',
  `is_timeout` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否超时',
  PRIMARY KEY (`processtask_step_id`, `sla_id`, `type`) USING BTREE,
  INDEX `idx_sla_id`(`sla_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '步骤sla耗时表';

-- ----------------------------
-- Table structure for processtask_step_tag
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_step_tag`  (
  `processtask_id` bigint NULL DEFAULT NULL COMMENT '工单ID',
  `processtask_step_id` bigint NOT NULL COMMENT '步骤ID',
  `tag_id` bigint NOT NULL COMMENT '标签ID',
  PRIMARY KEY (`processtask_step_id`, `tag_id`) USING BTREE,
  INDEX `idx_processtask_id`(`processtask_id`) USING BTREE,
  INDEX `idx_tag_id`(`tag_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工单步骤标签表';

-- ----------------------------
-- Table structure for processtask_step_task
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_step_task`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务id',
  `processtask_id` bigint NOT NULL COMMENT '工单id',
  `processtask_step_id` bigint NOT NULL COMMENT '步骤id',
  `owner` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建者',
  `status` enum('succeed','aborted','pending') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '状态',
  `create_time` timestamp(3) NULL DEFAULT NULL COMMENT '创建时间',
  `end_time` timestamp(3) NULL DEFAULT NULL COMMENT '结束时间',
  `task_config_id` bigint NOT NULL COMMENT '任务配置id',
  `content_hash` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建内容',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_processtask_step_id`(`processtask_step_id`) USING BTREE,
  INDEX `idx_processtask_id`(`processtask_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 907740878012433 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'processtask_step_task';

-- ----------------------------
-- Table structure for processtask_step_task_user
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_step_task_user`  (
  `id` bigint NOT NULL COMMENT 'id',
  `processtask_step_task_id` bigint NULL DEFAULT NULL COMMENT '任务id',
  `user_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '任务用户',
  `end_time` timestamp(3) NULL DEFAULT NULL COMMENT '任务完成时间',
  `status` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '完成状态',
  `is_delete` tinyint(1) NULL DEFAULT 0 COMMENT '是否已删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'processtask_step_task_user';

-- ----------------------------
-- Table structure for processtask_step_task_user_agent
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_step_task_user_agent`  (
  `processtask_step_task_user_id` bigint NOT NULL COMMENT '步骤任务用户ID',
  `processtask_step_task_id` bigint NULL DEFAULT NULL COMMENT '步骤任务ID',
  `user_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户uuid',
  `agent_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '代理人uuid',
  PRIMARY KEY (`processtask_step_task_user_id`) USING BTREE,
  INDEX `idx_processtask_step_task_id`(`processtask_step_task_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工单步骤任务代理表';

-- ----------------------------
-- Table structure for processtask_step_task_user_content
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_step_task_user_content`  (
  `id` bigint NOT NULL COMMENT 'id',
  `processtask_step_task_id` bigint NULL DEFAULT NULL COMMENT '步骤任务id',
  `processtask_step_task_user_id` bigint NULL DEFAULT NULL COMMENT '步骤任务用户id',
  `user_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户uuid',
  `content_hash` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '回复内容hash',
  `fcd` timestamp(3) NULL DEFAULT NULL COMMENT '回复时间',
  `lcd` timestamp(3) NULL DEFAULT NULL COMMENT '修改回复时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`processtask_step_task_user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'processtask_step_task_user_content';

-- ----------------------------
-- Table structure for processtask_step_task_user_file
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_step_task_user_file`  (
  `processtask_step_task_id` bigint NOT NULL COMMENT '步骤任务id',
  `processtask_step_task_user_id` bigint NOT NULL COMMENT '步骤任务用户id',
  `file_id` bigint NOT NULL COMMENT '附件id',
  `fcd` timestamp NOT NULL COMMENT '上传时间',
  PRIMARY KEY (`processtask_step_task_user_id`, `file_id`) USING BTREE,
  INDEX `idx_file_id`(`file_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'processtask_step_task_user_content';

-- ----------------------------
-- Table structure for processtask_step_timeaudit
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_step_timeaudit`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `processtask_step_id` bigint NULL DEFAULT NULL COMMENT '工单ID',
  `active_time` timestamp(3) NULL DEFAULT NULL COMMENT '激活时间',
  `start_time` timestamp(3) NULL DEFAULT NULL COMMENT '开始时间',
  `abort_time` timestamp(3) NULL DEFAULT NULL COMMENT '放弃时间',
  `complete_time` timestamp(3) NULL DEFAULT NULL COMMENT '包含成功或失败',
  `back_time` timestamp(3) NULL DEFAULT NULL COMMENT '回退时间',
  `pause_time` timestamp(3) NULL DEFAULT NULL COMMENT '挂起时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_processtask_step_id`(`processtask_step_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14326 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工单步骤操作时间审计表';

-- ----------------------------
-- Table structure for processtask_step_timer
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_step_timer`  (
  `processtask_id` bigint NOT NULL COMMENT '工单id',
  `processtask_step_id` bigint NOT NULL COMMENT '步骤id',
  `trigger_time` timestamp NULL DEFAULT NULL COMMENT '触发时间',
  PRIMARY KEY (`processtask_step_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工单步骤定时';

-- ----------------------------
-- Table structure for processtask_step_user
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_step_user` (
  `processtask_id` bigint NOT NULL COMMENT '工单ID',
  `processtask_step_id` bigint NOT NULL COMMENT '步骤ID',
  `user_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '处理人uuid',
  `user_type` enum('major','minor','history_major') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'major用户必须处理',
  `user_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '处理人名称',
  `status` enum('doing','done','transferred') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '状态',
  `action` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '动作',
  `start_time` timestamp(3) NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` timestamp(3) NULL DEFAULT NULL COMMENT '结束时间',
  `active_time` timestamp(3) NULL DEFAULT NULL COMMENT '激活时间',
  PRIMARY KEY (`processtask_step_id`,`user_uuid`,`user_type`,`status`) USING BTREE,
  INDEX `idx_processtask_id` (`processtask_id`) USING BTREE,
  INDEX `idx_action` (`action`) USING BTREE,
  INDEX `idx_type` (`user_type`) USING BTREE,
  INDEX `idx_useruuid` (`user_uuid`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='工单步骤处理人表';

-- ----------------------------
-- Table structure for processtask_step_worker
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_step_worker`  (
  `processtask_id` bigint NOT NULL COMMENT '工单ID',
  `processtask_step_id` bigint NOT NULL COMMENT '步骤ID',
  `type` enum('user','team','role') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '类型',
  `uuid` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '处理人uuid',
  `user_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '处理人类型，用于区分步骤处理人，子任务处理人',
  PRIMARY KEY (`processtask_id`, `processtask_step_id`, `uuid`, `user_type`) USING BTREE,
  INDEX `idx_processtask_step_id`(`processtask_step_id`) USING BTREE,
  INDEX `idx_type`(`type`) USING BTREE,
  INDEX `idx_uuid`(`uuid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '记录当前流程谁可以处理';

-- ----------------------------
-- Table structure for processtask_step_worker_policy
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_step_worker_policy`  (
  `processtask_id` bigint NOT NULL COMMENT '工单ID',
  `processtask_step_id` bigint NOT NULL COMMENT '步骤ID',
  `process_step_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '步骤uuid',
  `policy` enum('manual','automatic','assign','copy','fromer','form','attribute','prestepassign') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '策略',
  `sort` int NULL DEFAULT NULL COMMENT '排序',
  `config` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '策略配置，一段json',
  PRIMARY KEY (`processtask_id`, `processtask_step_id`, `policy`) USING BTREE,
  INDEX `idx_processtask_id`(`processtask_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工单步骤分配策略表';

-- ----------------------------
-- Table structure for processtask_tag
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_tag`  (
  `processtask_id` bigint NOT NULL COMMENT '工单id',
  `tag_id` bigint NOT NULL COMMENT '标签id',
  PRIMARY KEY (`processtask_id`, `tag_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工单打标签表';

-- ----------------------------
-- Table structure for processtask_time_cost
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_time_cost`  (
  `processtask_id` bigint NOT NULL COMMENT '工单id',
  `time_cost` bigint NOT NULL COMMENT '耗时，毫秒，根据工作日历计算',
  `realtime_cost` bigint NOT NULL COMMENT '耗时，毫秒，直接计算',
  PRIMARY KEY (`processtask_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工单耗时表';

-- ----------------------------
-- Table structure for processtask_tranfer_report
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_tranfer_report`  (
  `id` bigint NOT NULL COMMENT 'id',
  `channel_type_relation_id` bigint NOT NULL COMMENT '服务类型关系id',
  `from_processtask_id` bigint NOT NULL COMMENT '源工单ID',
  `to_processtask_id` bigint NOT NULL COMMENT '目标工单ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_from_processtask_id`(`from_processtask_id`) USING BTREE,
  INDEX `idx_to_processtask_id`(`to_processtask_id`) USING BTREE,
  INDEX `idx_channel_type_relation_id`(`channel_type_relation_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工单转报关系表';

-- ----------------------------
-- Table structure for processtask_urge
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_urge`  (
  `processtask_id` bigint NOT NULL COMMENT '工单ID',
  `lcu` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '催办用户',
  `lcd` timestamp(3) NOT NULL COMMENT '催办时间',
  PRIMARY KEY (`processtask_id`, `lcu`, `lcd`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci;

-- ----------------------------
-- Table structure for score_template
-- ----------------------------
CREATE TABLE IF NOT EXISTS `score_template`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '评分模版名称',
  `description` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '评分模版说明',
  `is_active` tinyint NULL DEFAULT NULL COMMENT '是否启用',
  `fcu` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建人ID',
  `lcu` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '更新人ID',
  `fcd` timestamp(3) NULL DEFAULT NULL COMMENT '创建时间',
  `lcd` timestamp(3) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1046397330505729 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '评分模版表';

-- ----------------------------
-- Table structure for score_template_dimension
-- ----------------------------
CREATE TABLE IF NOT EXISTS `score_template_dimension`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `score_template_id` bigint NOT NULL COMMENT '评分模版ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '评分维度名称',
  `description` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '评分维度说明',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1080456697987073 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '评分模版维度表';

-- ----------------------------
-- Table structure for task_config
-- ----------------------------
CREATE TABLE IF NOT EXISTS `task_config`  (
  `id` bigint NOT NULL COMMENT '自增id',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '任务名称',
  `num` int NULL DEFAULT NULL COMMENT '参与人数。-1：不做限制。',
  `policy` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '其中一个人完成即可：any,所有人完成：all',
  `is_active` tinyint(1) NULL DEFAULT NULL COMMENT '是否激活',
  `config` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '配置信息',
  `fcd` timestamp(3) NULL DEFAULT NULL COMMENT '创建时间',
  `fcu` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建人',
  `lcd` timestamp(3) NULL DEFAULT NULL COMMENT '修改时间',
  `lcu` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '修改人',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uniq_name`(`name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'task_config';

CREATE TABLE IF NOT EXISTS `process_workcenter_thead_config` (
  `hash` char(32) COLLATE utf8mb4_general_ci NOT NULL COMMENT '配置hash',
  `config` text COLLATE utf8mb4_general_ci COMMENT '配置',
  PRIMARY KEY (`hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for processtask_step_cost
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_step_cost` (
  `id` bigint NOT NULL COMMENT 'ID',
  `processtask_id` bigint NOT NULL COMMENT '工单id',
  `processtask_step_id` bigint DEFAULT NULL COMMENT '步骤id',
  `start_operate` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '开始操作类型',
  `start_status` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '开始操作状态',
  `start_time` timestamp(3) NOT NULL COMMENT '开始操作时间',
  `start_user_uuid` char(32) COLLATE utf8mb4_general_ci NOT NULL COMMENT '开始操作人',
  `end_operate` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '结束操作类型',
  `end_status` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '结束操作状态',
  `end_time` timestamp(3) NULL DEFAULT NULL COMMENT '结束操作时间',
  `end_user_uuid` char(32) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '结束操作人',
  `time_cost` bigint DEFAULT NULL COMMENT '工作时间耗时',
  `realtime_cost` bigint DEFAULT NULL COMMENT '自然时间耗时',
  PRIMARY KEY (`id`),
  KEY `idx_processtask_id_processtask_step_id` (`processtask_id`,`processtask_step_id`),
  KEY `idx_processtask_step_id` (`processtask_step_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='工单步骤操作耗时表';

-- ----------------------------
-- Table structure for processtask_step_cost_worker
-- ----------------------------
CREATE TABLE IF NOT EXISTS `processtask_step_cost_worker` (
  `id` bigint NOT NULL COMMENT 'ID',
  `operate_type` enum('start','end') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '起止操作',
  `cost_id` bigint NOT NULL COMMENT '耗时id',
  `type` enum('user','team','role') COLLATE utf8mb4_general_ci NOT NULL COMMENT '类型',
  `uuid` char(32) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'UUID',
  PRIMARY KEY (`id`),
  KEY `idx_cost_id` (`cost_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='工单步骤操作耗时处理人表';