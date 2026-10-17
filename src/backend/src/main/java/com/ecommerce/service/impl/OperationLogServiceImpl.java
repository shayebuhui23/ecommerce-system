package com.ecommerce.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.common.PageResult;
import com.ecommerce.entity.OperationLog;
import com.ecommerce.entity.User;
import com.ecommerce.mapper.OperationLogMapper;
import com.ecommerce.mapper.UserMapper;
import com.ecommerce.service.OperationLogService;
import com.ecommerce.vo.OperationLogVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 操作日志实现。
 * <p>
 * 采用业务代码显式记录的方式，只在订单、支付、发货、退款等关键节点写入，
 * 避免无差别记录全部请求导致日志表膨胀。
 */
@Slf4j
@Service
public class OperationLogServiceImpl implements OperationLogService {

    private final OperationLogMapper operationLogMapper;
    private final UserMapper userMapper;

    public OperationLogServiceImpl(OperationLogMapper operationLogMapper, UserMapper userMapper) {
        this.operationLogMapper = operationLogMapper;
        this.userMapper = userMapper;
    }

    @Override
    public void record(String module, String action, String detail) {
        Long userId = StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : null;
        String username = null;
        if (userId != null) {
            User user = userMapper.selectById(userId);
            username = user == null ? null : user.getUsername();
        }

        OperationLog entity = new OperationLog();
        entity.setUserId(userId);
        entity.setUsername(username);
        entity.setModule(module);
        entity.setAction(action);
        entity.setParams(detail);
        operationLogMapper.insert(entity);
    }

    @Override
    public PageResult<OperationLogVO> page(long pageNum, long pageSize, String module) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(module)) {
            wrapper.eq(OperationLog::getModule, module.trim());
        }
        wrapper.orderByDesc(OperationLog::getCreatedAt);
        Page<OperationLog> page = operationLogMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return PageResult.of(page, OperationLogVO::from);
    }
}
