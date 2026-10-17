package com.ecommerce.vo;

import com.ecommerce.entity.Role;
import com.ecommerce.entity.User;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 当前用户信息响应，不含密码等敏感字段。
 */
@Data
public class UserInfoVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String username;
    private String email;
    private String nickname;
    private String phone;
    private String country;
    private String roleCode;
    private String roleName;
    /** 1 启用，0 禁用 */
    private Integer status;
    private LocalDateTime createdAt;

    public static UserInfoVO from(User user, Role role) {
        UserInfoVO vo = new UserInfoVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setEmail(user.getEmail());
        vo.setNickname(user.getNickname());
        vo.setPhone(user.getPhone());
        vo.setCountry(user.getCountry());
        vo.setStatus(user.getStatus());
        vo.setCreatedAt(user.getCreatedAt());
        if (role != null) {
            vo.setRoleCode(role.getCode());
            vo.setRoleName(role.getName());
        }
        return vo;
    }
}
