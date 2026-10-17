package com.ecommerce.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.ecommerce.common.PageResult;
import com.ecommerce.common.Result;
import com.ecommerce.common.RoleCode;
import com.ecommerce.dto.UserStatusDTO;
import com.ecommerce.service.UserService;
import com.ecommerce.vo.UserInfoVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * 用户接口。
 * <p>
 * 涉及管理员能力的接口使用 {@code @SaCheckRole} 在服务端强制校验角色，
 * 前端的菜单隐藏不作为权限依据。
 */
@Validated
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public Result<UserInfoVO> currentUser() {
        return Result.success(userService.currentUser());
    }

    @SaCheckRole(RoleCode.ADMIN)
    @GetMapping("/page")
    public Result<PageResult<UserInfoVO>> page(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码不能小于 1") long pageNum,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页条数不能小于 1")
            @Max(value = 100, message = "每页条数不能超过 100") long pageSize,
            @RequestParam(required = false) String keyword) {
        return Result.success(userService.pageUsers(pageNum, pageSize, keyword));
    }

    @SaCheckRole(RoleCode.ADMIN)
    @PutMapping("/status")
    public Result<Void> updateStatus(@Valid @RequestBody UserStatusDTO dto) {
        userService.updateStatus(dto);
        return Result.success("状态已更新", null);
    }
}
