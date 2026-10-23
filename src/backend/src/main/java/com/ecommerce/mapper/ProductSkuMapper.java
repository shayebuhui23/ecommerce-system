package com.ecommerce.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ecommerce.entity.ProductSku;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 商品 SKU 数据访问。
 */
public interface ProductSkuMapper extends BaseMapper<ProductSku> {

    /**
     * SKU 级原子扣减，避免规格超卖。
     */
    @Update("UPDATE product_skus SET stock = stock - #{quantity} "
            + "WHERE id = #{skuId} AND stock >= #{quantity} AND status = 1 AND deleted = 0")
    int deductStock(@Param("skuId") Long skuId, @Param("quantity") Integer quantity);

    /**
     * 取消/退货时恢复 SKU 库存。
     */
    @Update("UPDATE product_skus SET stock = stock + #{quantity} WHERE id = #{skuId} AND deleted = 0")
    int restoreStock(@Param("skuId") Long skuId, @Param("quantity") Integer quantity);
}
