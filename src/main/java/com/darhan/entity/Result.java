package com.darhan.entity;


import lombok.Data;

import java.util.Map;

/**
 * 数据相应对象
 * {
 * success: 是否成功
 * code   : 返回码
 * message: 返回信息
 * data   : {
 * 返回数据
 * }
 * }
 */
@Data
public class Result {
    private boolean success; // 是否成功
    private Integer code; // 返回码
    private String message; // 返回信息
    private Object data; // 返回数据

    public Result() {}

    public Result(ResultCode code) {
        this.success = code.isSuccess();
        this.code = code.getCode();
        this.message = code.getMessage();
    }

    public Result(ResultCode code, Object data) {
        this.success = code.isSuccess();
        this.code = code.getCode();
        this.message = code.getMessage();
        this.data = data;
    }

    public Result(boolean success, Integer code, String message) {
        this.success = success;
        this.code = code;
        this.message = message;
    }

    public Result(Integer count) {
        this.success = true;
        this.code = 10000;
        this.message = "操作成功！影响了"+count+"行数据...";
        this.data = count;
    }

    public Result(String message) {
        this.success = false;
        this.code = 9998;
        this.message = message;
    }

    public void setResult(ResultCode code) {
        this.success = code.isSuccess();
        this.code = code.getCode();
        this.message = code.getMessage();
    }

    public void setResult(ResultCode code, Object data) {
        this.success = code.isSuccess();
        this.code = code.getCode();
        this.message = code.getMessage();
        this.data = data;
    }

    // 成功
    public static Result SUCCESS() {
        return new Result(ResultCode.SUCCESS);
    }

    // 系统错误
    public static Result ERROR() {
        return new Result(ResultCode.SERVER_ERROR);
    }

    // 失败
    public static Result FAIL() {
        return new Result(ResultCode.FAIL);
    }

    // 未登录
    public static Result UNAUTHENTICATED() {
        return new Result(ResultCode.UNAUTHENTICATED);
    }

    // 权限不足
    public static Result UNAUTHORISE() {
        return new Result(ResultCode.UNAUTHORISE);
    }
}
