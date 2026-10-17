package com.ecommerce.vo;

import com.ecommerce.entity.Review;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 商品评价响应。评价人昵称做脱敏处理。
 */
@Data
public class ReviewVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long orderItemId;
    private Long productId;
    private Integer rating;
    private String content;
    private String reviewer;
    private LocalDateTime createdAt;

    public static ReviewVO from(Review review, String reviewerName) {
        ReviewVO vo = new ReviewVO();
        vo.setId(review.getId());
        vo.setOrderItemId(review.getOrderItemId());
        vo.setProductId(review.getProductId());
        vo.setRating(review.getRating());
        vo.setContent(review.getContent());
        vo.setReviewer(mask(reviewerName));
        vo.setCreatedAt(review.getCreatedAt());
        return vo;
    }

    private static String mask(String name) {
        if (name == null || name.isEmpty()) {
            return "匿名用户";
        }
        return name.charAt(0) + "**";
    }
}
