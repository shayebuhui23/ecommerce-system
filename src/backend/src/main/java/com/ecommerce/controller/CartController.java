package com.ecommerce.controller;

import com.ecommerce.common.Result;
import com.ecommerce.dto.CartItemDTO;
import com.ecommerce.dto.CartQuantityDTO;
import com.ecommerce.service.CartService;
import com.ecommerce.vo.CartVO;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 购物车接口，只能操作当前登录用户的购物车。
 */
@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public Result<CartVO> getCart() {
        return Result.success(cartService.getCart());
    }

    @PostMapping("/items")
    public Result<Void> addItem(@Valid @RequestBody CartItemDTO dto) {
        cartService.addItem(dto);
        return Result.success("已加入购物车", null);
    }

    @PutMapping("/items/{id}")
    public Result<Void> updateQuantity(@PathVariable Long id, @Valid @RequestBody CartQuantityDTO dto) {
        cartService.updateQuantity(id, dto.getQuantity());
        return Result.success("数量已更新", null);
    }

    @DeleteMapping("/items/{id}")
    public Result<Void> removeItem(@PathVariable Long id) {
        cartService.removeItem(id);
        return Result.success("已移出购物车", null);
    }

    @DeleteMapping("/items")
    public Result<Void> clear() {
        cartService.clear();
        return Result.success("购物车已清空", null);
    }
}
