package com.ecommerce.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.common.OrderStatus;
import com.ecommerce.common.PageResult;
import com.ecommerce.common.ResultCode;
import com.ecommerce.dto.ReviewDTO;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderItem;
import com.ecommerce.entity.Review;
import com.ecommerce.entity.User;
import com.ecommerce.exception.BusinessException;
import com.ecommerce.mapper.OrderItemMapper;
import com.ecommerce.mapper.OrderMapper;
import com.ecommerce.mapper.ReviewMapper;
import com.ecommerce.mapper.UserMapper;
import com.ecommerce.security.LoginUserUtil;
import com.ecommerce.service.ReviewService;
import com.ecommerce.vo.ReviewSummaryVO;
import com.ecommerce.vo.ReviewVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 商品评价实现。
 * <p>
 * 评价必须关联"已完成订单的订单明细"，从而保证只有真实购买用户可以评价。
 */
@Slf4j
@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewMapper reviewMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final UserMapper userMapper;

    public ReviewServiceImpl(ReviewMapper reviewMapper,
                             OrderMapper orderMapper,
                             OrderItemMapper orderItemMapper,
                             UserMapper userMapper) {
        this.reviewMapper = reviewMapper;
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(ReviewDTO dto) {
        Long userId = LoginUserUtil.currentUserId();
        OrderItem item = orderItemMapper.selectById(dto.getOrderItemId());
        if (item == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "订单商品不存在");
        }
        Order order = orderMapper.selectById(item.getOrderId());
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "订单不存在");
        }
        if (!userId.equals(order.getUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "只能评价自己的订单商品");
        }
        if (!OrderStatus.COMPLETED.name().equals(order.getStatus())) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "只有已完成订单可以评价");
        }
        long exists = reviewMapper.selectCount(new LambdaQueryWrapper<Review>()
                .eq(Review::getOrderItemId, dto.getOrderItemId()));
        if (exists > 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "该商品已评价过，不能重复评价");
        }

        Review review = new Review();
        review.setOrderId(order.getId());
        review.setOrderItemId(item.getId());
        review.setUserId(userId);
        review.setProductId(item.getProductId());
        review.setRating(dto.getRating());
        review.setContent(dto.getContent());
        review.setStatus(1);
        reviewMapper.insert(review);
        log.info("商品评价提交：orderNo={}, productId={}, rating={}",
                order.getOrderNo(), item.getProductId(), dto.getRating());
    }

    @Override
    public PageResult<ReviewVO> pageByProduct(Long productId, long pageNum, long pageSize) {
        Page<Review> page = reviewMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<Review>()
                        .eq(Review::getProductId, productId)
                        .eq(Review::getStatus, 1)
                        .orderByDesc(Review::getCreatedAt));

        List<Long> userIds = page.getRecords().stream().map(Review::getUserId).distinct().collect(Collectors.toList());
        Map<Long, User> userMap = userIds.isEmpty()
                ? Collections.emptyMap()
                : userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        return PageResult.of(page, review -> {
            User user = userMap.get(review.getUserId());
            String name = user == null ? null : (user.getNickname() != null ? user.getNickname() : user.getUsername());
            return ReviewVO.from(review, name);
        });
    }

    @Override
    public ReviewSummaryVO summary(Long productId) {
        List<Review> reviews = reviewMapper.selectList(new LambdaQueryWrapper<Review>()
                .eq(Review::getProductId, productId)
                .eq(Review::getStatus, 1));
        if (reviews.isEmpty()) {
            return new ReviewSummaryVO(productId, 0, BigDecimal.ZERO);
        }
        int total = reviews.stream().mapToInt(Review::getRating).sum();
        BigDecimal average = BigDecimal.valueOf(total)
                .divide(BigDecimal.valueOf(reviews.size()), 1, RoundingMode.HALF_UP);
        return new ReviewSummaryVO(productId, reviews.size(), average);
    }
}
