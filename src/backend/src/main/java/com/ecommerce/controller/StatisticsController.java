package com.ecommerce.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.ecommerce.common.Result;
import com.ecommerce.common.RoleCode;
import com.ecommerce.service.StatisticsService;
import com.ecommerce.vo.StatisticsVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 经营统计接口，仅管理员与商家运营可访问。
 */
@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @SaCheckRole(value = {RoleCode.ADMIN, RoleCode.STAFF}, mode = SaMode.OR)
    @GetMapping("/overview")
    public Result<StatisticsVO> overview() {
        return Result.success(statisticsService.overview());
    }
}
