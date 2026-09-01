package com.logistics.entity;

/**
 * 任务-订单关联实体类
 * 用于记录配送任务中包含哪些订单，以及配送顺序
 */
public class TaskOrder {

    private Long id;          // 关联记录ID
    private Long taskId;      // 配送任务ID
    private Long orderId;     // 订单ID
    private Integer sequence; // 配送顺序（第几个配送）

    // ========== 无参构造方法 ==========
    public TaskOrder() {
    }

    // ========== 有参构造方法 ==========
    public TaskOrder(Long taskId, Long orderId, Integer sequence) {
        this.taskId = taskId;
        this.orderId = orderId;
        this.sequence = sequence;
    }

    // ========== Getter 和 Setter 方法 ==========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }

    // ========== toString 方法 ==========
    @Override
    public String toString() {
        return "TaskOrder{" +
                "id=" + id +
                ", taskId=" + taskId +
                ", orderId=" + orderId +
                ", sequence=" + sequence +
                '}';
    }
}