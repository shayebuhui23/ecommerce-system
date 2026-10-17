package com.ecommerce.vo;

import com.ecommerce.entity.Product;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品列表项响应。
 */
@Data
public class ProductVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long categoryId;
    private String categoryName;
    private Long sellerId;
    private String sellerName;
    private String skuCode;
    private String name;
    private String nameEn;
    private String imageUrl;
    private BigDecimal price;
    /** 成本价，仅管理员与商家可见 */
    private BigDecimal costPrice;
    private String currency;
    private Integer stock;
    private Integer salesCount;
    private Integer status;
    private LocalDateTime createdAt;

    public static ProductVO from(Product product, String categoryName, String sellerName) {
        ProductVO vo = new ProductVO();
        vo.setId(product.getId());
        vo.setCategoryId(product.getCategoryId());
        vo.setCategoryName(categoryName);
        vo.setSellerId(product.getSellerId());
        vo.setSellerName(sellerName);
        vo.setSkuCode(product.getSkuCode());
        vo.setName(product.getName());
        vo.setNameEn(product.getNameEn());
        vo.setImageUrl(product.getImageUrl());
        vo.setPrice(product.getPrice());
        vo.setCurrency(product.getCurrency());
        vo.setStock(product.getStock());
        vo.setSalesCount(product.getSalesCount());
        vo.setStatus(product.getStatus());
        vo.setCreatedAt(product.getCreatedAt());
        return vo;
    }
}
