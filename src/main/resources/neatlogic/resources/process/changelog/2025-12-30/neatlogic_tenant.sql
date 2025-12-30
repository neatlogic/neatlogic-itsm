ALTER TABLE `processtask_tag`
ADD INDEX `idx_tagid_processtaskid`(`tag_id`, `processtask_id`) USING BTREE;