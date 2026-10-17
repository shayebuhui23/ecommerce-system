-- =============================================================
-- 小型电商订单系统（跨境贸易场景扩展版）数据库结构
-- MySQL 8.0 / InnoDB / utf8mb4
-- 执行方式：mysql -u root -p < database/schema.sql
-- 说明：本脚本可重复执行（先删表再建表），仅用于开发与课程演示环境
-- =============================================================

CREATE DATABASE IF NOT EXISTS `ecommerce`
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_general_ci;

USE `ecommerce`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `operation_logs`;
DROP TABLE IF EXISTS `exchange_rates`;
DROP TABLE IF EXISTS `inventory_logs`;
DROP TABLE IF EXISTS `reviews`;
DROP TABLE IF EXISTS `after_sales`;
DROP TABLE IF EXISTS `shipments`;
DROP TABLE IF EXISTS `payments`;
DROP TABLE IF EXISTS `order_items`;
DROP TABLE IF EXISTS `orders`;
DROP TABLE IF EXISTS `addresses`;
DROP TABLE IF EXISTS `cart_items`;
DROP TABLE IF EXISTS `carts`;
DROP TABLE IF EXISTS `product_skus`;
DROP TABLE IF EXISTS `products`;
DROP TABLE IF EXISTS `categories`;
DROP TABLE IF EXISTS `users`;
DROP TABLE IF EXISTS `roles`;

-- ------------------------------------------------------------
-- 1. 角色定义
-- ------------------------------------------------------------
CREATE TABLE `roles` (
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `code`        VARCHAR(32)     NOT NULL COMMENT '角色编码：ADMIN/STAFF/CUSTOMER',
    `name`        VARCHAR(64)     NOT NULL COMMENT '角色名称',
    `description` VARCHAR(255)             DEFAULT NULL COMMENT '角色说明',
    `created_at`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_roles_code` (`code`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='角色定义';

-- ------------------------------------------------------------
-- 2. 用户（含角色关联）
-- ------------------------------------------------------------
CREATE TABLE `users` (
    `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username`   VARCHAR(50)     NOT NULL COMMENT '登录用户名',
    `email`      VARCHAR(100)    NOT NULL COMMENT '邮箱，可用于登录',
    `password`   VARCHAR(100)    NOT NULL COMMENT '密码哈希（BCrypt），不存明文',
    `nickname`   VARCHAR(50)              DEFAULT NULL COMMENT '昵称',
    `phone`      VARCHAR(30)              DEFAULT NULL COMMENT '联系电话',
    `country`    VARCHAR(64)              DEFAULT NULL COMMENT '所在国家或地区',
    `role_id`    BIGINT UNSIGNED NOT NULL COMMENT '角色 ID',
    `status`     TINYINT         NOT NULL DEFAULT 1 COMMENT '状态：1 启用，0 禁用',
    `deleted`    TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 正常，1 已删除',
    `created_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_users_username` (`username`),
    UNIQUE KEY `uk_users_email` (`email`),
    KEY `idx_users_role` (`role_id`),
    KEY `idx_users_status` (`status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='用户及角色关联';

-- ------------------------------------------------------------
-- 3. 商品分类
-- ------------------------------------------------------------
CREATE TABLE `categories` (
    `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `parent_id`  BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '父分类 ID，0 表示顶级',
    `name`       VARCHAR(64)     NOT NULL COMMENT '分类名称（中文）',
    `name_en`    VARCHAR(64)              DEFAULT NULL COMMENT '分类名称（英文）',
    `sort_order` INT             NOT NULL DEFAULT 0 COMMENT '排序值，越小越靠前',
    `status`     TINYINT         NOT NULL DEFAULT 1 COMMENT '状态：1 启用，0 停用',
    `deleted`    TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `created_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_categories_parent` (`parent_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='商品分类';

-- ------------------------------------------------------------
-- 4. 商品基础信息
-- ------------------------------------------------------------
CREATE TABLE `products` (
    `id`             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `category_id`    BIGINT UNSIGNED NOT NULL COMMENT '分类 ID',
    `sku_code`       VARCHAR(64)     NOT NULL COMMENT '商品级 SKU 编码，全局唯一',
    `name`           VARCHAR(128)    NOT NULL COMMENT '商品名称（中文）',
    `name_en`        VARCHAR(128)             DEFAULT NULL COMMENT '商品名称（英文）',
    `description`    TEXT                     DEFAULT NULL COMMENT '商品描述（中文）',
    `description_en` TEXT                     DEFAULT NULL COMMENT '商品描述（英文）',
    `image_url`      VARCHAR(255)             DEFAULT NULL COMMENT '商品主图地址',
    `price`          DECIMAL(12, 2)  NOT NULL COMMENT '售价，基础币种计价',
    `currency`       CHAR(3)         NOT NULL DEFAULT 'CNY' COMMENT '基础币种',
    `stock`          INT             NOT NULL DEFAULT 0 COMMENT '可用库存',
    `sales_count`    INT             NOT NULL DEFAULT 0 COMMENT '累计销量，用于统计排序',
    `status`         TINYINT         NOT NULL DEFAULT 0 COMMENT '状态：1 上架，0 下架',
    `deleted`        TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `created_at`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_products_sku` (`sku_code`),
    KEY `idx_products_category` (`category_id`),
    KEY `idx_products_status` (`status`),
    KEY `idx_products_name` (`name`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='商品基础信息';

-- ------------------------------------------------------------
-- 5. 商品 SKU（规格与库存）
-- ------------------------------------------------------------
CREATE TABLE `product_skus` (
    `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `product_id` BIGINT UNSIGNED NOT NULL COMMENT '所属商品 ID',
    `sku_code`   VARCHAR(64)     NOT NULL COMMENT 'SKU 编码，全局唯一',
    `spec_name`  VARCHAR(128)             DEFAULT NULL COMMENT '规格描述，如 Color:Black;Size:M',
    `price`      DECIMAL(12, 2)  NOT NULL COMMENT '该规格售价',
    `stock`      INT             NOT NULL DEFAULT 0 COMMENT '该规格库存',
    `status`     TINYINT         NOT NULL DEFAULT 1 COMMENT '状态：1 启用，0 停用',
    `deleted`    TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `created_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_skus_code` (`sku_code`),
    KEY `idx_skus_product` (`product_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='商品规格及库存';

-- ------------------------------------------------------------
-- 6. 购物车（一个客户一个购物车）
-- ------------------------------------------------------------
CREATE TABLE `carts` (
    `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`    BIGINT UNSIGNED NOT NULL COMMENT '所属客户 ID',
    `created_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_carts_user` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='购物车';

-- ------------------------------------------------------------
-- 7. 购物车明细
-- ------------------------------------------------------------
CREATE TABLE `cart_items` (
    `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `cart_id`    BIGINT UNSIGNED NOT NULL COMMENT '购物车 ID',
    `product_id` BIGINT UNSIGNED NOT NULL COMMENT '商品 ID',
    `sku_id`     BIGINT UNSIGNED          DEFAULT NULL COMMENT 'SKU ID，为空表示使用商品默认价格库存',
    `quantity`   INT             NOT NULL COMMENT '购买数量，必须为正整数',
    `created_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_cart_items` (`cart_id`, `product_id`),
    KEY `idx_cart_items_product` (`product_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='购物车明细';

-- ------------------------------------------------------------
-- 8. 客户收货地址
-- ------------------------------------------------------------
CREATE TABLE `addresses` (
    `id`             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`        BIGINT UNSIGNED NOT NULL COMMENT '所属客户 ID',
    `consignee`      VARCHAR(64)     NOT NULL COMMENT '收货人',
    `country`        VARCHAR(64)     NOT NULL COMMENT '国家或地区',
    `province_state` VARCHAR(64)              DEFAULT NULL COMMENT '省/州',
    `city`           VARCHAR(64)              DEFAULT NULL COMMENT '城市',
    `postal_code`    VARCHAR(20)              DEFAULT NULL COMMENT '邮政编码',
    `address_line`   VARCHAR(255)    NOT NULL COMMENT '详细地址',
    `phone`          VARCHAR(30)              DEFAULT NULL COMMENT '联系电话',
    `is_default`     TINYINT         NOT NULL DEFAULT 0 COMMENT '是否默认地址：1 是，0 否',
    `deleted`        TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `created_at`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_addresses_user` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='客户收货地址';

-- ------------------------------------------------------------
-- 9. 订单主表
-- ------------------------------------------------------------
CREATE TABLE `orders` (
    `id`                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_no`          VARCHAR(32)     NOT NULL COMMENT '订单编号，全局唯一',
    `user_id`           BIGINT UNSIGNED NOT NULL COMMENT '下单客户 ID',
    `currency`          CHAR(3)         NOT NULL DEFAULT 'CNY' COMMENT '订单币种',
    `exchange_rate`     DECIMAL(12, 6)  NOT NULL DEFAULT 1.000000 COMMENT '下单时采用的汇率快照',
    `total_amount`      DECIMAL(14, 2)  NOT NULL COMMENT '商品金额合计（订单币种）',
    `freight_amount`    DECIMAL(14, 2)  NOT NULL DEFAULT 0.00 COMMENT '运费',
    `pay_amount`        DECIMAL(14, 2)  NOT NULL COMMENT '应付金额 = 商品金额 + 运费',
    `base_amount`       DECIMAL(14, 2)  NOT NULL COMMENT '折算基础币种金额，用于跨币种统计',
    `consignee`         VARCHAR(64)     NOT NULL COMMENT '收货人快照',
    `country`           VARCHAR(64)     NOT NULL COMMENT '国家或地区快照',
    `province_state`    VARCHAR(64)              DEFAULT NULL COMMENT '省/州快照',
    `city`              VARCHAR(64)              DEFAULT NULL COMMENT '城市快照',
    `postal_code`       VARCHAR(20)              DEFAULT NULL COMMENT '邮政编码快照',
    `address_line`      VARCHAR(255)    NOT NULL COMMENT '详细地址快照',
    `phone`             VARCHAR(30)              DEFAULT NULL COMMENT '联系电话快照',
    `shipping_method`   VARCHAR(32)              DEFAULT NULL COMMENT '运输方式：AIR/SEA/EXPRESS',
    `status`            VARCHAR(32)     NOT NULL COMMENT '订单状态：PENDING_PAYMENT/PENDING_SHIPMENT/SHIPPED/COMPLETED/CANCELLED/AFTER_SALES/REFUNDED',
    `remark`            VARCHAR(500)             DEFAULT NULL COMMENT '订单备注',
    `paid_at`           DATETIME                 DEFAULT NULL COMMENT '支付时间',
    `shipped_at`        DATETIME                 DEFAULT NULL COMMENT '发货时间',
    `completed_at`      DATETIME                 DEFAULT NULL COMMENT '完成时间',
    `cancelled_at`      DATETIME                 DEFAULT NULL COMMENT '取消时间',
    `deleted`           TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `created_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_orders_no` (`order_no`),
    KEY `idx_orders_user` (`user_id`),
    KEY `idx_orders_status` (`status`),
    KEY `idx_orders_created` (`created_at`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='订单主表';

-- ------------------------------------------------------------
-- 10. 订单商品快照
-- ------------------------------------------------------------
CREATE TABLE `order_items` (
    `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_id`        BIGINT UNSIGNED NOT NULL COMMENT '订单 ID',
    `product_id`      BIGINT UNSIGNED NOT NULL COMMENT '商品 ID',
    `sku_id`          BIGINT UNSIGNED          DEFAULT NULL COMMENT 'SKU ID',
    `product_name`    VARCHAR(128)    NOT NULL COMMENT '下单时商品名称快照',
    `product_name_en` VARCHAR(128)             DEFAULT NULL COMMENT '下单时英文名称快照',
    `sku_code`        VARCHAR(64)     NOT NULL COMMENT '下单时 SKU 编码快照',
    `spec_name`       VARCHAR(128)             DEFAULT NULL COMMENT '下单时规格快照',
    `image_url`       VARCHAR(255)             DEFAULT NULL COMMENT '下单时图片快照',
    `unit_price`      DECIMAL(12, 2)  NOT NULL COMMENT '下单时单价快照',
    `quantity`        INT             NOT NULL COMMENT '购买数量',
    `subtotal`        DECIMAL(14, 2)  NOT NULL COMMENT '小计 = 单价 × 数量',
    `currency`        CHAR(3)         NOT NULL COMMENT '币种',
    `created_at`      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_order_items_order` (`order_id`),
    KEY `idx_order_items_product` (`product_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='订单商品快照';

-- ------------------------------------------------------------
-- 11. 模拟支付记录
-- ------------------------------------------------------------
CREATE TABLE `payments` (
    `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `payment_no` VARCHAR(32)     NOT NULL COMMENT '支付流水号，全局唯一',
    `order_id`   BIGINT UNSIGNED NOT NULL COMMENT '订单 ID',
    `user_id`    BIGINT UNSIGNED NOT NULL COMMENT '付款客户 ID',
    `amount`     DECIMAL(14, 2)  NOT NULL COMMENT '支付金额',
    `currency`   CHAR(3)         NOT NULL COMMENT '支付币种',
    `method`     VARCHAR(32)     NOT NULL DEFAULT 'MOCK' COMMENT '支付方式，本项目固定为模拟支付',
    `status`     TINYINT         NOT NULL COMMENT '状态：1 成功，0 失败',
    `fail_reason` VARCHAR(255)            DEFAULT NULL COMMENT '失败原因',
    `trade_no`   VARCHAR(64)              DEFAULT NULL COMMENT '模拟第三方交易号',
    `paid_at`    DATETIME                 DEFAULT NULL COMMENT '支付完成时间',
    `created_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_payments_no` (`payment_no`),
    KEY `idx_payments_order` (`order_id`),
    KEY `idx_payments_user` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='模拟支付记录';

-- ------------------------------------------------------------
-- 12. 发货及物流信息
-- ------------------------------------------------------------
CREATE TABLE `shipments` (
    `id`                 BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_id`           BIGINT UNSIGNED NOT NULL COMMENT '订单 ID',
    `shipping_company`   VARCHAR(64)     NOT NULL COMMENT '物流公司',
    `tracking_no`        VARCHAR(64)     NOT NULL COMMENT '物流单号',
    `shipping_method`    VARCHAR(32)              DEFAULT NULL COMMENT '运输方式：AIR/SEA/EXPRESS',
    `from_country`       VARCHAR(64)              DEFAULT NULL COMMENT '发货国家或地区',
    `to_country`         VARCHAR(64)              DEFAULT NULL COMMENT '目的国家或地区',
    `status`             VARCHAR(32)     NOT NULL DEFAULT 'PENDING' COMMENT '运输状态：PENDING/IN_TRANSIT/DELIVERED',
    `estimated_delivery` DATE                     DEFAULT NULL COMMENT '预计送达日期',
    `shipped_at`         DATETIME                 DEFAULT NULL COMMENT '发货时间',
    `created_at`         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_shipments_order` (`order_id`),
    KEY `idx_shipments_tracking` (`tracking_no`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='发货及物流信息';

-- ------------------------------------------------------------
-- 13. 售后与退款申请
-- ------------------------------------------------------------
CREATE TABLE `after_sales` (
    `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `after_sale_no` VARCHAR(32)     NOT NULL COMMENT '售后单号，全局唯一',
    `order_id`      BIGINT UNSIGNED NOT NULL COMMENT '订单 ID',
    `user_id`       BIGINT UNSIGNED NOT NULL COMMENT '申请客户 ID',
    `type`          VARCHAR(32)     NOT NULL DEFAULT 'REFUND' COMMENT '售后类型：REFUND 整单退款',
    `reason`        VARCHAR(255)    NOT NULL COMMENT '退款原因',
    `description`   VARCHAR(500)             DEFAULT NULL COMMENT '补充说明',
    `amount`        DECIMAL(14, 2)  NOT NULL COMMENT '申请退款金额，不得超过可退款金额',
    `currency`      CHAR(3)         NOT NULL COMMENT '退款币种，与原订单一致',
    `status`        VARCHAR(32)     NOT NULL COMMENT '状态：PENDING/APPROVED/REJECTED/REFUNDED',
    `audit_remark`  VARCHAR(500)             DEFAULT NULL COMMENT '审核意见',
    `audited_by`    BIGINT UNSIGNED          DEFAULT NULL COMMENT '审核人 ID',
    `audited_at`    DATETIME                 DEFAULT NULL COMMENT '审核时间',
    `refunded_at`   DATETIME                 DEFAULT NULL COMMENT '退款完成时间',
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_after_sales_no` (`after_sale_no`),
    KEY `idx_after_sales_order` (`order_id`),
    KEY `idx_after_sales_user` (`user_id`),
    KEY `idx_after_sales_status` (`status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='售后与退款申请';

-- ------------------------------------------------------------
-- 14. 商品评价
-- ------------------------------------------------------------
CREATE TABLE `reviews` (
    `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_id`      BIGINT UNSIGNED NOT NULL COMMENT '订单 ID',
    `order_item_id` BIGINT UNSIGNED NOT NULL COMMENT '订单明细 ID，同一明细只能评价一次',
    `user_id`       BIGINT UNSIGNED NOT NULL COMMENT '评价客户 ID',
    `product_id`    BIGINT UNSIGNED NOT NULL COMMENT '商品 ID',
    `rating`        TINYINT         NOT NULL COMMENT '评分 1~5',
    `content`       VARCHAR(500)             DEFAULT NULL COMMENT '评价内容',
    `status`        TINYINT         NOT NULL DEFAULT 1 COMMENT '状态：1 显示，0 隐藏',
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_reviews_order_item` (`order_item_id`),
    KEY `idx_reviews_product` (`product_id`),
    KEY `idx_reviews_user` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='商品评价';

-- ------------------------------------------------------------
-- 15. 库存变更记录
-- ------------------------------------------------------------
CREATE TABLE `inventory_logs` (
    `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `product_id`      BIGINT UNSIGNED NOT NULL COMMENT '商品 ID',
    `sku_id`          BIGINT UNSIGNED          DEFAULT NULL COMMENT 'SKU ID',
    `order_id`        BIGINT UNSIGNED          DEFAULT NULL COMMENT '关联订单 ID',
    `change_type`     VARCHAR(32)     NOT NULL COMMENT '变更类型：ORDER_DEDUCT/ORDER_CANCEL/REFUND_RESTORE/MANUAL_ADJUST',
    `change_quantity` INT             NOT NULL COMMENT '变更数量，正数增加、负数减少',
    `before_stock`    INT             NOT NULL COMMENT '变更前库存',
    `after_stock`     INT             NOT NULL COMMENT '变更后库存',
    `remark`          VARCHAR(255)             DEFAULT NULL COMMENT '备注',
    `operator_id`     BIGINT UNSIGNED          DEFAULT NULL COMMENT '操作人 ID',
    `created_at`      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_inventory_product` (`product_id`),
    KEY `idx_inventory_order` (`order_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='库存变更记录';

-- ------------------------------------------------------------
-- 16. 模拟汇率配置
-- ------------------------------------------------------------
CREATE TABLE `exchange_rates` (
    `id`             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `from_currency`  CHAR(3)         NOT NULL COMMENT '源币种',
    `to_currency`    CHAR(3)         NOT NULL COMMENT '目标币种',
    `rate`           DECIMAL(12, 6)  NOT NULL COMMENT '汇率：1 单位源币种 = rate 单位目标币种',
    `effective_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生效时间',
    `status`         TINYINT         NOT NULL DEFAULT 1 COMMENT '状态：1 启用，0 停用',
    `created_at`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_exchange_pair` (`from_currency`, `to_currency`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='模拟汇率配置';

-- ------------------------------------------------------------
-- 17. 关键操作审计日志
-- ------------------------------------------------------------
CREATE TABLE `operation_logs` (
    `id`             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`        BIGINT UNSIGNED          DEFAULT NULL COMMENT '操作人 ID',
    `username`       VARCHAR(50)              DEFAULT NULL COMMENT '操作人用户名快照',
    `module`         VARCHAR(64)              DEFAULT NULL COMMENT '业务模块',
    `action`         VARCHAR(128)             DEFAULT NULL COMMENT '操作描述',
    `request_method` VARCHAR(10)              DEFAULT NULL COMMENT 'HTTP 方法',
    `request_uri`    VARCHAR(255)             DEFAULT NULL COMMENT '请求地址',
    `params`         TEXT                     DEFAULT NULL COMMENT '请求参数摘要',
    `ip`             VARCHAR(64)              DEFAULT NULL COMMENT '客户端 IP',
    `created_at`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_operation_user` (`user_id`),
    KEY `idx_operation_created` (`created_at`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='关键操作审计日志';

SET FOREIGN_KEY_CHECKS = 1;
