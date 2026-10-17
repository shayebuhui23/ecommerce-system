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
    /** 已完成订单对应的商品成本合计（CNY） */
    private BigDecimal costAmount = BigDecimal.ZERO;
    /** 毛利 = 成交金额 - 成本 */
    private BigDecimal grossProfit = BigDecimal.ZERO;
    /** 毛利率（百分比值，如 35.20 表示 35.20%） */
    private BigDecimal grossMargin = BigDecimal.ZERO;
    private long afterSaleTotal;
    private long afterSalePending;
    private long userTotal;
    private long merchantTotal;
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
