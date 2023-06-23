package com.darhan.shiro;


import com.darhan.handler.RedisAuthenticationException;
import com.darhan.utils.JwtUtil;
import com.darhan.utils.RedisUtil;
import io.lettuce.core.RedisException;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 自定义Realm，实现Shiro认证
 */
@Component
public class CustomRealm extends AuthorizingRealm {

    private boolean status;

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof ShiroToken;
    }

    /**
     * 用户授权
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        System.out.println("用户授权");
        String id = JwtUtil.getId(principalCollection.toString());
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        if (RedisUtil.hasKey(id)) {
            Set<String> path = (Set<String>) RedisUtil.hget(id, "apis");
            info.setStringPermissions(path);
            // 判断管理员权限
            Map<String, Object> map = (Map<String, Object>) RedisUtil.hget(id, "user");
            if (Integer.parseInt(String.valueOf(map.get("level"))) == 1) {
                Set<String> role = new HashSet<>();
                role.add("sys_admin");
                info.setRoles(role);
            }
        }
        return info;
    }

    /**
     * 用户身份认证
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        System.out.println("用户身份认证");
        String token = (String) authenticationToken.getCredentials();
        String id = JwtUtil.getId(token);
        boolean tempStatus = false;
        try {
            tempStatus = RedisUtil.hasKey(id);
        } catch (Exception e) {
            if (e instanceof RedisException) {
                throw new RedisAuthenticationException("Redis错误", e);
            } else if (e instanceof RedisConnectionFailureException) {
                throw new RedisAuthenticationException("Redis链接失败", e);
            } else {
                throw new AuthenticationException("未知异常",e);
            }
        }
        if (!tempStatus) {
            throw new ExpiredCredentialsException("登录已过期！");
        }
        return new SimpleAuthenticationInfo(token, token, "CustomRealm");
    }
}
