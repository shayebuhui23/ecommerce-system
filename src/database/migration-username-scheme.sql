-- =============================================================
-- 升级：用户名按角色统一编号
--   普通客户：user + 6 位序号
--   商家运营：seller + 6 位序号
--   系统管理员：保持 admin 不变
-- 适用于旧版演示数据，可重复执行（仅重命名旧用户名）
-- =============================================================

SET NAMES utf8mb4;

UPDATE `users` SET `username` = 'seller000001', `email` = 'seller000001@ecommerce.local'
WHERE `username` = 'staff';
UPDATE `users` SET `username` = 'seller000002', `email` = 'seller000002@ecommerce.local'
WHERE `username` = 'staff2';
UPDATE `users` SET `username` = 'user000001', `email` = 'user000001@ecommerce.local'
WHERE `username` = 'customer';
UPDATE `users` SET `username` = 'user000002'
WHERE `username` = 'zhangsan';

SELECT u.`id`, u.`username`, r.`code` AS `role`
FROM `users` u
JOIN `roles` r ON r.`id` = u.`role_id`
ORDER BY u.`id`;
