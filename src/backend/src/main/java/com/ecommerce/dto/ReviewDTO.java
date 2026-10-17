package com.ecommerce.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 商品评价入参。
 */
@Data
public class ReviewDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "请选择要评价的商品")
    private Long orderItemId;

    @NotNull(message = "请选择评分")
    @Min(value = 1, message = "评分范围为 1~5 分")
    @Max(value = 5, message = "评分范围为 1~5 分")
    private Integer rating;

    @Size(max = 500, message = "评价内容不能超过 500 个字符")
    private String content;
}
