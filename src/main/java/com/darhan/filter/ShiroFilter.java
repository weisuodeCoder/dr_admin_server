package com.darhan.filter;

import com.alibaba.fastjson.JSON;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.darhan.entity.Result;
import com.darhan.entity.ResultCode;
import com.darhan.handler.RedisAuthenticationException;
import com.darhan.shiro.ShiroToken;
import com.darhan.utils.JwtUtil;
import com.darhan.utils.RedisUtil;
import io.lettuce.core.RedisException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.ExpiredCredentialsException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @ Program       :  com.ljnt.blog.filter.JWTFilter
 * @ Description   :  自定义jwt过滤器，对token进行处理
 * @ Author        :  lj
 * @ CreateDate    :  2020-2-4 17:28
 */
public class ShiroFilter extends BasicHttpAuthenticationFilter {

    /**
     * 判断是否允许通过
     *
     * @param request
     * @param response
     * @param mappedValue
     * @return
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        System.out.println("isAccessAllowed方法: 判断是否允许");
        try {
            return executeLogin(request, response);
        } catch (Exception e) {
            responseError(request, response, e);
            return false;
        }
    }

    /**
     * 是否进行登录请求
     *
     * @param request
     * @param response
     * @return
     */
    @Override
    protected boolean isLoginAttempt(ServletRequest request, ServletResponse response) {
        System.out.println("isLoginAttempt方法");
        String token = ((HttpServletRequest) request).getHeader("Authorization");
        if (token != null) {
            return true;
        }
        return false;
    }

    /**
     * 创建shiro token
     *
     * @param request
     * @param response
     * @return
     */
    @Override
    protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) {
        System.out.println("createToken方法");
        String jwtToken = ((HttpServletRequest) request).getHeader("Authorization");
        if (jwtToken != null) {
            ShiroToken shiroToken = new ShiroToken(jwtToken);
            return shiroToken;
        }
        return null;
    }

    /**
     * isAccessAllowed为false时调用，验证失败
     *
     * @param request
     * @param response
     * @return
     */
    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) {
        return false;
    }

    /**
     * executeLogin为false时调用，验证失败
     *
     * @param token
     * @param e
     * @param request
     * @param response
     * @return
     */
    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request, ServletResponse response) {
        System.out.println("onLoginFailure: 登录失败..");
        System.out.println(e);
        responseError(request, response, e);
        return false;
    }

    /**
     * shiro验证成功调用
     *
     * @param token
     * @param subject
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @Override
    protected boolean onLoginSuccess(AuthenticationToken token, Subject subject, ServletRequest request, ServletResponse response) throws Exception {
        System.out.println("onLoginSuccess：");
        String jwtToken = (String) token.getPrincipal();
        if (jwtToken != null) {
            try {
                if (JwtUtil.verify(jwtToken)) {
                    //判断Redis是否存在所对应的RefreshToken
                    String id = JwtUtil.getId(jwtToken);
                    Long currentTime = JwtUtil.getCurrentTime(jwtToken);
                    if (RedisUtil.hasKey(id)) {
                        Long currentTimeMillisRedis = (Long) RedisUtil.hget(id, "currentTime");
                        DateFormat df = new SimpleDateFormat("yyyy年MM月dd日HH:mm:ss");
                        System.out.println("RedisTime: "+df.format(new Date(currentTimeMillisRedis))+" === ClientTime: "+df.format(new Date(currentTime)));
                        if (currentTimeMillisRedis.equals(currentTime)) {
                            return true;
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("token验证：");
                if (e instanceof TokenExpiredException) { // 如果是token令牌过期，更新token
                    if (refreshToken(request, response)) {
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    responseError(request, response, new Result(ResultCode.SERVER_ERROR));
                }
            }
        }
        return true;
    }

    /**
     * 拦截器的前置方法，此处进行跨域处理
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        httpServletResponse.setHeader("Access-Control-Allow-Origin", "*");
        httpServletResponse.setHeader("Access-Control-Allow-Methods", "POST,OPTIONS");
        httpServletResponse.setHeader("Access-Control-Allow-Headers", "Origin,X-Requested-With,Content-Type,Accept,Authorization,token");
        if (httpServletRequest.getMethod().equals(RequestMethod.OPTIONS.name())) {
            httpServletResponse.setStatus(HttpStatus.OK.value());
            return true;
        }
        //如果不带token，不去验证shiro
        if (!isLoginAttempt(request, response)) {
            responseError(request, response, new Result(ResultCode.UNAUTHENTICATED));
            return false;
        }
        return super.preHandle(request, response);
    }


    /**
     * 刷新AccessToken，进行判断RefreshToken是否过期，未过期就返回新的AccessToken且继续正常访问
     *
     * @param request
     * @param response
     * @return
     */
    private boolean refreshToken(ServletRequest request, ServletResponse response) {
        System.out.println("refreshToken... 更新token");
        String token = ((HttpServletRequest) request).getHeader("Authorization");
        String id = JwtUtil.getId(token);
        Long currentTime = JwtUtil.getCurrentTime(token);
        // 判断Redis中RefreshToken是否存在
        if (RedisUtil.hasKey(id)) {
            // Redis中RefreshToken还存在，获取RefreshToken的时间戳
            Long currentTimeMillisRedis = (Long) RedisUtil.hget(id, "currentTime");
            DateFormat df = new SimpleDateFormat("yyyy年MM月dd日HH:mm:ss");

            System.out.println(df.format(new Date(currentTimeMillisRedis)) + "=====" + df.format(new Date(currentTime)));
            // 获取当前AccessToken中的时间戳，与RefreshToken的时间戳对比，如果当前时间戳一致，进行AccessToken刷新
            if (currentTimeMillisRedis.equals(currentTime)) {
                // 获取当前最新时间戳
                System.out.println("两个时间相同，更新时间");
                Long currentTimeMillis = System.currentTimeMillis();
                System.out.println(currentTimeMillis);
                RedisUtil.hset(id, "currentTime", currentTimeMillis, JwtUtil.REFRESH_EXPIRE_TIME);
                // 刷新AccessToken，设置时间戳为当前最新时间戳
                token = JwtUtil.createJwt(id, currentTimeMillis);
                HttpServletResponse httpServletResponse = (HttpServletResponse) response;
                httpServletResponse.setHeader("Authorization", token);
                httpServletResponse.setHeader("Access-Control-Expose-Headers", "Authorization");
                return true;
            } else {
                responseError(request, response, new Result(ResultCode.OVERDUE_ERROR));
            }
        }
        return false;
    }

    // 此处发生错误统一处理响应数据,指定result
    private void responseError(ServletRequest request, ServletResponse response, Result result) {
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        httpServletResponse.setContentType("application/json;charset=UTF-8");
        httpServletResponse.setCharacterEncoding("UTF-8");
        try {
            String s = JSON.toJSONString(result);
            // 不能用getWriter()方法，response同时只能使用getOutputStream和getWriter的一个方法，解决方法是将其改为getOutputStream()
            httpServletResponse.getOutputStream().write(s.getBytes("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            this.sendChallenge(request, httpServletResponse);
        }
    }

    // 此处发生错误统一处理响应数据,用exception处理
    private void responseError(ServletRequest request, ServletResponse response, Exception e) {
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        httpServletResponse.setContentType("application/json;charset=UTF-8");
        httpServletResponse.setCharacterEncoding("UTF-8");

        try {
            String json = ""; // 返回的json数据
            if (e instanceof ExpiredCredentialsException) { // 登录已过期
                json = JSON.toJSONString(new Result(ResultCode.OVERDUE_ERROR));
            } else if (e instanceof RedisAuthenticationException) { // 处理Redis异常
                if (e.getCause() instanceof RedisConnectionFailureException) {
                    json = JSON.toJSONString(new Result(ResultCode.REDIS_CONNECTION_ERROR));
                } else if (e.getCause() instanceof RedisException) {
                    json = JSON.toJSONString(new Result(ResultCode.REDIS_ERROR));
                } else {
                    json = JSON.toJSONString(new Result(ResultCode.REDIS_ERROR));
                }
                e.printStackTrace(); // redis抛异常
            } else { // 处理未知异常
                json = JSON.toJSONString(new Result(ResultCode.SERVER_ERROR));
                e.printStackTrace(); // 抛未知异常
            }
            httpServletResponse.getOutputStream().write(json.getBytes("UTF-8"));
        } catch (Exception io) {
            io.printStackTrace();
        } finally {
            this.sendChallenge(request, httpServletResponse);
        }
    }
}