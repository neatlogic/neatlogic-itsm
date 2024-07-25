INSERT IGNORE INTO `channel_authority` (`channel_uuid`, `type`, `uuid`, `action`) SELECT `uuid`, 'common', 'alluser', 'view' FROM channel;

INSERT IGNORE INTO `catalog_authority` (`catalog_uuid`, `type`, `uuid`, `action`) SELECT `uuid`, 'common', 'alluser', 'view' FROM catalog;

UPDATE `channel` SET `is_active_priority` = 0, `is_display_priority` = 0 WHERE `uuid` NOT IN (SELECT DISTINCT `channel_uuid` FROM `channel_priority`);