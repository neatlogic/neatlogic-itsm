ALTER TABLE `processtask_step_audit`
DROP INDEX `idx_action`,
ADD INDEX `idx_action_useruuid`(`action`, `user_uuid`) USING BTREE,
ADD INDEX `idx_processtask_step`(`processtask_step_id`) USING BTREE;

ALTER TABLE `processtask_step_cost`
ADD INDEX `idx_startoperateuser`(`start_operate`, `start_user_uuid`) USING BTREE,
ADD INDEX `idx_endoperateuser`(`end_operate`, `end_user_uuid`) USING BTREE;