package com.logistics.config;

import com.logistics.entity.RestrictedArea;
import com.logistics.entity.User;
import com.logistics.entity.Vehicle;
import com.logistics.entity.Warehouse;
import com.logistics.mapper.RestrictedAreaMapper;
import com.logistics.mapper.UserMapper;
import com.logistics.mapper.VehicleMapper;
import com.logistics.mapper.WarehouseMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private WarehouseMapper warehouseMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private VehicleMapper vehicleMapper;

    @Autowired
    private RestrictedAreaMapper restrictedAreaMapper;

    @Override
    public void run(String... args) throws Exception {
        List<Warehouse> existingWarehouses = warehouseMapper.selectAllActive();
        if (existingWarehouses == null || existingWarehouses.isEmpty()) {
            Warehouse w1 = new Warehouse();
            w1.setName("昌邑区仓库");
            w1.setAddress("吉林省吉林市昌邑区吉林大街100号");
            w1.setLongitude(new BigDecimal("126.5500"));
            w1.setLatitude(new BigDecimal("43.8400"));
            w1.setStatus("active");
            warehouseMapper.insert(w1);

            Warehouse w2 = new Warehouse();
            w2.setName("龙潭区仓库");
            w2.setAddress("吉林省吉林市龙潭区湘潭街200号");
            w2.setLongitude(new BigDecimal("126.6300"));
            w2.setLatitude(new BigDecimal("43.9200"));
            w2.setStatus("active");
            warehouseMapper.insert(w2);

            Warehouse w3 = new Warehouse();
            w3.setName("船营区仓库");
            w3.setAddress("吉林省吉林市船营区北京路300号");
            w3.setLongitude(new BigDecimal("126.4600"));
            w3.setLatitude(new BigDecimal("43.8300"));
            w3.setStatus("active");
            warehouseMapper.insert(w3);

            Warehouse w4 = new Warehouse();
            w4.setName("丰满区仓库");
            w4.setAddress("吉林省吉林市丰满区吉林大街400号");
            w4.setLongitude(new BigDecimal("126.5700"));
            w4.setLatitude(new BigDecimal("43.8100"));
            w4.setStatus("active");
            warehouseMapper.insert(w4);
        }

        User existingAdmin = userMapper.selectByUsername("admin");
        if (existingAdmin == null) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword("admin123");
            admin.setRealName("系统管理员");
            admin.setRole("admin");
            admin.setStatus("active");
            userMapper.insert(admin);
        }

        User existingOperator = userMapper.selectByUsername("operator");
        if (existingOperator == null) {
            User operator = new User();
            operator.setUsername("operator");
            operator.setPassword("operator123");
            operator.setRealName("普通操作员");
            operator.setRole("operator");
            operator.setStatus("active");
            userMapper.insert(operator);
        }

        List<Vehicle> existingVehicles = vehicleMapper.selectAll();
        if (existingVehicles == null || existingVehicles.isEmpty()) {
            Vehicle v1 = new Vehicle();
            v1.setPlateNumber("吉B-00001");
            v1.setVehicleType("van");
            v1.setVehicleCategory("small");
            v1.setCapacity(new BigDecimal("3000"));
            v1.setStatus("idle");
            v1.setCurrentLocation("昌邑区仓库");
            v1.setLongitude(new BigDecimal("126.5500"));
            v1.setLatitude(new BigDecimal("43.8400"));
            vehicleMapper.insert(v1);
            logger.info("初始化车辆：吉B-00001（小型厢式货车，可进市区）");

            Vehicle v2 = new Vehicle();
            v2.setPlateNumber("吉B-00002");
            v2.setVehicleType("truck");
            v2.setVehicleCategory("large");
            v2.setCapacity(new BigDecimal("10000"));
            v2.setStatus("idle");
            v2.setCurrentLocation("龙潭区仓库");
            v2.setLongitude(new BigDecimal("126.6300"));
            v2.setLatitude(new BigDecimal("43.9200"));
            vehicleMapper.insert(v2);
            logger.info("初始化车辆：吉B-00002（大型卡车，限城区）");

            Vehicle v3 = new Vehicle();
            v3.setPlateNumber("吉B-00003");
            v3.setVehicleType("van");
            v3.setVehicleCategory("small");
            v3.setCapacity(new BigDecimal("2500"));
            v3.setStatus("idle");
            v3.setCurrentLocation("船营区仓库");
            v3.setLongitude(new BigDecimal("126.4600"));
            v3.setLatitude(new BigDecimal("43.8300"));
            vehicleMapper.insert(v3);
            logger.info("初始化车辆：吉B-00003（小型厢式货车，可进市区）");

            Vehicle v4 = new Vehicle();
            v4.setPlateNumber("吉B-00004");
            v4.setVehicleType("truck");
            v4.setVehicleCategory("large");
            v4.setCapacity(new BigDecimal("8000"));
            v4.setStatus("idle");
            v4.setCurrentLocation("丰满区仓库");
            v4.setLongitude(new BigDecimal("126.5700"));
            v4.setLatitude(new BigDecimal("43.8100"));
            vehicleMapper.insert(v4);
            logger.info("初始化车辆：吉B-00004（大型卡车，限城区）");
        }

        List<RestrictedArea> existingAreas = restrictedAreaMapper.selectAllActive();
        if (existingAreas == null || existingAreas.isEmpty()) {
            RestrictedArea area1 = new RestrictedArea();
            area1.setName("吉林市中心城区");
            area1.setAreaType("city_center");
            area1.setCenterLng(new BigDecimal("126.5500"));
            area1.setCenterLat(new BigDecimal("43.8400"));
            area1.setRadius(new BigDecimal("3.0"));
            area1.setRestriction("no_large_trucks");
            area1.setStatus("active");
            restrictedAreaMapper.insert(area1);
            logger.info("初始化限行区域：吉林市中心城区（圆心: 126.55, 43.84, 半径: 3.0公里）");
        }
    }
}
