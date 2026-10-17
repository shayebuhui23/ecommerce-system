package com.ecommerce.security;

import cn.dev33.satoken.stp.StpUtil;

/**
 * 当前登录用户工具类。
 * <p>
 * 为后续"数据归属校验"（客户只能访问自己的订单/地址）预留统一入口，
 * 业务层不要直接从请求参数中取用户 ID。
 */
public final class LoginUserUtil {

    private LoginUserUtil() {
    }

    /**
     * 获取当前登录用户 ID，未登录时抛出 NotLoginException（由全局异常处理器转为 401）。
     */
    public static Long currentUserId() {
        return StpUtil.getLoginIdAsLong();
    }

    public static boolean isLogin() {
        return StpUtil.isLogin();
    }
}
