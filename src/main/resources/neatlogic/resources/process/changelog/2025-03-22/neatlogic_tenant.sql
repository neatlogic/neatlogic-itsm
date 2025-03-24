ALTER TABLE `processtask_step_task_user`
    ADD COLUMN `button` VARCHAR (100) NULL COMMENT '操作按钮' AFTER `is_delete`;