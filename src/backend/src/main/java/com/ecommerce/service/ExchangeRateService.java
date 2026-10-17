package com.ecommerce.service;

import com.ecommerce.vo.ExchangeRateVO;

import java.math.BigDecimal;
import java.util.List;

/**
 * 模拟汇率服务。
 */
public interface ExchangeRateService {

    List<ExchangeRateVO> list();

    /**
     * 管理员调整汇率，修改后仅影响新订单，历史订单保留下单时的汇率快照。
     */
    void updateRate(Long id, BigDecimal rate);

    /**
     * 获取从 from 到 to 的汇率；同币种返回 1，缺少配置时抛业务异常。
     */
    BigDecimal requireRate(String from, String to);
}
