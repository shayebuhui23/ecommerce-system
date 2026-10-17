package com.ecommerce.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 商品新增/编辑入参，SKU 列表随商品一起提交。
 */
@Data
public class ProductDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "请选择商品分类")
    private Long categoryId;

    @NotBlank(message = "商品 SKU 编码不能为空")
    @Size(max = 64, message = "SKU 编码不能超过 64 个字符")
    private String skuCode;

    @NotBlank(message = "商品名称不能为空")
    @Size(max = 128, message = "商品名称不能超过 128 个字符")
    private String name;

    @Size(max = 128, message = "英文名称不能超过 128 个字符")
    private String nameEn;

    private String description;

    private String descriptionEn;

    @Size(max = 255, message = "图片地址不能超过 255 个字符")
    private String imageUrl;

    @NotNull(message = "商品价格不能为空")
    @DecimalMin(value = "0.01", message = "商品价格必须大于 0")
    private BigDecimal price;

    @Pattern(regexp = "^[A-Z]{3}$", message = "币种需为 3 位大写字母，如 CNY、USD")
    private String currency = "CNY";

    @NotNull(message = "商品库存不能为空")
    @Min(value = 0, message = "商品库存不能为负数")
    private Integer stock;

    @Min(value = 0, message = "商品状态取值只能为 0 或 1")
    @Max(value = 1, message = "商品状态取值只能为 0 或 1")
    private Integer status = 0;

    @Valid
    private List<SkuDTO> skus = new ArrayList<>();
}
