package com.logistics.controller;

import com.logistics.entity.Vehicle;
import com.logistics.mapper.VehicleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vehicle")
@CrossOrigin(origins = "*")
public class VehicleController {

    @Autowired
    private VehicleMapper vehicleMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private List<Map<String, Object>> convertToCamelCase(List<Map<String, Object>> list) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> row : list) {
            Map<String, Object> camelRow = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                String camelKey = snakeToCamel(entry.getKey());
                camelRow.put(camelKey, entry.getValue());
            }
            result.add(camelRow);
        }
        return result;
    }

    private String snakeToCamel(String snakeCase) {
        StringBuilder builder = new StringBuilder();
        boolean nextUpperCase = false;
        for (char c : snakeCase.toCharArray()) {
            if (c == '_') {
                nextUpperCase = true;
            } else {
                if (nextUpperCase) {
                    builder.append(Character.toUpperCase(c));
                    nextUpperCase = false;
                } else {
                    builder.append(c);
                }
            }
        }
        return builder.toString();
    }

    @GetMapping("/list")
    public Map<String, Object> list() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Map<String, Object>> vehicles = jdbcTemplate.queryForList(
                "SELECT id, plate_number, vehicle_type, vehicle_category, capacity, status, current_location, longitude, latitude, create_time " +
                "FROM vehicle ORDER BY id ASC");
            result.put("code", 200);
            result.put("data", convertToCamelCase(vehicles));
        } catch (Exception e) {
            try {
                List<Map<String, Object>> vehicles = jdbcTemplate.queryForList(
                    "SELECT id, plate_number, vehicle_type, capacity, status, current_location, longitude, latitude, create_time " +
                    "FROM vehicle ORDER BY id ASC");
                result.put("code", 200);
                result.put("data", convertToCamelCase(vehicles));
            } catch (Exception ex) {
                result.put("code", 500);
                result.put("message", "查询车辆失败：" + ex.getMessage());
            }
        }
        return result;
    }

    @GetMapping("/available")
    public Map<String, Object> available() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Map<String, Object>> vehicles = jdbcTemplate.queryForList(
                "SELECT id, plate_number, vehicle_type, vehicle_category, capacity, status, current_location, longitude, latitude, create_time " +
                "FROM vehicle WHERE status = 'idle' ORDER BY id ASC");
            result.put("code", 200);
            result.put("data", convertToCamelCase(vehicles));
        } catch (Exception e) {
            try {
                List<Map<String, Object>> vehicles = jdbcTemplate.queryForList(
                    "SELECT id, plate_number, vehicle_type, capacity, status, current_location, longitude, latitude, create_time " +
                    "FROM vehicle WHERE status = 'idle' ORDER BY id ASC");
                result.put("code", 200);
                result.put("data", convertToCamelCase(vehicles));
            } catch (Exception ex) {
                result.put("code", 500);
                result.put("message", "查询车辆失败：" + ex.getMessage());
            }
        }
        return result;
    }

    @PostMapping("/add")
    public Map<String, Object> add(@RequestBody Vehicle vehicle) {
        Map<String, Object> result = new HashMap<>();
        if (vehicle.getStatus() == null) {
            vehicle.setStatus("idle");
        }
        if (vehicle.getVehicleType() == null || vehicle.getVehicleType().isEmpty()) {
            vehicle.setVehicleType("van");
        }
        int rows = vehicleMapper.insert(vehicle);
        if (rows > 0) {
            result.put("code", 200);
            result.put("data", vehicle);
            result.put("message", "车辆添加成功");
        } else {
            result.put("code", 500);
            result.put("message", "车辆添加失败");
        }
        return result;
    }

    @PutMapping("/update")
    public Map<String, Object> update(@RequestBody Vehicle vehicle) {
        Map<String, Object> result = new HashMap<>();
        int rows = vehicleMapper.updateFullById(vehicle);
        if (rows > 0) {
            result.put("code", 200);
            result.put("message", "车辆更新成功");
        } else {
            result.put("code", 500);
            result.put("message", "车辆更新失败");
        }
        return result;
    }

    @DeleteMapping("/delete/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> vehicle;
            try {
                vehicle = jdbcTemplate.queryForMap("SELECT id, plate_number, status FROM vehicle WHERE id = ?", id);
            } catch (Exception e) {
                result.put("code", 500);
                result.put("message", "车辆不存在或查询失败");
                return result;
            }
            String status = (String) vehicle.get("status");
            if (status != null && !"idle".equals(status)) {
                result.put("code", 500);
                result.put("message", "只有空闲状态的车辆可以删除");
                return result;
            }
            
            Integer taskCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM delivery_task WHERE vehicle_id = ? AND status != 'completed'",
                Integer.class, id);
            if (taskCount != null && taskCount > 0) {
                String plateNumber = (String) vehicle.get("plate_number");
                result.put("code", 500);
                result.put("message", "车辆【" + (plateNumber != null ? plateNumber : id) + "】有 " + taskCount + " 条未完成的配送任务，无法删除");
                return result;
            }
            
            int rows = jdbcTemplate.update("DELETE FROM vehicle WHERE id = ?", id);
            if (rows > 0) {
                result.put("code", 200);
                result.put("message", "车辆删除成功");
            } else {
                result.put("code", 500);
                result.put("message", "车辆删除失败");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "删除失败：" + e.getMessage());
        }
        return result;
    }
}
