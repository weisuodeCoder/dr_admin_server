package com.darhan.entity;

/**
 * 公共的返回码
 * 返回码code：
 * 成功：10000
 * 失败：10001
 * 未登录：10002
 * 未授权：10003
 * id不存在：10004
 * 脚本不存在：10005
 * 参数异常：10006
 * 抛出异常：99999
 * <p>
 * 账号或用户名不存在：20001
 * 密码错误: 20002
 * 账号已存在：20003
 * 用户名已存在：20004
 * 该账号已被冻结：20005
 * 登录已过期： 20006
 * 未分配角色： 20007
 * 非法登录： 20010
 * <p>
 * redis服务异常：30001
 * redis服务链接异常：30002
 */
public enum ResultCode {
    SUCCESS(true, 10000, "操作成功！"),
    // 系统错误返回码
    FAIL(false, 10001, "操作失败！"),
    UNAUTHENTICATED(false, 10002, "未登录！"),
    UNAUTHORISE(false, 10003, "权限不足！"),
    DATA_NOT_FOUND(false, 10004, "id不存在！"),
    API_NOT_FOUND(false, 10005, "脚本不存在！"),
    BAD_SQL(false, 10005, "sql语法错误！"),
    API_TYPE_ERROR(false, 10006, "参数异常，请检查参数格式！"),
    // 账号错误返回码
    USER_NOT_FOUND(false, 20001, "账号或用户名不存在！"),
    USER_PASSWOED_ERROR(false, 20002, "密码错误！"),
    USER_FREEZE(false, 20005, "该账号已被冻结！"),
    OVERDUE_ERROR(false, 20006, "登录已过期,请重新登录！"),
    UNDISTRIBUTED_ERROR(false, 20007, "该账号未分配角色，请联系管理员！"),
    ILLEGAL_ERROR(false, 20010, "非法登录！"),
    // Redis服务错误返回码
    REDIS_ERROR(false, 30001, "Redis服务发生异常，请联系管理员！"),
    REDIS_CONNECTION_ERROR(false, 30002, "服务器与Redis连接失败，请联系管理员！"),
    // 系统错误
    CUSTOM_ERROR(false, 9997, "未知异常，请联系管理员！"),
    SERVER_ERROR(false, 9999, "抱歉，系统繁忙，请稍后重试！");

    // 操作是否成功
    private boolean success;
    // 操作代码
    private int code;
    // 提示信息
    private String message;

    ResultCode(boolean success, int code, String message) {
        this.success = success;
        this.code = code;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
