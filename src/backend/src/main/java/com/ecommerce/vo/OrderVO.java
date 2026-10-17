package com.ecommerce.vo;

import com.ecommerce.common.OrderStatus;
import com.ecommerce.entity.Order;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单列表项响应。
 */
@Data
public class OrderVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String orderNo;
    private Long userId;
    private String status;
    private String statusText;
    private String currency;
    private BigDecimal totalAmount;
    private BigDecimal freightAmount;
    private BigDecimal payAmount;
    /** 下单时的汇率快照（基础币种 CNY -> 订单币种） */
    private BigDecimal exchangeRate;
    /** 折算为基础币种 CNY 的金额，用于跨币种统计 */
    private BigDecimal baseAmount;
    private Integer itemCount;
    private String consignee;
    private String country;
    private LocalDateTime createdAt;

    public static OrderVO from(Order order) {
        OrderVO vo = new OrderVO();
        fill(vo, order);
        return vo;
    }

    protected static void fill(OrderVO vo, Order order) {
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setUserId(order.getUserId());
        vo.setStatus(order.getStatus());
        OrderStatus status = OrderStatus.of(order.getStatus());
        vo.setStatusText(status == null ? order.getStatus() : status.getText());
        vo.setCurrency(order.getCurrency());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setFreightAmount(order.getFreightAmount());
        vo.setPayAmount(order.getPayAmount());
        vo.setExchangeRate(order.getExchangeRate());
        vo.setBaseAmount(order.getBaseAmount());
        vo.setConsignee(order.getConsignee());
        vo.setCountry(order.getCountry());
        vo.setCreatedAt(order.getCreatedAt());
    }
}
