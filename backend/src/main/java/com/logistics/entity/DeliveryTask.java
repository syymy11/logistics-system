// entity/DeliveryTask.java
package com.logistics.entity;

import java.math.BigDecimal;
import java.util.Date;

public class DeliveryTask {
    private Long id;
    private String taskNo;
    private Long vehicleId;
    private String routePath;
    private BigDecimal totalDistance;
    private BigDecimal totalWeight;
    private String status;
    private Date startTime;
    private Date endTime;
    private Date createTime;
    private Date updateTime;

    // ========== 无参构造方法 ==========
    public DeliveryTask() {
    }

    // ========== Getter 和 Setter 方法 ==========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTaskNo() {
        return taskNo;
    }

    public void setTaskNo(String taskNo) {
        this.taskNo = taskNo;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getRoutePath() {
        return routePath;
    }

    public void setRoutePath(String routePath) {
        this.routePath = routePath;
    }

    public BigDecimal getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(BigDecimal totalDistance) {
        this.totalDistance = totalDistance;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public BigDecimal getTotalWeight() {
        return totalWeight;
    }

    public void setTotalWeight(BigDecimal totalWeight) {
        this.totalWeight = totalWeight;
    }

    // ========== toString 方法 ==========
    @Override
    public String toString() {
        return "DeliveryTask{" +
                "id=" + id +
                ", taskNo='" + taskNo + '\'' +
                ", vehicleId=" + vehicleId +
                ", routePath='" + routePath + '\'' +
                ", totalDistance=" + totalDistance +
                ", totalWeight=" + totalWeight +
                ", status='" + status + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}