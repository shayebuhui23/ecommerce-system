package com.ecommerce.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.ecommerce.common.Result;
import com.ecommerce.common.RoleCode;
import com.ecommerce.dto.ExchangeRateUpdateDTO;
import com.ecommerce.service.ExchangeRateService;
import com.ecommerce.vo.ExchangeRateVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * 模拟汇率接口。查询对所有登录用户开放，调整仅管理员可用。
 */
@RestController
@RequestMapping("/api/exchange-rate")
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    public ExchangeRateController(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    @GetMapping("/list")
    public Result<List<ExchangeRateVO>> list() {
        return Result.success(exchangeRateService.list());
    }

    @SaCheckRole(RoleCode.ADMIN)
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ExchangeRateUpdateDTO dto) {
        exchangeRateService.updateRate(id, dto.getRate());
        return Result.success("汇率已更新", null);
    }
}
