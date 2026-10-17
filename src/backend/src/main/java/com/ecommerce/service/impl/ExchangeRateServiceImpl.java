package com.ecommerce.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ecommerce.common.ResultCode;
import com.ecommerce.entity.ExchangeRate;
import com.ecommerce.exception.BusinessException;
import com.ecommerce.mapper.ExchangeRateMapper;
import com.ecommerce.service.ExchangeRateService;
import com.ecommerce.vo.ExchangeRateVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 模拟汇率实现。
 */
@Slf4j
@Service
public class ExchangeRateServiceImpl implements ExchangeRateService {

    private final ExchangeRateMapper exchangeRateMapper;

    public ExchangeRateServiceImpl(ExchangeRateMapper exchangeRateMapper) {
        this.exchangeRateMapper = exchangeRateMapper;
    }

    @Override
    public List<ExchangeRateVO> list() {
        return exchangeRateMapper.selectList(new LambdaQueryWrapper<ExchangeRate>()
                        .orderByAsc(ExchangeRate::getFromCurrency)
                        .orderByAsc(ExchangeRate::getToCurrency))
                .stream()
                .map(ExchangeRateVO::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRate(Long id, BigDecimal rate) {
        if (rate == null || rate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "汇率必须大于 0");
        }
        ExchangeRate existing = exchangeRateMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "汇率配置不存在");
        }

        ExchangeRate update = new ExchangeRate();
        update.setId(id);
        update.setRate(rate);
        update.setEffectiveTime(LocalDateTime.now());
        exchangeRateMapper.updateById(update);
        log.info("汇率调整：{}->{}, {} -> {}", existing.getFromCurrency(), existing.getToCurrency(),
                existing.getRate(), rate);
    }

    @Override
    public BigDecimal requireRate(String from, String to) {
        if (from == null || to == null || from.equals(to)) {
            return BigDecimal.ONE;
        }
        ExchangeRate rate = exchangeRateMapper.selectOne(new LambdaQueryWrapper<ExchangeRate>()
                .eq(ExchangeRate::getFromCurrency, from)
                .eq(ExchangeRate::getToCurrency, to)
                .eq(ExchangeRate::getStatus, 1)
                .last("LIMIT 1"));
        if (rate == null || rate.getRate() == null) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR,
                    "缺少 " + from + " 到 " + to + " 的汇率配置，请联系管理员");
        }
        return rate.getRate();
    }
}
