package com.ecommerce.service;

import com.ecommerce.dto.CategoryDTO;
import com.ecommerce.vo.CategoryVO;

import java.util.List;

/**
 * 商品分类服务。
 */
public interface CategoryService {

    /**
     * 查询两级分类树。
     */
    List<CategoryVO> tree();

    void create(CategoryDTO dto);

    void update(Long id, CategoryDTO dto);

    void delete(Long id);
}
