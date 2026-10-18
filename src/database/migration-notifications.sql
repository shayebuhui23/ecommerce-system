-- =============================================================
-- 升级：站内消息通知（客户催发货提醒等）
-- 可重复执行
-- =============================================================

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `notifications` (
    `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`    BIGINT UNSIGNED NOT NULL COMMENT '接收人 ID',
    `type`       VARCHAR(32)     NOT NULL COMMENT '消息类型，如 ORDER_URGE',
    `title`      VARCHAR(128)    NOT NULL COMMENT '消息标题',
    `content`    VARCHAR(500)             DEFAULT NULL COMMENT '消息内容',
    `related_id` BIGINT UNSIGNED          DEFAULT NULL COMMENT '关联业务 ID',
    `link`       VARCHAR(255)             DEFAULT NULL COMMENT '点击跳转的前端路径',
    `is_read`    TINYINT         NOT NULL DEFAULT 0 COMMENT '是否已读：1 已读，0 未读',
    `deleted`    TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `created_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_notifications_user` (`user_id`, `is_read`, `created_at`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='站内消息通知';
