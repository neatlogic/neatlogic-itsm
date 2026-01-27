ALTER TABLE `processtask_step`
    CHANGE `is_active` `is_active` TINYINT NOT NULL COMMENT '是否激活，终止：-1,未处理过：0，正在处理：1，处理完毕：2';
ALTER TABLE `processtask_step_rel`
    CHANGE `is_hit` `is_hit` TINYINT DEFAULT 0 NULL COMMENT '0：没有触发流转，-1：触发了流转但条件不满足，1：触发了流转条件满足';
