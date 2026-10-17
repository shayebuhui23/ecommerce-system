package com.ecommerce.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 登录入参，account 支持用户名或邮箱。
 */
@Data
public class LoginDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "请输入用户名或邮箱")
    private String account;

    @NotBlank(message = "请输入密码")
    private String password;
}
