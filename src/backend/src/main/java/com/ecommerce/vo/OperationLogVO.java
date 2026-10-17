package com.ecommerce.vo;

import com.ecommerce.entity.OperationLog;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志响应。
 */
@Data
public class OperationLogVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private String username;
    private String module;
    private String action;
    private String params;
    private LocalDateTime createdAt;

    public static OperationLogVO from(OperationLog log) {
        OperationLogVO vo = new OperationLogVO();
        vo.setId(log.getId());
        vo.setUserId(log.getUserId());
        vo.setUsername(log.getUsername());
        vo.setModule(log.getModule());
        vo.setAction(log.getAction());
        vo.setParams(log.getParams());
        vo.setCreatedAt(log.getCreatedAt());
        return vo;
    }
}
