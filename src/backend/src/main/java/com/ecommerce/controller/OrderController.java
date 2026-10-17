package com.ecommerce.controller;

import com.ecommerce.common.PageResult;
import com.ecommerce.common.Result;
import com.ecommerce.dto.OrderCreateDTO;
import com.ecommerce.service.OrderService;
import com.ecommerce.vo.OrderDetailVO;
import com.ecommerce.vo.OrderVO;
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
 * 订单接口。
 * <p>
 * 客户只能查询与操作自己的订单；商家与管理员可查询全部订单（用于履约）。
 */
@Validated
@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public Result<OrderDetailVO> create(@Valid @RequestBody OrderCreateDTO dto) {
        return Result.success("订单已创建", orderService.create(dto));
    }

    @GetMapping("/page")
    public Result<PageResult<OrderVO>> page(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码不能小于 1") long pageNum,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页条数不能小于 1")
            @Max(value = 100, message = "每页条数不能超过 100") long pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        return Result.success(orderService.page(pageNum, pageSize, status, keyword));
    }

    @GetMapping("/{id}")
    public Result<OrderDetailVO> detail(@PathVariable Long id) {
        return Result.success(orderService.detail(id));
    }

    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        orderService.cancel(id);
        return Result.success("订单已取消", null);
    }
}
