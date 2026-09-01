<template>
  <div class="dashboard-container">
    <div class="welcome-section">
      <h2>欢迎使用<span class="highlight">物流配送管理系统</span></h2>
      <p class="subtitle">高效、智能的配送解决方案，让物流更简单</p>
    </div>

    <div class="stats-grid">
      <div class="stat-card" v-for="(stat, index) in statsData" :key="index">
        <div class="stat-icon" :style="{ background: stat.gradient }">
          <i :class="stat.icon"></i>
        </div>
        <div class="stat-info">
          <span class="stat-value" :style="{ color: stat.color }">{{ stat.value }}</span>
          <span class="stat-label">{{ stat.label }}</span>
        </div>
      </div>
    </div>

    <div class="charts-grid">
      <div class="chart-card">
        <div class="section-header">
          <h3><i class="el-icon-pie-chart"></i> 订单状态分布</h3>
        </div>
        <div class="chart-container">
          <div ref="pieChart" class="pie-chart"></div>
          <div class="pie-legend">
            <div class="legend-item">
              <span class="legend-dot" style="background: #f5576c;"></span>
              <span class="legend-label">待配送</span>
              <span class="legend-value">{{ stats.pending }}</span>
            </div>
            <div class="legend-item">
              <span class="legend-dot" style="background: #4facfe;"></span>
              <span class="legend-label">已分配</span>
              <span class="legend-value">{{ dispatchedCount }}</span>
            </div>
            <div class="legend-item">
              <span class="legend-dot" style="background: #43e97b;"></span>
              <span class="legend-label">已送达</span>
              <span class="legend-value">{{ stats.completed }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="trend-actions-grid">
      <div class="chart-card">
        <div class="section-header">
          <h3><i class="el-icon-data-line"></i> 配送趋势</h3>
        </div>
        <div class="chart-container">
          <div ref="lineChart" class="line-chart"></div>
        </div>
      </div>

      <div class="quick-actions">
        <div class="section-header">
          <h3><i class="el-icon-menu"></i> 快捷操作</h3>
        </div>
        <div class="action-buttons">
          <div class="action-btn success" @click="$router.push('/orders')">
            <div class="action-icon"><i class="el-icon-document"></i></div>
            <div class="action-text">
              <span class="action-title">订单管理</span>
              <span class="action-desc">添加和查看订单</span>
            </div>
            <div class="action-arrow"><i class="el-icon-arrow-right"></i></div>
          </div>
          <div class="action-btn info" @click="$router.push('/tracking')">
            <div class="action-icon"><i class="el-icon-search"></i></div>
            <div class="action-text">
              <span class="action-title">订单追踪</span>
              <span class="action-desc">实时追踪配送状态</span>
            </div>
            <div class="action-arrow"><i class="el-icon-arrow-right"></i></div>
          </div>
          <div class="action-btn primary" @click="$router.push('/dispatch')" v-if="isAdmin">
            <div class="action-icon"><i class="el-icon-location"></i></div>
            <div class="action-text">
              <span class="action-title">智能调度</span>
              <span class="action-desc">AI优化配送路线</span>
            </div>
            <div class="action-arrow"><i class="el-icon-arrow-right"></i></div>
          </div>
          <div class="action-btn warning" @click="$router.push('/management')" v-if="isAdmin">
            <div class="action-icon"><i class="el-icon-setting"></i></div>
            <div class="action-text">
              <span class="action-title">配送管理</span>
              <span class="action-desc">车辆与仓库管理</span>
            </div>
            <div class="action-arrow"><i class="el-icon-arrow-right"></i></div>
          </div>
        </div>
      </div>
    </div>

    <div class="content-grid">
      <div class="recent-orders">
        <div class="section-header">
          <h3><i class="el-icon-tickets"></i> 最近订单</h3>
          <el-button type="text" @click="$router.push('/orders')">查看全部 <i class="el-icon-arrow-right"></i></el-button>
        </div>
        <div class="order-list">
          <div class="order-item" v-for="order in recentOrders" :key="order.id">
            <div class="order-left">
              <div class="order-icon" :class="getOrderClass(order.status)">
                <i :class="getOrderIcon(order.status)"></i>
              </div>
              <div class="order-info">
                <span class="order-no">{{ order.orderNo }}</span>
                <span class="order-address">{{ order.customerAddress }}</span>
              </div>
            </div>
            <div class="order-right">
              <el-tag :type="getStatusType(order.status)" size="small">{{ getStatusText(order.status) }}</el-tag>
            </div>
          </div>
          <div class="empty-state" v-if="recentOrders.length === 0">
            <i class="el-icon-folder-opened"></i>
            <p>暂无订单数据</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts'

export default {
  name: 'DashboardView',
  data() {
    return {
      stats: {
        orders: 0,
        pending: 0,
        vehicles: 0,
        completed: 0
      },
      recentOrders: [],
      orders: [],
      trendData: {
        days: [],
        pending: [],
        completed: []
      }
    }
  },
  computed: {
    isAdmin() {
      const user = localStorage.getItem('user')
      if (!user) return false
      const userData = JSON.parse(user)
      return userData.role === 'admin'
    },
    statsData() {
      return [
        {
          label: '总订单数',
          value: this.stats.orders,
          icon: 'el-icon-document',
          gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
          color: '#667eea'
        },
        {
          label: '待配送',
          value: this.stats.pending,
          icon: 'el-icon-time',
          gradient: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
          color: '#f5576c'
        },
        {
          label: '可用车辆',
          value: this.stats.vehicles,
          icon: 'el-icon-truck',
          gradient: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
          color: '#00b09b'
        },
        {
          label: '已完成',
          value: this.stats.completed,
          icon: 'el-icon-circle-check',
          gradient: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
          color: '#43e97b'
        }
      ]
    },
    dispatchedCount() {
      return this.stats.orders - this.stats.pending - this.stats.completed
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    async loadData() {
      try {
        const ordersRes = await this.$http.get('/order/list', { params: { page: 1, size: 500 } })
        const orders = Array.isArray(ordersRes.data) ? ordersRes.data : (Array.isArray(ordersRes.data?.data) ? ordersRes.data.data : [])
        this.orders = orders
        this.stats.orders = orders.length
        this.stats.pending = orders.filter(o => o.status === 'pending').length
        this.stats.completed = orders.filter(o => o.status === 'delivered').length
        this.recentOrders = orders.slice(0, 5)

        this.calculateTrendData(orders)

        try {
          const vehiclesRes = await this.$http.get('/dispatch/vehicles')
          const vehicles = Array.isArray(vehiclesRes.data) ? vehiclesRes.data : (Array.isArray(vehiclesRes.data?.data) ? vehiclesRes.data.data : [])
          this.stats.vehicles = vehicles.filter(v => v.status === 'idle').length
        } catch (vehiclesError) {
          console.warn('加载车辆数据失败:', vehiclesError)
        }

        setTimeout(() => {
          this.initCharts()
        }, 100)
      } catch (error) {
        console.error('加载数据失败:', error)
        this.orders = []
        this.recentOrders = []
      }
    },
    calculateTrendData(orders) {
      const today = new Date()
      const days = []
      const pendingByDay = []
      const completedByDay = []

      for (let i = 6; i >= 0; i--) {
        const date = new Date(today)
        date.setDate(date.getDate() - i)
        const dayName = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'][date.getDay()]
        days.push(dayName)

        const dayStart = new Date(date.getFullYear(), date.getMonth(), date.getDate())
        const dayEnd = new Date(dayStart)
        dayEnd.setDate(dayEnd.getDate() + 1)

        const dayOrders = orders.filter(o => {
          if (!o.createTime) return false
          const createDate = new Date(o.createTime)
          return createDate >= dayStart && createDate < dayEnd
        })

        const dayPending = dayOrders.filter(o => o.status === 'pending').length
        const dayCompleted = dayOrders.filter(o => o.status === 'delivered').length

        pendingByDay.push(dayPending)
        completedByDay.push(dayCompleted)
      }

      this.trendData = {
        days,
        pending: pendingByDay,
        completed: completedByDay
      }
    },
    initCharts() {
      this.initPieChart()
      this.initLineChart()
    },
    initPieChart() {
      if (!this.$refs.pieChart) return
      const chart = echarts.init(this.$refs.pieChart)
      const dispatched = this.stats.orders - this.stats.pending - this.stats.completed
      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c} ({d}%)'
        },
        series: [{
          type: 'pie',
          radius: ['50%', '75%'],
          center: ['50%', '50%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 6,
            borderColor: '#fff',
            borderWidth: 3
          },
          label: {
            show: false
          },
          emphasis: {
            label: {
              show: true,
              fontSize: 14,
              fontWeight: 'bold'
            }
          },
          data: [
            { value: this.stats.pending, name: '待配送', itemStyle: { color: '#f5576c' } },
            { value: dispatched, name: '已分配', itemStyle: { color: '#4facfe' } },
            { value: this.stats.completed, name: '已送达', itemStyle: { color: '#43e97b' } }
          ]
        }]
      }
      chart.setOption(option)
    },
    initLineChart() {
      if (!this.$refs.lineChart) return
      const chart = echarts.init(this.$refs.lineChart)
      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'cross' }
        },
        legend: {
          data: ['待配送', '已完成'],
          bottom: 0,
          textStyle: { color: '#6b7c8a' }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '15%',
          top: '10%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: this.trendData.days,
          axisLine: { lineStyle: { color: '#e0e0e0' } },
          axisLabel: { color: '#6b7c8a' }
        },
        yAxis: {
          type: 'value',
          axisLine: { show: false },
          axisLabel: { color: '#6b7c8a' },
          splitLine: { lineStyle: { color: '#f0f2f5' } }
        },
        series: [
          {
            name: '待配送',
            type: 'line',
            smooth: true,
            symbol: 'circle',
            symbolSize: 8,
            data: this.trendData.pending,
            itemStyle: { color: '#f5576c' },
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: 'rgba(245, 87, 108, 0.3)' },
                { offset: 1, color: 'rgba(245, 87, 108, 0.05)' }
              ])
            }
          },
          {
            name: '已完成',
            type: 'line',
            smooth: true,
            symbol: 'circle',
            symbolSize: 8,
            data: this.trendData.completed,
            itemStyle: { color: '#43e97b' },
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: 'rgba(67, 233, 123, 0.3)' },
                { offset: 1, color: 'rgba(67, 233, 123, 0.05)' }
              ])
            }
          }
        ]
      }
      chart.setOption(option)
    },
    getStatusType(status) {
      const typeMap = {
        'pending': 'warning',
        'dispatched': 'primary',
        'delivered': 'success'
      }
      return typeMap[status] || 'info'
    },
    getStatusText(status) {
      const textMap = {
        'pending': '待配送',
        'dispatched': '已分配',
        'delivered': '已送达'
      }
      return textMap[status] || status
    },
    getOrderClass(status) {
      const classMap = {
        'pending': 'pending',
        'dispatched': 'dispatched',
        'delivered': 'delivered'
      }
      return classMap[status] || 'pending'
    },
    getOrderIcon(status) {
      const iconMap = {
        'pending': 'el-icon-time',
        'dispatched': 'el-icon-truck',
        'delivered': 'el-icon-circle-check'
      }
      return iconMap[status] || 'el-icon-document'
    }
  }
}
</script>

<style scoped>
.dashboard-container {
  padding: 30px 40px;
  max-width: 1400px;
  margin: 0 auto;
  background: linear-gradient(180deg, #f8fafc 0%, #eef2f7 100%);
  min-height: calc(100vh - 66px);
}

.welcome-section {
  text-align: center;
  margin-bottom: 40px;
  padding: 30px 0;
}

.welcome-section h2 {
  font-size: 32px;
  font-weight: 600;
  color: #1a2a3a;
  margin-bottom: 12px;
}

.welcome-section .highlight {
  background: linear-gradient(135deg, #409EFF, #53A8FF);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.welcome-section .subtitle {
  font-size: 16px;
  color: #6b7c8a;
  letter-spacing: 1px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 280px);
  gap: 24px;
  margin-bottom: 40px;
  justify-content: flex-end;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 24px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  cursor: default;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
  font-size: 28px;
  color: white;
  flex-shrink: 0;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #8a9aaa;
  margin-top: 4px;
}

.charts-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 24px;
  margin-bottom: 30px;
}

.trend-actions-grid {
  display: grid;
  grid-template-columns: 1fr 380px;
  gap: 24px;
  margin-bottom: 30px;
}

.chart-card {
  background: white;
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
}

.chart-container {
  display: flex;
  align-items: center;
  gap: 20px;
  height: 280px;
}

.pie-chart {
  width: 200px;
  height: 200px;
  flex-shrink: 0;
}

.pie-legend {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.legend-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  flex-shrink: 0;
}

.legend-label {
  font-size: 14px;
  color: #6b7c8a;
  flex: 1;
}

.legend-value {
  font-size: 20px;
  font-weight: 700;
  color: #1a2a3a;
}

.line-chart {
  width: 100%;
  height: 240px;
}

.content-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 30px;
}

.quick-actions,
.recent-orders {
  background: white;
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 2px solid #f0f2f5;
}

.section-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1a2a3a;
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-header h3 i {
  color: #409EFF;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.action-btn {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.action-btn.primary {
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.1), rgba(83, 168, 255, 0.1));
  border-color: rgba(64, 158, 255, 0.2);
}

.action-btn.primary:hover {
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.2), rgba(83, 168, 255, 0.2));
  border-color: #409EFF;
  transform: translateX(8px);
}

.action-btn.success {
  background: linear-gradient(135deg, rgba(103, 194, 58, 0.1), rgba(123, 210, 78, 0.1));
  border-color: rgba(103, 194, 58, 0.2);
}

.action-btn.success:hover {
  background: linear-gradient(135deg, rgba(103, 194, 58, 0.2), rgba(123, 210, 78, 0.2));
  border-color: #67C23A;
  transform: translateX(8px);
}

.action-btn.warning {
  background: linear-gradient(135deg, rgba(230, 162, 60, 0.1), rgba(244, 178, 80, 0.1));
  border-color: rgba(230, 162, 60, 0.2);
}

.action-btn.warning:hover {
  background: linear-gradient(135deg, rgba(230, 162, 60, 0.2), rgba(244, 178, 80, 0.2));
  border-color: #E6A23C;
  transform: translateX(8px);
}

.action-btn.info {
  background: linear-gradient(135deg, rgba(90, 152, 222, 0.1), rgba(100, 170, 240, 0.1));
  border-color: rgba(90, 152, 222, 0.2);
}

.action-btn.info:hover {
  background: linear-gradient(135deg, rgba(90, 152, 222, 0.2), rgba(100, 170, 240, 0.2));
  border-color: #5A98DE;
  transform: translateX(8px);
}

.action-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  font-size: 20px;
}

.action-btn.primary .action-icon {
  background: linear-gradient(135deg, #409EFF, #53A8FF);
  color: white;
}

.action-btn.success .action-icon {
  background: linear-gradient(135deg, #67C23A, #85CE61);
  color: white;
}

.action-btn.warning .action-icon {
  background: linear-gradient(135deg, #E6A23C, #F56C6C);
  color: white;
}

.action-btn.info .action-icon {
  background: linear-gradient(135deg, #5A98DE, #64AAF0);
  color: white;
}

.action-text {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.action-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a2a3a;
}

.action-desc {
  font-size: 13px;
  color: #8a9aaa;
  margin-top: 2px;
}

.action-arrow {
  color: #c0c8d0;
  font-size: 18px;
  transition: transform 0.3s ease;
}

.action-btn:hover .action-arrow {
  transform: translateX(4px);
  color: #409EFF;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.order-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-radius: 12px;
  background: #f8fafc;
  transition: all 0.3s ease;
}

.order-item:hover {
  background: #f0f4f8;
  transform: translateX(4px);
}

.order-left {
  display: flex;
  align-items: center;
  gap: 14px;
}

.order-icon {
  width: 42px;
  height: 42px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  color: white;
}

.order-icon.pending {
  background: linear-gradient(135deg, #f093fb, #f5576c);
}

.order-icon.dispatched {
  background: linear-gradient(135deg, #4facfe, #00f2fe);
}

.order-icon.delivered {
  background: linear-gradient(135deg, #43e97b, #38f9d7);
}

.order-info {
  display: flex;
  flex-direction: column;
}

.order-no {
  font-size: 14px;
  font-weight: 600;
  color: #1a2a3a;
}

.order-address {
  font-size: 12px;
  color: #8a9aaa;
  margin-top: 2px;
  max-width: 280px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.order-right {
  flex-shrink: 0;
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: #c0c8d0;
}

.empty-state i {
  font-size: 48px;
  margin-bottom: 12px;
}

.empty-state p {
  margin: 0;
  font-size: 14px;
}

@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .charts-grid {
    grid-template-columns: 1fr;
  }

  .content-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .dashboard-container {
    padding: 20px;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .charts-grid {
    grid-template-columns: 1fr;
  }

  .chart-container {
    flex-direction: column;
    height: auto;
  }

  .pie-chart {
    width: 180px;
    height: 180px;
  }

  .welcome-section h2 {
    font-size: 24px;
  }

  .stat-card {
    padding: 18px;
  }

  .stat-value {
    font-size: 26px;
  }
}
</style>