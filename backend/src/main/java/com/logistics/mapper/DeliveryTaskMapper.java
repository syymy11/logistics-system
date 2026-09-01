// mapper/DeliveryTaskMapper.java
package com.logistics.mapper;

import com.logistics.entity.DeliveryTask;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DeliveryTaskMapper {

        @Select("SELECT * FROM delivery_task WHERE id = #{id}")
        DeliveryTask selectById(Long id);

        @Select("SELECT * FROM delivery_task WHERE status = #{status}")
        List<DeliveryTask> selectByStatus(String status);

        @Select("SELECT * FROM delivery_task WHERE vehicle_id = #{vehicleId}")
        List<DeliveryTask> selectByVehicleId(Long vehicleId);

        @Select("SELECT * FROM delivery_task ORDER BY create_time DESC")
        List<DeliveryTask> selectAll();

        @Insert("INSERT INTO delivery_task(task_no, vehicle_id, total_distance, total_weight, " +
                        "status, create_time) VALUES(#{taskNo}, #{vehicleId}, " +
                        "#{totalDistance}, #{totalWeight}, #{status}, #{createTime})")
        @Options(useGeneratedKeys = true, keyProperty = "id")
        int insert(DeliveryTask task);

        @Update("UPDATE delivery_task SET status=#{status}, start_time=#{startTime}, " +
                        "end_time=#{endTime} WHERE id=#{id}")
        int updateById(DeliveryTask task);

        @Update("UPDATE delivery_task SET status=#{status} WHERE id=#{id}")
        int updateStatus(@Param("id") Long id, @Param("status") String status);

        @Delete("DELETE FROM delivery_task WHERE id = #{id}")
        int deleteById(@Param("id") Long id);
}