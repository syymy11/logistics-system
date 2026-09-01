package com.logistics.controller;

import com.logistics.entity.Warehouse;
import com.logistics.mapper.WarehouseMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/warehouse")
@CrossOrigin(origins = "*")
public class WarehouseController {

    @Autowired
    private WarehouseMapper warehouseMapper;

    @GetMapping("/list")
    public Map<String, Object> list() {
        Map<String, Object> result = new HashMap<>();
        List<Warehouse> warehouses = warehouseMapper.selectAllActive();
        result.put("code", 200);
        result.put("data", warehouses);
        return result;
    }

    @GetMapping("/default")
    public Map<String, Object> getDefault() {
        Map<String, Object> result = new HashMap<>();
        Warehouse warehouse = warehouseMapper.selectDefault();
        result.put("code", 200);
        result.put("data", warehouse);
        return result;
    }

    @PostMapping("/add")
    public Map<String, Object> add(@RequestBody Warehouse warehouse) {
        Map<String, Object> result = new HashMap<>();
        if (warehouse.getStatus() == null) {
            warehouse.setStatus("active");
        }
        int rows = warehouseMapper.insert(warehouse);
        if (rows > 0) {
            result.put("code", 200);
            result.put("data", warehouse);
            result.put("message", "仓库添加成功");
        } else {
            result.put("code", 500);
            result.put("message", "仓库添加失败");
        }
        return result;
    }

    @PutMapping("/update")
    public Map<String, Object> update(@RequestBody Warehouse warehouse) {
        Map<String, Object> result = new HashMap<>();
        int rows = warehouseMapper.update(warehouse);
        if (rows > 0) {
            result.put("code", 200);
            result.put("message", "仓库更新成功");
        } else {
            result.put("code", 500);
            result.put("message", "仓库更新失败");
        }
        return result;
    }

    @DeleteMapping("/delete/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        int rows = warehouseMapper.deleteById(id);
        if (rows > 0) {
            result.put("code", 200);
            result.put("message", "仓库删除成功");
        } else {
            result.put("code", 500);
            result.put("message", "仓库删除失败");
        }
        return result;
    }
}
