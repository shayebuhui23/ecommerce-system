package com.ecommerce.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商品 SKU 入参，随商品一起提交。
 */
@Data
public class SkuDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 为空表示新增 */
    private Long id;

    @NotBlank(message = "SKU 编码不能为空")
    @Size(max = 64, message = "SKU 编码不能超过 64 个字符")
    private String skuCode;

    @Size(max = 128, message = "规格描述不能超过 128 个字符")
    private String specName;

    @NotNull(message = "SKU 价格不能为空")
    @DecimalMin(value = "0.01", message = "SKU 价格必须大于 0")
    private BigDecimal price;

    @NotNull(message = "SKU 库存不能为空")
    @Min(value = 0, message = "SKU 库存不能为负数")
    private Integer stock;

    @Min(value = 0, message = "SKU 状态取值只能为 0 或 1")
    @Max(value = 1, message = "SKU 状态取值只能为 0 或 1")
    private Integer status = 1;
}
