package com.ecommerce.exception;

import com.ecommerce.common.ResultCode;
import lombok.Getter;

/**
 * 业务异常。
 * <p>
 * Service 层在业务规则不满足时抛出，由全局异常处理器统一转换为 {@code Result} 响应，
 * 避免在 Controller 中散落 if-else 错误处理。
 */
@Getter
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final ResultCode resultCode;

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.resultCode = resultCode;
    }

    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.resultCode = resultCode;
    }
}
