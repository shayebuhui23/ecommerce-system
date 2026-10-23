package com.ecommerce.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 加入购物车入参。
 */
@Data
public class CartItemDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "请选择商品")
    private Long productId;

    /** SKU ID；商品存在规格时必填 */
    @Min(value = 1, message = "请选择商品规格")
    private Long skuId;

    @NotNull(message = "购买数量不能为空")
    @Min(value = 1, message = "购买数量必须大于 0")
    @Max(value = 999, message = "单次最多购买 999 件")
    private Integer quantity;
}
