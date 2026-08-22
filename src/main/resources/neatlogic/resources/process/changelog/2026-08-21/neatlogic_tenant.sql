ALTER TABLE `catalog_authority`
    CHANGE `action` `action` ENUM ('report', 'view', 'delegate') CHARSET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '授权类型：report-上报，view-查看，delegate-代报';
ALTER TABLE `channel_authority`
    CHANGE `action` `action` ENUM ('report', 'view', 'delegate') CHARSET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '授权类型：report-上报，view-查看，delegate-代报';

INSERT IGNORE INTO `catalog_authority` (`catalog_uuid`, `type`, `uuid`, `action`)
SELECT `uuid`, 'common', 'alluser', 'delegate'
FROM `catalog`
WHERE `uuid` NOT IN (SELECT `catalog_uuid` FROM `catalog_authority` WHERE `action` = 'delegate');

INSERT IGNORE INTO `channel_authority` (`channel_uuid`, `type`, `uuid`, `action`)
SELECT `uuid`, 'common', 'alluser', 'delegate'
FROM `channel`
WHERE `uuid` NOT IN (SELECT `channel_uuid` FROM `channel_authority` WHERE `action` = 'delegate');
