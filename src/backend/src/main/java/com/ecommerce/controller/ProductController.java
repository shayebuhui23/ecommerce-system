package com.ecommerce.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.ecommerce.common.PageResult;
import com.ecommerce.common.Result;
import com.ecommerce.common.RoleCode;
import com.ecommerce.dto.ProductDTO;
import com.ecommerce.service.ProductService;
import com.ecommerce.vo.ProductDetailVO;
import com.ecommerce.vo.ProductVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
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
 * 商品接口。
 * <p>
 * 查询对所有登录用户开放（客户只能看到上架商品），维护操作仅限管理员与商家运营。
 */
@Validated
@RestController
@RequestMapping("/api/product")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/page")
    public Result<PageResult<ProductVO>> page(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码不能小于 1") long pageNum,
            @RequestParam(defaultValue = "12") @Min(value = 1, message = "每页条数不能小于 1")
            @Max(value = 100, message = "每页条数不能超过 100") long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) @Min(0) @Max(1) Integer status) {
        return Result.success(productService.page(pageNum, pageSize, keyword, categoryId, status));
    }

    @GetMapping("/{id}")
    public Result<ProductDetailVO> detail(@PathVariable Long id) {
        return Result.success(productService.detail(id));
    }

    @SaCheckRole(value = {RoleCode.ADMIN, RoleCode.STAFF}, mode = SaMode.OR)
    @PostMapping
    public Result<Long> create(@Valid @RequestBody ProductDTO dto) {
        return Result.success("商品已创建", productService.create(dto));
    }

    @SaCheckRole(value = {RoleCode.ADMIN, RoleCode.STAFF}, mode = SaMode.OR)
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ProductDTO dto) {
        productService.update(id, dto);
        return Result.success("商品已更新", null);
    }

    @SaCheckRole(value = {RoleCode.ADMIN, RoleCode.STAFF}, mode = SaMode.OR)
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(
            @PathVariable Long id,
            @RequestParam @Min(value = 0, message = "状态取值只能为 0 或 1")
            @Max(value = 1, message = "状态取值只能为 0 或 1") Integer status) {
        productService.updateStatus(id, status);
        return Result.success(status == 1 ? "商品已上架" : "商品已下架", null);
    }

    @SaCheckRole(value = {RoleCode.ADMIN, RoleCode.STAFF}, mode = SaMode.OR)
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return Result.success("商品已删除", null);
    }
}
