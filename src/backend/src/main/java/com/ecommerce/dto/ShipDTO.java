package com.ecommerce.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 发货入参。物流公司与物流单号必填。
 */
@Data
public class ShipDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "请填写物流公司")
    @Size(max = 64, message = "物流公司名称不能超过 64 个字符")
    private String shippingCompany;

    @NotBlank(message = "请填写物流单号")
    @Size(max = 64, message = "物流单号不能超过 64 个字符")
    private String trackingNo;

    @Size(max = 32, message = "运输方式不能超过 32 个字符")
    private String shippingMethod = "EXPRESS";

    @Size(max = 64, message = "发货国家或地区不能超过 64 个字符")
    private String fromCountry = "China";

    @Size(max = 64, message = "目的国家或地区不能超过 64 个字符")
    private String toCountry;

    /** 预计送达日期，仅作演示 */
    private LocalDate estimatedDelivery;
}
