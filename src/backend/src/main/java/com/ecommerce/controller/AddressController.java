package com.ecommerce.controller;

import com.ecommerce.common.Result;
import com.ecommerce.dto.AddressDTO;
import com.ecommerce.service.AddressService;
import com.ecommerce.vo.AddressVO;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * 收货地址接口，只能操作当前登录用户自己的地址。
 */
@RestController
@RequestMapping("/api/address")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping("/list")
    public Result<List<AddressVO>> list() {
        return Result.success(addressService.list());
    }

    @PostMapping
    public Result<Long> create(@Valid @RequestBody AddressDTO dto) {
        return Result.success("地址已保存", addressService.create(dto));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody AddressDTO dto) {
        addressService.update(id, dto);
        return Result.success("地址已更新", null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        addressService.delete(id);
        return Result.success("地址已删除", null);
    }

    @PutMapping("/{id}/default")
    public Result<Void> setDefault(@PathVariable Long id) {
        addressService.setDefault(id);
        return Result.success("已设为默认地址", null);
    }
}
