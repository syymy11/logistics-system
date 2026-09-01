package com.logistics.mapper;

import com.logistics.entity.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM users WHERE username = #{username}")
    User selectByUsername(String username);

    @Select("SELECT * FROM users WHERE id = #{id}")
    User selectById(Long id);

    @Insert("INSERT INTO users(username, password, real_name, role, status, create_time) " +
            "VALUES(#{username}, #{password}, #{realName}, #{role}, #{status}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Update("UPDATE users SET password = #{password}, real_name = #{realName}, " +
            "role = #{role}, status = #{status}, update_time = NOW() WHERE id = #{id}")
    int update(User user);

    @Delete("DELETE FROM users WHERE id = #{id}")
    int deleteById(Long id);
}