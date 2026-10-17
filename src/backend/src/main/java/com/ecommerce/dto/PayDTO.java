package com.ecommerce.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 模拟支付入参。
 * <p>
 * amount 用于校验前端展示与后端应付金额是否一致，最终仍以订单中的金额为准。
 */
@Data
public class PayDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "订单不能为空")
    private Long orderId;

    @NotNull(message = "支付金额不能为空")
    @DecimalMin(value = "0.01", message = "支付金额必须大于 0")
    private BigDecimal amount;

    /** true 模拟支付成功；false 模拟支付失败（仅用于演示异常分支） */
    private Boolean success = Boolean.TRUE;
}
