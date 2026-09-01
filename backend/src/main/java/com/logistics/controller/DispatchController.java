// controller/DispatchController.java
package com.logistics.controller;

import com.logistics.entity.DeliveryTask;
import com.logistics.service.DispatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dispatch")
@CrossOrigin(origins = "*")
public class DispatchController {

    @Autowired
    private DispatchService dispatchService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private com.logistics.mapper.RestrictedAreaMapper restrictedAreaMapper;

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

    @PostMapping("/plan")
    public Map<String, Object> planDelivery(@RequestBody Map<String, Object> params) {
        // 处理orderIds的类型转换
        @SuppressWarnings("unchecked")
        List<Object> orderIdObjs = (List<Object>) params.get("orderIds");
        List<Long> orderIds = new ArrayList<>();
        if (orderIdObjs != null) {
            for (Object obj : orderIdObjs) {
                if (obj instanceof Integer) {
                    orderIds.add(((Integer) obj).longValue());
                } else if (obj instanceof Long) {
                    orderIds.add((Long) obj);
                } else if (obj instanceof String) {
                    orderIds.add(Long.valueOf((String) obj));
                }
            }
        }
        // 处理vehicleId的类型转换
        Object vehicleIdObj = params.get("vehicleId");
        Long vehicleId = null;
        if (vehicleIdObj instanceof Integer) {
            vehicleId = ((Integer) vehicleIdObj).longValue();
        } else if (vehicleIdObj instanceof Long) {
            vehicleId = (Long) vehicleIdObj;
        } else if (vehicleIdObj instanceof String) {
            vehicleId = Long.valueOf((String) vehicleIdObj);
        }

        Map<String, Object> result = new HashMap<>();
        try {
            DeliveryTask task = dispatchService.dispatchOrders(orderIds, vehicleId);
            result.put("code", 200);
            result.put("data", task);
            result.put("message", "调度成功");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @GetMapping("/vehicles")
    public Map<String, Object> getAvailableVehicles() {
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

    @GetMapping("/route/{taskId}")
    public Map<String, Object> getRoute(@PathVariable Long taskId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<String[]> coordinates = dispatchService.getTaskRouteCoordinates(taskId);
            result.put("code", 200);
            result.put("data", coordinates);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @GetMapping("/task/{taskId}")
    public Map<String, Object> getTaskDetails(@PathVariable Long taskId) {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> taskDetails = dispatchService.getTaskDetails(taskId);
            result.put("code", 200);
            result.put("data", taskDetails);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @GetMapping("/task/by-order/{orderId}")
    public Map<String, Object> getTaskByOrderId(@PathVariable Long orderId) {
        Map<String, Object> result = new HashMap<>();
        try {
            DeliveryTask task = dispatchService.getTaskByOrderId(orderId);
            result.put("code", 200);
            result.put("data", task);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @GetMapping("/restricted-areas")
    public Map<String, Object> getRestrictedAreas() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Map<String, Object>> areas = jdbcTemplate.queryForList(
                "SELECT id, name, area_type, center_lng, center_lat, radius, restriction, time_rule, status, create_time " +
                "FROM restricted_area ORDER BY id ASC");
            result.put("code", 200);
            result.put("data", convertToCamelCase(areas));
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "查询限行区域失败：" + e.getMessage());
        }
        return result;
    }

    @PostMapping("/restricted-areas")
    public Map<String, Object> addRestrictedArea(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            String name = (String) params.get("name");
            String areaType = (String) params.get("areaType");
            String restriction = (String) params.get("restriction");
            String status = params.get("status") != null ? (String) params.get("status") : "active";
            
            java.math.BigDecimal centerLng = params.get("centerLng") != null ? 
                new java.math.BigDecimal(params.get("centerLng").toString()) : null;
            java.math.BigDecimal centerLat = params.get("centerLat") != null ? 
                new java.math.BigDecimal(params.get("centerLat").toString()) : null;
            java.math.BigDecimal radius = params.get("radius") != null ? 
                new java.math.BigDecimal(params.get("radius").toString()) : null;
            
            String sql = "INSERT INTO restricted_area(name, area_type, center_lng, center_lat, radius, restriction, time_rule, status, create_time) " +
                         "VALUES(?, ?, ?, ?, ?, ?, ?, ?, NOW())";
            jdbcTemplate.update(sql, name, areaType, centerLng, centerLat, radius, restriction, null, status);
            
            result.put("code", 200);
            result.put("message", "添加成功");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "添加限行区域失败：" + e.getMessage());
        }
        return result;
    }

    @PutMapping("/restricted-areas/{id}")
    public Map<String, Object> updateRestrictedArea(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            String name = (String) params.get("name");
            String areaType = (String) params.get("areaType");
            String restriction = (String) params.get("restriction");
            String status = (String) params.get("status");
            
            java.math.BigDecimal centerLng = params.get("centerLng") != null ? 
                new java.math.BigDecimal(params.get("centerLng").toString()) : null;
            java.math.BigDecimal centerLat = params.get("centerLat") != null ? 
                new java.math.BigDecimal(params.get("centerLat").toString()) : null;
            java.math.BigDecimal radius = params.get("radius") != null ? 
                new java.math.BigDecimal(params.get("radius").toString()) : null;
            
            String sql = "UPDATE restricted_area SET name = ?, area_type = ?, center_lng = ?, center_lat = ?, radius = ?, restriction = ?, status = ? WHERE id = ?";
            jdbcTemplate.update(sql, name, areaType, centerLng, centerLat, radius, restriction, status, id);
            
            result.put("code", 200);
            result.put("message", "更新成功");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "更新限行区域失败：" + e.getMessage());
        }
        return result;
    }

    @DeleteMapping("/restricted-areas/{id}")
    public Map<String, Object> deleteRestrictedArea(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            String sql = "DELETE FROM restricted_area WHERE id = ?";
            jdbcTemplate.update(sql, id);
            result.put("code", 200);
            result.put("message", "删除成功");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "删除限行区域失败：" + e.getMessage());
        }
        return result;
    }
}