INSERT IGNORE INTO `channel_authority` (`channel_uuid`, `type`, `uuid`, `action`) SELECT a.`uuid`, 'common', 'alluser', 'view' FROM channel a LEFT JOIN `channel_authority` b ON b.`channel_uuid` = a.`uuid` AND b.`action` = 'view' WHERE b.`channel_uuid` IS NULL;

INSERT IGNORE INTO `catalog_authority` (`catalog_uuid`, `type`, `uuid`, `action`) SELECT a.`uuid`, 'common', 'alluser', 'view' FROM catalog a LEFT JOIN `catalog_authority` b ON b.`catalog_uuid` = a.`uuid` AND b.`action` = 'view' WHERE b.`catalog_uuid` IS NULL;
