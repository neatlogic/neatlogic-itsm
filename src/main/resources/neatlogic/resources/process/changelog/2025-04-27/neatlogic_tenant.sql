ALTER TABLE `processtask_auto_score`
    ADD COLUMN `error` TEXT NULL COMMENT '错误日志' AFTER `config`;