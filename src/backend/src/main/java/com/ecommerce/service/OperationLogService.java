package com.ecommerce.service;

import com.ecommerce.common.PageResult;
import com.ecommerce.vo.OperationLogVO;

/**
 * 关键操作审计日志服务。
 */
public interface OperationLogService {

    /**
     * 记录当前登录用户的关键操作。
     */
    void record(String module, String action, String detail);

    /**
     * 管理员分页查询操作日志。
     */
    PageResult<OperationLogVO> page(long pageNum, long pageSize, String module);
}
