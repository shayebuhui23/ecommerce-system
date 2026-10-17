package com.ecommerce.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 管理员启用/禁用用户入参。
 */
@Data
public class UserStatusDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "用户 ID 不能为空")
    private Long userId;

    /** 1 启用，0 禁用 */
    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态取值只能为 0 或 1")
    @Max(value = 1, message = "状态取值只能为 0 或 1")
    private Integer status;
}
