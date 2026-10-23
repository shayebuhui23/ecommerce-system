package com.ecommerce.vo;

import com.ecommerce.entity.OrderItem;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单明细响应（下单时的快照数据）。
 */
@Data
public class OrderItemVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long productId;
    /** 商品所属商家 */
    private Long sellerId;
    private String sellerName;
    private Long skuId;
    private String specName;
    private String productName;
    private String productNameEn;
    private String skuCode;
    private String imageUrl;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal subtotal;

    public static OrderItemVO from(OrderItem item) {
        OrderItemVO vo = new OrderItemVO();
        vo.setId(item.getId());
        vo.setProductId(item.getProductId());
        vo.setSkuId(item.getSkuId());
        vo.setSpecName(item.getSpecName());
        vo.setProductName(item.getProductName());
        vo.setProductNameEn(item.getProductNameEn());
        vo.setSkuCode(item.getSkuCode());
        vo.setImageUrl(item.getImageUrl());
        vo.setUnitPrice(item.getUnitPrice());
        vo.setQuantity(item.getQuantity());
        vo.setSubtotal(item.getSubtotal());
        return vo;
    }
}
