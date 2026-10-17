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
DELETE FROM `product_skus`;
DELETE FROM `products`;
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
-- 演示商品（包含上架、缺货、下架三种状态，便于验证筛选与权限）
-- ------------------------------------------------------------
INSERT INTO `products` (`id`, `category_id`, `sku_code`, `name`, `name_en`, `description`, `description_en`, `image_url`, `price`, `currency`, `stock`, `sales_count`, `status`) VALUES
(1, 1, 'EC-HEADPHONE-001', '主动降噪无线耳机', 'Active Noise-Cancelling Headphones',
 '40mm 动圈单元，混合主动降噪，单次充电续航 38 小时，支持多设备连接与低延迟游戏模式。',
 '40mm dynamic drivers with hybrid ANC, 38-hour battery life, multipoint pairing and a low-latency gaming mode.',
 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=900&q=80', 899.00, 'CNY', 120, 36, 1),
(2, 2, 'HM-KETTLE-002', '便携手冲咖啡壶套装', 'Portable Pour-Over Coffee Kit',
 '304 不锈钢细口壶搭配陶瓷滤杯，适合差旅与露营场景，整套收纳后仅占背包侧袋空间。',
 'A 304 stainless steel gooseneck kettle paired with a ceramic dripper, sized for travel and camping.',
 'https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=900&q=80', 268.00, 'CNY', 60, 18, 1),
(3, 3, 'OD-BACKPACK-035', '轻量户外登山包 35L', 'Ultralight Hiking Backpack 35L',
 '210D 防撕裂尼龙面料，背负系统可调节，自重 780g，适合两日徒步与短途跨境出差。',
 '210D ripstop nylon with an adjustable harness, 780g total weight, built for two-day hikes and short business trips.',
 'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=900&q=80', 459.00, 'CNY', 45, 27, 1),
(4, 4, 'BT-ESSENTIALOIL-004', '天然植物精油礼盒', 'Botanical Essential Oil Gift Set',
 '包含甜橙、雪松、薰衣草三支单方精油，低温冷压萃取，适合作为跨境节日礼品。',
 'A trio of sweet orange, cedarwood and lavender single-origin oils, cold-pressed and gift-boxed.',
 'https://images.unsplash.com/photo-1608571423902-eed4a5ad8108?w=900&q=80', 199.00, 'CNY', 80, 41, 1),
(5, 3, 'OD-CAMPCHAIR-005', '折叠露营椅', 'Folding Camping Chair',
 '航空铝合金支架，承重 120kg，收纳后长度 38cm，附带便携收纳袋。',
 'Aviation-grade aluminium frame supporting 120kg, folds down to 38cm with a carry bag.',
 'https://images.unsplash.com/photo-1504280390367-361c6d9f38f4?w=900&q=80', 158.00, 'CNY', 0, 52, 1),
(6, 1, 'EC-SMARTMUG-006', '智能恒温马克杯', 'Smart Temperature Control Mug',
 '支持 45~65 度区间恒温，磁吸充电底座，配套 App 可自定义饮品温度曲线。',
 'Holds any temperature between 45 and 65 degrees with a magnetic charging coaster and an app for custom profiles.',
 'https://images.unsplash.com/photo-1514228742587-6b1558fcca3d?w=900&q=80', 399.00, 'CNY', 30, 12, 0),
(7, 2, 'HM-LINENBED-007', '水洗亚麻四件套', 'Washed Linen Bedding Set',
 '法国亚麻纤维，水洗工艺处理，透气亲肤，含被套、床单与两只枕套。',
 'French flax, garment-washed for softness and breathability, including duvet cover, flat sheet and two pillowcases.',
 'https://images.unsplash.com/photo-1522771739844-6a9f6d5f14af?w=900&q=80', 699.00, 'CNY', 25, 9, 1),
(8, 4, 'BT-CLEANSER-008', '氨基酸温和洁面乳', 'Amino Acid Gentle Cleanser',
 '弱酸性配方，含氨基酸表活与神经酰胺，敏感肌可用，120ml 容量符合机舱携带标准。',
 'A mildly acidic formula with amino acid surfactants and ceramides, cabin-friendly 120ml size.',
 'https://images.unsplash.com/photo-1556228720-195a672e8a03?w=900&q=80', 89.00, 'CNY', 200, 88, 1);

-- ------------------------------------------------------------
-- 演示 SKU（颜色 / 容量规格）
-- ------------------------------------------------------------
INSERT INTO `product_skus` (`product_id`, `sku_code`, `spec_name`, `price`, `stock`, `status`) VALUES
(1, 'EC-HEADPHONE-001-BK', 'Color: Midnight Black', 899.00, 70, 1),
(1, 'EC-HEADPHONE-001-WH', 'Color: Cloud White',   929.00, 50, 1),
(3, 'OD-BACKPACK-035-BK', 'Color: Black / 35L',    459.00, 30, 1),
(3, 'OD-BACKPACK-035-OL', 'Color: Olive / 35L',    459.00, 15, 1);

-- ------------------------------------------------------------
-- 模拟汇率（管理员维护，不接实时外汇接口）
-- ------------------------------------------------------------
INSERT INTO `exchange_rates` (`from_currency`, `to_currency`, `rate`, `status`) VALUES
('CNY', 'USD', 0.140000, 1),
('USD', 'CNY', 7.140000, 1);
