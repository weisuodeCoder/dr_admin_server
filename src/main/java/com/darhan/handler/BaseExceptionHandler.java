package com.darhan.handler;

import com.darhan.entity.Result;
import com.darhan.entity.ResultCode;
import lombok.extern.slf4j.Slf4j;

import org.apache.shiro.ShiroException;
import org.apache.shiro.authz.AuthorizationException;
import org.python.core.PyException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * 自定义公共异常处理器
 * 1.声明异常处理器:@ControllerAdvice
 * 2.统一处理异常
 */
@Slf4j
@ControllerAdvice
public class BaseExceptionHandler {
    @ExceptionHandler(ArithmeticException.class)
    @ResponseBody
    public Result zeroError(HttpServletRequest req, HttpServletResponse res, ArithmeticException e) {
        Result result = new Result(ResultCode.FAIL, e.getMessage());
        printLog(e, "除零异常");
        return result;
    }

    @ExceptionHandler(value = ShiroException.class)
    @ResponseBody
    public Result error(HttpServletRequest request, HttpServletResponse response, ShiroException e) {
        System.out.println("*** AuthorizationException");
        return new Result(ResultCode.UNAUTHORISE);
    }

    @ExceptionHandler(Exception.class) // 指定处理什么样的异常
    @ResponseBody
    public Result error(HttpServletRequest req, HttpServletResponse res, Exception e) {
        if(e instanceof ShiroException) {
            System.out.println("测试通过了");
        }
        if(e.getClass() == CustomException.class) {
            CustomException ce = (CustomException) e;
            Result result = new Result(ce.getResultCode());
            printLog(e, "自定义异常");
            return result;
        }else {
            Result result = new Result(ResultCode.SERVER_ERROR, e.getMessage());
            printLog(e, "系统异常");
            return result;
        }
    }

    @ExceptionHandler(PyException.class)
    @ResponseBody
    public Result pyError(PyException e) {
        String message = e.getMessage();
        log.warn(message);

        if (message.indexOf("EmptyResultDataAccessException") >= 0) {
            printLog(e, "空数据异常");
            return new Result(ResultCode.DATA_NOT_FOUND, e.getMessage());
        } else if (message.indexOf("BadSqlGrammarException") >= 0) {
            printLog(e, "sql语法错误");
            return new Result(ResultCode.BAD_SQL, e.getMessage());
        } else if(message.indexOf("IOError")>=0) {
            printLog(e, "脚本路径异常");
            return new Result(ResultCode.API_NOT_FOUND, e.getMessage());
        } else if(message.indexOf("TypeError")>=0) {
            printLog(e, "jython类型错误");
            return new Result(ResultCode.API_TYPE_ERROR,e.getMessage());
        }else {
            printLog(e, "接口——系统异常");
            return new Result(ResultCode.SERVER_ERROR, e.getMessage());
        }
    }

    // 统一处理日志
    private void printLog(Exception e, String message) {
        log.error("***** " + message + " *****");
        log.error("" + e);
        e.printStackTrace();
    }
}
