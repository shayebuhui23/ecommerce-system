package com.ecommerce.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.ecommerce.common.Result;
import com.ecommerce.common.RoleCode;
import com.ecommerce.dto.CategoryDTO;
import com.ecommerce.service.CategoryService;
import com.ecommerce.vo.CategoryVO;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * 商品分类接口。
 * <p>
 * 查询对所有登录用户开放；维护操作仅限管理员与商家运营。
 */
@RestController
@RequestMapping("/api/category")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/tree")
    public Result<List<CategoryVO>> tree() {
        return Result.success(categoryService.tree());
    }

    @SaCheckRole(value = {RoleCode.ADMIN, RoleCode.STAFF}, mode = SaMode.OR)
    @PostMapping
    public Result<Void> create(@Valid @RequestBody CategoryDTO dto) {
        categoryService.create(dto);
        return Result.success("分类已创建", null);
    }

    @SaCheckRole(value = {RoleCode.ADMIN, RoleCode.STAFF}, mode = SaMode.OR)
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody CategoryDTO dto) {
        categoryService.update(id, dto);
        return Result.success("分类已更新", null);
    }

    @SaCheckRole(value = {RoleCode.ADMIN, RoleCode.STAFF}, mode = SaMode.OR)
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.success("分类已删除", null);
    }
}
