package com.logistics.entity;

import java.math.BigDecimal;

/**
 * 车辆实体类
 */
public class Vehicle {

    private Long id;                    // 车辆ID
    private String plateNumber;         // 车牌号
    private String vehicleType;         // 车辆类型: van/truck/motorcycle
    private String vehicleCategory;     // 车辆规格: small(小车)/large(大车)
    private BigDecimal capacity;        // 载重上限(kg)  ← 必须有这个字段
    private String status;              // 状态: idle/on_way/maintenance
    private String currentLocation;     // 当前位置描述
    private BigDecimal longitude;       // 当前经度
    private BigDecimal latitude;        // 当前纬度

    // ========== 无参构造方法 ==========
    public Vehicle() {
    }

    // ========== 有参构造方法 ==========
    public Vehicle(String plateNumber, String vehicleType, BigDecimal capacity) {
        this.plateNumber = plateNumber;
        this.vehicleType = vehicleType;
        this.capacity = capacity;
        this.status = "idle";
    }

    // ========== Getter 和 Setter 方法 ==========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getVehicleCategory() {
        return vehicleCategory;
    }

    public void setVehicleCategory(String vehicleCategory) {
        this.vehicleCategory = vehicleCategory;
    }

    public BigDecimal getCapacity() {    // ← 必须有这个方法
        return capacity;
    }

    public void setCapacity(BigDecimal capacity) {
        this.capacity = capacity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(String currentLocation) {
        this.currentLocation = currentLocation;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    // ========== toString 方法 ==========
    @Override
    public String toString() {
        return "Vehicle{" +
                "id=" + id +
                ", plateNumber='" + plateNumber + '\'' +
                ", vehicleType='" + vehicleType + '\'' +
                ", capacity=" + capacity +
                ", status='" + status + '\'' +
                '}';
    }
}