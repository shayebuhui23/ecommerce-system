package com.ecommerce.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 购物车响应，金额由后端汇总。
 */
@Data
public class CartVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<CartItemVO> items = new ArrayList<>();
    private Integer totalQuantity;
    private BigDecimal totalAmount;
    private String currency = "CNY";
    private Integer totalCount;
    private Integer availableCount;
}
