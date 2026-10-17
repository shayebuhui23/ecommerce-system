package com.ecommerce.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 个人资料修改入参。除账号状态与角色外，其余资料均可自行修改。
 */
@Data
public class ProfileDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过 100 个字符")
    private String email;

    @Size(max = 50, message = "昵称不能超过 50 个字符")
    private String nickname;

    @Size(max = 30, message = "联系电话不能超过 30 个字符")
    private String phone;

    @Size(max = 64, message = "国家或地区不能超过 64 个字符")
    private String country;
}
