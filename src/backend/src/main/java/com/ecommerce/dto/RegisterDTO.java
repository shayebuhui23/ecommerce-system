package com.ecommerce.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 注册入参。
 * <p>
 * 注意：本 DTO 不接收角色 ID，只接受注册类型（客户 / 商家），
 * 由服务端映射为 CUSTOMER 或 STAFF，防止客户端通过传入 roleId 自行提权。
 */
@Data
public class RegisterDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 注册类型：CUSTOMER（普通客户，默认）/ MERCHANT（商家）。
     */
    @Pattern(regexp = "^(CUSTOMER|MERCHANT)$", message = "注册类型只能是 CUSTOMER 或 MERCHANT")
    private String registerType;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过 100 个字符")
    private String email;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度需在 6~32 个字符之间")
    private String password;

    @Size(max = 50, message = "昵称长度不能超过 50 个字符")
    private String nickname;

    @Size(max = 64, message = "国家或地区长度不能超过 64 个字符")
    private String country;
}
