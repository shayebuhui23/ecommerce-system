package com.ecommerce.security;

import cn.dev33.satoken.stp.StpInterface;
import com.ecommerce.entity.Role;
import com.ecommerce.entity.User;
import com.ecommerce.mapper.RoleMapper;
import com.ecommerce.mapper.UserMapper;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Sa-Token 权限数据源：根据登录用户查询其角色编码。
 * <p>
 * 本项目采用单商家模式，一个账号对应一个角色，因此返回单元素列表。
 * 若后续需要多角色，只需让 users 与 roles 建立多对多关系并在本类中调整查询。
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;

    public StpInterfaceImpl(UserMapper userMapper, RoleMapper roleMapper) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
    }

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 本项目采用角色级权限控制，暂不细分到按钮级权限
        return Collections.emptyList();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        Long userId = toUserId(loginId);
        if (userId == null) {
            return Collections.emptyList();
        }
        User user = userMapper.selectById(userId);
        if (user == null || user.getRoleId() == null) {
            return Collections.emptyList();
        }
        Role role = roleMapper.selectById(user.getRoleId());
        if (role == null || role.getCode() == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(role.getCode());
    }

    private Long toUserId(Object loginId) {
        if (loginId == null) {
            return null;
        }
        try {
            return Long.valueOf(loginId.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
