package com.ecommerce.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.ecommerce.common.PageResult;
import com.ecommerce.common.Result;
import com.ecommerce.common.RoleCode;
import com.ecommerce.dto.PayDTO;
import com.ecommerce.service.PaymentService;
import com.ecommerce.vo.PaymentVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * 模拟支付接口。
 */
@Validated
@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/pay")
    public Result<PaymentVO> pay(@Valid @RequestBody PayDTO dto) {
        PaymentVO vo = paymentService.pay(dto);
        return Result.success(vo.getStatus() != null && vo.getStatus() == 1 ? "支付成功" : "支付失败", vo);
    }

    @GetMapping("/order/{orderId}")
    public Result<PaymentVO> queryByOrder(@PathVariable Long orderId) {
        return Result.success(paymentService.queryByOrder(orderId));
    }

    @SaCheckRole(RoleCode.ADMIN)
    @GetMapping("/page")
    public Result<PageResult<PaymentVO>> page(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码不能小于 1") long pageNum,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页条数不能小于 1")
            @Max(value = 100, message = "每页条数不能超过 100") long pageSize,
            @RequestParam(required = false) String keyword) {
        return Result.success(paymentService.page(pageNum, pageSize, keyword));
    }
}
