-- =============================================================
-- 升级：商品成本价（用于商家成本管理与毛利统计）
-- 适用于已按旧版 schema 初始化过的数据库，可重复执行
-- =============================================================

SET NAMES utf8mb4;

SET @col_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'products'
      AND COLUMN_NAME = 'cost_price'
);

SET @ddl := IF(
    @col_exists = 0,
    'ALTER TABLE `products` ADD COLUMN `cost_price` DECIMAL(12, 2) NOT NULL DEFAULT 0.00 COMMENT ''成本价，仅商家与管理员可见'' AFTER `price`',
    'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 为演示商品补齐成本价（仅当仍是默认 0 时更新）
UPDATE `products` SET `cost_price` = 520.00 WHERE `sku_code` = 'EC-HEADPHONE-001' AND `cost_price` = 0;
UPDATE `products` SET `cost_price` = 140.00 WHERE `sku_code` = 'HM-KETTLE-002' AND `cost_price` = 0;
UPDATE `products` SET `cost_price` = 210.00 WHERE `sku_code` = 'OD-BACKPACK-035' AND `cost_price` = 0;
UPDATE `products` SET `cost_price` = 95.00 WHERE `sku_code` = 'BT-ESSENTIALOIL-004' AND `cost_price` = 0;
UPDATE `products` SET `cost_price` = 70.00 WHERE `sku_code` = 'OD-CAMPCHAIR-005' AND `cost_price` = 0;
UPDATE `products` SET `cost_price` = 180.00 WHERE `sku_code` = 'EC-SMARTMUG-006' AND `cost_price` = 0;
UPDATE `products` SET `cost_price` = 380.00 WHERE `sku_code` = 'HM-LINENBED-007' AND `cost_price` = 0;
UPDATE `products` SET `cost_price` = 35.00 WHERE `sku_code` = 'BT-CLEANSER-008' AND `cost_price` = 0;
