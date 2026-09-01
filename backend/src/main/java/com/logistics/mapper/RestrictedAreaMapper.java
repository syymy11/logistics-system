package com.logistics.mapper;

import com.logistics.entity.RestrictedArea;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RestrictedAreaMapper {

    @Select("SELECT * FROM restricted_area WHERE id = #{id}")
    RestrictedArea selectById(Long id);

    @Select("SELECT * FROM restricted_area WHERE status = 'active'")
    List<RestrictedArea> selectAllActive();

    @Select("SELECT * FROM restricted_area")
    List<RestrictedArea> selectAll();

    @Insert("INSERT INTO restricted_area(name, area_type, center_lng, center_lat, radius, " +
            "restriction, time_rule, status, create_time) " +
            "VALUES(#{name}, #{areaType}, #{centerLng}, #{centerLat}, #{radius}, " +
            "#{restriction}, #{timeRule}, #{status}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(RestrictedArea area);

    @Update("UPDATE restricted_area SET name = #{name}, area_type = #{areaType}, " +
            "center_lng = #{centerLng}, center_lat = #{centerLat}, radius = #{radius}, " +
            "restriction = #{restriction}, time_rule = #{timeRule}, status = #{status} WHERE id = #{id}")
    int update(RestrictedArea area);

    @Delete("DELETE FROM restricted_area WHERE id = #{id}")
    int deleteById(Long id);
}
