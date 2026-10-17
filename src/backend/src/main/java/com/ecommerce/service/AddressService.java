package com.ecommerce.service;

import com.ecommerce.dto.AddressDTO;
import com.ecommerce.vo.AddressVO;

import java.util.List;

/**
 * 收货地址服务，所有操作均按当前登录用户隔离。
 */
public interface AddressService {

    List<AddressVO> list();

    Long create(AddressDTO dto);

    void update(Long id, AddressDTO dto);

    void delete(Long id);

    void setDefault(Long id);
}
