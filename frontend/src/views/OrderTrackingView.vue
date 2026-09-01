<template>
  <div class="tracking-container">
    <div class="page-header">
      <h2>订单追踪</h2>
      <p class="subtitle">实时查看您的订单配送状态</p>
    </div>

    <div class="filter-section">
      <el-select v-model="statusFilter" placeholder="订单状态" @change="loadOrders" clearable>
        <el-option label="全部订单" value=""></el-option>
        <el-option label="待配送" value="pending"></el-option>
        <el-option label="已分配" value="dispatched"></el-option>
        <el-option label="配送中" value="in_transit"></el-option>
        <el-option label="已送达" value="delivered"></el-option>
      </el-select>
      <el-input v-model="searchKey" placeholder="搜索订单号" @keyup.enter.native="loadOrders" clearable style="width: 200px; margin-left: 15px;"></el-input>
      <el-button type="primary" @click="loadOrders" :loading="loading" icon="el-icon-refresh">刷新</el-button>
    </div>

    <div class="stats-row">
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="stat-card" @click="filterByStatus('')">
            <div class="stat-icon all">
              <i class="el-icon-document"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.total }}</div>
              <div class="stat-label">全部订单</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card" @click="filterByStatus('pending')">
            <div class="stat-icon pending">
              <i class="el-icon-time"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.pending }}</div>
              <div class="stat-label">待配送</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card" @click="filterByStatus('in_transit')">
            <div class="stat-icon transit">
              <i class="el-icon-truck"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.inTransit }}</div>
              <div class="stat-label">配送中</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card" @click="filterByStatus('delivered')">
            <div class="stat-icon delivered">
              <i class="el-icon-circle-check"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.delivered }}</div>
              <div class="stat-label">已送达</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <div class="order-list">
      <el-empty v-if="!loading && orders.length === 0" description="暂无订单数据"></el-empty>
      
      <div class="order-card" v-for="order in filteredOrders" :key="order.id" @click="viewOrderDetail(order)">
        <div class="order-header">
          <div class="order-no">{{ order.orderNo }}</div>
          <el-tag :type="getStatusType(order.status)" size="medium">{{ getStatusText(order.status) }}</el-tag>
        </div>
        
        <div class="order-content">
          <div class="order-info-item">
            <i class="el-icon-user"></i>
            <span>{{ order.customerName }}</span>
            <span class="phone">{{ order.customerPhone }}</span>
          </div>
          <div class="order-info-item">
            <i class="el-icon-location-outline"></i>
            <span>{{ order.customerAddress }}</span>
          </div>
          <div class="order-info-item">
            <i class="el-icon-goods"></i>
            <span>重量：{{ order.weight }} kg</span>
          </div>
        </div>
        
        <div class="order-footer">
          <span class="create-time">创建时间：{{ formatDate(order.createTime) }}</span>
          <el-button type="primary" size="small" icon="el-icon-view" @click.stop="viewOrderDetail(order)">查看详情</el-button>
        </div>
        
        <div class="progress-timeline" v-if="order.status !== 'pending'">
          <div class="timeline-item" :class="{ active: true }">
            <div class="timeline-dot"></div>
            <div class="timeline-content">
              <span class="timeline-title">订单创建</span>
              <span class="timeline-time">{{ formatDate(order.createTime) }}</span>
            </div>
          </div>
          <div class="timeline-item" :class="{ active: order.status === 'dispatched' || order.status === 'in_transit' || order.status === 'delivered' }">
            <div class="timeline-dot"></div>
            <div class="timeline-content">
              <span class="timeline-title">已分配配送</span>
            </div>
          </div>
          <div class="timeline-item" :class="{ active: order.status === 'in_transit' || order.status === 'delivered' }">
            <div class="timeline-dot"></div>
            <div class="timeline-content">
              <span class="timeline-title">配送中</span>
            </div>
          </div>
          <div class="timeline-item" :class="{ active: order.status === 'delivered' }">
            <div class="timeline-dot"></div>
            <div class="timeline-content">
              <span class="timeline-title">已送达</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <el-dialog title="订单详情" :visible.sync="showDetailDialog" width="600px" top="5vh">
      <div v-if="currentOrder" class="order-detail">
        <div class="detail-header">
          <div class="detail-order-no">{{ currentOrder.orderNo }}</div>
          <el-tag :type="getStatusType(currentOrder.status)" size="large">{{ getStatusText(currentOrder.status) }}</el-tag>
        </div>
        
        <el-divider content-position="left">订单信息</el-divider>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="客户姓名">{{ currentOrder.customerName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentOrder.customerPhone }}</el-descriptions-item>
          <el-descriptions-item label="配送地址" :span="2">{{ currentOrder.customerAddress }}</el-descriptions-item>
          <el-descriptions-item label="货物重量">{{ currentOrder.weight }} kg</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDate(currentOrder.createTime) }}</el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left" v-if="currentTask">配送任务</el-divider>
        <div v-if="currentTask" class="task-info">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="任务编号">{{ currentTask.taskNo }}</el-descriptions-item>
            <el-descriptions-item label="总距离">{{ currentTask.totalDistance }} km</el-descriptions-item>
            <el-descriptions-item label="任务状态">
              <el-tag :type="getTaskStatusType(currentTask.status)">{{ getTaskStatusText(currentTask.status) }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ formatDate(currentTask.createTime) }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="detail-actions">
          <el-button @click="showDetailDialog = false">关闭</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getOrders } from '@/api'

export default {
  name: 'OrderTrackingView',
  data() {
    return {
      orders: [],
      loading: false,
      statusFilter: '',
      searchKey: '',
      showDetailDialog: false,
      currentOrder: null,
      currentTask: null
    }
  },
  computed: {
    stats() {
      const stats = {
        total: this.orders.length,
        pending: 0,
        inTransit: 0,
        delivered: 0
      }
      this.orders.forEach(o => {
        if (o.status === 'pending') stats.pending++
        else if (o.status === 'in_transit' || o.status === 'dispatched') stats.inTransit++
        else if (o.status === 'delivered') stats.delivered++
      })
      return stats
    },
    filteredOrders() {
      let result = this.orders
      
      if (this.statusFilter) {
        result = result.filter(o => o.status === this.statusFilter)
      }
      
      if (this.searchKey) {
        const key = this.searchKey.toLowerCase()
        result = result.filter(o => 
          o.orderNo.toLowerCase().includes(key) ||
          (o.customerName && o.customerName.toLowerCase().includes(key)) ||
          (o.customerAddress && o.customerAddress.toLowerCase().includes(key))
        )
      }
      
      return result
    }
  },
  mounted() {
    this.loadOrders()
  },
  methods: {
    async loadOrders() {
      this.loading = true
      try {
        const res = await getOrders(1, 500, this.statusFilter || null)
        this.orders = res.data || []
        this.orders.sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
      } catch (error) {
        console.error('加载订单失败:', error)
        this.$message.error('加载订单失败')
      } finally {
        this.loading = false
      }
    },
    filterByStatus(status) {
      this.statusFilter = status
      this.loadOrders()
    },
    async viewOrderDetail(order) {
      this.currentOrder = order
      this.currentTask = null
      this.showDetailDialog = true
      
      if (order.status !== 'pending') {
        try {
          const taskRes = await this.$http.get(`/dispatch/task/by-order/${order.id}`)
          if (taskRes.data && taskRes.data.code === 200) {
            this.currentTask = taskRes.data.data
          }
        } catch (e) {
          console.log('该订单暂无配送任务')
        }
      }
    },
    getStatusType(status) {
      const typeMap = {
        'pending': 'warning',
        'dispatched': 'primary',
        'in_transit': 'info',
        'delivered': 'success'
      }
      return typeMap[status] || 'info'
    },
    getStatusText(status) {
      const textMap = {
        'pending': '待配送',
        'dispatched': '已分配',
        'in_transit': '配送中',
        'delivered': '已送达'
      }
      return textMap[status] || status
    },
    getTaskStatusType(status) {
      const typeMap = {
        'planned': 'info',
        'in_progress': 'warning',
        'completed': 'success'
      }
      return typeMap[status] || 'info'
    },
    getTaskStatusText(status) {
      const textMap = {
        'planned': '已规划',
        'in_progress': '配送中',
        'completed': '已完成'
      }
      return textMap[status] || status
    },
    formatDate(date) {
      if (!date) return '-'
      const d = new Date(date)
      return d.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    }
  }
}
</script>

<style scoped>
.tracking-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
  min-height: calc(100vh - 60px);
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  font-size: 28px;
  color: #1a2a3a;
  font-weight: 600;
}

.page-header .subtitle {
  margin: 0;
  font-size: 14px;
  color: #6b7c8a;
}

.filter-section {
  display: flex;
  align-items: center;
  margin-bottom: 24px;
  padding: 16px 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.stats-row {
  margin-bottom: 24px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  cursor: pointer;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  font-size: 24px;
  color: white;
}

.stat-icon.all {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-icon.pending {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-icon.transit {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-icon.delivered {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1a2a3a;
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: #8a9aaa;
  margin-top: 4px;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.order-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.order-card:hover {
  border-color: #409EFF;
  box-shadow: 0 4px 20px rgba(64, 158, 255, 0.15);
  transform: translateY(-2px);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.order-no {
  font-size: 18px;
  font-weight: 600;
  color: #1a2a3a;
  font-family: monospace;
}

.order-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 16px;
}

.order-info-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  font-size: 14px;
  color: #5a6a7a;
}

.order-info-item i {
  color: #409EFF;
  flex-shrink: 0;
  margin-top: 2px;
}

.order-info-item .phone {
  color: #8a9aaa;
  margin-left: 10px;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid #f0f2f5;
}

.create-time {
  font-size: 12px;
  color: #9ca3af;
}

.progress-timeline {
  display: flex;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f0f2f5;
}

.timeline-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
}

.timeline-item:not(:last-child)::after {
  content: '';
  position: absolute;
  top: 10px;
  left: 50%;
  width: 100%;
  height: 3px;
  background: #e5e7eb;
}

.timeline-item.active::after {
  background: linear-gradient(90deg, #409EFF, #53A8FF);
}

.timeline-dot {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #e5e7eb;
  border: 3px solid #fff;
  box-shadow: 0 0 0 2px #e5e7eb;
  position: relative;
  z-index: 1;
}

.timeline-item.active .timeline-dot {
  background: linear-gradient(135deg, #409EFF 0%, #53A8FF 100%);
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.3);
}

.timeline-content {
  margin-top: 8px;
  text-align: center;
}

.timeline-title {
  font-size: 12px;
  font-weight: 500;
  color: #9ca3af;
  display: block;
}

.timeline-item.active .timeline-title {
  color: #1a2a3a;
}

.timeline-time {
  font-size: 11px;
  color: #c0c8d0;
  margin-top: 2px;
  display: block;
}

.order-detail {
  padding: 10px 0;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.detail-order-no {
  font-size: 22px;
  font-weight: 600;
  color: #1a2a3a;
  font-family: monospace;
}

.task-info {
  margin-top: 16px;
}

.detail-actions {
  margin-top: 24px;
  text-align: right;
}

@media (max-width: 768px) {
  .tracking-container {
    padding: 16px;
  }

  .stats-row .el-col {
    margin-bottom: 16px;
  }

  .filter-section {
    flex-wrap: wrap;
    gap: 10px;
  }

  .progress-timeline {
    flex-direction: column;
    gap: 16px;
  }

  .timeline-item {
    align-items: flex-start;
  }

  .timeline-item:not(:last-child)::after {
    top: 0;
    left: 9px;
    width: 3px;
    height: 100%;
  }

  .timeline-content {
    margin-left: 30px;
    margin-top: -16px;
    text-align: left;
  }
}
</style>
