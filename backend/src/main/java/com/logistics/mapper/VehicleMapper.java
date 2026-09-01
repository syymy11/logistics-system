// mapper/VehicleMapper.java
package com.logistics.mapper;

import com.logistics.entity.Vehicle;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface VehicleMapper {

    @Select("SELECT * FROM vehicle WHERE id = #{id}")
    Vehicle selectById(Long id);

    @Select("SELECT * FROM vehicle WHERE status = #{status}")
    List<Vehicle> selectByStatus(String status);

    @Select("SELECT * FROM vehicle")
    List<Vehicle> selectAll();

    @Update("UPDATE vehicle SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    @Insert("INSERT INTO vehicle(plate_number, vehicle_type, vehicle_category, capacity, status, current_location, longitude, latitude) " +
            "VALUES(#{plateNumber}, #{vehicleType}, #{vehicleCategory}, #{capacity}, #{status}, #{currentLocation}, #{longitude}, #{latitude})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Vehicle vehicle);

    @Update("UPDATE vehicle SET status=#{status} WHERE id=#{id}")
    int updateById(Vehicle vehicle);

    @Update("UPDATE vehicle SET " +
            "plate_number=#{plateNumber}, " +
            "vehicle_type=#{vehicleType}, " +
            "vehicle_category=#{vehicleCategory}, " +
            "capacity=#{capacity}, " +
            "status=#{status}, " +
            "current_location=#{currentLocation}, " +
            "longitude=#{longitude}, " +
            "latitude=#{latitude} " +
            "WHERE id=#{id}")
    int updateFullById(Vehicle vehicle);

    @Delete("DELETE FROM vehicle WHERE id = #{id}")
    int deleteById(Long id);
}