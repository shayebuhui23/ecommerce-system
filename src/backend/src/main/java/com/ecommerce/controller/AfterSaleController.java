package com.ecommerce.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.ecommerce.common.PageResult;
import com.ecommerce.common.Result;
import com.ecommerce.common.RoleCode;
import com.ecommerce.dto.AfterSaleApplyDTO;
import com.ecommerce.dto.AfterSaleAuditDTO;
import com.ecommerce.service.AfterSaleService;
import com.ecommerce.vo.AfterSaleVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * 售后与退款接口。
 */
@Validated
@RestController
@RequestMapping("/api/after-sale")
public class AfterSaleController {

    private final AfterSaleService afterSaleService;

    public AfterSaleController(AfterSaleService afterSaleService) {
        this.afterSaleService = afterSaleService;
    }

    @PostMapping
    public Result<AfterSaleVO> apply(@Valid @RequestBody AfterSaleApplyDTO dto) {
        return Result.success("退款申请已提交", afterSaleService.apply(dto));
    }

    @SaCheckRole(value = {RoleCode.ADMIN, RoleCode.STAFF}, mode = SaMode.OR)
    @PutMapping("/{id}/audit")
    public Result<AfterSaleVO> audit(@PathVariable Long id, @Valid @RequestBody AfterSaleAuditDTO dto) {
        AfterSaleVO vo = afterSaleService.audit(id, dto);
        return Result.success(Boolean.TRUE.equals(dto.getApproved()) ? "已通过并退款" : "已驳回", vo);
    }

    @GetMapping("/order/{orderId}")
    public Result<AfterSaleVO> queryByOrder(@PathVariable Long orderId) {
        return Result.success(afterSaleService.queryByOrder(orderId));
    }

    @GetMapping("/page")
    public Result<PageResult<AfterSaleVO>> page(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码不能小于 1") long pageNum,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页条数不能小于 1")
            @Max(value = 100, message = "每页条数不能超过 100") long pageSize,
            @RequestParam(required = false) String status) {
        return Result.success(afterSaleService.page(pageNum, pageSize, status));
    }
}
