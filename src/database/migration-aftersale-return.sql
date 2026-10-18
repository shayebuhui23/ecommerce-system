-- =============================================================
-- 升级：售后支持「仅退款 / 退货退款」与退货物流
-- 可重复执行
-- =============================================================

SET NAMES utf8mb4;

SET @db := DATABASE();

SET @sql := IF((SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'after_sales' AND COLUMN_NAME = 'return_shipping_company') = 0,
    'ALTER TABLE `after_sales` ADD COLUMN `return_shipping_company` VARCHAR(64) DEFAULT NULL COMMENT ''退货物流公司'' AFTER `audited_at`',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF((SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'after_sales' AND COLUMN_NAME = 'return_tracking_no') = 0,
    'ALTER TABLE `after_sales` ADD COLUMN `return_tracking_no` VARCHAR(64) DEFAULT NULL COMMENT ''退货物流单号'' AFTER `return_shipping_company`',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF((SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'after_sales' AND COLUMN_NAME = 'return_shipped_at') = 0,
    'ALTER TABLE `after_sales` ADD COLUMN `return_shipped_at` DATETIME DEFAULT NULL COMMENT ''买家寄回时间'' AFTER `return_tracking_no`',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF((SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'after_sales' AND COLUMN_NAME = 'return_received_at') = 0,
    'ALTER TABLE `after_sales` ADD COLUMN `return_received_at` DATETIME DEFAULT NULL COMMENT ''商家确认收货时间'' AFTER `return_shipped_at`',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 旧数据统一归为仅退款
UPDATE `after_sales` SET `type` = 'REFUND_ONLY' WHERE `type` = 'REFUND';

-- 已完成的订单不支持仅退款，进行中的旧售后统一改为退货退款
UPDATE `after_sales` a JOIN `orders` o ON o.id = a.`order_id` SET a.`type` = 'RETURN_REFUND' WHERE a.`status` = 'PENDING' AND a.`type` = 'REFUND_ONLY' AND o.`status` = 'COMPLETED';
