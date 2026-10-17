package com.ecommerce.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ecommerce.common.OrderStatus;
import com.ecommerce.entity.AfterSale;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderItem;
import com.ecommerce.entity.Product;
import com.ecommerce.mapper.AfterSaleMapper;
import com.ecommerce.mapper.OrderItemMapper;
import com.ecommerce.mapper.OrderMapper;
import com.ecommerce.mapper.ProductMapper;
import com.ecommerce.mapper.UserMapper;
import com.ecommerce.service.StatisticsService;
import com.ecommerce.vo.StatisticsVO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 经营统计实现。
 * <p>
 * 口径说明：
 * 1. 成交金额使用订单下单时折算的 base_amount（CNY），不同币种订单不会直接相加；
 * 2. 热门商品按订单明细累计销量排序取前 5，未完成订单也会计入销量（下单即占库存）。
 */
@Service
public class StatisticsServiceImpl implements StatisticsService {

    private static final int TOP_PRODUCT_LIMIT = 5;

    private final ProductMapper productMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final AfterSaleMapper afterSaleMapper;
    private final UserMapper userMapper;

    public StatisticsServiceImpl(ProductMapper productMapper,
                                 OrderMapper orderMapper,
                                 OrderItemMapper orderItemMapper,
                                 AfterSaleMapper afterSaleMapper,
                                 UserMapper userMapper) {
        this.productMapper = productMapper;
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.afterSaleMapper = afterSaleMapper;
        this.userMapper = userMapper;
    }

    @Override
    public StatisticsVO overview() {
        StatisticsVO vo = new StatisticsVO();
        vo.setProductTotal(productMapper.selectCount(null));
        vo.setProductOnShelf(productMapper.selectCount(
                new LambdaQueryWrapper<Product>().eq(Product::getStatus, 1)));
        vo.setOrderTotal(orderMapper.selectCount(null));
        vo.setUserTotal(userMapper.selectCount(null));

        Map<String, Long> statusCounts = new LinkedHashMap<>();
        for (OrderStatus status : OrderStatus.values()) {
            long count = orderMapper.selectCount(
                    new LambdaQueryWrapper<Order>().eq(Order::getStatus, status.name()));
            statusCounts.put(status.getText(), count);
        }
        vo.setOrderStatusCounts(statusCounts);

        List<Order> completedOrders = orderMapper.selectList(
                new LambdaQueryWrapper<Order>().eq(Order::getStatus, OrderStatus.COMPLETED.name()));
        vo.setCompletedOrderCount(completedOrders.size());
        vo.setCompletedAmount(completedOrders.stream()
                .map(Order::getBaseAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        vo.setAfterSaleTotal(afterSaleMapper.selectCount(null));
        vo.setAfterSalePending(afterSaleMapper.selectCount(
                new LambdaQueryWrapper<AfterSale>().eq(AfterSale::getStatus, "PENDING")));

        vo.setTopProducts(buildTopProducts());
        return vo;
    }

    private List<StatisticsVO.TopProductVO> buildTopProducts() {
        List<OrderItem> items = orderItemMapper.selectList(null);
        if (items.isEmpty()) {
            return new ArrayList<>();
        }
        Map<Long, Long> quantityMap = items.stream().collect(Collectors.groupingBy(
                OrderItem::getProductId, Collectors.summingLong(OrderItem::getQuantity)));

        List<Map.Entry<Long, Long>> top = quantityMap.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(TOP_PRODUCT_LIMIT)
                .collect(Collectors.toList());

        List<Long> productIds = top.stream().map(Map.Entry::getKey).collect(Collectors.toList());
        Map<Long, Product> productMap = productMapper.selectBatchIds(productIds).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

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
