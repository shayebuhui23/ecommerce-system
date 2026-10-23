-- =============================================================
-- 项目审查修复迁移：订单快照、超时、SKU、索引与售后唯一约束
-- 可重复执行，建议在 MySQL 8.0 执行
-- =============================================================

SET NAMES utf8mb4;
SET @db := DATABASE();

-- 1. order_items：商家与成本价快照
SET @sql := IF((SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'order_items' AND COLUMN_NAME = 'seller_id') = 0,
    'ALTER TABLE `order_items` ADD COLUMN `seller_id` BIGINT UNSIGNED DEFAULT NULL COMMENT ''下单时商家 ID 快照'' AFTER `product_id`',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF((SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'order_items' AND COLUMN_NAME = 'cost_price') = 0,
    'ALTER TABLE `order_items` ADD COLUMN `cost_price` DECIMAL(12,2) DEFAULT NULL COMMENT ''下单时成本价快照'' AFTER `unit_price`',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 已存在明细尝试用商品当前归属回填快照
UPDATE `order_items` oi JOIN `products` p ON p.`id` = oi.`product_id`
SET oi.`seller_id` = p.`seller_id`
WHERE oi.`seller_id` IS NULL;

-- 2. orders：支付截止时间
SET @sql := IF((SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'expire_at') = 0,
    'ALTER TABLE `orders` ADD COLUMN `expire_at` DATETIME DEFAULT NULL COMMENT ''支付截止时间'' AFTER `remark`',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 3. cart_items：SKU 参与唯一键
UPDATE `cart_items` SET `sku_id` = 0 WHERE `sku_id` IS NULL;
ALTER TABLE `cart_items` MODIFY COLUMN `sku_id` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'SKU ID，0 表示使用商品默认价格库存';
SET @idx := (SELECT COUNT(*) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'cart_items' AND INDEX_NAME = 'uk_cart_items');
SET @sql := IF(@idx > 0, 'ALTER TABLE `cart_items` DROP INDEX `uk_cart_items`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
ALTER TABLE `cart_items` ADD UNIQUE KEY `uk_cart_items` (`cart_id`, `product_id`, `sku_id`);

-- 4. 订单列表联合索引
SET @idx := (SELECT COUNT(*) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'orders' AND INDEX_NAME = 'idx_orders_user_status_created');
SET @sql := IF(@idx = 0,
    'ALTER TABLE `orders` ADD INDEX `idx_orders_user_status_created` (`user_id`, `status`, `created_at`)',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 5. 售后单进行中唯一约束（生成列 NULL 不参与唯一性）
SET @col := (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'after_sales' AND COLUMN_NAME = 'active_flag');
SET @sql := IF(@col = 0,
    'ALTER TABLE `after_sales` ADD COLUMN `active_flag` TINYINT GENERATED ALWAYS AS (CASE WHEN `status` IN (''PENDING'',''WAITING_RETURN'',''WAITING_RECEIPT'') THEN 1 ELSE NULL END) STORED',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @idx := (SELECT COUNT(*) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'after_sales' AND INDEX_NAME = 'uk_after_sales_active');
SET @sql := IF(@idx = 0,
    'ALTER TABLE `after_sales` ADD UNIQUE KEY `uk_after_sales_active` (`order_id`, `active_flag`)',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @idx := (SELECT COUNT(*) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'after_sales' AND INDEX_NAME = 'idx_after_sales_order_status');
SET @sql := IF(@idx = 0,
    'ALTER TABLE `after_sales` ADD INDEX `idx_after_sales_order_status` (`order_id`, `status`)',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 6. payments：支付/退款流水类型
SET @sql := IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'payments' AND COLUMN_NAME = 'type') = 0, 'ALTER TABLE `payments` ADD COLUMN `type` VARCHAR(16) NOT NULL DEFAULT ''PAY'' COMMENT ''流水类型：PAY 支付，REFUND 退款'' AFTER `currency`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql := IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'payments' AND COLUMN_NAME = 'refund_of') = 0, 'ALTER TABLE `payments` ADD COLUMN `refund_of` BIGINT UNSIGNED DEFAULT NULL COMMENT ''退款对应的原支付记录 ID'' AFTER `type`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
