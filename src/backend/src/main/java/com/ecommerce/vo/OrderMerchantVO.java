package com.ecommerce.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 订单关联商家信息（名称、联系电话与发货地址）。
 */
@Data
public class OrderMerchantVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long sellerId;
    private String sellerName;
    private String phone;
    private String country;
    private String provinceState;
    private String city;
    private String postalCode;
    private String addressLine;
}
