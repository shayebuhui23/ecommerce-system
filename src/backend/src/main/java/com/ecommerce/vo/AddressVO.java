package com.ecommerce.vo;

import com.ecommerce.entity.Address;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 收货地址响应。
 */
@Data
public class AddressVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String consignee;
    private String country;
    private String provinceState;
    private String city;
    private String postalCode;
    private String addressLine;
    private String phone;
    private Integer isDefault;
    private LocalDateTime createdAt;

    public static AddressVO from(Address address) {
        AddressVO vo = new AddressVO();
        vo.setId(address.getId());
        vo.setConsignee(address.getConsignee());
        vo.setCountry(address.getCountry());
        vo.setProvinceState(address.getProvinceState());
        vo.setCity(address.getCity());
        vo.setPostalCode(address.getPostalCode());
        vo.setAddressLine(address.getAddressLine());
        vo.setPhone(address.getPhone());
        vo.setIsDefault(address.getIsDefault());
        vo.setCreatedAt(address.getCreatedAt());
        return vo;
    }
}
