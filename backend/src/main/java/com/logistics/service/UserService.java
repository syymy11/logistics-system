package com.logistics.service;

import com.logistics.entity.User;
import com.logistics.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserMapper userMapper;

    public User login(String username, String password) {
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            logger.warn("用户不存在: {}", username);
            return null;
        }
        if (!user.getPassword().equals(password)) {
            logger.warn("密码错误: {}", username);
            return null;
        }
        if (!"active".equals(user.getStatus())) {
            logger.warn("用户已禁用: {}", username);
            return null;
        }
        logger.info("用户登录成功: {}", username);
        return user;
    }

    public User getUserById(Long id) {
        return userMapper.selectById(id);
    }

    public User getUserByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    public boolean register(User user) {
        User existing = userMapper.selectByUsername(user.getUsername());
        if (existing != null) {
            logger.warn("用户名已存在: {}", user.getUsername());
            return false;
        }
        user.setStatus("active");
        user.setRole("operator");
        int result = userMapper.insert(user);
        return result > 0;
    }

    public boolean updateUser(User user) {
        int result = userMapper.update(user);
        return result > 0;
    }
}