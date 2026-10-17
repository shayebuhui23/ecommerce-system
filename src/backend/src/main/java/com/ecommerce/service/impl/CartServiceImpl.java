package com.ecommerce.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ecommerce.common.ResultCode;
import com.ecommerce.dto.CartItemDTO;
import com.ecommerce.entity.Cart;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.BusinessException;
import com.ecommerce.mapper.CartItemMapper;
import com.ecommerce.mapper.CartMapper;
import com.ecommerce.mapper.ProductMapper;
import com.ecommerce.security.LoginUserUtil;
import com.ecommerce.service.CartService;
import com.ecommerce.vo.CartItemVO;
import com.ecommerce.vo.CartVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 购物车服务实现。
 * <p>
 * 关键约束：购物车不保存商品价格快照，展示与结算都以商品当前价格为准；
 * 所有读写都会校验购物车归属，防止越权访问他人购物车。
 */
@Slf4j
@Service
public class CartServiceImpl implements CartService {

    private static final int STATUS_ON_SHELF = 1;
    private static final int MAX_QUANTITY = 999;

    private final CartMapper cartMapper;
    private final CartItemMapper cartItemMapper;
    private final ProductMapper productMapper;

    public CartServiceImpl(CartMapper cartMapper, CartItemMapper cartItemMapper, ProductMapper productMapper) {
        this.cartMapper = cartMapper;
        this.cartItemMapper = cartItemMapper;
        this.productMapper = productMapper;
    }

    @Override
    public CartVO getCart() {
        Long userId = LoginUserUtil.currentUserId();
        Cart cart = findCart(userId);
        CartVO vo = new CartVO();
        vo.setTotalQuantity(0);
        vo.setTotalCount(0);
        vo.setAvailableCount(0);
        vo.setTotalAmount(BigDecimal.ZERO);
        if (cart == null) {
            return vo;
        }

        List<CartItem> items = cartItemMapper.selectList(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getCartId, cart.getId())
                .orderByDesc(CartItem::getCreatedAt));
        if (items.isEmpty()) {
            return vo;
        }

        List<Long> productIds = items.stream().map(CartItem::getProductId).distinct().collect(Collectors.toList());
        Map<Long, Product> productMap = productMapper.selectBatchIds(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        List<CartItemVO> itemVOs = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        int totalQuantity = 0;
        int availableCount = 0;
        for (CartItem item : items) {
            Product product = productMap.get(item.getProductId());
            if (product == null) {
                // 商品已被删除，直接跳过，避免脏数据影响结算
                continue;
            }
            CartItemVO itemVO = CartItemVO.of(item.getId(), product.getId(), product.getName(),
                    product.getNameEn(), product.getImageUrl(), product.getSkuCode(), product.getPrice(),
                    product.getCurrency(), product.getStock(), item.getQuantity(), product.getStatus());
            itemVOs.add(itemVO);
            totalQuantity += item.getQuantity();
            if (Boolean.TRUE.equals(itemVO.getAvailable())) {
                totalAmount = totalAmount.add(itemVO.getSubtotal());
                availableCount++;
            }
        }

        vo.setItems(itemVOs);
        vo.setTotalQuantity(totalQuantity);
        vo.setTotalCount(itemVOs.size());
        vo.setAvailableCount(availableCount);
        vo.setTotalAmount(totalAmount);
        if (!itemVOs.isEmpty()) {
            vo.setCurrency(itemVOs.get(0).getCurrency());
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addItem(CartItemDTO dto) {
        Long userId = LoginUserUtil.currentUserId();
        Product product = requirePurchasableProduct(dto.getProductId(), dto.getQuantity());

        Cart cart = getOrCreateCart(userId);
        CartItem existing = cartItemMapper.selectOne(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getCartId, cart.getId())
                .eq(CartItem::getProductId, dto.getProductId()));

        if (existing == null) {
            CartItem item = new CartItem();
            item.setCartId(cart.getId());
            item.setProductId(product.getId());
            item.setQuantity(dto.getQuantity());
            cartItemMapper.insert(item);
            return;
        }

        int newQuantity = existing.getQuantity() + dto.getQuantity();
        if (newQuantity > MAX_QUANTITY) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "单个商品最多购买 " + MAX_QUANTITY + " 件");
        }
        if (product.getStock() != null && newQuantity > product.getStock()) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "库存不足，当前仅剩 " + product.getStock() + " 件");
        }
        CartItem update = new CartItem();
        update.setId(existing.getId());
        update.setQuantity(newQuantity);
        cartItemMapper.updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateQuantity(Long itemId, Integer quantity) {
        CartItem item = requireOwnItem(itemId);
        Product product = productMapper.selectById(item.getProductId());
        if (product == null || product.getStatus() == null || product.getStatus() != STATUS_ON_SHELF) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "商品已下架，请从购物车移除");
        }
        if (product.getStock() == null || quantity > product.getStock()) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "库存不足，当前仅剩 "
                    + (product.getStock() == null ? 0 : product.getStock()) + " 件");
        }

        CartItem update = new CartItem();
        update.setId(item.getId());
        update.setQuantity(quantity);
        cartItemMapper.updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeItem(Long itemId) {
        CartItem item = requireOwnItem(itemId);
        cartItemMapper.deleteById(item.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clear() {
        Long userId = LoginUserUtil.currentUserId();
        Cart cart = findCart(userId);
        if (cart == null) {
            return;
        }
        cartItemMapper.delete(new LambdaQueryWrapper<CartItem>().eq(CartItem::getCartId, cart.getId()));
    }

    private Cart findCart(Long userId) {
        return cartMapper.selectOne(new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, userId));
    }

    private Cart getOrCreateCart(Long userId) {
        Cart cart = findCart(userId);
        if (cart != null) {
            return cart;
        }
        Cart created = new Cart();
        created.setUserId(userId);
        cartMapper.insert(created);
        return created;
    }

    private CartItem requireOwnItem(Long itemId) {
        CartItem item = cartItemMapper.selectById(itemId);
        if (item == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "购物车项不存在");
        }
        // 归属校验：购物车项必须属于当前用户的购物车
        Cart cart = cartMapper.selectById(item.getCartId());
        Long userId = LoginUserUtil.currentUserId();
        if (cart == null || !userId.equals(cart.getUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权操作他人的购物车");
        }
        return item;
    }

    private Product requirePurchasableProduct(Long productId, Integer quantity) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "商品不存在");
        }
        if (product.getStatus() == null || product.getStatus() != STATUS_ON_SHELF) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "商品已下架，无法加入购物车");
        }
        if (product.getStock() == null || product.getStock() < quantity) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "库存不足，当前仅剩 "
                    + (product.getStock() == null ? 0 : product.getStock()) + " 件");
        }
        return product;
    }
}
