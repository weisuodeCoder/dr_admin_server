package com.darhan.controller;

import com.darhan.config.FileConfig;
import com.darhan.config.ParamsConfig;
import com.darhan.entity.Result;
import com.darhan.entity.ResultCode;
import com.darhan.service.CoreService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PreDestroy;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.Future;

@CrossOrigin // 支持跨域
@RestController
@RequestMapping("/core")
public class CoreController {
    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    FileConfig fileConfig;
    @Autowired
    ParamsConfig paramsConfig;

    private final CoreService coreService;

    public CoreController() {
        int poolSize = 10; // 设置线程池大小
        coreService = new CoreService(poolSize);
    }

    @RequestMapping("/{project}/{plate}/{module}/{script}")
    @ResponseBody
    public Result getRequest(@RequestBody Map params,
                             ServletRequest request, ServletResponse response) throws Exception {
        // shiro验证，若权限不足则返回
        Subject subject = SecurityUtils.getSubject();
        HttpServletRequest req = (HttpServletRequest) request;
        String url = req.getRequestURI();
        System.out.println(url);
        if(subject.hasRole("sys_admin") || subject.isPermitted(url) || url.contains("/core/authc/") || url.contains("/core/anon/")) {
            url = url.substring(5);
            String path = fileConfig.getApipath() + url + ".py";
            Future<Result> future = coreService.setApiParams(path, params, jdbcTemplate, request, response);
            return future.get(); // 阻塞等待任务执行完成并返回结果
        }
        return new Result(ResultCode.UNAUTHORISE);
    }

    @PreDestroy
    public void cleanup() {
        coreService.shutdown();
    }
}