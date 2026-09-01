package com.logistics.service;

import com.logistics.algorithm.GeneticAlgorithm;
import com.logistics.entity.DeliveryTask;
import com.logistics.entity.Order;
import com.logistics.entity.RestrictedArea;
import com.logistics.entity.TaskOrder;
import com.logistics.entity.Vehicle;
import com.logistics.entity.Warehouse;
import com.logistics.mapper.DeliveryTaskMapper;
import com.logistics.mapper.OrderMapper;
import com.logistics.mapper.RestrictedAreaMapper;
import com.logistics.mapper.TaskOrderMapper;
import com.logistics.mapper.VehicleMapper;
import com.logistics.mapper.WarehouseMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class DispatchService {

    private static final Logger logger = LoggerFactory.getLogger(DispatchService.class);

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private VehicleMapper vehicleMapper;

    @Autowired
    private DeliveryTaskMapper deliveryTaskMapper;

    @Autowired
    private TaskOrderMapper taskOrderMapper;

    @Autowired
    private AmapService amapService;

    @Autowired
    private WarehouseMapper warehouseMapper;

    @Autowired
    private RestrictedAreaMapper restrictedAreaMapper;

    private String[] defaultDepotCoord = new String[] { "126.5500", "43.8400" };

    private String[] getDepotCoord() {
        try {
            Warehouse warehouse = warehouseMapper.selectDefault();
            if (warehouse != null && warehouse.getLongitude() != null && warehouse.getLatitude() != null) {
                return new String[] {
                        String.valueOf(warehouse.getLongitude()),
                        String.valueOf(warehouse.getLatitude())
                };
            }
        } catch (Exception e) {
            logger.warn("获取仓库坐标失败，使用默认坐标", e);
        }
        return defaultDepotCoord;
    }

    private AtomicInteger taskCounter = new AtomicInteger(0);

    private String generateTaskNo() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        int seq = taskCounter.incrementAndGet() % 1000;
        return "TASK" + sdf.format(new Date()) + String.format("%03d", seq);
    }

    private double calculateHaversineDistance(double lng1, double lat1, double lng2, double lat2) {
        double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private double calculateDistance(double lng1, double lat1, double lng2, double lat2) {
        double dx = lng2 - lng1;
        double dy = lat2 - lat1;
        return Math.sqrt(dx * dx + dy * dy);
    }

    private boolean isUrbanOrder(String address) {
        if (address == null || address.trim().isEmpty()) {
            return false;
        }
        String addr = address;
        return addr.contains("市中心") ||
               addr.contains("城区") ||
               addr.contains("市区") ||
               addr.contains("商业街") ||
               addr.contains("步行街") ||
               addr.contains("广场") ||
               addr.contains("购物") ||
               addr.contains("CBD") ||
               addr.contains("写字楼") ||
               addr.contains("商业区");
    }

    private boolean isInRestrictedArea(BigDecimal lng, BigDecimal lat) {
        if (lng == null || lat == null) {
            return false;
        }
        List<RestrictedArea> areas = restrictedAreaMapper.selectAllActive();
        if (areas == null || areas.isEmpty()) {
            return false;
        }
        for (RestrictedArea area : areas) {
            if (area.getCenterLng() == null || area.getCenterLat() == null || area.getRadius() == null) {
                continue;
            }
            double distance = calculateHaversineDistance(
                lng.doubleValue(), lat.doubleValue(),
                area.getCenterLng().doubleValue(),
                area.getCenterLat().doubleValue()
            );
            if (distance <= area.getRadius().doubleValue()) {
                logger.info("订单坐标 ({}, {}) 在限行区域 '{}' 内（距离: {} 公里）",
                    lng, lat, area.getName(), String.format("%.2f", distance));
                return true;
            }
        }
        return false;
    }

    private boolean hasRestrictedOrders(List<Order> orders) {
        for (Order order : orders) {
            if (order.getLongitude() != null && order.getLatitude() != null) {
                if (isInRestrictedArea(order.getLongitude(), order.getLatitude())) {
                    return true;
                }
            }
            if (isUrbanOrder(order.getCustomerAddress())) {
                return true;
            }
        }
        return false;
    }

    private boolean isLargeVehicle(Vehicle vehicle) {
        if (vehicle == null) return false;
        if ("large".equals(vehicle.getVehicleCategory())) {
            return true;
        }
        if ("truck".equals(vehicle.getVehicleType())) {
            return true;
        }
        return false;
    }

    private Vehicle autoSelectVehicle(List<Order> orders, List<Warehouse> warehouses) throws Exception {
        List<Vehicle> idleVehicles = vehicleMapper.selectByStatus("idle");
        if (idleVehicles == null || idleVehicles.isEmpty()) {
            throw new Exception("没有空闲车辆");
        }

        double totalWeight = orders.stream()
                .mapToDouble(o -> o.getWeight() != null ? o.getWeight().doubleValue() : 0.0)
                .sum();

        boolean hasRestrictedOrders = hasRestrictedOrders(orders);
        if (hasRestrictedOrders) {
            logger.info("检测到有限行区域订单，将排除大型车辆");
        }

        double centerLng = orders.stream()
                .mapToDouble(o -> o.getLongitude() != null ? o.getLongitude().doubleValue() : 0.0)
                .average()
                .orElse(0);
        double centerLat = orders.stream()
                .mapToDouble(o -> o.getLatitude() != null ? o.getLatitude().doubleValue() : 0.0)
                .average()
                .orElse(0);

        Warehouse nearestWarehouse = warehouses.get(0);
        double minWarehouseDist = Double.MAX_VALUE;
        for (Warehouse w : warehouses) {
            if (w.getLongitude() != null && w.getLatitude() != null) {
                double dist = calculateDistance(
                        centerLng, centerLat,
                        w.getLongitude().doubleValue(),
                        w.getLatitude().doubleValue());
                if (dist < minWarehouseDist) {
                    minWarehouseDist = dist;
                    nearestWarehouse = w;
                }
            }
        }

        double targetLng = nearestWarehouse.getLongitude() != null ? nearestWarehouse.getLongitude().doubleValue() : centerLng;
        double targetLat = nearestWarehouse.getLatitude() != null ? nearestWarehouse.getLatitude().doubleValue() : centerLat;

        List<Vehicle> suitableVehicles = new ArrayList<>();
        for (Vehicle v : idleVehicles) {
            if (v.getCapacity() != null && v.getCapacity().doubleValue() >= totalWeight) {
                if (hasRestrictedOrders && isLargeVehicle(v)) {
                    logger.warn("车辆 {} 是大型车辆，跳过（有限行区域订单）", v.getPlateNumber());
                    continue;
                }
                suitableVehicles.add(v);
            }
        }

        if (suitableVehicles.isEmpty()) {
            if (hasRestrictedOrders) {
                throw new Exception("有限行区域配送订单，没有可用的小型车辆（大车限行），订单总重量：" + totalWeight + "kg");
            }
            throw new Exception("没有足够载重的空闲车辆，订单总重量：" + totalWeight + "kg");
        }

        Vehicle bestVehicle = null;
        double bestScore = Double.MAX_VALUE;
        for (Vehicle v : suitableVehicles) {
            double dist = 0;
            if (v.getLongitude() != null && v.getLatitude() != null) {
                dist = calculateDistance(
                        targetLng, targetLat,
                        v.getLongitude().doubleValue(),
                        v.getLatitude().doubleValue());
            }
            double capacity = v.getCapacity() != null ? v.getCapacity().doubleValue() : Double.MAX_VALUE;
            double capacityScore = capacity - totalWeight;
            double score = dist * 100 + capacityScore * 0.01;
            if (score < bestScore) {
                bestScore = score;
                bestVehicle = v;
            }
        }

        return bestVehicle;
    }

    private Warehouse findNearestWarehouse(Order order, List<Warehouse> warehouses) {
        if (warehouses == null || warehouses.isEmpty()) {
            return null;
        }
        
        Warehouse nearest = null;
        double minDistance = Double.MAX_VALUE;
        
        for (Warehouse warehouse : warehouses) {
            if (warehouse.getLongitude() == null || warehouse.getLatitude() == null) {
                continue;
            }
            if (order.getLongitude() == null || order.getLatitude() == null) {
                continue;
            }
            
            double distance = calculateDistance(
                    order.getLongitude().doubleValue(),
                    order.getLatitude().doubleValue(),
                    warehouse.getLongitude().doubleValue(),
                    warehouse.getLatitude().doubleValue()
            );
            
            if (distance < minDistance) {
                minDistance = distance;
                nearest = warehouse;
            }
        }
        
        return nearest;
    }

    @Transactional
    public DeliveryTask dispatchOrders(List<Long> orderIds, Long vehicleId) throws Exception {
        logger.info("开始调度任务，订单数量：{}, 车辆ID：{}", orderIds.size(), vehicleId);

        if (orderIds == null || orderIds.isEmpty()) {
            throw new IllegalArgumentException("订单ID列表不能为空");
        }

        List<Warehouse> warehouses = warehouseMapper.selectAllActive();
        if (warehouses == null || warehouses.isEmpty()) {
            throw new Exception("没有可用仓库");
        }
        logger.info("可用仓库数量：{}", warehouses.size());

        List<Order> orders = new ArrayList<>();
        for (Long id : orderIds) {
            if (id == null) continue;
            Order order = orderMapper.selectById(id);
            if (order != null && "pending".equals(order.getStatus())) {
                if (order.getLongitude() == null || order.getLatitude() == null) {
                    throw new Exception("订单 " + order.getOrderNo() + " 缺少坐标信息");
                }
                if (order.getWeight() == null) {
                    throw new Exception("订单 " + order.getOrderNo() + " 缺少重量信息");
                }
                orders.add(order);
            }
        }
        if (orders.isEmpty()) {
            throw new Exception("没有找到待配送的订单");
        }
        logger.info("获取到待配送订单数量：{}", orders.size());

        Vehicle vehicle;
        if (vehicleId != null) {
            vehicle = vehicleMapper.selectById(vehicleId);
            if (vehicle == null) {
                throw new Exception("车辆不存在");
            }
            if (!"idle".equals(vehicle.getStatus())) {
                throw new Exception("车辆状态不是空闲");
            }
            double totalWeight = orders.stream()
                    .mapToDouble(o -> o.getWeight() != null ? o.getWeight().doubleValue() : 0.0)
                    .sum();
            if (vehicle.getCapacity() == null || vehicle.getCapacity().doubleValue() < totalWeight) {
                throw new Exception("车辆载重不足，需要：" + totalWeight + "kg，车辆载重：" + 
                    (vehicle.getCapacity() != null ? vehicle.getCapacity() : 0) + "kg");
            }
            
            boolean hasRestrictedOrders = hasRestrictedOrders(orders);
            if (hasRestrictedOrders && isLargeVehicle(vehicle)) {
                throw new Exception("选择的车辆是大型车辆，无法配送限行区域订单（城区限行）");
            }
            
            logger.info("使用指定车辆：{}，载重：{}", vehicle.getPlateNumber(), vehicle.getCapacity());
        } else {
            vehicle = autoSelectVehicle(orders, warehouses);
            logger.info("自动选择车辆：{}，载重：{}", vehicle.getPlateNumber(), vehicle.getCapacity());
        }

        Map<Warehouse, List<Order>> warehouseOrderMap = new LinkedHashMap<>();
        Map<Order, Warehouse> orderWarehouseMap = new HashMap<>();

        for (Order order : orders) {
            Warehouse nearest = findNearestWarehouse(order, warehouses);
            if (nearest == null) {
                nearest = warehouses.get(0);
            }
            warehouseOrderMap.computeIfAbsent(nearest, k -> new ArrayList<>()).add(order);
            orderWarehouseMap.put(order, nearest);
            logger.info("订单 {} 分配到仓库 {}", order.getOrderNo(), nearest.getName());
        }

        logger.info("订单分组结果：");
        for (Map.Entry<Warehouse, List<Order>> entry : warehouseOrderMap.entrySet()) {
            logger.info("  仓库 {}：{} 个订单", entry.getKey().getName(), entry.getValue().size());
        }

        double totalDistance = 0;
        double totalWeight = 0;
        List<Order> orderedOrders = new ArrayList<>();

        for (Map.Entry<Warehouse, List<Order>> entry : warehouseOrderMap.entrySet()) {
            Warehouse warehouse = entry.getKey();
            List<Order> warehouseOrders = entry.getValue();
            
            List<String[]> locations = new ArrayList<>();
            locations.add(new String[] {
                    String.valueOf(warehouse.getLongitude()),
                    String.valueOf(warehouse.getLatitude())
            });
            
            List<Double> demands = new ArrayList<>();
            demands.add(0.0);
            
            for (Order order : warehouseOrders) {
                locations.add(new String[] {
                        String.valueOf(order.getLongitude()),
                        String.valueOf(order.getLatitude())
                });
                demands.add(order.getWeight().doubleValue());
                totalWeight += order.getWeight().doubleValue();
            }

            logger.info("计算仓库 {} 的距离矩阵，位置数量：{}", warehouse.getName(), locations.size());
            double[][] distanceMatrix = amapService.getDistanceMatrix(locations);

            double vehicleCapacity = vehicle.getCapacity().doubleValue();
            GeneticAlgorithm ga = new GeneticAlgorithm(distanceMatrix, demands, vehicleCapacity);
            GeneticAlgorithm.Solution solution = ga.run();
            
            if (solution == null || solution.routes == null || solution.routes.isEmpty()) {
                logger.warn("仓库 {} 路径规划失败，使用原顺序", warehouse.getName());
                for (int i = 0; i < warehouseOrders.size(); i++) {
                    orderedOrders.add(warehouseOrders.get(i));
                }
                continue;
            }

            totalDistance += solution.totalDistance;
            logger.info("仓库 {} 路径规划完成，总距离：{}公里，路径数量：{}", 
                    warehouse.getName(), solution.totalDistance, solution.routes.size());

            for (List<Integer> route : solution.routes) {
                for (int nodeIndex : route) {
                    if (nodeIndex == 0) continue;
                    int orderIndex = nodeIndex - 1;
                    if (orderIndex >= 0 && orderIndex < warehouseOrders.size()) {
                        orderedOrders.add(warehouseOrders.get(orderIndex));
                    }
                }
            }
        }

        logger.info("最终配送顺序：{} 个订单，总距离：{}公里", orderedOrders.size(), totalDistance);

        DeliveryTask task = new DeliveryTask();
        task.setTaskNo(generateTaskNo());
        task.setVehicleId(vehicle.getId());
        task.setStatus("planned");
        task.setTotalDistance(BigDecimal.valueOf(totalDistance));
        task.setTotalWeight(BigDecimal.valueOf(totalWeight));
        task.setCreateTime(new Date());
        
        int insertResult = deliveryTaskMapper.insert(task);
        if (insertResult <= 0) {
            throw new Exception("保存配送任务失败");
        }
        logger.info("保存配送任务成功，任务编号：{}", task.getTaskNo());

        int sequence = 0;
        for (Order order : orderedOrders) {
            sequence++;
            
            TaskOrder taskOrder = new TaskOrder();
            taskOrder.setTaskId(task.getId());
            taskOrder.setOrderId(order.getId());
            taskOrder.setSequence(sequence);
            int taskOrderResult = taskOrderMapper.insert(taskOrder);
            if (taskOrderResult <= 0) {
                throw new Exception("保存任务订单关联失败");
            }

            order.setStatus("dispatched");
            order.setAssignTime(new Date());
            int orderResult = orderMapper.updateById(order);
            if (orderResult <= 0) {
                throw new Exception("更新订单状态失败");
            }
        }
        logger.info("保存任务订单关联成功，共关联 {} 个订单", sequence);

        vehicle.setStatus("on_way");
        int vehicleResult = vehicleMapper.updateById(vehicle);
        if (vehicleResult <= 0) {
            throw new Exception("更新车辆状态失败");
        }

        logger.info("调度任务完成，任务ID：{}", task.getId());
        return task;
    }

    public List<String[]> getTaskRouteCoordinates(Long taskId) throws Exception {
        logger.info("开始获取配送任务的路径坐标，任务ID：{}", taskId);

        DeliveryTask task = deliveryTaskMapper.selectById(taskId);
        if (task == null) {
            throw new Exception("任务不存在");
        }

        List<TaskOrder> taskOrders = taskOrderMapper.selectByTaskIdOrderBySequence(taskId);
        logger.info("获取到任务下的订单数量：{}", taskOrders.size());

        String[] depotCoord = getDepotCoord();
        List<String[]> coordinates = new ArrayList<>();
        coordinates.add(depotCoord);

        for (TaskOrder taskOrder : taskOrders) {
            Order order = orderMapper.selectById(taskOrder.getOrderId());
            if (order != null && order.getLongitude() != null && order.getLatitude() != null) {
                coordinates.add(new String[] {
                        String.valueOf(order.getLongitude()),
                        String.valueOf(order.getLatitude())
                });
            }
        }

        coordinates.add(depotCoord);

        logger.info("获取路径坐标完成，坐标点数量：{}", coordinates.size());
        return coordinates;
    }

    public Map<String, Object> getTaskDetails(Long taskId) throws Exception {
        logger.info("开始获取配送任务详情，任务ID：{}", taskId);

        DeliveryTask task = deliveryTaskMapper.selectById(taskId);
        if (task == null) {
            throw new Exception("任务不存在");
        }

        Vehicle vehicle = vehicleMapper.selectById(task.getVehicleId());

        List<TaskOrder> taskOrders = taskOrderMapper.selectByTaskIdOrderBySequence(taskId);
        List<Order> orders = new ArrayList<>();
        for (TaskOrder taskOrder : taskOrders) {
            Order order = orderMapper.selectById(taskOrder.getOrderId());
            if (order != null) {
                orders.add(order);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("task", task);
        result.put("vehicle", vehicle);
        result.put("orders", orders);

        logger.info("获取任务详情完成，订单数量：{}", orders.size());
        return result;
    }

    public DeliveryTask getTaskByOrderId(Long orderId) throws Exception {
        logger.info("开始根据订单ID获取所属任务，订单ID：{}", orderId);

        TaskOrder taskOrder = taskOrderMapper.selectByOrderId(orderId);
        if (taskOrder == null) {
            throw new Exception("订单未分配任务");
        }

        DeliveryTask task = deliveryTaskMapper.selectById(taskOrder.getTaskId());
        if (task == null) {
            throw new Exception("任务不存在");
        }

        logger.info("获取任务成功，任务ID：{}", task.getId());
        return task;
    }
}
