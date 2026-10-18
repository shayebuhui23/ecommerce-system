package com.ecommerce.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 售后申请入参。退款金额由后端按订单应付金额计算。
 */
@Data
public class AfterSaleApplyDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "请选择订单")
    private Long orderId;

    /** REFUND_ONLY 仅退款 / RETURN_REFUND 退货退款；为空时按订单状态自动判断 */
    @Pattern(regexp = "^(REFUND_ONLY|RETURN_REFUND)$", message = "售后类型不合法")
    private String type;

    @NotBlank(message = "请填写退款原因")
    @Size(max = 255, message = "退款原因不能超过 255 个字符")
    private String reason;

    @Size(max = 500, message = "补充说明不能超过 500 个字符")
    private String description;
}
