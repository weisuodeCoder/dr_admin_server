package com.darhan.controller;

import com.darhan.entity.Result;
import com.darhan.service.UserService;
import com.darhan.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@CrossOrigin // 解决跨域问题
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    UserService userService;

    @RequestMapping("/login")
    @ResponseBody
    public Result login(@RequestBody Map params, HttpServletResponse response) {
        return userService.login(params, response);
    }

    @RequestMapping("/create")
    @ResponseBody
    public Result create(@RequestBody Map params) {
        return userService.create(params);
    }
}
