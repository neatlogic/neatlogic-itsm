ALTER TABLE `processtask`
ADD COLUMN `region_id` bigint NULL COMMENT '地域id' AFTER `need_score`;