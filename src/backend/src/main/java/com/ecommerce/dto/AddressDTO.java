package com.ecommerce.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 收货地址入参。
 */
@Data
public class AddressDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "收货人不能为空")
    @Size(max = 64, message = "收货人不能超过 64 个字符")
    private String consignee;

    @NotBlank(message = "国家或地区不能为空")
    @Size(max = 64, message = "国家或地区不能超过 64 个字符")
    private String country;

    @Size(max = 64, message = "省/州不能超过 64 个字符")
    private String provinceState;

    @Size(max = 64, message = "城市不能超过 64 个字符")
    private String city;

    @Size(max = 20, message = "邮政编码不能超过 20 个字符")
    private String postalCode;

    @NotBlank(message = "详细地址不能为空")
    @Size(max = 255, message = "详细地址不能超过 255 个字符")
    private String addressLine;

    @Size(max = 30, message = "联系电话不能超过 30 个字符")
    private String phone;

    @Min(value = 0, message = "默认地址取值只能为 0 或 1")
    @Max(value = 1, message = "默认地址取值只能为 0 或 1")
    private Integer isDefault = 0;
}
