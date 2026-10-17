package com.ecommerce.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.common.OrderStatus;
import com.ecommerce.common.PageResult;
import com.ecommerce.common.ResultCode;
import com.ecommerce.common.RoleCode;
import com.ecommerce.dto.PayDTO;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.Payment;
import com.ecommerce.exception.BusinessException;
import com.ecommerce.mapper.OrderMapper;
import com.ecommerce.mapper.PaymentMapper;
import com.ecommerce.security.LoginUserUtil;
import com.ecommerce.service.OperationLogService;
import com.ecommerce.service.PaymentService;
import com.ecommerce.vo.PaymentVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 模拟支付实现。
 * <p>
 * 关键规则：
 * 1. 只有待付款订单可以支付，且支付金额必须等于订单应付金额；
 * 2. 支付成功通过"订单 ID + 待付款状态"条件更新流转，重复支付第二次影响 0 行；
 * 3. 模拟失败只记录失败流水，订单状态保持不变。
 */
@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService {

    private static final int PAYMENT_SUCCESS = 1;
    private static final int PAYMENT_FAILED = 0;

    private final PaymentMapper paymentMapper;
    private final OrderMapper orderMapper;
    private final OperationLogService operationLogService;

    public PaymentServiceImpl(PaymentMapper paymentMapper,
                              OrderMapper orderMapper,
                              OperationLogService operationLogService) {
        this.paymentMapper = paymentMapper;
        this.orderMapper = orderMapper;
        this.operationLogService = operationLogService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentVO pay(PayDTO dto) {
        Long userId = LoginUserUtil.currentUserId();
        Order order = orderMapper.selectById(dto.getOrderId());
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "订单不存在");
        }
        if (!userId.equals(order.getUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权支付他人的订单");
        }
        if (!OrderStatus.PENDING_PAYMENT.name().equals(order.getStatus())) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "当前订单状态不允许支付");
        }
        if (order.getPayAmount().compareTo(dto.getAmount()) != 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "支付金额与订单应付金额不一致");
        }
        boolean alreadyPaid = paymentMapper.selectCount(new LambdaQueryWrapper<Payment>()
                .eq(Payment::getOrderId, order.getId())
                .eq(Payment::getStatus, PAYMENT_SUCCESS)) > 0;
        if (alreadyPaid) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "该订单已完成支付，请勿重复支付");
        }

        boolean simulateSuccess = dto.getSuccess() == null || dto.getSuccess();
        Payment payment = new Payment();
        payment.setPaymentNo(generatePaymentNo());
        payment.setOrderId(order.getId());
        payment.setUserId(userId);
        payment.setAmount(order.getPayAmount());
        payment.setCurrency(order.getCurrency());
        payment.setMethod("MOCK");
        payment.setTradeNo("MOCK" + System.currentTimeMillis());

        if (!simulateSuccess) {
            payment.setStatus(PAYMENT_FAILED);
            payment.setFailReason("模拟支付失败（演示分支）");
            paymentMapper.insert(payment);
            log.info("模拟支付失败：orderNo={}", order.getOrderNo());
            operationLogService.record("PAYMENT", "模拟支付失败", order.getOrderNo());
            return PaymentVO.from(payment, order.getOrderNo());
        }

        // 条件更新：并发或重复支付时只有第一次成功，订单状态与支付记录保持一致
        Order update = new Order();
        update.setStatus(OrderStatus.PENDING_SHIPMENT.name());
        update.setPaidAt(LocalDateTime.now());
        int affected = orderMapper.update(update, new LambdaUpdateWrapper<Order>()
                .eq(Order::getId, order.getId())
                .eq(Order::getStatus, OrderStatus.PENDING_PAYMENT.name()));
        if (affected == 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "订单状态已变化，请刷新后重试");
        }

        payment.setStatus(PAYMENT_SUCCESS);
        payment.setPaidAt(LocalDateTime.now());
        paymentMapper.insert(payment);
        log.info("模拟支付成功：orderNo={}, amount={}", order.getOrderNo(), order.getPayAmount());
        operationLogService.record("PAYMENT", "模拟支付成功",
                order.getOrderNo() + " | " + order.getPayAmount() + " " + order.getCurrency());
        return PaymentVO.from(payment, order.getOrderNo());
    }

    @Override
    public PaymentVO queryByOrder(Long orderId) {
        Order order = requireAccessibleOrder(orderId);
        List<Payment> payments = paymentMapper.selectList(new LambdaQueryWrapper<Payment>()
                .eq(Payment::getOrderId, order.getId())
                .orderByDesc(Payment::getCreatedAt)
                .last("LIMIT 1"));
        return payments.isEmpty() ? null : PaymentVO.from(payments.get(0), order.getOrderNo());
    }

    @Override
    public PageResult<PaymentVO> page(long pageNum, long pageSize, String keyword) {
        if (!StpUtil.hasRole(RoleCode.ADMIN)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "仅管理员可以查询支付记录");
        }
        LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Payment::getPaymentNo, keyword.trim());
        }
        wrapper.orderByDesc(Payment::getCreatedAt);
        Page<Payment> page = paymentMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);

        List<Long> orderIds = page.getRecords().stream().map(Payment::getOrderId).distinct().collect(Collectors.toList());
        Map<Long, Order> orderMap = orderIds.isEmpty()
                ? java.util.Collections.emptyMap()
                : orderMapper.selectBatchIds(orderIds).stream()
                .collect(Collectors.toMap(Order::getId, Function.identity()));

        return PageResult.of(page, payment -> {
            Order order = orderMap.get(payment.getOrderId());
            return PaymentVO.from(payment, order == null ? null : order.getOrderNo());
        });
    }

    private Order requireAccessibleOrder(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "订单不存在");
        }
        boolean manager = StpUtil.hasRole(RoleCode.ADMIN) || StpUtil.hasRole(RoleCode.STAFF);
        if (!manager && !LoginUserUtil.currentUserId().equals(order.getUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权查看他人订单的支付记录");
        }
        return order;
    }

    private String generatePaymentNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "PAY" + timestamp + String.format("%03d", ThreadLocalRandom.current().nextInt(1000));
    }
}
