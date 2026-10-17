package com.ecommerce.vo;

import com.ecommerce.entity.Shipment;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 物流信息响应。
 */
@Data
public class ShipmentVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long orderId;
    private String shippingCompany;
    private String trackingNo;
    private String shippingMethod;
    private String fromCountry;
    private String toCountry;
    private String status;
    private String statusText;
    private LocalDate estimatedDelivery;
    private LocalDateTime shippedAt;

    public static ShipmentVO from(Shipment shipment) {
        ShipmentVO vo = new ShipmentVO();
        vo.setId(shipment.getId());
        vo.setOrderId(shipment.getOrderId());
        vo.setShippingCompany(shipment.getShippingCompany());
        vo.setTrackingNo(shipment.getTrackingNo());
        vo.setShippingMethod(shipment.getShippingMethod());
        vo.setFromCountry(shipment.getFromCountry());
        vo.setToCountry(shipment.getToCountry());
        vo.setStatus(shipment.getStatus());
        vo.setStatusText(statusText(shipment.getStatus()));
        vo.setEstimatedDelivery(shipment.getEstimatedDelivery());
        vo.setShippedAt(shipment.getShippedAt());
        return vo;
    }

    public static String statusText(String status) {
        if ("PENDING".equals(status)) {
            return "待揽收";
        }
        if ("IN_TRANSIT".equals(status)) {
            return "运输中";
        }
        if ("DELIVERED".equals(status)) {
            return "已送达";
        }
        return "未知状态";
    }
}
