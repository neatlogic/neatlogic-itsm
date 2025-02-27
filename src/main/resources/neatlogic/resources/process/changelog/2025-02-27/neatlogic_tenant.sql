ALTER TABLE `processtask_step_in_operation`
    ADD COLUMN `server_id` BIGINT NULL COMMENT '服务器ID' AFTER `expire_time`;