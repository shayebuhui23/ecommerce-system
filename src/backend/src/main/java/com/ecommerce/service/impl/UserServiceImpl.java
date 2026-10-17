package com.ecommerce.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.common.PageResult;
import com.ecommerce.common.ResultCode;
import com.ecommerce.common.RoleCode;
import com.ecommerce.dto.LoginDTO;
import com.ecommerce.dto.ProfileDTO;
import com.ecommerce.dto.RegisterDTO;
import com.ecommerce.dto.UserStatusDTO;
import com.ecommerce.entity.Role;
import com.ecommerce.entity.User;
import com.ecommerce.exception.BusinessException;
import com.ecommerce.mapper.RoleMapper;
import com.ecommerce.mapper.UserMapper;
import com.ecommerce.security.LoginUserUtil;
import com.ecommerce.service.UserService;
import com.ecommerce.vo.LoginVO;
import com.ecommerce.vo.UserInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 用户与认证服务实现。
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private static final int STATUS_ENABLED = 1;

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserMapper userMapper, RoleMapper roleMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterDTO dto) {
        // 用户名与邮箱均为唯一约束，先做友好校验，避免直接抛数据库异常
        if (existsByUsername(dto.getUsername())) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }
        if (existsByEmail(dto.getEmail())) {
            throw new BusinessException(ResultCode.EMAIL_EXISTS);
        }

        Role customerRole = roleMapper.selectByCode(RoleCode.CUSTOMER);
        if (customerRole == null) {
            throw new BusinessException(ResultCode.ROLE_NOT_CONFIGURED);
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        // 密码只保存 BCrypt 哈希，永不落库明文
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(StringUtils.hasText(dto.getNickname()) ? dto.getNickname() : dto.getUsername());
        user.setCountry(dto.getCountry());
        user.setRoleId(customerRole.getId());
        user.setStatus(STATUS_ENABLED);
        userMapper.insert(user);
        log.info("新用户注册成功：username={}, role={}", user.getUsername(), customerRole.getCode());
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        User user = findByAccount(dto.getAccount());
        // 账号不存在与密码错误返回同一提示，避免暴露账号是否存在
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.ACCOUNT_OR_PASSWORD_ERROR);
        }
        if (user.getStatus() == null || user.getStatus() != STATUS_ENABLED) {
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }

        StpUtil.login(user.getId());

        Role role = user.getRoleId() == null ? null : roleMapper.selectById(user.getRoleId());
        LoginVO vo = new LoginVO();
        vo.setToken(StpUtil.getTokenValue());
        vo.setTokenName(StpUtil.getTokenName());
        vo.setUserInfo(UserInfoVO.from(user, role));
        log.info("用户登录成功：username={}", user.getUsername());
        return vo;
    }

    @Override
    public void logout() {
        StpUtil.logout();
    }

    @Override
    public UserInfoVO currentUser() {
        Long userId = LoginUserUtil.currentUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        Role role = user.getRoleId() == null ? null : roleMapper.selectById(user.getRoleId());
        return UserInfoVO.from(user, role);
    }

    @Override
    public PageResult<UserInfoVO> pageUsers(long pageNum, long pageSize, String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            String like = keyword.trim();
            wrapper.and(w -> w.like(User::getUsername, like)
                    .or().like(User::getNickname, like)
                    .or().like(User::getEmail, like));
        }
        wrapper.orderByDesc(User::getCreatedAt);

        Page<User> page = userMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        Map<Long, Role> roleMap = roleMapper.selectList(null).stream()
                .collect(Collectors.toMap(Role::getId, Function.identity()));
        return PageResult.of(page, user -> UserInfoVO.from(user, roleMap.get(user.getRoleId())));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(UserStatusDTO dto) {
        User target = userMapper.selectById(dto.getUserId());
        if (target == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        // 禁止管理员把自己禁用，避免系统失去唯一管理入口
        if (dto.getStatus() == 0 && dto.getUserId().equals(LoginUserUtil.currentUserId())) {
            throw new BusinessException(ResultCode.CANNOT_DISABLE_SELF);
        }

        User update = new User();
        update.setId(target.getId());
        update.setStatus(dto.getStatus());
        userMapper.updateById(update);
        log.info("管理员变更用户状态：userId={}, status={}", target.getId(), dto.getStatus());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserInfoVO updateProfile(ProfileDTO dto) {
        Long userId = LoginUserUtil.currentUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        User update = new User();
        update.setId(userId);
        update.setNickname(dto.getNickname());
        update.setPhone(dto.getPhone());
        update.setCountry(dto.getCountry());
        userMapper.updateById(update);
        log.info("用户修改个人资料：userId={}", userId);
        return currentUser();
    }

    private boolean existsByUsername(String username) {
        return userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, username)) > 0;
    }

    private boolean existsByEmail(String email) {
        return userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getEmail, email)) > 0;
    }

    private User findByAccount(String account) {
        List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>()
                .and(w -> w.eq(User::getUsername, account).or().eq(User::getEmail, account))
                .last("LIMIT 1"));
        return users.isEmpty() ? null : users.get(0);
    }
}
