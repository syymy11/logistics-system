package com.logistics.entity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单实体类
 */
public class Order {

    private Long id;                    // 订单ID
    private String orderNo;             // 订单编号
    private String customerName;        // 客户名称
    private String customerPhone;       // 客户电话
    private String customerAddress;     // 客户地址
    private BigDecimal longitude;       // 经度
    private BigDecimal latitude;        // 纬度
    private BigDecimal weight;          // 重量(kg)
    private String status;              // 状态: pending/dispatched/delivered/cancelled
    private Date assignTime;            // 分配时间
    private Date deliveryTime;          // 送达时间
    private Date createTime;            // 创建时间

    // ========== 无参构造方法 ==========
    public Order() {
    }

    // ========== 有参构造方法 ==========
    public Order(String orderNo, String customerName, String customerPhone,
                 String customerAddress, BigDecimal longitude, BigDecimal latitude,
                 BigDecimal weight) {
        this.orderNo = orderNo;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.customerAddress = customerAddress;
        this.longitude = longitude;
        this.latitude = latitude;
        this.weight = weight;
        this.status = "pending";
        this.createTime = new Date();
    }

    // ========== Getter 和 Setter 方法 ==========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
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

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getAssignTime() {
        return assignTime;
    }

    public void setAssignTime(Date assignTime) {
        this.assignTime = assignTime;
    }

    public Date getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(Date deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    // ========== toString 方法 ==========
    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", orderNo='" + orderNo + '\'' +
                ", customerName='" + customerName + '\'' +
                ", customerPhone='" + customerPhone + '\'' +
                ", customerAddress='" + customerAddress + '\'' +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", weight=" + weight +
                ", status='" + status + '\'' +
                ", assignTime=" + assignTime +
                ", deliveryTime=" + deliveryTime +
                ", createTime=" + createTime +
                '}';
    }
}