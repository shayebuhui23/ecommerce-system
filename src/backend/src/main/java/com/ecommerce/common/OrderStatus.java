package com.ecommerce.common;

import lombok.Getter;

/**
 * 订单状态与合法流转规则。
 * <p>
 * 状态流转集中在此处校验，Service 层不得直接修改 orders.status，
 * 避免通过任意改状态绕过业务校验。
 */
@Getter
public enum OrderStatus {

    PENDING_PAYMENT("待付款"),
    PENDING_SHIPMENT("待发货"),
    SHIPPED("已发货"),
    COMPLETED("已完成"),
    CANCELLED("已取消"),
    AFTER_SALES("售后处理中"),
    REFUNDED("已退款");

    private final String text;

    OrderStatus(String text) {
        this.text = text;
    }

    public static OrderStatus of(String code) {
        for (OrderStatus status : values()) {
            if (status.name().equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 判断当前状态是否可以流转到目标状态。
     */
    public boolean canTransitionTo(OrderStatus target) {
        if (target == null) {
            return false;
        }
        switch (this) {
            case PENDING_PAYMENT:
                // 待付款可支付，或直接取消
                return target == PENDING_SHIPMENT || target == CANCELLED;
            case PENDING_SHIPMENT:
                return target == SHIPPED || target == AFTER_SALES;
            case SHIPPED:
                return target == COMPLETED || target == AFTER_SALES;
            case AFTER_SALES:
                return target == REFUNDED || target == COMPLETED;
            default:
                return false;
        }
    }

    /**
     * 是否允许客户取消。
     */
    public boolean cancellable() {
        return this == PENDING_PAYMENT;
    }
}
