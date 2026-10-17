package com.ecommerce.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import com.ecommerce.common.ResultCode;
import com.ecommerce.entity.User;
import com.ecommerce.exception.BusinessException;
import com.ecommerce.mapper.UserMapper;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 鉴权配置。
 * <p>
 * 除登录、注册接口外，所有接口默认要求登录；角色权限由 Controller 上的
 * {@code @SaCheckRole} 注解校验（SaInterceptor 默认开启注解鉴权）。
 * 前端菜单隐藏只是体验优化，真正的权限判断全部在后端完成。
 * <p>
 * 每次受保护请求会校验账号状态，被管理员禁用的账号即使持有未过期的令牌也无法继续操作。
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    private static final String[] EXCLUDE_PATHS = {
            "/api/auth/login",
            "/api/auth/register",
            "/error"
    };

    private static final int STATUS_ENABLED = 1;

    private final UserMapper userMapper;

    public SaTokenConfig(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> checkLoginAndStatus()))
                .addPathPatterns("/**")
                .excludePathPatterns(EXCLUDE_PATHS);
    }

    private void checkLoginAndStatus() {
        StpUtil.checkLogin();
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userMapper.selectById(userId);
        if (user == null) {
            StpUtil.logout();
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        if (user.getStatus() == null || user.getStatus() != STATUS_ENABLED) {
            StpUtil.logout();
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }
    }
}
