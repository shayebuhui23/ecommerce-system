package com.ecommerce.service;

import com.ecommerce.vo.StatisticsVO;

/**
 * 经营统计服务。
 */
public interface StatisticsService {

    /**
     * 经营概览：商品、订单、金额、售后与热门商品。
     */
    StatisticsVO overview();
}
