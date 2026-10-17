package com.ecommerce.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 经营统计响应。
 * <p>
 * 金额统一按订单下单时折算的基础币种（CNY）汇总，避免不同币种直接相加。
 */
@Data
public class StatisticsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private long productTotal;
    private long productOnShelf;
    private long orderTotal;
    private long completedOrderCount;
    private BigDecimal completedAmount = BigDecimal.ZERO;
    private long afterSaleTotal;
    private long afterSalePending;
    private long userTotal;
    private Map<String, Long> orderStatusCounts = new LinkedHashMap<>();
    private List<TopProductVO> topProducts = new ArrayList<>();

    /**
     * 热门商品条目。
     */
    @Data
    public static class TopProductVO implements Serializable {
        private static final long serialVersionUID = 1L;

        private Long productId;
        private String productName;
        private long quantity;
        private long salesCount;
    }
}
