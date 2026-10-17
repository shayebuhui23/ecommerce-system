package com.ecommerce.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.ecommerce.common.PageResult;
import com.ecommerce.common.Result;
import com.ecommerce.common.RoleCode;
import com.ecommerce.service.OperationLogService;
import com.ecommerce.vo.OperationLogVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * 操作审计日志接口，仅系统管理员可访问。
 */
@Validated
@RestController
@RequestMapping("/api/operation-log")
public class OperationLogController {

    private final OperationLogService operationLogService;

    public OperationLogController(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    @SaCheckRole(RoleCode.ADMIN)
    @GetMapping("/page")
    public Result<PageResult<OperationLogVO>> page(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码不能小于 1") long pageNum,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "每页条数不能小于 1")
            @Max(value = 100, message = "每页条数不能超过 100") long pageSize,
            @RequestParam(required = false) String module) {
        return Result.success(operationLogService.page(pageNum, pageSize, module));
    }
}
