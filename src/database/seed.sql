-- =============================================================
-- 小型电商订单系统 初始化演示数据
-- 执行前提：已执行 database/schema.sql
-- 执行方式：mysql -u root -p < database/seed.sql
--
-- 演示账号（密码统一为 123456，库中仅保存 BCrypt 哈希）：
--   admin    / 123456  系统管理员 ADMIN
--   staff    / 123456  商家运营   STAFF
--   customer / 123456  普通客户   CUSTOMER
-- =============================================================

USE `ecommerce`;

SET NAMES utf8mb4;

-- 清空演示数据，保证脚本可重复执行
DELETE FROM `users`;
DELETE FROM `roles`;
DELETE FROM `categories`;
DELETE FROM `exchange_rates`;

-- ------------------------------------------------------------
-- 角色
-- ------------------------------------------------------------
INSERT INTO `roles` (`id`, `code`, `name`, `description`) VALUES
(1, 'ADMIN',    '系统管理员', '系统配置、用户管理、全量数据查询与经营统计'),
(2, 'STAFF',    '商家运营',   '商品、库存、订单履约、发货与售后审核'),
(3, 'CUSTOMER', '普通客户',   '商品浏览、购物车、下单支付、收货与售后申请');

-- ------------------------------------------------------------
-- 演示账号（密码均为 123456）
-- ------------------------------------------------------------
INSERT INTO `users` (`username`, `email`, `password`, `nickname`, `phone`, `country`, `role_id`, `status`) VALUES
('admin',    'admin@ecommerce.local',
 '$2b$10$Iornnz5GXlGqOy7tz1h9/.cs/pfOHVHjHcsvMghBSwWvWXB0Ijanm',
 '系统管理员', '13800000001', 'China', 1, 1),
('staff',    'staff@ecommerce.local',
 '$2b$10$.jAVWDW2W0kASIarhoC5VuHmvITI.yQCRI4PyvCy5tu0Z/hPw6gB2',
 '运营小林',   '13800000002', 'China', 2, 1),
('customer', 'customer@ecommerce.local',
 '$2b$10$NJL51TP960mgE5FvQ.lonemIZGBup1P1EVytAK3N8c8Qissykp1Um',
 '跨境买家 Alex', '13800000003', 'United States', 3, 1);

-- ------------------------------------------------------------
-- 商品分类（中英文，模拟外贸商品目录）
-- ------------------------------------------------------------
INSERT INTO `categories` (`id`, `parent_id`, `name`, `name_en`, `sort_order`, `status`) VALUES
(1, 0, '消费电子', 'Consumer Electronics', 10, 1),
(2, 0, '家居生活', 'Home & Living',        20, 1),
(3, 0, '户外运动', 'Outdoor & Sports',     30, 1),
(4, 0, '美妆个护', 'Beauty & Personal Care', 40, 1);

-- ------------------------------------------------------------
-- 模拟汇率（管理员维护，不接实时外汇接口）
-- ------------------------------------------------------------
INSERT INTO `exchange_rates` (`from_currency`, `to_currency`, `rate`, `status`) VALUES
('CNY', 'USD', 0.140000, 1),
('USD', 'CNY', 7.140000, 1);
