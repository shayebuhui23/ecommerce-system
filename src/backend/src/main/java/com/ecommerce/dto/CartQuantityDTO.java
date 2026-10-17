package com.ecommerce.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 修改购物车数量入参。
 */
@Data
public class CartQuantityDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "购买数量不能为空")
    @Min(value = 1, message = "购买数量必须大于 0")
    @Max(value = 999, message = "单次最多购买 999 件")
    private Integer quantity;
}
