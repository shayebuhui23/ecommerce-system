-- =============================================================
-- 多商家改造迁移脚本（已初始化过数据库时执行一次）
-- 全新初始化请直接使用最新的 schema.sql + seed.sql，无需执行本脚本
-- 执行方式：mysql -u root -p < database/migration-multimerchant.sql
-- =============================================================

USE `ecommerce`;

SET NAMES utf8mb4;

-- 1. 商品增加商家归属（users 表中 STAFF 角色的用户 ID）
ALTER TABLE `products`
    ADD COLUMN `seller_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '商家用户 ID（users 表中 STAFF 角色）' AFTER `category_id`,
    ADD KEY `idx_products_seller` (`seller_id`);

-- 2. 新增第二个演示商家（密码同为 123456，复用 BCrypt 哈希）
INSERT INTO `users` (`username`, `email`, `password`, `nickname`, `phone`, `country`, `role_id`, `status`)
SELECT 'staff2', 'staff2@ecommerce.local',
       '$2b$10$.jAVWDW2W0kASIarhoC5VuHmvITI.yQCRI4PyvCy5tu0Z/hPw6gB2',
       '商家 · 云帆优选', '13800000004', 'China', 2, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `users` WHERE `username` = 'staff2');

-- 3. 为已有商品补充商家归属
UPDATE `products`
SET `seller_id` = (SELECT `id` FROM `users` WHERE `username` = 'staff')
WHERE `seller_id` IS NULL
  AND `sku_code` IN ('EC-HEADPHONE-001', 'HM-KETTLE-002', 'EC-SMARTMUG-006', 'HM-LINENBED-007');

UPDATE `products`
SET `seller_id` = (SELECT `id` FROM `users` WHERE `username` = 'staff2')
WHERE `seller_id` IS NULL
  AND `sku_code` IN ('OD-BACKPACK-035', 'BT-ESSENTIALOIL-004', 'OD-CAMPCHAIR-005', 'BT-CLEANSER-008');

-- 剩余商品统一归属第一个商家，避免出现无主商品
UPDATE `products`
SET `seller_id` = (SELECT `id` FROM `users` WHERE `username` = 'staff')
WHERE `seller_id` IS NULL;

-- 4. 补充京东等真实平台常见的商品分类
INSERT INTO `categories` (`parent_id`, `name`, `name_en`, `sort_order`, `status`)
SELECT 0, '食饮百货', 'Food & General Merchandise', 50, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `categories` WHERE `name` = '食饮百货');

INSERT INTO `categories` (`parent_id`, `name`, `name_en`, `sort_order`, `status`)
SELECT 0, '服饰鞋包', 'Apparel, Shoes & Bags', 60, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `categories` WHERE `name` = '服饰鞋包');

INSERT INTO `categories` (`parent_id`, `name`, `name_en`, `sort_order`, `status`)
SELECT 0, '母婴玩具', 'Mother, Baby & Toys', 70, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `categories` WHERE `name` = '母婴玩具');

INSERT INTO `categories` (`parent_id`, `name`, `name_en`, `sort_order`, `status`)
SELECT 0, '图书文娱', 'Books & Media', 80, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `categories` WHERE `name` = '图书文娱');

INSERT INTO `categories` (`parent_id`, `name`, `name_en`, `sort_order`, `status`)
SELECT 0, '汽车用品', 'Automotive', 90, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `categories` WHERE `name` = '汽车用品');

INSERT INTO `categories` (`parent_id`, `name`, `name_en`, `sort_order`, `status`)
SELECT 0, '宠物用品', 'Pet Supplies', 100, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `categories` WHERE `name` = '宠物用品');

-- 5. 清理联调遗留的临时分类（没有商品引用时才删除）
DELETE FROM `categories`
WHERE `name` = 'Temporary Category'
  AND NOT EXISTS (SELECT 1 FROM `products` WHERE `products`.`category_id` = `categories`.`id`);
