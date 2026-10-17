package com.ecommerce.vo;

import com.ecommerce.entity.Order;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单详情响应，含商品快照与收货地址快照。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderDetailVO extends OrderVO {

    private static final long serialVersionUID = 1L;

    private String provinceState;
    private String city;
    private String postalCode;
    private String addressLine;
    private String phone;
    private String shippingMethod;
    private String remark;
    private LocalDateTime paidAt;
    private LocalDateTime shippedAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;
    private List<OrderItemVO> items = new ArrayList<>();

    public static OrderDetailVO from(Order order) {
        OrderDetailVO vo = new OrderDetailVO();
        fill(vo, order);
        vo.setProvinceState(order.getProvinceState());
        vo.setCity(order.getCity());
        vo.setPostalCode(order.getPostalCode());
        vo.setAddressLine(order.getAddressLine());
        vo.setPhone(order.getPhone());
        vo.setShippingMethod(order.getShippingMethod());
        vo.setRemark(order.getRemark());
        vo.setPaidAt(order.getPaidAt());
        vo.setShippedAt(order.getShippedAt());
        vo.setCompletedAt(order.getCompletedAt());
        vo.setCancelledAt(order.getCancelledAt());
        return vo;
    }
}
