package com.logistics.controller;

import com.logistics.entity.User;
import com.logistics.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();
        String username = params.get("username");
        String password = params.get("password");

        logger.info("登录请求: username={}", username);

        User user = userService.login(username, password);
        if (user == null) {
            result.put("code", 401);
            result.put("message", "用户名或密码错误");
            return result;
        }

        Map<String, Object> userData = new HashMap<>();
        userData.put("id", user.getId());
        userData.put("username", user.getUsername());
        userData.put("realName", user.getRealName());
        userData.put("role", user.getRole());

        result.put("code", 200);
        result.put("data", userData);
        result.put("message", "登录成功");
        logger.info("用户登录成功: {}", username);
        return result;
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody User user) {
        Map<String, Object> result = new HashMap<>();
        logger.info("注册请求: username={}", user.getUsername());

        boolean success = userService.register(user);
        if (!success) {
            result.put("code", 400);
            result.put("message", "用户名已存在");
            return result;
        }

        result.put("code", 200);
        result.put("message", "注册成功");
        return result;
    }

    @GetMapping("/info")
    public Map<String, Object> getUserInfo(@RequestParam Long id) {
        Map<String, Object> result = new HashMap<>();
        User user = userService.getUserById(id);
        if (user == null) {
            result.put("code", 404);
            result.put("message", "用户不存在");
            return result;
        }

        Map<String, Object> userData = new HashMap<>();
        userData.put("id", user.getId());
        userData.put("username", user.getUsername());
        userData.put("realName", user.getRealName());
        userData.put("role", user.getRole());

        result.put("code", 200);
        result.put("data", userData);
        return result;
    }

    @PostMapping("/logout")
    public Map<String, Object> logout() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "退出成功");
        return result;
    }
}