package com.ecommerce.vo;

import com.ecommerce.entity.ProductSku;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商品 SKU 响应。
 */
@Data
public class SkuVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String skuCode;
    private String specName;
    private BigDecimal price;
    private Integer stock;
    private Integer status;

    public static SkuVO from(ProductSku sku) {
        SkuVO vo = new SkuVO();
        vo.setId(sku.getId());
        vo.setSkuCode(sku.getSkuCode());
        vo.setSpecName(sku.getSpecName());
        vo.setPrice(sku.getPrice());
        vo.setStock(sku.getStock());
        vo.setStatus(sku.getStatus());
        return vo;
    }
}
