package com.ecommerce.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ecommerce.common.OrderStatus;
import com.ecommerce.common.ResultCode;
import com.ecommerce.common.RoleCode;
import com.ecommerce.dto.ShipDTO;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.Shipment;
import com.ecommerce.exception.BusinessException;
import com.ecommerce.mapper.OrderMapper;
import com.ecommerce.mapper.ShipmentMapper;
import com.ecommerce.security.LoginUserUtil;
import com.ecommerce.service.ShipmentService;
import com.ecommerce.vo.ShipmentVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 发货与物流实现。
 * <p>
 * 状态校验全部基于"订单 ID + 当前状态"的条件更新，避免并发下重复发货或重复确认收货。
 */
@Slf4j
@Service
public class ShipmentServiceImpl implements ShipmentService {

    private static final String SHIPMENT_IN_TRANSIT = "IN_TRANSIT";
    private static final String SHIPMENT_DELIVERED = "DELIVERED";

    private final OrderMapper orderMapper;
    private final ShipmentMapper shipmentMapper;

    public ShipmentServiceImpl(OrderMapper orderMapper, ShipmentMapper shipmentMapper) {
        this.orderMapper = orderMapper;
        this.shipmentMapper = shipmentMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShipmentVO ship(Long orderId, ShipDTO dto) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "订单不存在");
        }
        if (!OrderStatus.PENDING_SHIPMENT.name().equals(order.getStatus())) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "只有待发货订单可以发货");
        }
        boolean shipped = shipmentMapper.selectCount(
                new LambdaQueryWrapper<Shipment>().eq(Shipment::getOrderId, orderId)) > 0;
        if (shipped) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "该订单已存在物流信息，请勿重复发货");
        }

        Order update = new Order();
        update.setStatus(OrderStatus.SHIPPED.name());
        update.setShippedAt(LocalDateTime.now());
        int affected = orderMapper.update(update, new LambdaUpdateWrapper<Order>()
                .eq(Order::getId, orderId)
                .eq(Order::getStatus, OrderStatus.PENDING_SHIPMENT.name()));
        if (affected == 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "订单状态已变化，请刷新后重试");
        }

        Shipment shipment = new Shipment();
        shipment.setOrderId(orderId);
        shipment.setShippingCompany(dto.getShippingCompany().trim());
        shipment.setTrackingNo(dto.getTrackingNo().trim());
        shipment.setShippingMethod(StringUtils.hasText(dto.getShippingMethod()) ? dto.getShippingMethod() : "EXPRESS");
        shipment.setFromCountry(StringUtils.hasText(dto.getFromCountry()) ? dto.getFromCountry() : "China");
        shipment.setToCountry(StringUtils.hasText(dto.getToCountry()) ? dto.getToCountry() : order.getCountry());
        shipment.setStatus(SHIPMENT_IN_TRANSIT);
        shipment.setShippedAt(LocalDateTime.now());
        shipment.setEstimatedDelivery(dto.getEstimatedDelivery() != null
                ? dto.getEstimatedDelivery()
                : LocalDate.now().plusDays(7));
        shipmentMapper.insert(shipment);

        log.info("订单发货：orderNo={}, company={}, trackingNo={}",
                order.getOrderNo(), shipment.getShippingCompany(), shipment.getTrackingNo());
        return ShipmentVO.from(shipment);
    }

    @Override
    public ShipmentVO queryByOrder(Long orderId) {
        requireAccessibleOrder(orderId);
        List<Shipment> shipments = shipmentMapper.selectList(new LambdaQueryWrapper<Shipment>()
                .eq(Shipment::getOrderId, orderId)
                .orderByDesc(Shipment::getCreatedAt)
                .last("LIMIT 1"));
        return shipments.isEmpty() ? null : ShipmentVO.from(shipments.get(0));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void receive(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "订单不存在");
        }
        if (!LoginUserUtil.currentUserId().equals(order.getUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "只能确认自己的订单");
        }
        if (!OrderStatus.SHIPPED.name().equals(order.getStatus())) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "只有已发货订单可以确认收货");
        }

        Order update = new Order();
        update.setStatus(OrderStatus.COMPLETED.name());
        update.setCompletedAt(LocalDateTime.now());
        int affected = orderMapper.update(update, new LambdaUpdateWrapper<Order>()
                .eq(Order::getId, orderId)
                .eq(Order::getStatus, OrderStatus.SHIPPED.name()));
        if (affected == 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "订单状态已变化，请刷新后重试");
        }

        List<Shipment> shipments = shipmentMapper.selectList(
                new LambdaQueryWrapper<Shipment>().eq(Shipment::getOrderId, orderId));
        for (Shipment shipment : shipments) {
            Shipment shipmentUpdate = new Shipment();
            shipmentUpdate.setId(shipment.getId());
            shipmentUpdate.setStatus(SHIPMENT_DELIVERED);
            shipmentMapper.updateById(shipmentUpdate);
        }
        log.info("订单已确认收货：orderNo={}", order.getOrderNo());
    }

    private Order requireAccessibleOrder(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "订单不存在");
        }
        boolean manager = StpUtil.hasRole(RoleCode.ADMIN) || StpUtil.hasRole(RoleCode.STAFF);
        if (!manager && !LoginUserUtil.currentUserId().equals(order.getUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权查看他人订单的物流信息");
        }
        return order;
    }
}
