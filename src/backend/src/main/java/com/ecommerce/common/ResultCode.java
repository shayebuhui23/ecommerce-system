package com.ecommerce.common;

/**
 * 统一业务错误码。
 * <p>
 * 约定：1000 以下复用 HTTP 语义码，1000 以上为业务细分码；
 * HTTP 状态码由全局异常处理器根据此枚举映射，前端只需读取响应体中的 code / message。
 */
public enum ResultCode {

    SUCCESS(200, "操作成功"),

    BAD_REQUEST(400, "请求参数有误"),
    UNAUTHORIZED(401, "登录状态无效，请重新登录"),
    FORBIDDEN(403, "没有权限执行该操作"),
    NOT_FOUND(404, "请求的资源不存在"),
    METHOD_NOT_ALLOWED(405, "不支持的请求方法"),
    INTERNAL_ERROR(500, "系统繁忙，请稍后再试"),

    BUSINESS_ERROR(1000, "业务处理失败"),

    USERNAME_EXISTS(1001, "该用户名已被注册"),
    EMAIL_EXISTS(1002, "该邮箱已被注册"),
    ACCOUNT_OR_PASSWORD_ERROR(1003, "账号或密码错误"),
    ACCOUNT_DISABLED(1004, "账号已被禁用，请联系系统管理员"),
    ROLE_NOT_CONFIGURED(1005, "角色配置缺失，请联系系统管理员"),
    CANNOT_DISABLE_SELF(1006, "不能禁用当前登录的管理员账号");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
