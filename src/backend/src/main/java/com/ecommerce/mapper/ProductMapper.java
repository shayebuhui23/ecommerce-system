package com.ecommerce.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ecommerce.entity.Product;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 商品数据访问。
 */
public interface ProductMapper extends BaseMapper<Product> {

    /**
     * 带库存条件的原子扣减。
     * <p>
     * 条件 stock >= quantity 写在 SQL 中，由数据库保证并发安全，
     * 返回值 0 表示库存不足（而不是先查后改造成的超卖）。
     */
    @Update("UPDATE products SET stock = stock - #{quantity}, sales_count = sales_count + #{quantity} "
            + "WHERE id = #{productId} AND stock >= #{quantity} AND status = 1 AND deleted = 0")
    int deductStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    /**
     * 恢复库存（取消订单、退款回库）。重复调用会重复增加，因此调用前必须做状态校验。
     */
    @Update("UPDATE products SET stock = stock + #{quantity} WHERE id = #{productId} AND deleted = 0")
    int restoreStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);
}
