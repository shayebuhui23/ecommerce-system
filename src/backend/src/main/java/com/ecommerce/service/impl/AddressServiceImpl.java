package com.ecommerce.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ecommerce.common.ResultCode;
import com.ecommerce.dto.AddressDTO;
import com.ecommerce.entity.Address;
import com.ecommerce.exception.BusinessException;
import com.ecommerce.mapper.AddressMapper;
import com.ecommerce.security.LoginUserUtil;
import com.ecommerce.service.AddressService;
import com.ecommerce.vo.AddressVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 收货地址服务实现。
 * <p>
 * 数据归属校验：任何读写操作都必须校验 address.userId 与当前登录用户一致，
 * 防止通过修改请求参数访问他人地址。
 */
@Slf4j
@Service
public class AddressServiceImpl implements AddressService {

    private final AddressMapper addressMapper;

    public AddressServiceImpl(AddressMapper addressMapper) {
        this.addressMapper = addressMapper;
    }

    @Override
    public List<AddressVO> list() {
        Long userId = LoginUserUtil.currentUserId();
        List<Address> addresses = addressMapper.selectList(new LambdaQueryWrapper<Address>()
                .eq(Address::getUserId, userId)
                .orderByDesc(Address::getIsDefault)
                .orderByDesc(Address::getCreatedAt));
        return addresses.stream().map(AddressVO::from).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(AddressDTO dto) {
        Long userId = LoginUserUtil.currentUserId();
        boolean firstAddress = addressMapper.selectCount(
                new LambdaQueryWrapper<Address>().eq(Address::getUserId, userId)) == 0;
        boolean asDefault = dto.getIsDefault() != null && dto.getIsDefault() == 1;
        // 首个地址自动设为默认，避免下单时无可用默认地址
        int isDefault = firstAddress || asDefault ? 1 : 0;
        if (isDefault == 1) {
            clearDefault(userId);
        }

        Address address = new Address();
        address.setUserId(userId);
        fill(address, dto);
        address.setIsDefault(isDefault);
        addressMapper.insert(address);
        return address.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, AddressDTO dto) {
        Long userId = LoginUserUtil.currentUserId();
        Address existing = requireOwnAddress(id, userId);

        if (dto.getIsDefault() != null && dto.getIsDefault() == 1) {
            clearDefault(userId);
        }

        Address update = new Address();
        update.setId(existing.getId());
        fill(update, dto);
        update.setIsDefault(dto.getIsDefault());
        addressMapper.updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Long userId = LoginUserUtil.currentUserId();
        Address existing = requireOwnAddress(id, userId);
        addressMapper.deleteById(id);

        // 删除默认地址后，自动把最近新增的地址补为默认
        if (existing.getIsDefault() != null && existing.getIsDefault() == 1) {
            List<Address> rest = addressMapper.selectList(new LambdaQueryWrapper<Address>()
                    .eq(Address::getUserId, userId)
                    .orderByDesc(Address::getCreatedAt));
            if (!rest.isEmpty()) {
                Address promote = new Address();
                promote.setId(rest.get(0).getId());
                promote.setIsDefault(1);
                addressMapper.updateById(promote);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefault(Long id) {
        Long userId = LoginUserUtil.currentUserId();
        Address existing = requireOwnAddress(id, userId);
        clearDefault(userId);

        Address update = new Address();
        update.setId(existing.getId());
        update.setIsDefault(1);
        addressMapper.updateById(update);
    }

    private void fill(Address address, AddressDTO dto) {
        address.setConsignee(dto.getConsignee().trim());
        address.setCountry(dto.getCountry().trim());
        address.setProvinceState(dto.getProvinceState());
        address.setCity(dto.getCity());
        address.setPostalCode(dto.getPostalCode());
        address.setAddressLine(dto.getAddressLine().trim());
        address.setPhone(dto.getPhone());
    }

    private void clearDefault(Long userId) {
        Address reset = new Address();
        reset.setIsDefault(0);
        addressMapper.update(reset, new LambdaUpdateWrapper<Address>()
                .eq(Address::getUserId, userId)
                .eq(Address::getIsDefault, 1));
    }

    private Address requireOwnAddress(Long id, Long userId) {
        Address address = addressMapper.selectById(id);
        if (address == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "收货地址不存在");
        }
        if (!userId.equals(address.getUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权操作他人的收货地址");
        }
        return address;
    }
}
