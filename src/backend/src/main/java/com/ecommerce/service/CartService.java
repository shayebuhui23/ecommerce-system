package com.ecommerce.service;

import com.ecommerce.dto.CartItemDTO;
import com.ecommerce.vo.CartVO;

/**
 * 购物车服务，所有操作按当前登录用户隔离。
 */
public interface CartService {

    /**
     * 查询当前用户购物车，金额与可用性由后端实时计算。
     */
    CartVO getCart();

    /**
     * 加入购物车；已存在同一商品时累加数量。
     */
    void addItem(CartItemDTO dto);

    /**
     * 修改指定购物车项的数量。
     */
    void updateQuantity(Long itemId, Integer quantity);

    /**
     * 删除指定购物车项。
     */
    void removeItem(Long itemId);

    /**
     * 清空购物车。
     */
    void clear();
}
