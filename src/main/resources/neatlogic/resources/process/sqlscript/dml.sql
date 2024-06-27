-- ----------------------------
-- Records of process_workcenter
-- ----------------------------
BEGIN;
INSERT ignore INTO `process_workcenter` (`uuid`, `name`, `type`, `sort`, `condition_config`, `support`, `catalog_id`, `is_show_total`) VALUES ('allProcessTask', '所有工单', 'factory', 1, '{\n        \"conditionGroupList\": [],\n        \"conditionGroupRelList\": [],\n        \"startTimeCondition\": {\n            \"timeRange\": \"1\",\n            \"timeUnit\": \"year\"\n        },\n        \"handlerType\": \"simple\",\n        \"isProcessingOfMine\": 0,\n        \"uuid\": \"allProcessTask\"\n    }', 'pc', NULL, 0);
INSERT ignore INTO `process_workcenter` (`uuid`, `name`, `type`, `sort`, `condition_config`, `support`, `catalog_id`, `is_show_total`) VALUES ('doneOfMineProcessTask', '我的已办', 'factory', 3, '{\n        \"handlerType\": \"simple\",\n        \"startTimeCondition\": {\n            \"timeRange\": \"1\",\n            \"timeUnit\": \"year\"\n        },\n        \"conditionGroupList\": [\n            {\n                \"uuid\": \"7b4d42e03ff4413483836c3289a938af\",\n                \"conditionList\": [\n                    {\n                        \"uuid\": \"64b156e0c67c4755864e6b4c36e0f2ff\",\n                        \"type\": \"common\",\n                        \"name\": \"aboutme\",\n                        \"valueList\": [\n                            \"doneOfMine\"\n                        ],\n                        \"expression\": \"include\"\n                    }\n                ],\n                \"conditionRelList\": [],\n                \"channelUuidList\": []\n            }\n        ],\n        \"isProcessingOfMine\": 0,\n        \"conditionGroupRelList\": [],\n        \"isProcessing\": 0,\n        \"uuid\": \"doneOfMineProcessTask\"\n    }', 'all', NULL, 0);
INSERT ignore INTO `process_workcenter` (`uuid`, `name`, `type`, `sort`, `condition_config`, `support`, `catalog_id`, `is_show_total`) VALUES ('draftProcessTask', '我的草稿', 'factory', 4, '{\n        \"handlerType\": \"simple\",\n        \"startTimeCondition\": {\n            \"timeRange\": \"1\",\n            \"timeUnit\": \"year\"\n        },\n        \"conditionGroupList\": [\n            {\n                \"uuid\": \"05158b6b22d544b39181ef0a1a38c776\",\n                \"conditionList\": [\n                    {\n                        \"uuid\": \"77d6733ddfdb47e2acb6f11f5dd3f2f5\",\n                        \"type\": \"common\",\n                        \"name\": \"status\",\n                        \"valueList\": [\n                            \"draft\"\n                        ],\n                        \"expression\": \"include\"\n                    },\n                    {\n                        \"uuid\": \"822cbdec2ec44f0e8ae7af4e065b4d48\",\n                        \"type\": \"common\",\n                        \"name\": \"owner\",\n                        \"valueList\": [\n                            \"common#loginuser\"\n                        ],\n                        \"expression\": \"include\"\n                    }\n                ],\n                \"conditionRelList\": [\n                    {\n                        \"from\": \"77d6733ddfdb47e2acb6f11f5dd3f2f5\",\n                        \"to\": \"822cbdec2ec44f0e8ae7af4e065b4d48\",\n                        \"joinType\": \"and\"\n                    }\n                ],\n                \"channelUuidList\": []\n            }\n        ],\n        \"isProcessingOfMine\": 0,\n        \"conditionGroupRelList\": [],\n        \"uuid\": \"draftProcessTask\"\n    }', 'pc', NULL, 0);
INSERT ignore INTO `process_workcenter` (`uuid`, `name`, `type`, `sort`, `condition_config`, `support`, `catalog_id`, `is_show_total`) VALUES ('processingOfMineProcessTask', '我的待办', 'factory', 2, '{\n        \"conditionGroupList\": [],\n        \"conditionGroupRelList\": [],\n        \"startTimeCondition\": {\n            \"timeRange\": \"1\",\n            \"timeUnit\": \"year\"\n        },\n        \"handlerType\": \"simple\",\n        \"isProcessingOfMine\": 1,\n        \"uuid\": \"allProcessTask\"\n    }', 'all', NULL, 0);
COMMIT;
-- ----------------------------
-- Records of process_workcenter_authority
-- ----------------------------
BEGIN;
INSERT ignore INTO `process_workcenter_authority` VALUES ('allProcessTask', 'common', 'alluser');
INSERT ignore INTO `process_workcenter_authority` VALUES ('doneOfMineProcessTask', 'common', 'alluser');
INSERT ignore INTO `process_workcenter_authority` VALUES ('draftProcessTask', 'common', 'alluser');
INSERT ignore INTO `process_workcenter_authority` VALUES ('processingOfMineProcessTask', 'common', 'alluser');
COMMIT;
