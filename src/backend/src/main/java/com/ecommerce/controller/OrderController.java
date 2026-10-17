package com.ecommerce.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.ecommerce.common.PageResult;
import com.ecommerce.common.Result;
import com.ecommerce.common.RoleCode;
import com.ecommerce.dto.OrderCreateDTO;
import com.ecommerce.dto.ShipDTO;
import com.ecommerce.service.OrderService;
import com.ecommerce.service.ShipmentService;
import com.ecommerce.vo.OrderDetailVO;
import com.ecommerce.vo.OrderVO;
import com.ecommerce.vo.ShipmentVO;
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
 * 客户只能查询与操作自己的订单；商家与管理员可查询全部订单并执行发货。
 */
@Validated
@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;
    private final ShipmentService shipmentService;

    public OrderController(OrderService orderService, ShipmentService shipmentService) {
        this.orderService = orderService;
        this.shipmentService = shipmentService;
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

    /**
     * 商家发货。
     */
    @SaCheckRole(value = {RoleCode.ADMIN, RoleCode.STAFF}, mode = SaMode.OR)
    @PostMapping("/{id}/ship")
    public Result<ShipmentVO> ship(@PathVariable Long id, @Valid @RequestBody ShipDTO dto) {
        return Result.success("发货成功", shipmentService.ship(id, dto));
    }

    /**
     * 查询物流信息（客户查自己的，商家与管理员可查全部）。
     */
    @GetMapping("/{id}/shipment")
    public Result<ShipmentVO> shipment(@PathVariable Long id) {
        return Result.success(shipmentService.queryByOrder(id));
    }

    /**
     * 客户确认收货。
     */
    @PostMapping("/{id}/receive")
    public Result<Void> receive(@PathVariable Long id) {
        shipmentService.receive(id);
        return Result.success("已确认收货", null);
    }
}
