package com.ecommerce.vo;

import com.ecommerce.entity.AfterSale;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 售后单响应。
 */
@Data
public class AfterSaleVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String afterSaleNo;
    private Long orderId;
    private String orderNo;
    private Long userId;
    private String reason;
    private String description;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String statusText;
    private String auditRemark;
    private LocalDateTime auditedAt;
    private LocalDateTime refundedAt;
    private LocalDateTime createdAt;

    public static AfterSaleVO from(AfterSale afterSale, String orderNo) {
        AfterSaleVO vo = new AfterSaleVO();
        vo.setId(afterSale.getId());
        vo.setAfterSaleNo(afterSale.getAfterSaleNo());
        vo.setOrderId(afterSale.getOrderId());
        vo.setOrderNo(orderNo);
        vo.setUserId(afterSale.getUserId());
        vo.setReason(afterSale.getReason());
        vo.setDescription(afterSale.getDescription());
        vo.setAmount(afterSale.getAmount());
        vo.setCurrency(afterSale.getCurrency());
        vo.setStatus(afterSale.getStatus());
        vo.setStatusText(statusText(afterSale.getStatus()));
        vo.setAuditRemark(afterSale.getAuditRemark());
        vo.setAuditedAt(afterSale.getAuditedAt());
        vo.setRefundedAt(afterSale.getRefundedAt());
        vo.setCreatedAt(afterSale.getCreatedAt());
        return vo;
    }

    public static String statusText(String status) {
        if ("PENDING".equals(status)) {
            return "待审核";
        }
        if ("APPROVED".equals(status)) {
            return "已通过";
        }
        if ("REJECTED".equals(status)) {
            return "已驳回";
        }
        if ("REFUNDED".equals(status)) {
            return "已退款";
        }
        return "未知状态";
    }
}
