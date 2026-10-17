package com.ecommerce.service;

import com.ecommerce.common.PageResult;
import com.ecommerce.dto.LoginDTO;
import com.ecommerce.dto.RegisterDTO;
import com.ecommerce.dto.UserStatusDTO;
import com.ecommerce.vo.LoginVO;
import com.ecommerce.vo.UserInfoVO;

/**
 * 用户与认证服务。
 */
public interface UserService {

    /**
     * 注册普通客户账号，角色由服务端固定为 CUSTOMER。
     */
    void register(RegisterDTO dto);

    /**
     * 登录并签发令牌。
     */
    LoginVO login(LoginDTO dto);

    /**
     * 注销当前登录态。
     */
    void logout();

    /**
     * 查询当前登录用户信息（含角色）。
     */
    UserInfoVO currentUser();

    /**
     * 管理员分页查询用户。
     */
    PageResult<UserInfoVO> pageUsers(long pageNum, long pageSize, String keyword);

    /**
     * 管理员启用/禁用用户。
     */
    void updateStatus(UserStatusDTO dto);
}
