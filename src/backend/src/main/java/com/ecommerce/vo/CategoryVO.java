package com.ecommerce.vo;

import com.ecommerce.entity.Category;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 商品分类响应，children 用于两级分类展示。
 */
@Data
public class CategoryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long parentId;
    private String name;
    private String nameEn;
    private Integer sortOrder;
    private Integer status;
    private List<CategoryVO> children = new ArrayList<>();

    public static CategoryVO from(Category category) {
        CategoryVO vo = new CategoryVO();
        vo.setId(category.getId());
        vo.setParentId(category.getParentId());
        vo.setName(category.getName());
        vo.setNameEn(category.getNameEn());
        vo.setSortOrder(category.getSortOrder());
        vo.setStatus(category.getStatus());
        return vo;
    }
}
