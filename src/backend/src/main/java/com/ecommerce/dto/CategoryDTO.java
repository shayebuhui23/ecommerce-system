package com.ecommerce.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 商品分类新增/编辑入参。
 */
@Data
public class CategoryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 父分类 ID，0 表示顶级分类 */
    @Min(value = 0, message = "父分类参数不合法")
    private Long parentId = 0L;

    @NotBlank(message = "分类名称不能为空")
    @Size(max = 64, message = "分类名称不能超过 64 个字符")
    private String name;

    @Size(max = 64, message = "英文分类名称不能超过 64 个字符")
    private String nameEn;

    @Min(value = 0, message = "排序值不能为负数")
    private Integer sortOrder = 0;

    @Min(value = 0, message = "状态取值只能为 0 或 1")
    @Max(value = 1, message = "状态取值只能为 0 或 1")
    private Integer status = 1;
}
