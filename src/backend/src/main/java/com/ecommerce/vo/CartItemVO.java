package com.ecommerce.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 购物车明细响应。
 * <p>
 * price 与 subtotal 均基于商品当前价格实时计算，购物车不保存价格快照。
 */
@Data
public class CartItemVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long productId;
    private String productName;
    private String productNameEn;
    private String imageUrl;
    private String skuCode;
    private BigDecimal price;
    private String currency;
    private Integer stock;
    private Integer quantity;
    private BigDecimal subtotal;
    /** 商品当前是否可购买（上架且库存充足） */
    private Boolean available;
    private String unavailableReason;

    public static CartItemVO of(Long id, Long productId, String productName, String productNameEn,
                                String imageUrl, String skuCode, BigDecimal price, String currency,
                                Integer stock, Integer quantity, Integer productStatus) {
        CartItemVO vo = new CartItemVO();
        vo.setId(id);
        vo.setProductId(productId);
        vo.setProductName(productName);
        vo.setProductNameEn(productNameEn);
        vo.setImageUrl(imageUrl);
        vo.setSkuCode(skuCode);
        vo.setPrice(price);
        vo.setCurrency(currency);
        vo.setStock(stock);
        vo.setQuantity(quantity);
        vo.setSubtotal(price.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP));

        boolean onShelf = productStatus != null && productStatus == 1;
        if (!onShelf) {
            vo.setAvailable(false);
            vo.setUnavailableReason("商品已下架");
        } else if (stock == null || stock < quantity) {
            vo.setAvailable(false);
            vo.setUnavailableReason("库存不足");
        } else {
            vo.setAvailable(true);
        }
        return vo;
    }
}
