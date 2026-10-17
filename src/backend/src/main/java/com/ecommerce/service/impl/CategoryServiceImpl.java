package com.ecommerce.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ecommerce.common.ResultCode;
import com.ecommerce.dto.CategoryDTO;
import com.ecommerce.entity.Category;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.BusinessException;
import com.ecommerce.mapper.CategoryMapper;
import com.ecommerce.mapper.ProductMapper;
import com.ecommerce.service.CategoryService;
import com.ecommerce.vo.CategoryVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品分类服务实现。
 * <p>
 * 业务规则：最多两级分类；同一父分类下名称不重复；存在子分类或商品引用时不允许删除。
 */
@Slf4j
@Service
public class CategoryServiceImpl implements CategoryService {

    private static final long ROOT_PARENT_ID = 0L;

    private final CategoryMapper categoryMapper;
    private final ProductMapper productMapper;

    public CategoryServiceImpl(CategoryMapper categoryMapper, ProductMapper productMapper) {
        this.categoryMapper = categoryMapper;
        this.productMapper = productMapper;
    }

    @Override
    public List<CategoryVO> tree() {
        List<Category> categories = categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .orderByAsc(Category::getSortOrder)
                .orderByAsc(Category::getId));

        Map<Long, CategoryVO> voMap = new LinkedHashMap<>();
        categories.forEach(category -> voMap.put(category.getId(), CategoryVO.from(category)));

        List<CategoryVO> roots = new ArrayList<>();
        for (Category category : categories) {
            CategoryVO vo = voMap.get(category.getId());
            Long parentId = category.getParentId();
            if (parentId == null || parentId == ROOT_PARENT_ID) {
                roots.add(vo);
                continue;
            }
            CategoryVO parent = voMap.get(parentId);
            if (parent == null) {
                // 父分类已删除时降级为顶级，避免数据“消失”
                roots.add(vo);
            } else {
                parent.getChildren().add(vo);
            }
        }
        return roots;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(CategoryDTO dto) {
        Long parentId = normalizeParentId(dto.getParentId());
        validateParent(parentId);
        checkDuplicate(parentId, dto.getName(), null);

        Category category = new Category();
        category.setParentId(parentId);
        category.setName(dto.getName().trim());
        category.setNameEn(dto.getNameEn());
        category.setSortOrder(dto.getSortOrder() == null ? 0 : dto.getSortOrder());
        category.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        categoryMapper.insert(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, CategoryDTO dto) {
        Category existing = categoryMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "分类不存在");
        }

        Long parentId = normalizeParentId(dto.getParentId());
        if (parentId.equals(id)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "不能将分类设置为自己的上级分类");
        }
        validateParent(parentId);
        // 已有子分类的分类不能再挂到其他分类下，避免出现三级结构
        boolean hasChildren = categoryMapper.selectCount(
                new LambdaQueryWrapper<Category>().eq(Category::getParentId, id)) > 0;
        if (hasChildren && parentId != ROOT_PARENT_ID) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该分类下存在子分类，不能改为二级分类");
        }
        checkDuplicate(parentId, dto.getName(), id);

        Category update = new Category();
        update.setId(id);
        update.setParentId(parentId);
        update.setName(dto.getName().trim());
        update.setNameEn(dto.getNameEn());
        update.setSortOrder(dto.getSortOrder());
        update.setStatus(dto.getStatus());
        categoryMapper.updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Category existing = categoryMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "分类不存在");
        }
        boolean hasChildren = categoryMapper.selectCount(
                new LambdaQueryWrapper<Category>().eq(Category::getParentId, id)) > 0;
        if (hasChildren) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "该分类下还有子分类，请先处理子分类");
        }
        boolean usedByProduct = productMapper.selectCount(
                new LambdaQueryWrapper<Product>().eq(Product::getCategoryId, id)) > 0;
        if (usedByProduct) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "该分类下还有商品，不能删除");
        }
        categoryMapper.deleteById(id);
        log.info("删除商品分类：id={}, name={}", id, existing.getName());
    }

    private Long normalizeParentId(Long parentId) {
        return parentId == null ? ROOT_PARENT_ID : parentId;
    }

    private void validateParent(Long parentId) {
        if (parentId == ROOT_PARENT_ID) {
            return;
        }
        Category parent = categoryMapper.selectById(parentId);
        if (parent == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "上级分类不存在");
        }
        if (parent.getParentId() != null && parent.getParentId() != ROOT_PARENT_ID) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "最多支持两级分类，不能选择二级分类作为上级");
        }
    }

    private void checkDuplicate(Long parentId, String name, Long excludeId) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<Category>()
                .eq(Category::getParentId, parentId)
                .eq(Category::getName, name.trim());
        if (excludeId != null) {
            wrapper.ne(Category::getId, excludeId);
        }
        if (categoryMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "同级下已存在同名分类");
        }
    }
}
