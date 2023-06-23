package com.darhan.handler;

import org.apache.shiro.authc.AuthenticationException;

// shiro统一处理RedisException
public class RedisAuthenticationException extends AuthenticationException {
    public RedisAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}