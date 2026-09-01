// controller/OrderController.java
package com.logistics.controller;

import com.logistics.entity.DeliveryTask;
import com.logistics.entity.Order;
import com.logistics.entity.TaskOrder;
import com.logistics.entity.Vehicle;
import com.logistics.mapper.DeliveryTaskMapper;
import com.logistics.mapper.OrderMapper;
import com.logistics.mapper.TaskOrderMapper;
import com.logistics.mapper.VehicleMapper;
import com.logistics.service.AmapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private TaskOrderMapper taskOrderMapper;

    @Autowired
    private AmapService amapService;

    @Autowired
    private DeliveryTaskMapper deliveryTaskMapper;

    @Autowired
    private VehicleMapper vehicleMapper;

    @GetMapping("/list")
    public Map<String, Object> list(@RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            String status) {
        Map<String, Object> result = new HashMap<>();
        int offset = (page - 1) * size;
        List<Order> orders;
        int total;
        if (status == null || status.isEmpty() || "all".equals(status)) {
            orders = orderMapper.selectAll(offset, size);
            total = orderMapper.countAll();
        } else {
            orders = orderMapper.selectByStatusAndPage(status, offset, size);
            total = orderMapper.countByStatus(status);
        }

        result.put("code", 200);
        result.put("data", orders);
        result.put("total", total);
        return result;
    }

    @GetMapping("/pending")
    public Map<String, Object> getPendingOrders() {
        List<Order> orders = orderMapper.selectByStatus("pending");
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", orders);
        return result;
    }

    @PostMapping("/add")
    public Map<String, Object> addOrder(@RequestBody Order order) {
        order.setStatus("pending");
        order.setCreateTime(new Date());

        if (order.getLongitude() == null || order.getLatitude() == null) {
            try {
                String[] coords = amapService.geocode(order.getCustomerAddress());
                if (coords != null && coords.length >= 2) {
                    order.setLongitude(new BigDecimal(coords[0]));
                    order.setLatitude(new BigDecimal(coords[1]));
                } else {
                    setDefaultCoordinates(order);
                }
            } catch (Exception e) {
                System.out.println("地址解析失败，使用默认坐标: " + e.getMessage());
                setDefaultCoordinates(order);
            }
        }

        orderMapper.insert(order);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "订单添加成功");
        return result;
    }

    private void setDefaultCoordinates(Order order) {
        order.setLongitude(new BigDecimal("116.397428"));
        order.setLatitude(new BigDecimal("39.90923"));
    }

    @PutMapping("/status")
    public Map<String, Object> updateStatus(@RequestParam Long id, @RequestParam String status) {
        Order order = orderMapper.selectById(id);
        order.setStatus(status);
        if ("delivered".equals(status)) {
            order.setDeliveryTime(new Date());
        }
        orderMapper.updateById(order);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "状态更新成功");
        return result;
    }

    @PostMapping("/reset/{id}")
    public Map<String, Object> resetOrder(@PathVariable Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 404);
            result.put("message", "订单不存在");
            return result;
        }

        TaskOrder taskOrder = taskOrderMapper.selectByOrderId(id);
        Long taskId = null;
        boolean isLastOrder = false;
        DeliveryTask deliveryTask = null;

        if (taskOrder != null) {
            taskId = taskOrder.getTaskId();
            List<TaskOrder> allTaskOrders = taskOrderMapper.selectByTaskIdOrderBySequence(taskId);
            if (allTaskOrders.size() == 1) {
                isLastOrder = true;
                deliveryTask = deliveryTaskMapper.selectById(taskId);
            }
        }

        order.setStatus("pending");
        order.setAssignTime(null);
        orderMapper.updateById(order);

        taskOrderMapper.deleteByOrderId(id);

        if (isLastOrder && deliveryTask != null) {
            if (deliveryTask.getVehicleId() != null) {
                Vehicle vehicle = vehicleMapper.selectById(deliveryTask.getVehicleId());
                if (vehicle != null && "on_way".equals(vehicle.getStatus())) {
                    vehicle.setStatus("idle");
                    vehicleMapper.updateById(vehicle);
                }
            }
            deliveryTaskMapper.deleteById(taskId);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "订单已重置为未配送");
        return result;
    }

    @DeleteMapping("/delete/{id}")
    public Map<String, Object> deleteOrder(@PathVariable Long id) {
        taskOrderMapper.deleteByOrderId(id);
        orderMapper.deleteById(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "删除成功");
        return result;
    }
}
