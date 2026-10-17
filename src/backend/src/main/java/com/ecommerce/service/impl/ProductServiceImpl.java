package com.ecommerce.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.common.PageResult;
import com.ecommerce.common.ResultCode;
import com.ecommerce.common.RoleCode;
import com.ecommerce.dto.ProductDTO;
import com.ecommerce.dto.SkuDTO;
import com.ecommerce.entity.Category;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.ProductSku;
import com.ecommerce.exception.BusinessException;
import com.ecommerce.mapper.CategoryMapper;
import com.ecommerce.mapper.ProductMapper;
import com.ecommerce.mapper.ProductSkuMapper;
import com.ecommerce.service.ProductService;
import com.ecommerce.vo.ProductDetailVO;
import com.ecommerce.vo.ProductVO;
import com.ecommerce.vo.SkuVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 商品服务实现。
 * <p>
 * 关键规则：商品必须归属启用状态的分类；价格必须大于 0；库存不能为负；
 * SKU 编码全局唯一；商品只做逻辑删除，保证后续订单快照可追溯。
 */
@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

    private static final int STATUS_ON_SHELF = 1;
    private static final int STATUS_OFF_SHELF = 0;

    private final ProductMapper productMapper;
    private final ProductSkuMapper productSkuMapper;
    private final CategoryMapper categoryMapper;

    public ProductServiceImpl(ProductMapper productMapper,
                              ProductSkuMapper productSkuMapper,
                              CategoryMapper categoryMapper) {
        this.productMapper = productMapper;
        this.productSkuMapper = productSkuMapper;
        this.categoryMapper = categoryMapper;
    }

    @Override
    public PageResult<ProductVO> page(long pageNum, long pageSize, String keyword, Long categoryId, Integer status) {
        boolean manager = isManager();

        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        // 客户侧只能看到上架商品，即使前端传入 status 也不生效
        if (!manager) {
            wrapper.eq(Product::getStatus, STATUS_ON_SHELF);
        } else if (status != null) {
            wrapper.eq(Product::getStatus, status);
        }

        if (categoryId != null) {
            wrapper.in(Product::getCategoryId, collectCategoryIds(categoryId));
        }

        if (StringUtils.hasText(keyword)) {
            String like = keyword.trim();
            wrapper.and(w -> w.like(Product::getName, like)
                    .or().like(Product::getNameEn, like)
                    .or().like(Product::getSkuCode, like));
        }

        wrapper.orderByDesc(Product::getCreatedAt);
        Page<Product> page = productMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);

        Map<Long, Category> categoryMap = loadCategoryMap();
        return PageResult.of(page, product -> ProductVO.from(product, categoryName(categoryMap, product.getCategoryId())));
    }

    @Override
    public ProductDetailVO detail(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "商品不存在");
        }
        if (!isManager() && product.getStatus() != STATUS_ON_SHELF) {
            throw new BusinessException(ResultCode.NOT_FOUND, "商品已下架");
        }

        Category category = product.getCategoryId() == null ? null : categoryMapper.selectById(product.getCategoryId());
        ProductDetailVO vo = ProductDetailVO.from(product, category == null ? null : category.getName());
        List<ProductSku> skus = productSkuMapper.selectList(
                new LambdaQueryWrapper<ProductSku>().eq(ProductSku::getProductId, id));
        vo.setSkus(skus.stream().map(SkuVO::from).collect(Collectors.toList()));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(ProductDTO dto) {
        requireEnabledCategory(dto.getCategoryId());
        if (productMapper.selectCount(new LambdaQueryWrapper<Product>()
                .eq(Product::getSkuCode, dto.getSkuCode().trim())) > 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "商品 SKU 编码已存在");
        }
        validateSkuCodes(dto.getSkus());

        Product product = new Product();
        fillProduct(product, dto);
        product.setSalesCount(0);
        productMapper.insert(product);

        dto.getSkus().forEach(sku -> saveSku(product.getId(), sku));
        log.info("新增商品：id={}, name={}", product.getId(), product.getName());
        return product.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, ProductDTO dto) {
        Product existing = requireProduct(id);
        requireEnabledCategory(dto.getCategoryId());
        if (productMapper.selectCount(new LambdaQueryWrapper<Product>()
                .eq(Product::getSkuCode, dto.getSkuCode().trim())
                .ne(Product::getId, id)) > 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "商品 SKU 编码已存在");
        }
        validateSkuCodes(dto.getSkus());

        Product update = new Product();
        update.setId(id);
        fillProduct(update, dto);
        productMapper.updateById(update);

        syncSkus(id, dto.getSkus());
        log.info("编辑商品：id={}, name={}", id, existing.getName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        requireProduct(id);
        Product update = new Product();
        update.setId(id);
        update.setStatus(status);
        productMapper.updateById(update);
        log.info("变更商品上下架状态：id={}, status={}", id, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        requireProduct(id);
        // 逻辑删除商品及其 SKU，历史订单仍保留快照数据
        productSkuMapper.delete(new LambdaQueryWrapper<ProductSku>().eq(ProductSku::getProductId, id));
        productMapper.deleteById(id);
        log.info("逻辑删除商品：id={}", id);
    }

    private void fillProduct(Product product, ProductDTO dto) {
        product.setCategoryId(dto.getCategoryId());
        product.setSkuCode(dto.getSkuCode().trim());
        product.setName(dto.getName().trim());
        product.setNameEn(dto.getNameEn());
        product.setDescription(dto.getDescription());
        product.setDescriptionEn(dto.getDescriptionEn());
        product.setImageUrl(dto.getImageUrl());
        product.setPrice(dto.getPrice());
        product.setCurrency(StringUtils.hasText(dto.getCurrency()) ? dto.getCurrency() : "CNY");
        product.setStock(dto.getStock());
        product.setStatus(dto.getStatus() == null ? STATUS_OFF_SHELF : dto.getStatus());
    }

    private void syncSkus(Long productId, List<SkuDTO> skus) {
        List<ProductSku> existingSkus = productSkuMapper.selectList(
                new LambdaQueryWrapper<ProductSku>().eq(ProductSku::getProductId, productId));
        Map<Long, ProductSku> existingMap = existingSkus.stream()
                .collect(Collectors.toMap(ProductSku::getId, Function.identity()));

        Set<Long> keepIds = new HashSet<>();
        for (SkuDTO skuDto : skus) {
            if (skuDto.getId() == null) {
                saveSku(productId, skuDto);
                continue;
            }
            ProductSku exist = existingMap.get(skuDto.getId());
            if (exist == null) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "SKU 不属于当前商品");
            }
            ProductSku update = new ProductSku();
            update.setId(skuDto.getId());
            update.setSkuCode(skuDto.getSkuCode().trim());
            update.setSpecName(skuDto.getSpecName());
            update.setPrice(skuDto.getPrice());
            update.setStock(skuDto.getStock());
            update.setStatus(skuDto.getStatus() == null ? 1 : skuDto.getStatus());
            productSkuMapper.updateById(update);
            keepIds.add(skuDto.getId());
        }

        // 未出现在提交列表中的旧 SKU 视为删除
        existingSkus.stream()
                .filter(sku -> !keepIds.contains(sku.getId()))
                .forEach(sku -> productSkuMapper.deleteById(sku.getId()));
    }

    private void saveSku(Long productId, SkuDTO skuDto) {
        ProductSku sku = new ProductSku();
        sku.setProductId(productId);
        sku.setSkuCode(skuDto.getSkuCode().trim());
        sku.setSpecName(skuDto.getSpecName());
        sku.setPrice(skuDto.getPrice());
        sku.setStock(skuDto.getStock());
        sku.setStatus(skuDto.getStatus() == null ? 1 : skuDto.getStatus());
        productSkuMapper.insert(sku);
    }

    /**
     * 校验 SKU 编码：提交列表内部不重复，且不与库中其他 SKU 冲突。
     */
    private void validateSkuCodes(List<SkuDTO> skus) {
        if (skus == null || skus.isEmpty()) {
            return;
        }
        Set<String> codes = new HashSet<>();
        for (SkuDTO sku : skus) {
            String code = sku.getSkuCode().trim();
            if (!codes.add(code)) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "SKU 编码重复：" + code);
            }
            LambdaQueryWrapper<ProductSku> wrapper = new LambdaQueryWrapper<ProductSku>()
                    .eq(ProductSku::getSkuCode, code);
            if (sku.getId() != null) {
                wrapper.ne(ProductSku::getId, sku.getId());
            }
            if (productSkuMapper.selectCount(wrapper) > 0) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "SKU 编码已被占用：" + code);
            }
        }
    }

    private Product requireProduct(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "商品不存在");
        }
        return product;
    }

    private void requireEnabledCategory(Long categoryId) {
        Category category = categoryMapper.selectById(categoryId);
        if (category == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "商品分类不存在");
        }
        if (category.getStatus() == null || category.getStatus() != 1) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该分类已停用，不能用于商品");
        }
    }

    private List<Long> collectCategoryIds(Long categoryId) {
        List<Long> ids = new ArrayList<>();
        ids.add(categoryId);
        List<Category> children = categoryMapper.selectList(
                new LambdaQueryWrapper<Category>().eq(Category::getParentId, categoryId));
        children.forEach(child -> ids.add(child.getId()));
        return ids;
    }

    private Map<Long, Category> loadCategoryMap() {
        return categoryMapper.selectList(null).stream()
                .collect(Collectors.toMap(Category::getId, Function.identity()));
    }

    private String categoryName(Map<Long, Category> categoryMap, Long categoryId) {
        Category category = categoryId == null ? null : categoryMap.get(categoryId);
        return category == null ? null : category.getName();
    }

    /**
     * 商家运营与系统管理员具备商品维护权限。
     */
    private boolean isManager() {
        return StpUtil.hasRole(RoleCode.ADMIN) || StpUtil.hasRole(RoleCode.STAFF);
    }
}
