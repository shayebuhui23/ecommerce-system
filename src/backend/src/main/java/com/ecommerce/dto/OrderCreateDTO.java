package com.ecommerce.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
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

    @Size(max = 32, message = "运输方式长度不能超过 32 个字符")
    private String shippingMethod = "EXPRESS";

    @Size(max = 500, message = "订单备注不能超过 500 个字符")
    private String remark;
}
