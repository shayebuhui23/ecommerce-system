package com.ecommerce.service;

import com.ecommerce.common.PageResult;
import com.ecommerce.dto.ProductDTO;
import com.ecommerce.vo.ProductDetailVO;
import com.ecommerce.vo.ProductVO;

/**
 * 商品服务。
 */
public interface ProductService {

    /**
     * 分页查询商品。商家与管理员可查看全部商品，客户只能看到已上架商品。
     */
    PageResult<ProductVO> page(long pageNum, long pageSize, String keyword, Long categoryId, Integer status);

    /**
     * 商品详情，含中英文描述与 SKU 列表。
     */
    ProductDetailVO detail(Long id);

    Long create(ProductDTO dto);

    void update(Long id, ProductDTO dto);

    void updateStatus(Long id, Integer status);

    void delete(Long id);
}
