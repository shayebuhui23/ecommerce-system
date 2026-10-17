package com.ecommerce.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.common.OrderStatus;
import com.ecommerce.common.PageResult;
import com.ecommerce.common.ResultCode;
import com.ecommerce.common.RoleCode;
import com.ecommerce.dto.AfterSaleApplyDTO;
import com.ecommerce.dto.AfterSaleAuditDTO;
import com.ecommerce.entity.AfterSale;
import com.ecommerce.entity.InventoryLog;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderItem;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.BusinessException;
import com.ecommerce.mapper.AfterSaleMapper;
import com.ecommerce.mapper.InventoryLogMapper;
import com.ecommerce.mapper.OrderItemMapper;
import com.ecommerce.mapper.OrderMapper;
import com.ecommerce.mapper.ProductMapper;
import com.ecommerce.security.LoginUserUtil;
import com.ecommerce.service.AfterSaleService;
import com.ecommerce.service.OperationLogService;
import com.ecommerce.vo.AfterSaleVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 售后与退款实现。
 * <p>
 * 退款规则（首版整单退款）：
 * 1. 仅待发货、已发货、已完成订单可申请，待付款请直接取消订单；
 * 2. 同一订单同时只能有一条进行中的售后单；
 * 3. 审核通过使用"订单 ID + 可退状态"条件更新，重复审核影响 0 行；
 * 4. 未发货订单退款恢复库存，已发货订单不恢复（商品已在客户手中）。
 */
@Slf4j
@Service
public class AfterSaleServiceImpl implements AfterSaleService {

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_REJECTED = "REJECTED";
    private static final String STATUS_REFUNDED = "REFUNDED";

    private static final List<String> REFUNDABLE_STATUS = Arrays.asList(
            OrderStatus.PENDING_SHIPMENT.name(),
            OrderStatus.SHIPPED.name(),
            OrderStatus.COMPLETED.name());

    private final AfterSaleMapper afterSaleMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;
    private final InventoryLogMapper inventoryLogMapper;
    private final OperationLogService operationLogService;

    public AfterSaleServiceImpl(AfterSaleMapper afterSaleMapper,
                                OrderMapper orderMapper,
                                OrderItemMapper orderItemMapper,
                                ProductMapper productMapper,
                                InventoryLogMapper inventoryLogMapper,
                                OperationLogService operationLogService) {
        this.afterSaleMapper = afterSaleMapper;
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.productMapper = productMapper;
        this.inventoryLogMapper = inventoryLogMapper;
        this.operationLogService = operationLogService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AfterSaleVO apply(AfterSaleApplyDTO dto) {
        Long userId = LoginUserUtil.currentUserId();
        Order order = orderMapper.selectById(dto.getOrderId());
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "订单不存在");
        }
        if (!userId.equals(order.getUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权为他人的订单申请退款");
        }
        if (!REFUNDABLE_STATUS.contains(order.getStatus())) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "当前订单状态不支持退款申请");
        }
        long activeCount = afterSaleMapper.selectCount(new LambdaQueryWrapper<AfterSale>()
                .eq(AfterSale::getOrderId, order.getId())
                .in(AfterSale::getStatus, STATUS_PENDING, "APPROVED"));
        if (activeCount > 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "该订单已有进行中的售后申请");
        }

        AfterSale afterSale = new AfterSale();
        afterSale.setAfterSaleNo(generateAfterSaleNo());
        afterSale.setOrderId(order.getId());
        afterSale.setUserId(userId);
        afterSale.setType("REFUND");
        afterSale.setReason(dto.getReason().trim());
        afterSale.setDescription(dto.getDescription());
        // 整单退款：金额取订单应付金额，不允许前端指定
        afterSale.setAmount(order.getPayAmount());
        afterSale.setCurrency(order.getCurrency());
        afterSale.setStatus(STATUS_PENDING);
        afterSaleMapper.insert(afterSale);
        log.info("售后申请提交：orderNo={}, amount={}", order.getOrderNo(), afterSale.getAmount());
        operationLogService.record("AFTER_SALE", "提交退款申请",
                afterSale.getAfterSaleNo() + " | " + afterSale.getAmount() + " " + afterSale.getCurrency());
        return AfterSaleVO.from(afterSale, order.getOrderNo());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AfterSaleVO audit(Long id, AfterSaleAuditDTO dto) {
        AfterSale afterSale = afterSaleMapper.selectById(id);
        if (afterSale == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "售后单不存在");
        }
        if (!STATUS_PENDING.equals(afterSale.getStatus())) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "该售后单已处理，请勿重复操作");
        }
        Order order = orderMapper.selectById(afterSale.getOrderId());
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "关联订单不存在");
        }

        if (!Boolean.TRUE.equals(dto.getApproved())) {
            AfterSale reject = new AfterSale();
            reject.setStatus(STATUS_REJECTED);
            reject.setAuditRemark(dto.getAuditRemark());
            reject.setAuditedBy(LoginUserUtil.currentUserId());
            reject.setAuditedAt(LocalDateTime.now());
            int affected = afterSaleMapper.update(reject, new LambdaUpdateWrapper<AfterSale>()
                    .eq(AfterSale::getId, id)
                    .eq(AfterSale::getStatus, STATUS_PENDING));
            if (affected == 0) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "该售后单已处理，请勿重复操作");
            }
            log.info("售后申请驳回：afterSaleNo={}", afterSale.getAfterSaleNo());
            operationLogService.record("AFTER_SALE", "驳回退款申请", afterSale.getAfterSaleNo());
            return AfterSaleVO.from(afterSaleMapper.selectById(id), order.getOrderNo());
        }

        // 订单状态条件更新为已退款，防止并发重复退款
        Order update = new Order();
        update.setStatus(OrderStatus.REFUNDED.name());
        int affectedOrder = orderMapper.update(update, new LambdaUpdateWrapper<Order>()
                .eq(Order::getId, order.getId())
                .in(Order::getStatus, REFUNDABLE_STATUS));
        if (affectedOrder == 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "订单状态已变化，无法退款");
        }

        AfterSale refund = new AfterSale();
        refund.setStatus(STATUS_REFUNDED);
        refund.setAuditRemark(dto.getAuditRemark());
        refund.setAuditedBy(LoginUserUtil.currentUserId());
        refund.setAuditedAt(LocalDateTime.now());
        refund.setRefundedAt(LocalDateTime.now());
        afterSaleMapper.update(refund, new LambdaUpdateWrapper<AfterSale>()
                .eq(AfterSale::getId, id)
                .eq(AfterSale::getStatus, STATUS_PENDING));

        // 未发货订单退款需要把库存还回去；已发货订单商品在客户手中，不回库
        boolean notShipped = order.getShippedAt() == null;
        if (notShipped) {
            List<OrderItem> items = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId()));
            for (OrderItem item : items) {
                productMapper.restoreStock(item.getProductId(), item.getQuantity());
                recordInventoryLog(item, order.getId());
            }
        }
        log.info("售后退款完成：afterSaleNo={}, restoreStock={}", afterSale.getAfterSaleNo(), notShipped);
        operationLogService.record("AFTER_SALE", "退款完成",
                afterSale.getAfterSaleNo() + " | " + afterSale.getAmount() + " " + afterSale.getCurrency());
        return AfterSaleVO.from(afterSaleMapper.selectById(id), order.getOrderNo());
    }

    @Override
    public AfterSaleVO queryByOrder(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "订单不存在");
        }
        boolean manager = StpUtil.hasRole(RoleCode.ADMIN) || StpUtil.hasRole(RoleCode.STAFF);
        if (!manager && !LoginUserUtil.currentUserId().equals(order.getUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权查看他人订单的售后信息");
        }
        List<AfterSale> list = afterSaleMapper.selectList(new LambdaQueryWrapper<AfterSale>()
                .eq(AfterSale::getOrderId, orderId)
                .orderByDesc(AfterSale::getCreatedAt)
                .last("LIMIT 1"));
        return list.isEmpty() ? null : AfterSaleVO.from(list.get(0), order.getOrderNo());
    }

    @Override
    public PageResult<AfterSaleVO> page(long pageNum, long pageSize, String status) {
        LambdaQueryWrapper<AfterSale> wrapper = new LambdaQueryWrapper<>();
        if (!isManager()) {
            wrapper.eq(AfterSale::getUserId, LoginUserUtil.currentUserId());
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(AfterSale::getStatus, status.trim());
        }
        wrapper.orderByDesc(AfterSale::getCreatedAt);

        Page<AfterSale> page = afterSaleMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<Long> orderIds = page.getRecords().stream().map(AfterSale::getOrderId).distinct().collect(Collectors.toList());
        Map<Long, Order> orderMap = orderIds.isEmpty()
                ? Collections.emptyMap()
                : orderMapper.selectBatchIds(orderIds).stream()
                .collect(Collectors.toMap(Order::getId, Function.identity()));
        return PageResult.of(page, afterSale -> {
            Order order = orderMap.get(afterSale.getOrderId());
            return AfterSaleVO.from(afterSale, order == null ? null : order.getOrderNo());
        });
    }

    private void recordInventoryLog(OrderItem item, Long orderId) {
        Product product = productMapper.selectById(item.getProductId());
        int afterStock = product == null || product.getStock() == null ? 0 : product.getStock();

        InventoryLog logEntity = new InventoryLog();
        logEntity.setProductId(item.getProductId());
        logEntity.setOrderId(orderId);
        logEntity.setChangeType("REFUND_RESTORE");
        logEntity.setChangeQuantity(item.getQuantity());
        logEntity.setAfterStock(afterStock);
        logEntity.setBeforeStock(afterStock - item.getQuantity());
        logEntity.setRemark("退款恢复库存");
        logEntity.setOperatorId(LoginUserUtil.currentUserId());
        inventoryLogMapper.insert(logEntity);
    }

    private String generateAfterSaleNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "AS" + timestamp + String.format("%03d", ThreadLocalRandom.current().nextInt(1000));
    }

    private boolean isManager() {
        return StpUtil.hasRole(RoleCode.ADMIN) || StpUtil.hasRole(RoleCode.STAFF);
    }
}
