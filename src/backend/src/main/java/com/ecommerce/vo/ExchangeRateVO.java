package com.ecommerce.vo;

import com.ecommerce.entity.ExchangeRate;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 汇率配置响应。
 */
@Data
public class ExchangeRateVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String fromCurrency;
    private String toCurrency;
    private BigDecimal rate;
    private Integer status;
    private LocalDateTime effectiveTime;
    private LocalDateTime updatedAt;

    public static ExchangeRateVO from(ExchangeRate rate) {
        ExchangeRateVO vo = new ExchangeRateVO();
        vo.setId(rate.getId());
        vo.setFromCurrency(rate.getFromCurrency());
        vo.setToCurrency(rate.getToCurrency());
        vo.setRate(rate.getRate());
        vo.setStatus(rate.getStatus());
        vo.setEffectiveTime(rate.getEffectiveTime());
        vo.setUpdatedAt(rate.getUpdatedAt());
        return vo;
    }
}
