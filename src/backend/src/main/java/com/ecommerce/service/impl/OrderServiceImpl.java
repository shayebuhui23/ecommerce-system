package com.ecommerce.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.common.OrderStatus;
import com.ecommerce.common.PageResult;
import com.ecommerce.common.ResultCode;
import com.ecommerce.common.RoleCode;
import com.ecommerce.dto.OrderCreateDTO;
import com.ecommerce.entity.Address;
import com.ecommerce.entity.Cart;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.InventoryLog;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderItem;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.Shipment;
import com.ecommerce.exception.BusinessException;
import com.ecommerce.mapper.AddressMapper;
import com.ecommerce.mapper.CartItemMapper;
import com.ecommerce.mapper.CartMapper;
import com.ecommerce.mapper.InventoryLogMapper;
import com.ecommerce.mapper.OrderItemMapper;
import com.ecommerce.mapper.OrderMapper;
import com.ecommerce.mapper.ProductMapper;
import com.ecommerce.mapper.ShipmentMapper;
import com.ecommerce.security.LoginUserUtil;
import com.ecommerce.service.ExchangeRateService;
import com.ecommerce.service.OperationLogService;
import com.ecommerce.service.OrderService;
import com.ecommerce.vo.OrderDetailVO;
import com.ecommerce.vo.OrderItemVO;
import com.ecommerce.vo.OrderVO;
import com.ecommerce.vo.ShipmentVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 订单服务实现。
 * <p>
 * 关键设计：
 * 1. 金额、单价、运费全部由后端计算，前端提交的金额一律不采信；
 * 2. 商品与地址在下单时固化为快照，后续改价或改地址不影响历史订单；
 * 3. 库存使用带条件的原子 UPDATE 扣减，库存不足直接抛异常回滚整个事务；
 * 4. 取消订单通过"状态 + 订单 ID"条件更新保证只生效一次，避免重复回库。
 */
@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    /** 满 500 免运费，否则固定 20（首版规则，跨币种运费在阶段五完善） */
    private static final BigDecimal FREE_FREIGHT_THRESHOLD = new BigDecimal("500.00");
    private static final BigDecimal DEFAULT_FREIGHT = new BigDecimal("20.00");
    private static final int STATUS_ON_SHELF = 1;

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final CartMapper cartMapper;
    private final CartItemMapper cartItemMapper;
    private final ProductMapper productMapper;
    private final AddressMapper addressMapper;
    private final InventoryLogMapper inventoryLogMapper;
    private final ShipmentMapper shipmentMapper;
    private final ExchangeRateService exchangeRateService;
    private final OperationLogService operationLogService;

    public OrderServiceImpl(OrderMapper orderMapper,
                            OrderItemMapper orderItemMapper,
                            CartMapper cartMapper,
                            CartItemMapper cartItemMapper,
                            ProductMapper productMapper,
                            AddressMapper addressMapper,
                            InventoryLogMapper inventoryLogMapper,
                            ShipmentMapper shipmentMapper,
                            ExchangeRateService exchangeRateService,
                            OperationLogService operationLogService) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.cartMapper = cartMapper;
        this.cartItemMapper = cartItemMapper;
        this.productMapper = productMapper;
        this.addressMapper = addressMapper;
        this.inventoryLogMapper = inventoryLogMapper;
        this.shipmentMapper = shipmentMapper;
        this.exchangeRateService = exchangeRateService;
        this.operationLogService = operationLogService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderDetailVO create(OrderCreateDTO dto) {
        Long userId = LoginUserUtil.currentUserId();
        Address address = requireOwnAddress(dto.getAddressId(), userId);
        List<CartItem> cartItems = loadCheckoutItems(userId, dto.getCartItemIds());

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            Product product = productMapper.selectById(cartItem.getProductId());
            if (product == null) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "商品不存在，请刷新购物车");
            }
            if (product.getStatus() == null || product.getStatus() != STATUS_ON_SHELF) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "商品「" + product.getName() + "」已下架");
            }
            if (product.getStock() == null || product.getStock() < cartItem.getQuantity()) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR,
                        "商品「" + product.getName() + "」库存不足，请调整数量");
            }
            BigDecimal subtotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity()))
                    .setScale(2, RoundingMode.HALF_UP);
            totalAmount = totalAmount.add(subtotal);
            orderItems.add(buildOrderItem(product, cartItem.getQuantity(), subtotal));
        }

        // 运费与免邮门槛按基础币种 CNY 判断
        BigDecimal baseFreight = totalAmount.compareTo(FREE_FREIGHT_THRESHOLD) >= 0
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : DEFAULT_FREIGHT;
        String currency = StringUtils.hasText(dto.getCurrency()) ? dto.getCurrency() : "CNY";
        BigDecimal exchangeRate = exchangeRateService.requireRate("CNY", currency);

        BigDecimal orderTotal = totalAmount.multiply(exchangeRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal orderFreight = baseFreight.multiply(exchangeRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal payAmount = orderTotal.add(orderFreight);
        BigDecimal baseAmount = totalAmount.add(baseFreight);

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setCurrency(currency);
        // 保存下单时汇率快照，后续调整汇率不影响本订单
        order.setExchangeRate(exchangeRate);
        order.setTotalAmount(orderTotal);
        order.setFreightAmount(orderFreight);
        order.setPayAmount(payAmount);
        order.setBaseAmount(baseAmount);
        order.setConsignee(address.getConsignee());
        order.setCountry(address.getCountry());
        order.setProvinceState(address.getProvinceState());
        order.setCity(address.getCity());
        order.setPostalCode(address.getPostalCode());
        order.setAddressLine(address.getAddressLine());
        order.setPhone(address.getPhone());
        order.setShippingMethod(dto.getShippingMethod());
        order.setStatus(OrderStatus.PENDING_PAYMENT.name());
        order.setRemark(dto.getRemark());
        orderMapper.insert(order);

        for (OrderItem item : orderItems) {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
            // 条件更新：数量在 SQL 中参与判断，库存不足时影响行数为 0
            int affected = productMapper.deductStock(item.getProductId(), item.getQuantity());
            if (affected == 0) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR,
                        "商品「" + item.getProductName() + "」库存不足，请调整后重试");
            }
            recordInventoryLog(item.getProductId(), order.getId(), "ORDER_DEDUCT",
                    item.getQuantity(), userId, "创建订单扣减库存");
        }

        // 下单成功后移除已结算的购物车项，重复提交会因购物车为空而失败
        cartItemMapper.deleteBatchIds(cartItems.stream().map(CartItem::getId).collect(Collectors.toList()));
        log.info("订单创建成功：orderNo={}, userId={}, payAmount={} {}", order.getOrderNo(), userId, payAmount, currency);
        operationLogService.record("ORDER", "创建订单",
                order.getOrderNo() + " | " + payAmount + " " + currency);
        return detail(order.getId());
    }

    @Override
    public PageResult<OrderVO> page(long pageNum, long pageSize, String status, String keyword) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if (!isManager()) {
            wrapper.eq(Order::getUserId, LoginUserUtil.currentUserId());
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(Order::getStatus, status.trim());
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Order::getOrderNo, keyword.trim());
        }
        wrapper.orderByDesc(Order::getCreatedAt);

        Page<Order> page = orderMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        Map<Long, Integer> itemCountMap = countItems(page.getRecords());
        return PageResult.of(page, order -> {
            OrderVO vo = OrderVO.from(order);
            vo.setItemCount(itemCountMap.getOrDefault(order.getId(), 0));
            return vo;
        });
    }

    @Override
    public OrderDetailVO detail(Long id) {
        Order order = requireAccessibleOrder(id);
        OrderDetailVO vo = OrderDetailVO.from(order);
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId()));
        vo.setItems(items.stream().map(OrderItemVO::from).collect(Collectors.toList()));
        vo.setItemCount(items.stream().mapToInt(OrderItem::getQuantity).sum());

        List<Shipment> shipments = shipmentMapper.selectList(
                new LambdaQueryWrapper<Shipment>().eq(Shipment::getOrderId, order.getId()));
        if (!shipments.isEmpty()) {
            vo.setShipment(ShipmentVO.from(shipments.get(0)));
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long id) {
        Order order = requireAccessibleOrder(id);
        OrderStatus current = OrderStatus.of(order.getStatus());
        if (current == null || !current.cancellable()) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "当前订单状态不允许取消");
        }

        // 带状态条件更新：并发或重复取消时只有第一次能成功，避免重复恢复库存
        Order update = new Order();
        update.setStatus(OrderStatus.CANCELLED.name());
        update.setCancelledAt(LocalDateTime.now());
        int affected = orderMapper.update(update, new LambdaUpdateWrapper<Order>()
                .eq(Order::getId, id)
                .eq(Order::getStatus, OrderStatus.PENDING_PAYMENT.name()));
        if (affected == 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "订单状态已变化，请刷新后重试");
        }

        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, id));
        for (OrderItem item : items) {
            productMapper.restoreStock(item.getProductId(), item.getQuantity());
            recordInventoryLog(item.getProductId(), id, "ORDER_CANCEL",
                    item.getQuantity(), LoginUserUtil.currentUserId(), "取消订单恢复库存");
        }
        log.info("订单已取消并恢复库存：orderNo={}", order.getOrderNo());
    }

    private OrderItem buildOrderItem(Product product, Integer quantity, BigDecimal subtotal) {
        OrderItem item = new OrderItem();
        item.setProductId(product.getId());
        item.setProductName(product.getName());
        item.setProductNameEn(product.getNameEn());
        item.setSkuCode(product.getSkuCode());
        item.setImageUrl(product.getImageUrl());
        item.setUnitPrice(product.getPrice());
        item.setQuantity(quantity);
        item.setSubtotal(subtotal);
        item.setCurrency(product.getCurrency());
        return item;
    }

    private void recordInventoryLog(Long productId, Long orderId, String changeType,
                                    Integer quantity, Long operatorId, String remark) {
        Product product = productMapper.selectById(productId);
        int afterStock = product == null || product.getStock() == null ? 0 : product.getStock();
        int delta = "ORDER_DEDUCT".equals(changeType) ? -quantity : quantity;

        InventoryLog inventoryLog = new InventoryLog();
        inventoryLog.setProductId(productId);
        inventoryLog.setOrderId(orderId);
        inventoryLog.setChangeType(changeType);
        inventoryLog.setChangeQuantity(delta);
        inventoryLog.setAfterStock(afterStock);
        inventoryLog.setBeforeStock(afterStock - delta);
        inventoryLog.setRemark(remark);
        inventoryLog.setOperatorId(operatorId);
        inventoryLogMapper.insert(inventoryLog);
    }

    private List<CartItem> loadCheckoutItems(Long userId, List<Long> itemIds) {
        Cart cart = cartMapper.selectOne(new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, userId));
        if (cart == null) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "购物车为空，无法创建订单");
        }
        LambdaQueryWrapper<CartItem> wrapper = new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getCartId, cart.getId());
        if (itemIds != null && !itemIds.isEmpty()) {
            wrapper.in(CartItem::getId, itemIds);
        }
        List<CartItem> items = cartItemMapper.selectList(wrapper);
        if (items.isEmpty()) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "没有可结算的商品");
        }
        return items;
    }

    private Address requireOwnAddress(Long addressId, Long userId) {
        Address address = addressMapper.selectById(addressId);
        if (address == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "收货地址不存在");
        }
        if (!userId.equals(address.getUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权使用他人的收货地址");
        }
        return address;
    }

    private Order requireAccessibleOrder(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "订单不存在");
        }
        if (!isManager() && !LoginUserUtil.currentUserId().equals(order.getUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权访问他人订单");
        }
        return order;
    }

    private Map<Long, Integer> countItems(List<Order> orders) {
        if (orders.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> orderIds = orders.stream().map(Order::getId).collect(Collectors.toList());
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().in(OrderItem::getOrderId, orderIds));
        return items.stream().collect(Collectors.groupingBy(OrderItem::getOrderId,
                Collectors.summingInt(OrderItem::getQuantity)));
    }

    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "ORD" + timestamp + String.format("%03d", ThreadLocalRandom.current().nextInt(1000));
    }

    private boolean isManager() {
        return StpUtil.hasRole(RoleCode.ADMIN) || StpUtil.hasRole(RoleCode.STAFF);
    }
}
