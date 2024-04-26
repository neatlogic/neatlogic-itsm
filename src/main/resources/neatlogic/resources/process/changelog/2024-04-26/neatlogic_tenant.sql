ALTER TABLE `processtask`
    CHANGE `owner` `owner` VARCHAR(50) CHARSET utf8mb4 COLLATE utf8mb4_general_ci NULL   COMMENT '上报人',
    CHANGE `reporter` `reporter` VARCHAR(50) CHARSET utf8mb4 COLLATE utf8mb4_general_ci NULL   COMMENT '代报人';

ALTER TABLE `processtask`
ADD COLUMN `region_id` bigint NULL COMMENT '地域id' AFTER `need_score`;
