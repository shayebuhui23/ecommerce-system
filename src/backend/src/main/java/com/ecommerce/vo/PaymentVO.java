package com.ecommerce.vo;

import com.ecommerce.entity.Payment;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付记录响应。
 */
@Data
public class PaymentVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String paymentNo;
    private Long orderId;
    private String orderNo;
    private BigDecimal amount;
    private String currency;
    private String method;
    private Integer status;
    private String statusText;
    private String failReason;
    private LocalDateTime paidAt;

    public static PaymentVO from(Payment payment, String orderNo) {
        PaymentVO vo = new PaymentVO();
        vo.setId(payment.getId());
        vo.setPaymentNo(payment.getPaymentNo());
        vo.setOrderId(payment.getOrderId());
        vo.setOrderNo(orderNo);
        vo.setAmount(payment.getAmount());
        vo.setCurrency(payment.getCurrency());
        vo.setMethod(payment.getMethod());
        vo.setStatus(payment.getStatus());
        vo.setStatusText(payment.getStatus() != null && payment.getStatus() == 1 ? "支付成功" : "支付失败");
        vo.setFailReason(payment.getFailReason());
        vo.setPaidAt(payment.getPaidAt());
        return vo;
    }
}
