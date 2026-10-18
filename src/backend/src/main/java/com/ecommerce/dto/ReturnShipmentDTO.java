package com.ecommerce.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 买家填写退货物流入参。
 */
@Data
public class ReturnShipmentDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "请填写退货物流公司")
    @Size(max = 64, message = "物流公司不能超过 64 个字符")
    private String shippingCompany;

    @NotBlank(message = "请填写退货物流单号")
    @Size(max = 64, message = "物流单号不能超过 64 个字符")
    private String trackingNo;
}
