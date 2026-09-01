// mapper/TaskOrderMapper.java
package com.logistics.mapper;

import com.logistics.entity.TaskOrder;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TaskOrderMapper {

    @Insert("INSERT INTO task_order(task_id, order_id, sequence) VALUES(#{taskId}, #{orderId}, #{sequence})")
    int insert(TaskOrder taskOrder);

    @Select("SELECT * FROM task_order WHERE task_id = #{taskId} ORDER BY sequence")
    List<TaskOrder> selectByTaskIdOrderBySequence(@Param("taskId") Long taskId);

    @Select("SELECT * FROM task_order WHERE order_id = #{orderId}")
    TaskOrder selectByOrderId(@Param("orderId") Long orderId);

    @Delete("DELETE FROM task_order WHERE order_id = #{orderId}")
    int deleteByOrderId(@Param("orderId") Long orderId);
}