package com.logistics.mapper;

import com.logistics.entity.Order;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface OrderMapper {

        // 根据ID查询订单
        @Select("SELECT * FROM orders WHERE id = #{id}")
        Order selectById(Long id);

        // 根据状态查询订单
        @Select("SELECT * FROM orders WHERE status = #{status}")
        List<Order> selectByStatus(String status);

        // 查询所有订单（不分状态）
        @Select("SELECT * FROM orders ORDER BY create_time DESC LIMIT #{offset}, #{size}")
        List<Order> selectAll(@Param("offset") int offset, @Param("size") int size);

        // 分页查询订单
        @Select("SELECT * FROM orders WHERE status = #{status} " +
                        "ORDER BY create_time DESC LIMIT #{offset}, #{size}")
        List<Order> selectByStatusAndPage(@Param("status") String status,
                        @Param("offset") int offset,
                        @Param("size") int size);

        // 统计订单总数
        @Select("SELECT COUNT(*) FROM orders")
        int countAll();

        // 统计订单数量
        @Select("SELECT COUNT(*) FROM orders WHERE status = #{status}")
        int countByStatus(@Param("status") String status);

        // 批量查询订单（根据ID列表）

        // 插入订单
        @Insert("INSERT INTO orders(order_no, customer_name, customer_phone, customer_address, " +
                        "longitude, latitude, weight, status, create_time) VALUES(" +
                        "#{orderNo}, #{customerName}, #{customerPhone}, #{customerAddress}, " +
                        "#{longitude}, #{latitude}, #{weight}, #{status}, #{createTime})")
        @Options(useGeneratedKeys = true, keyProperty = "id")
        int insert(Order order);

        // 更新订单
        @Update("UPDATE orders SET status = #{status}, assign_time = #{assignTime}, " +
                        "delivery_time = #{deliveryTime} WHERE id = #{id}")
        int updateById(Order order);

        // 删除订单
        @Delete("DELETE FROM orders WHERE id = #{id}")
        int deleteById(Long id);
}