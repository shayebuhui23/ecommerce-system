package com.ecommerce.vo;

import com.ecommerce.entity.Product;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * 商品详情响应，包含中英文描述与 SKU 列表。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProductDetailVO extends ProductVO {

    private static final long serialVersionUID = 1L;

    private String description;
    private String descriptionEn;
    private List<SkuVO> skus = new ArrayList<>();

    public static ProductDetailVO from(Product product, String categoryName) {
        ProductDetailVO vo = new ProductDetailVO();
        vo.setId(product.getId());
        vo.setCategoryId(product.getCategoryId());
        vo.setCategoryName(categoryName);
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
        vo.setDescription(product.getDescription());
        vo.setDescriptionEn(product.getDescriptionEn());
        return vo;
    }
}
