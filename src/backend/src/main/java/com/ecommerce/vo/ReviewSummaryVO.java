package com.ecommerce.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商品评分汇总。
 */
@Data
public class ReviewSummaryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long productId;
    private long reviewCount;
    private BigDecimal averageRating;

    public ReviewSummaryVO(Long productId, long reviewCount, BigDecimal averageRating) {
        this.productId = productId;
        this.reviewCount = reviewCount;
        this.averageRating = averageRating;
    }
}
