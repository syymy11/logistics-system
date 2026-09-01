<template>
  <div class="orders-container">
    <div class="header-section">
      <h2>订单管理</h2>
    </div>

    <div class="filter-section">
      <el-select v-model="statusFilter" placeholder="订单状态" @change="handleFilterChange">
        <el-option label="全部订单" value="all"></el-option>
        <el-option label="待配送" value="pending"></el-option>
        <el-option label="已分配" value="dispatched"></el-option>
        <el-option label="配送中" value="in_transit"></el-option>
        <el-option label="已送达" value="delivered"></el-option>
      </el-select>
      <el-button type="primary" @click="showAddDialog = true">添加订单</el-button>
    </div>

    <el-table :data="orders" v-loading="loading" border stripe>
      <el-table-column prop="orderNo" label="订单号" width="180"></el-table-column>
      <el-table-column prop="customerName" label="客户名称" width="120"></el-table-column>
      <el-table-column prop="customerPhone" label="联系电话" width="130"></el-table-column>
      <el-table-column prop="customerAddress" label="配送地址" show-overflow-tooltip></el-table-column>
      <el-table-column prop="weight" label="重量(kg)" width="100"></el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template slot-scope="scope">
          <el-tag :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="160">
        <template slot-scope="scope">
          {{ formatDate(scope.row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template slot-scope="scope">
          <el-button type="text" size="small" @click="viewOrder(scope.row)">查看</el-button>
          <el-button v-if="scope.row.status !== 'pending'" type="text" size="small" @click="resetOrder(scope.row.id)">重置</el-button>
          <el-button type="text" size="small" @click="deleteOrder(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination v-if="total > 0" background layout="prev, pager, next" :total="total" :page-size="pageSize"
      :current-page="currentPage" @current-change="handlePageChange" class="pagination">
    </el-pagination>

    <el-dialog title="添加订单" :visible.sync="showAddDialog" width="600px">
      <el-form :model="orderForm" label-width="100px">
        <el-form-item label="订单号">
          <el-input v-model="orderForm.orderNo" placeholder="请输入订单号"></el-input>
        </el-form-item>
        <el-form-item label="客户名称">
          <el-input v-model="orderForm.customerName" placeholder="请输入客户名称"></el-input>
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="orderForm.customerPhone" placeholder="请输入联系电话"></el-input>
        </el-form-item>
        <el-form-item label="配送地址">
          <div style="display: flex; gap: 10px; width: 100%;">
            <el-input v-model="orderForm.customerAddress" placeholder="请输入配送地址" style="flex: 1;"></el-input>
            <el-button type="primary" @click="showMapPicker">
              <i class="el-icon-location"></i> 地图选点
            </el-button>
          </div>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="经度">
              <el-input v-model="orderForm.longitude" placeholder="经度"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="纬度">
              <el-input v-model="orderForm.latitude" placeholder="纬度"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="重量(kg)">
          <el-input-number v-model="orderForm.weight" :min="0.1" :max="10000" :step="0.1"></el-input-number>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="addOrder">确定</el-button>
      </span>
    </el-dialog>

    <el-dialog title="订单详情" :visible.sync="showDetailDialog" width="600px">
      <el-descriptions :column="2" border v-if="currentOrder">
        <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentOrder.status)">{{ getStatusText(currentOrder.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="客户名称">{{ currentOrder.customerName }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentOrder.customerPhone }}</el-descriptions-item>
        <el-descriptions-item label="配送地址" :span="2">{{ currentOrder.customerAddress }}</el-descriptions-item>
        <el-descriptions-item label="重量">{{ currentOrder.weight }} kg</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDate(currentOrder.createTime) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <MapPicker
      :visible.sync="showMapPickerDialog"
      :defaultLng="currentPickerLng"
      :defaultLat="currentPickerLat"
      @confirm="handleMapConfirm"
    ></MapPicker>
  </div>
</template>

<script>
import { getOrders, addOrder as addOrderApi, deleteOrder as deleteOrderApi, resetOrder as resetOrderApi } from '@/api'
import MapPicker from '@/components/MapPicker.vue'

export default {
  name: 'OrdersView',
  components: { MapPicker },
  data() {
    return {
      orders: [],
      loading: false,
      statusFilter: 'all',
      currentPage: 1,
      pageSize: 10,
      total: 0,
      showAddDialog: false,
      showDetailDialog: false,
      currentOrder: null,
      orderForm: {
        orderNo: '',
        customerName: '',
        customerPhone: '',
        customerAddress: '',
        longitude: '',
        latitude: '',
        weight: 1
      },
      showMapPickerDialog: false,
      currentPickerLng: 126.55,
      currentPickerLat: 43.84
    }
  },
  mounted() {
    this.loadOrders()
  },
  methods: {
    async loadOrders() {
      this.loading = true
      try {
        const params = {
          page: this.currentPage,
          size: this.pageSize
        }
        if (this.statusFilter !== 'all') {
          params.status = this.statusFilter
        }
        const res = await getOrders(this.currentPage, this.pageSize, this.statusFilter === 'all' ? null : this.statusFilter)
        this.orders = res.data || []
        this.total = res.total || this.orders.length
      } catch (error) {
        console.error('加载订单失败:', error)
        this.$message.error('加载订单失败')
      } finally {
        this.loading = false
      }
    },
    handleFilterChange() {
      this.currentPage = 1
      this.loadOrders()
    },
    handlePageChange(page) {
      this.currentPage = page
      this.loadOrders()
    },
    async addOrder() {
      if (!this.orderForm.orderNo || !this.orderForm.customerName || !this.orderForm.customerAddress) {
        this.$message.warning('请填写完整信息')
        return
      }
      if (!this.orderForm.longitude || !this.orderForm.latitude) {
        this.$message.warning('请在地图上选择位置')
        return
      }
      try {
        await addOrderApi(this.orderForm)
        this.$message.success('添加成功')
        this.resetOrderForm()
        this.showAddDialog = false
        this.loadOrders()
      } catch (error) {
        this.$message.error('添加失败')
      }
    },
    resetOrderForm() {
      this.orderForm = {
        orderNo: '',
        customerName: '',
        customerPhone: '',
        customerAddress: '',
        longitude: '',
        latitude: '',
        weight: 1
      }
    },
    showMapPicker() {
      if (this.orderForm.longitude && this.orderForm.latitude) {
        this.currentPickerLng = parseFloat(this.orderForm.longitude)
        this.currentPickerLat = parseFloat(this.orderForm.latitude)
      } else {
        this.currentPickerLng = 126.55
        this.currentPickerLat = 43.84
      }
      this.showMapPickerDialog = true
    },
    handleMapConfirm(result) {
      this.orderForm.customerAddress = result.address
      this.orderForm.longitude = result.longitude
      this.orderForm.latitude = result.latitude
      this.showMapPickerDialog = false
    },
    viewOrder(order) {
      this.currentOrder = order
      this.showDetailDialog = true
    },
    async deleteOrder(id) {
      try {
        await this.$confirm('确认删除该订单?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        await deleteOrderApi(id)
        this.$message.success('删除成功')
        this.loadOrders()
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('删除失败')
        }
      }
    },
    async resetOrder(id) {
      try {
        await this.$confirm('确认将该订单重置为未配送状态?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        await resetOrderApi(id)
        this.$message.success('重置成功，订单已恢复为未配送状态')
        this.loadOrders()
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('重置失败')
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
    formatDate(date) {
      if (!date) return '-'
      const d = new Date(date)
      return d.toLocaleString('zh-CN')
    }
  }
}
</script>

<style scoped>
.orders-container {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.header-section {
  margin-bottom: 20px;
}

.header-section h2 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.filter-section {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  text-align: center;
}
</style>