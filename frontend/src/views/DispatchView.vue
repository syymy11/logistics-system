<!-- views/DispatchView.vue -->
<template>
  <div class="dispatch-container">
    <!-- 左侧：订单列表和路径规划 -->
    <div class="left-panel">
      <!-- 订单列表 -->
      <div class="order-panel">
        <div class="panel-header">
          <h3>订单列表</h3>
          <div class="header-actions">
            <el-select v-model="orderStatus" placeholder="订单状态" size="small" @change="loadOrders">
              <el-option label="全部" value=""></el-option>
              <el-option label="待配送" value="pending"></el-option>
              <el-option label="已分配" value="dispatched"></el-option>
              <el-option label="已配送" value="delivered"></el-option>
            </el-select>
            <el-button type="primary" size="small" @click="loadOrders" :loading="loading.orders"
              style="margin-left: 10px;">
              刷新
            </el-button>
          </div>
        </div>
        <el-table :data="orders" height="100%" @selection-change="handleSelectionChange" v-loading="loading.orders"
          element-loading-text="加载中...">
          <el-table-column type="selection" width="55"></el-table-column>
          <el-table-column prop="orderNo" label="订单号" width="180"></el-table-column>
          <el-table-column prop="customerName" label="客户名称" width="120"></el-table-column>
          <el-table-column prop="customerAddress" label="地址" min-width="150" show-overflow-tooltip></el-table-column>
          <el-table-column prop="weight" label="重量(kg)" width="80"></el-table-column>
          <el-table-column label="操作" width="120">
            <template slot-scope="scope">
              <el-button v-if="scope.row.status === 'dispatched'" type="primary" size="small"
                @click="trackOrder(scope.row.id)">
                跟踪
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="dispatch-form">
          <el-form label-width="80px">
            <el-form-item label="选择车辆">
              <el-select v-model="selectedVehicleId" placeholder="自动选择" :loading="loading.vehicles" clearable
                :disabled="selectedOrders.length === 0">
                <el-option label="系统自动选择" :value="null"></el-option>
                <el-option v-for="v in vehicles" :key="v.id" :label="getVehicleLabel(v)"
                  :value="v.id" :disabled="isVehicleDisabled(v)">
                  <span style="display: flex; justify-content: space-between; align-items: center;">
                    <span>{{ v.plateNumber }} (载重: {{ v.capacity }}kg)</span>
                    <span :style="{marginLeft: '10px'}">
                      <el-tag v-if="isLargeVehicle(v)" size="mini" type="danger" effect="dark">大车（限城区）</el-tag>
                      <el-tag v-else size="mini" type="success" effect="dark">小车（可进市区）</el-tag>
                    </span>
                  </span>
                </el-option>
              </el-select>
            </el-form-item>
            <div v-if="hasUrbanOrders && selectedVehicleId" class="restriction-hint">
              <el-alert
                :title="isLargeVehicleById(selectedVehicleId) ? '⚠️ 所选车辆为大车，有市区订单可能无法配送' : '✅ 所选车辆为小车，可以配送市区订单'"
                :type="isLargeVehicleById(selectedVehicleId) ? 'warning' : 'success'"
                :closable="false"
                show-icon
                size="small">
              </el-alert>
            </div>
            <el-form-item label="路线策略">
              <el-select v-model="routePolicy" placeholder="请选择" size="small">
                <el-option label="最短距离" value="LEAST_DISTANCE"></el-option>
                <el-option label="最短时间" value="LEAST_TIME"></el-option>
                <el-option label="避免高速" value="AVOID_HIGHWAYS"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="success" @click="startDispatch" :loading="loading.dispatch"
                :disabled="selectedOrders.length === 0">
                开始智能调度
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>

    </div>

    <!-- 右侧：地图可视化 -->
    <div class="map-panel">
      <div id="mapContainer" class="map-container" v-loading="loading.map" element-loading-text="地图加载中..."></div>
      <div id="panel" class="route-panel"></div>
      <div class="task-info" v-if="currentTask && showTaskInfo" @click="toggleTaskInfo">
        <h4>任务信息 <span style="float: right; font-size: 10px; cursor: pointer;">点击隐藏</span></h4>
        <p>任务编号：{{ currentTask.taskNo }}</p>
        <p>总距离：{{ currentTask.totalDistance }} km</p>
        <p>状态：<el-tag :type="taskStatusType">{{ taskStatusText }}</el-tag></p>
      </div>
    </div>
  </div>
</template>

<script>
import AMapLoader from '@amap/amap-jsapi-loader'
import { getPendingOrders, getVehicles, planDelivery, getRoute, getOrdersByStatus, getTaskByOrderId, getTaskDetails } from '@/api'

export default {
  name: 'DispatchView',
  data() {
    return {
      orders: [],
      selectedOrders: [],
      vehicles: [],
      selectedVehicleId: null,
      currentTask: null,
      showTaskInfo: true,
      map: null,
      markerList: [],
      polyline: null,
      driving: null,
      AMap: null,
      orderStatus: '',
      routePolicy: 'LEAST_DISTANCE',
      loading: {
        orders: false,
        vehicles: false,
        dispatch: false,
        map: true
      }
    }
  },
  computed: {
    taskStatusText() {
      const statusMap = {
        'planned': '已规划',
        'in_progress': '配送中',
        'completed': '已完成'
      }
      return this.currentTask ? statusMap[this.currentTask.status] : ''
    },
    taskStatusType() {
      const typeMap = {
        'planned': 'info',
        'in_progress': 'warning',
        'completed': 'success'
      }
      return this.currentTask ? typeMap[this.currentTask.status] : 'info'
    },
    hasUrbanOrders() {
      if (!this.selectedOrders || this.selectedOrders.length === 0) return false
      const urbanKeywords = ['市中心', '城区', '市区', '商业街', '步行街', '广场', '购物', 'CBD', '写字楼', '商业区']
      return this.selectedOrders.some(order => {
        if (!order.customerAddress) return false
        return urbanKeywords.some(kw => order.customerAddress.includes(kw))
      })
    }
  },
  mounted() {
    this.initMap()
    this.loadData()
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize)
    if (this.map) {
      this.map.destroy()
    }
  },
  methods: {
    toggleTaskInfo() {
      this.showTaskInfo = !this.showTaskInfo
    },
    handleResize() {
      if (this.map) {
        this.map.resize()
      }
    },
    async loadData() {
      try {
        await Promise.all([
          this.loadOrders(),
          this.loadVehicles()
        ])
      } catch (error) {
        console.error('加载数据失败:', error)
      }
    },
    async loadOrders() {
      this.loading.orders = true
      try {
        let res
        if (this.orderStatus === 'pending') {
          res = await getPendingOrders()
        } else {
          res = await getOrdersByStatus(this.orderStatus)
        }
        this.orders = res.data
      } catch (error) {
        this.$message.error(`加载订单失败: ${error.message}`)
      } finally {
        this.loading.orders = false
      }
    },
    async loadVehicles() {
      this.loading.vehicles = true
      try {
        const res = await getVehicles()
        this.vehicles = res.data
      } catch (error) {
        this.$message.error(`加载车辆失败: ${error.message}`)
      } finally {
        this.loading.vehicles = false
      }
    },
    handleSelectionChange(selection) {
      this.selectedOrders = selection
    },
    isLargeVehicle(v) {
      if (!v) return false
      if (v.vehicleCategory === 'large') return true
      if (v.vehicleType === 'truck') return true
      return false
    },
    isLargeVehicleById(vehicleId) {
      const vehicle = this.vehicles.find(v => v.id === vehicleId)
      return this.isLargeVehicle(vehicle)
    },
    getVehicleLabel(v) {
      const type = this.isLargeVehicle(v) ? '大车' : '小车'
      return `${v.plateNumber} (${type}, 载重: ${v.capacity}kg)`
    },
    isVehicleDisabled(v) {
      if (!this.hasUrbanOrders) return false
      return this.isLargeVehicle(v)
    },
    async startDispatch() {
      if (this.selectedOrders.length === 0) {
        this.$message.warning('请选择订单')
        return
      }

      // 检查选中的订单是否都是待配送状态
      const nonPendingOrders = this.selectedOrders.filter(o => o.status !== 'pending')
      if (nonPendingOrders.length > 0) {
        this.$message.warning('只能调度待配送状态的订单')
        return
      }

      this.loading.dispatch = true
      try {
        const orderIds = this.selectedOrders.map(o => o.id)
        const res = await planDelivery(orderIds, this.selectedVehicleId)
        this.currentTask = res.data
        this.$message.success('调度成功')
        await this.showRoute(this.currentTask.id)
        this.loadOrders()  // 刷新订单列表
      } catch (error) {
        this.$message.error(`调度失败: ${error.message}`)
      } finally {
        this.loading.dispatch = false
      }
    },
    async trackOrder(orderId) {
      this.loading.dispatch = true
      try {
        const taskRes = await getTaskByOrderId(orderId)
        const task = taskRes.data

        const taskDetailsRes = await getTaskDetails(task.id)
        const taskDetails = taskDetailsRes.data

        this.currentTask = taskDetails.task

        await this.showRoute(task.id)

        this.$message.success('开始跟踪订单')
      } catch (error) {
        if (error.message === '订单未分配任务') {
          this.$message.warning('该订单尚未调度，请在"智能调度"页面选择订单和车辆后点击"开始智能调度"')
        } else {
          this.$message.error(`跟踪失败: ${error.message}`)
        }
      } finally {
        this.loading.dispatch = false
      }
    },
    initMap() {
      window._AMapSecurityConfig = {
        securityJsCode: "b3560386f4c1e5c7c0e62826e8ea8dd1",
      };
      AMapLoader.load({
        key: '575f3f0bcbcba88fbe0b87c96cfb9cd8', // 使用与后端相同的API Key
        version: '2.0',
        plugins: ['AMap.Marker', 'AMap.Polyline', 'AMap.InfoWindow', 'AMap.Driving']
      }).then(AMap => {
        this.AMap = AMap
        this.map = new AMap.Map('mapContainer', {
          zoom: 12,
          center: [116.397428, 39.90923],
          viewMode: '2D',
          resizeEnable: true
        })
        this.loading.map = false
      }).catch(e => {
        console.error('地图加载失败', e)
        this.loading.map = false
        this.$message.error('地图加载失败，请刷新页面重试')
      })
    },
    async showRoute(taskId) {
      try {
        const res = await getRoute(taskId)
        if (res.data && res.data.length > 0) {
          // 清除旧标记和路线
          this.clearMap()

          // 转换坐标格式
          const points = res.data.map(coord => [parseFloat(coord[0]), parseFloat(coord[1])])

          // 添加标记点
          if (this.AMap) {
            points.forEach((point, index) => {
              const marker = new this.AMap.Marker({
                position: point,
                map: this.map,
                label: {
                  content: index === 0 ? '仓库' : `客户${index}`,
                  offset: new this.AMap.Pixel(0, -20)
                }
              })
              this.markerList.push(marker)
            })

            // 使用 AMap.plugin 加载 Driving 插件
            this.AMap.plugin('AMap.Driving', () => {
              // 构建路径规划的起终点和途经点
              const routePoints = points.map(point => {
                return new this.AMap.LngLat(point[0], point[1])
              })

              console.log('开始路径规划，路线点:', routePoints)

              // 使用高德地图的路线规划服务，不使用panel
              let policy = this.AMap.DrivingPolicy.LEAST_DISTANCE
              if (this.routePolicy === 'LEAST_TIME') {
                policy = this.AMap.DrivingPolicy.LEAST_TIME
              } else if (this.routePolicy === 'AVOID_HIGHWAYS') {
                policy = this.AMap.DrivingPolicy.AVOID_HIGHWAYS
              }
              this.driving = new this.AMap.Driving({
                map: this.map,
                panel: 'panel',
                policy: policy
              })
              const driving = this.driving

              // 构建途经点参数
              const waypoints = []
              if (routePoints.length > 2) {
                for (let i = 1; i < routePoints.length - 1; i++) {
                  waypoints.push(routePoints[i])
                }
              }

              // 执行路径规划
              driving.search(routePoints[0], routePoints[routePoints.length - 1], {
                waypoints: waypoints
              }, (status, result) => {
                console.log('路径规划结果，状态:', status, '结果:', result)
                if (status === 'complete') {
                  console.log('绘制驾车路线完成')
                } else {
                  console.error('获取驾车数据失败：', result)
                  // 路径规划失败时，使用直线连接作为备用方案
                  this.polyline = new this.AMap.Polyline({
                    path: points,
                    map: this.map,
                    strokeColor: '#409EFF',
                    strokeWeight: 4,
                    strokeOpacity: 0.8,
                    lineJoin: 'round'
                  })

                  // 调整视野
                  this.map.setFitView(this.markerList.concat(this.polyline))
                }
              })
            })
          }
        }
      } catch (error) {
        console.error('获取路线失败:', error)
        this.$message.error(`获取路线失败: ${error.message}`)
      }
    },
    clearMap() {
      // 清除标记
      this.markerList.forEach(marker => {
        if (marker && marker.setMap) {
          marker.setMap(null)
        }
      })
      this.markerList = []

      // 清除路线
      if (this.polyline && this.polyline.setMap) {
        this.polyline.setMap(null)
        this.polyline = null
      }

      // 清除路径规划
      if (this.driving) {
        this.driving.clear()
        this.driving = null
      }
    }
  }
}
</script>

<style scoped>
.dispatch-container {
  display: flex;
  height: calc(100vh - 60px);
  padding: 0;
  gap: 0;
  box-sizing: border-box;
}

.left-panel {
  width: 380px;
  display: flex;
  flex-direction: column;
  gap: 0;
  height: 100%;
  overflow: hidden;
  background: white;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
}

.order-panel {
  flex: 1;
  background: white;
  padding: 15px;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.order-panel .el-table {
  flex: 1;
  min-height: 0;
  font-size: 14px;
  overflow: auto;
}

.order-panel .el-table td {
  padding: 12px 8px;
  line-height: 1.5;
}

.map-panel {
  flex: 1;
  position: relative;
  background: #f5f5f5;
  overflow: hidden;
  min-height: 0;
}

.map-container {
  width: 100%;
  height: 100%;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.panel-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 500;
}

.header-actions {
  display: flex;
  align-items: center;
}

.header-actions .el-select {
  width: 120px;
}

.dispatch-form {
  margin-top: 20px;
  padding-top: 15px;
  border-top: 1px solid #eee;
  margin-top: auto;
}

.restriction-hint {
  margin-top: -10px;
  margin-bottom: 10px;
}

.route-panel {
  position: absolute;
  top: 5px;
  left: 5px;
  width: 260px;
  max-height: 280px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  z-index: 99;
  overflow: auto;
}

.task-info {
  position: absolute;
  bottom: 10px;
  right: 10px;
  background: white;
  padding: 10px;
  border-radius: 4px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
  font-size: 12px;
  max-width: 250px;
  z-index: 90;
  transition: all 0.3s ease;
}

.task-info:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  transform: translateY(-2px);
}

.task-info h4 {
  margin: 0 0 4px 0;
  font-size: 12px;
  font-weight: 500;
  border-bottom: 1px solid #eee;
  padding-bottom: 3px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.task-info p {
  margin: 3px 0;
  line-height: 1.2;
}

@media (max-width: 768px) {
  .task-info {
    max-width: 150px;
    font-size: 10px;
  }

  .route-panel {
    width: 220px;
    max-height: 40%;
  }

  .map-container {
    height: calc(100vh - 70px);
  }
}

@media (max-width: 1200px) {
  .order-panel {
    width: 400px;
  }
}

@media (max-width: 992px) {
  .dispatch-container {
    flex-direction: column;
    height: auto;
    min-height: calc(100vh - 60px);
  }

  .order-panel {
    width: 100%;
    margin-bottom: 20px;
  }

  .map-panel {
    height: 500px;
  }
}
</style>
