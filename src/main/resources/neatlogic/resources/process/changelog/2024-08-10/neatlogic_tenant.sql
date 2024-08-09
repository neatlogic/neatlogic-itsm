ALTER TABLE `processtask_step_user`
    CHANGE `status` `status` ENUM('doing','done','transferred','someonetransferred') CHARSET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL   COMMENT '状态';