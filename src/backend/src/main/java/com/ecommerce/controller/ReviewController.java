package com.ecommerce.controller;

import com.ecommerce.common.PageResult;
import com.ecommerce.common.Result;
import com.ecommerce.dto.ReviewDTO;
import com.ecommerce.service.ReviewService;
import com.ecommerce.vo.ReviewSummaryVO;
import com.ecommerce.vo.ReviewVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * 商品评价接口。
 */
@Validated
@RestController
@RequestMapping("/api/review")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public Result<Void> create(@Valid @RequestBody ReviewDTO dto) {
        reviewService.create(dto);
        return Result.success("评价已提交", null);
    }

    @GetMapping("/product/{productId}")
    public Result<PageResult<ReviewVO>> pageByProduct(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码不能小于 1") long pageNum,
            @RequestParam(defaultValue = "5") @Min(value = 1, message = "每页条数不能小于 1")
            @Max(value = 50, message = "每页条数不能超过 50") long pageSize) {
        return Result.success(reviewService.pageByProduct(productId, pageNum, pageSize));
    }

    @GetMapping("/product/{productId}/summary")
    public Result<ReviewSummaryVO> summary(@PathVariable Long productId) {
        return Result.success(reviewService.summary(productId));
    }
}
