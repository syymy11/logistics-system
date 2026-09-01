package com.logistics.mapper;

import com.logistics.entity.Warehouse;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface WarehouseMapper {

    @Select("SELECT * FROM warehouse WHERE id = #{id}")
    Warehouse selectById(Long id);

    @Select("SELECT * FROM warehouse WHERE status = 'active'")
    List<Warehouse> selectAllActive();

    @Select("SELECT * FROM warehouse WHERE status = 'active' ORDER BY id LIMIT 1")
    Warehouse selectDefault();

    @Insert("INSERT INTO warehouse(name, address, longitude, latitude, status) " +
            "VALUES(#{name}, #{address}, #{longitude}, #{latitude}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Warehouse warehouse);

    @Update("UPDATE warehouse SET name = #{name}, address = #{address}, " +
            "longitude = #{longitude}, latitude = #{latitude}, status = #{status} WHERE id = #{id}")
    int update(Warehouse warehouse);

    @Delete("DELETE FROM warehouse WHERE id = #{id}")
    int deleteById(Long id);
}
