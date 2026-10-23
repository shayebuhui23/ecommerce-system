package com.ecommerce.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单创建入参。
 * <p>
 * 只接收"买哪些购物车项"，金额、单价、运费全部由后端计算，
 * 不信任前端提交的任何金额字段。
 */
@Data
public class OrderCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "请选择收货地址")
    private Long addressId;

    /** 参与结算的购物车项 ID；为空表示结算购物车全部商品 */
    private List<Long> cartItemIds = new ArrayList<>();

    /** 立即购买的商品 ID，与 cartItemIds 二选一 */
    @Min(value = 1, message = "请选择商品")
    private Long productId;

    /** 立即购买数量 */
    @Min(value = 1, message = "购买数量必须大于 0")
    @Max(value = 999, message = "单次最多购买 999 件")
    private Integer quantity;

    /** 立即购买的商品规格 */
    @Min(value = 1, message = "请选择商品规格")
    private Long skuId;

    @Size(max = 32, message = "运输方式长度不能超过 32 个字符")
    private String shippingMethod = "EXPRESS";

    /** 订单币种，默认基础币种 CNY，其他币种按管理员维护的模拟汇率换算 */
    @Pattern(regexp = "^[A-Z]{3}$", message = "币种需为 3 位大写字母，如 CNY、USD")
    private String currency = "CNY";

    @Size(max = 500, message = "订单备注不能超过 500 个字符")
    private String remark;
}
