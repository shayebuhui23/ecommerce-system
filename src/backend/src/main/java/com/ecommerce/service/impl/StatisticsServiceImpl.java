package com.ecommerce.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ecommerce.common.OrderStatus;
import com.ecommerce.common.RoleCode;
import com.ecommerce.entity.AfterSale;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderItem;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.Role;
import com.ecommerce.entity.User;
import com.ecommerce.mapper.AfterSaleMapper;
import com.ecommerce.mapper.OrderItemMapper;
import com.ecommerce.mapper.OrderMapper;
import com.ecommerce.mapper.ProductMapper;
import com.ecommerce.mapper.RoleMapper;
import com.ecommerce.mapper.UserMapper;
import com.ecommerce.security.LoginUserUtil;
import com.ecommerce.service.StatisticsService;
import com.ecommerce.vo.StatisticsVO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 经营统计实现。
 * <p>
 * 口径说明：
 * 1. 成交金额使用订单下单时折算的基础币种（CNY），不同币种订单不会直接相加；
 * 2. 热门商品按订单明细累计销量排序取前 5，未完成订单也会计入销量（下单即占库存）；
 * 3. 系统管理员统计全平台数据，商家运营仅统计本店商品相关的订单、金额与售后。
 */
@Service
public class StatisticsServiceImpl implements StatisticsService {

    private static final int TOP_PRODUCT_LIMIT = 5;

    private final ProductMapper productMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final AfterSaleMapper afterSaleMapper;
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;

    public StatisticsServiceImpl(ProductMapper productMapper,
                                 OrderMapper orderMapper,
                                 OrderItemMapper orderItemMapper,
                                 AfterSaleMapper afterSaleMapper,
                                 UserMapper userMapper,
                                 RoleMapper roleMapper) {
        this.productMapper = productMapper;
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.afterSaleMapper = afterSaleMapper;
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
    }

    @Override
    public StatisticsVO overview() {
        StatisticsVO vo = new StatisticsVO();

        // 商家运营只看本店数据，管理员看全平台
        Long sellerId = StpUtil.hasRole(RoleCode.STAFF) && !StpUtil.hasRole(RoleCode.ADMIN)
                ? LoginUserUtil.currentUserId()
                : null;

        List<Product> products = productMapper.selectList(sellerId == null
                ? null
                : new LambdaQueryWrapper<Product>().eq(Product::getSellerId, sellerId));
        vo.setProductTotal(products.size());
        vo.setProductOnShelf(products.stream().filter(p -> p.getStatus() != null && p.getStatus() == 1).count());

        // 工作台「用户总数」只统计已注册的普通客户
        Role customerRole = roleMapper.selectByCode(RoleCode.CUSTOMER);
        vo.setUserTotal(customerRole == null ? 0 : userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getRoleId, customerRole.getId())));
        Role staffRole = roleMapper.selectByCode(RoleCode.STAFF);
        vo.setMerchantTotal(staffRole == null ? 0 : userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getRoleId, staffRole.getId())));

        Set<Long> scopedOrderIds = sellerId == null ? null : orderIdsOfSeller(sellerId);
        List<Order> orders = loadOrders(scopedOrderIds);
        vo.setOrderTotal(orders.size());

        Map<String, Long> statusCounts = new LinkedHashMap<>();
        for (OrderStatus status : OrderStatus.values()) {
            statusCounts.put(status.getText(), 0L);
        }
        for (Order order : orders) {
            OrderStatus status = OrderStatus.of(order.getStatus());
            if (status != null) {
                statusCounts.merge(status.getText(), 1L, Long::sum);
            }
        }
        vo.setOrderStatusCounts(statusCounts);

        List<Order> completedOrders = orders.stream()
                .filter(order -> OrderStatus.COMPLETED.name().equals(order.getStatus()))
                .collect(Collectors.toList());
        vo.setCompletedOrderCount(completedOrders.size());
        BigDecimal revenue = completedAmount(sellerId, completedOrders);
        BigDecimal cost = completedCost(sellerId, completedOrders);
        BigDecimal profit = revenue.subtract(cost);
        vo.setCompletedAmount(revenue);
        vo.setCostAmount(cost);
        vo.setGrossProfit(profit);
        vo.setGrossMargin(revenue.compareTo(BigDecimal.ZERO) > 0
                ? profit.multiply(new BigDecimal("100")).divide(revenue, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);

        if (sellerId == null) {
            vo.setAfterSaleTotal(afterSaleMapper.selectCount(null));
            vo.setAfterSalePending(afterSaleMapper.selectCount(
                    new LambdaQueryWrapper<AfterSale>().eq(AfterSale::getStatus, "PENDING")));
        } else if (scopedOrderIds.isEmpty()) {
            vo.setAfterSaleTotal(0);
            vo.setAfterSalePending(0);
        } else {
            vo.setAfterSaleTotal(afterSaleMapper.selectCount(new LambdaQueryWrapper<AfterSale>()
                    .in(AfterSale::getOrderId, scopedOrderIds)));
            vo.setAfterSalePending(afterSaleMapper.selectCount(new LambdaQueryWrapper<AfterSale>()
                    .eq(AfterSale::getStatus, "PENDING")
                    .in(AfterSale::getOrderId, scopedOrderIds)));
        }

        vo.setTopProducts(buildTopProducts(sellerId, products));
        return vo;
    }

    /**
     * 商家相关订单：订单明细中包含本店商品的订单。
     */
    private Set<Long> orderIdsOfSeller(Long sellerId) {
        List<Long> productIds = productMapper.selectList(
                        new LambdaQueryWrapper<Product>().eq(Product::getSellerId, sellerId))
                .stream()
                .map(Product::getId)
                .collect(Collectors.toList());
        if (productIds.isEmpty()) {
            return Collections.emptySet();
        }
        return orderItemMapper.selectList(
                        new LambdaQueryWrapper<OrderItem>().in(OrderItem::getProductId, productIds))
                .stream()
                .map(OrderItem::getOrderId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private List<Order> loadOrders(Set<Long> orderIds) {
        if (orderIds == null) {
            return orderMapper.selectList(null);
        }
        if (orderIds.isEmpty()) {
            return Collections.emptyList();
        }
        return orderMapper.selectList(new LambdaQueryWrapper<Order>().in(Order::getId, orderIds));
    }

    /**
     * 管理员按整单金额统计；商家按本店商品明细小计统计，避免把其他商家的销售额算进来。
     */
    private BigDecimal completedAmount(Long sellerId, List<Order> completedOrders) {
        if (completedOrders.isEmpty()) {
            return BigDecimal.ZERO;
        }
        if (sellerId == null) {
            return completedOrders.stream()
                    .map(Order::getBaseAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        List<Long> orderIds = completedOrders.stream().map(Order::getId).collect(Collectors.toList());
        List<Long> ownProductIds = productMapper.selectList(
                        new LambdaQueryWrapper<Product>().eq(Product::getSellerId, sellerId))
                .stream()
                .map(Product::getId)
                .collect(Collectors.toList());
        if (ownProductIds.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                        .in(OrderItem::getOrderId, orderIds)
                        .in(OrderItem::getProductId, ownProductIds))
                .stream()
                .map(OrderItem::getSubtotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 已完成订单的商品成本：数量 × 商品当前成本价，商家只统计本店商品。
     */
    private BigDecimal completedCost(Long sellerId, List<Order> completedOrders) {
        if (completedOrders.isEmpty()) {
            return BigDecimal.ZERO;
        }
        List<Long> orderIds = completedOrders.stream().map(Order::getId).collect(Collectors.toList());
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().in(OrderItem::getOrderId, orderIds));
        if (items.isEmpty()) {
            return BigDecimal.ZERO;
        }
        List<Long> productIds = items.stream().map(OrderItem::getProductId).distinct().collect(Collectors.toList());
        Map<Long, Product> productMap = productMapper.selectBatchIds(productIds).stream()
                .collect(Collectors.toMap(Product::getId, product -> product));
        BigDecimal cost = BigDecimal.ZERO;
        for (OrderItem item : items) {
            Product product = productMap.get(item.getProductId());
            if (product == null || product.getCostPrice() == null) {
                continue;
            }
            if (sellerId != null && !sellerId.equals(product.getSellerId())) {
                continue;
            }
            cost = cost.add(product.getCostPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        return cost;
    }

    private List<StatisticsVO.TopProductVO> buildTopProducts(Long sellerId, List<Product> scopedProducts) {
        List<OrderItem> items;
        Map<Long, Product> productMap;
        if (sellerId == null) {
            items = orderItemMapper.selectList(null);
            productMap = new LinkedHashMap<>();
        } else {
            List<Long> ownProductIds = scopedProducts.stream().map(Product::getId).collect(Collectors.toList());
            if (ownProductIds.isEmpty()) {
                return new ArrayList<>();
            }
            items = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OrderItem>().in(OrderItem::getProductId, ownProductIds));
            productMap = scopedProducts.stream().collect(Collectors.toMap(Product::getId, p -> p));
        }
        if (items.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Long, Long> quantityMap = items.stream().collect(Collectors.groupingBy(
                OrderItem::getProductId, Collectors.summingLong(OrderItem::getQuantity)));

        List<Map.Entry<Long, Long>> top = quantityMap.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(TOP_PRODUCT_LIMIT)
                .collect(Collectors.toList());

        if (sellerId == null) {
            List<Long> productIds = top.stream().map(Map.Entry::getKey).collect(Collectors.toList());
            productMap = productMapper.selectBatchIds(productIds).stream()
                    .collect(Collectors.toMap(Product::getId, p -> p));
        }

        List<StatisticsVO.TopProductVO> result = new ArrayList<>();
        for (Map.Entry<Long, Long> entry : top) {
            Product product = productMap.get(entry.getKey());
            StatisticsVO.TopProductVO item = new StatisticsVO.TopProductVO();
            item.setProductId(entry.getKey());
            item.setProductName(product == null ? "已删除商品" : product.getName());
            item.setQuantity(entry.getValue());
            item.setSalesCount(product == null || product.getSalesCount() == null ? 0 : product.getSalesCount());
            result.add(item);
        }
        return result;
    }
}
