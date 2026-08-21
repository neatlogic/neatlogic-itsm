-- 扩展服务目录和服务通道授权类型，使 action=delegate 可以参与联合主键并正常保存。
ALTER TABLE `catalog_authority`
    MODIFY COLUMN `action` ENUM('report', 'view', 'delegate') NOT NULL COMMENT '授权类型：report-上报，view-查看，delegate-代报';

ALTER TABLE `channel_authority`
    MODIFY COLUMN `action` ENUM('report', 'view', 'delegate') NOT NULL COMMENT '授权类型：report-上报，view-查看，delegate-代报';

-- 为存量目录和服务补充“所有人”代报授权，保持升级前上报人可修改的兼容行为。
INSERT IGNORE INTO `catalog_authority` (`catalog_uuid`, `type`, `uuid`, `action`)
SELECT `uuid`, 'common', 'alluser', 'delegate'
FROM `catalog`;

INSERT IGNORE INTO `channel_authority` (`channel_uuid`, `type`, `uuid`, `action`)
SELECT `uuid`, 'common', 'alluser', 'delegate'
FROM `channel`;
