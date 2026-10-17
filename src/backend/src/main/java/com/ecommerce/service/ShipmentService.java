package com.ecommerce.service;

import com.ecommerce.dto.ShipDTO;
import com.ecommerce.vo.ShipmentVO;

/**
 * 发货与物流服务。
 */
public interface ShipmentService {

    /**
     * 商家发货：仅待发货订单可发货，物流公司与单号必填。
     */
    ShipmentVO ship(Long orderId, ShipDTO dto);

    /**
     * 查询订单物流信息（客户只能查自己的订单）。
     */
    ShipmentVO queryByOrder(Long orderId);

    /**
     * 客户确认收货：仅已发货订单可确认，确认后订单完成、物流标记为已送达。
     */
    void receive(Long orderId);
}
