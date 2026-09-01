package com.logistics.entity;

import java.math.BigDecimal;
import java.util.Date;

public class RestrictedArea {

    private Long id;
    private String name;
    private String areaType;
    private BigDecimal centerLng;
    private BigDecimal centerLat;
    private BigDecimal radius;
    private String restriction;
    private String timeRule;
    private String status;
    private Date createTime;

    public RestrictedArea() {
    }

    public RestrictedArea(String name, String areaType, BigDecimal centerLng, BigDecimal centerLat, 
                          BigDecimal radius, String restriction) {
        this.name = name;
        this.areaType = areaType;
        this.centerLng = centerLng;
        this.centerLat = centerLat;
        this.radius = radius;
        this.restriction = restriction;
        this.status = "active";
        this.createTime = new Date();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAreaType() {
        return areaType;
    }

    public void setAreaType(String areaType) {
        this.areaType = areaType;
    }

    public BigDecimal getCenterLng() {
        return centerLng;
    }

    public void setCenterLng(BigDecimal centerLng) {
        this.centerLng = centerLng;
    }

    public BigDecimal getCenterLat() {
        return centerLat;
    }

    public void setCenterLat(BigDecimal centerLat) {
        this.centerLat = centerLat;
    }

    public BigDecimal getRadius() {
        return radius;
    }

    public void setRadius(BigDecimal radius) {
        this.radius = radius;
    }

    public String getRestriction() {
        return restriction;
    }

    public void setRestriction(String restriction) {
        this.restriction = restriction;
    }

    public String getTimeRule() {
        return timeRule;
    }

    public void setTimeRule(String timeRule) {
        this.timeRule = timeRule;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "RestrictedArea{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", areaType='" + areaType + '\'' +
                ", centerLng=" + centerLng +
                ", centerLat=" + centerLat +
                ", radius=" + radius +
                ", restriction='" + restriction + '\'' +
                ", timeRule='" + timeRule + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
