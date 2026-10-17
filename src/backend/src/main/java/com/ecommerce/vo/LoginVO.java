package com.ecommerce.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 登录响应：令牌 + 用户信息（含角色，供前端渲染菜单）。
 */
@Data
public class LoginVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 令牌值 */
    private String token;

    /** 令牌名称，前端需按该名称放入请求头 */
    private String tokenName;

    private UserInfoVO userInfo;
}
