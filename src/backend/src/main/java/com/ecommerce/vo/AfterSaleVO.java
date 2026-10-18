package com.ecommerce.vo;

import com.ecommerce.entity.AfterSale;
import lombok.Data;
import org.springframework.util.StringUtils;

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
    /** REFUND_ONLY 仅退款 / RETURN_REFUND 退货退款 */
    private String type;
    private String typeText;
    private String reason;
    private String description;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String statusText;
    private String auditRemark;
    private LocalDateTime auditedAt;
    private String returnShippingCompany;
    private String returnTrackingNo;
    private LocalDateTime returnShippedAt;
    private LocalDateTime returnReceivedAt;
    private LocalDateTime refundedAt;
    private LocalDateTime createdAt;

    public static AfterSaleVO from(AfterSale afterSale, String orderNo) {
        AfterSaleVO vo = new AfterSaleVO();
        vo.setId(afterSale.getId());
        vo.setAfterSaleNo(afterSale.getAfterSaleNo());
        vo.setOrderId(afterSale.getOrderId());
        vo.setOrderNo(orderNo);
        vo.setUserId(afterSale.getUserId());
        vo.setType(StringUtils.hasText(afterSale.getType()) ? afterSale.getType() : "REFUND_ONLY");
        vo.setTypeText(typeText(vo.getType()));
        vo.setReason(afterSale.getReason());
        vo.setDescription(afterSale.getDescription());
        vo.setAmount(afterSale.getAmount());
        vo.setCurrency(afterSale.getCurrency());
        vo.setStatus(afterSale.getStatus());
        vo.setStatusText(statusText(afterSale.getStatus()));
        vo.setAuditRemark(afterSale.getAuditRemark());
        vo.setAuditedAt(afterSale.getAuditedAt());
        vo.setReturnShippingCompany(afterSale.getReturnShippingCompany());
        vo.setReturnTrackingNo(afterSale.getReturnTrackingNo());
        vo.setReturnShippedAt(afterSale.getReturnShippedAt());
        vo.setReturnReceivedAt(afterSale.getReturnReceivedAt());
        vo.setRefundedAt(afterSale.getRefundedAt());
        vo.setCreatedAt(afterSale.getCreatedAt());
        return vo;
    }

    public static String typeText(String type) {
        return "RETURN_REFUND".equals(type) ? "退货退款" : "仅退款";
    }

    public static String statusText(String status) {
        if ("PENDING".equals(status)) {
            return "待审核";
        }
        if ("WAITING_RETURN".equals(status)) {
            return "待买家寄回";
        }
        if ("WAITING_RECEIPT".equals(status)) {
            return "待商家收货";
        }
        if ("APPROVED".equals(status)) {
            return "已通过";
        }
        if ("REJECTED".equals(status)) {
            return "已拒绝";
        }
        if ("REFUNDED".equals(status)) {
            return "已完成";
        }
        return "未知状态";
    }
}
