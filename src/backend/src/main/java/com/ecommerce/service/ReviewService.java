package com.ecommerce.service;

import com.ecommerce.common.PageResult;
import com.ecommerce.dto.ReviewDTO;
import com.ecommerce.vo.ReviewSummaryVO;
import com.ecommerce.vo.ReviewVO;

/**
 * 商品评价服务。
 */
public interface ReviewService {

    /**
     * 提交评价：仅已完成订单、且同一订单明细只能评价一次。
     */
    void create(ReviewDTO dto);

    /**
     * 商品评价分页列表。
     */
    PageResult<ReviewVO> pageByProduct(Long productId, long pageNum, long pageSize);

    /**
     * 商品评分汇总（平均分、评价数）。
     */
    ReviewSummaryVO summary(Long productId);
}
