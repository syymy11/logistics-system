<template>
  <div class="vehicles-container">
    <div class="page-header">
      <h2>车辆管理</h2>
      <el-button type="primary" icon="el-icon-plus" @click="openAddDialog">添加车辆</el-button>
    </div>

    <div class="stats-row">
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon total">
              <i class="el-icon-truck"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.total }}</div>
              <div class="stat-label">总车辆数</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon idle">
              <i class="el-icon-circle-check"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.idle }}</div>
              <div class="stat-label">空闲车辆</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon working">
              <i class="el-icon-position"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.working }}</div>
              <div class="stat-label">配送中</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon maintenance">
              <i class="el-icon-setting"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.maintenance }}</div>
              <div class="stat-label">维护中</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <div class="table-container">
      <div class="table-header">
        <el-select v-model="filterStatus" placeholder="按状态筛选" size="small" @change="loadVehicles" clearable>
          <el-option label="空闲" value="idle"></el-option>
          <el-option label="配送中" value="on_way"></el-option>
          <el-option label="维护中" value="maintenance"></el-option>
        </el-select>
        <el-input v-model="searchKeyword" placeholder="搜索车牌号" size="small" style="width: 200px; margin-left: 10px;" @keyup.enter.native="loadVehicles" clearable>
          <el-button slot="append" icon="el-icon-search" @click="loadVehicles"></el-button>
        </el-input>
        <el-button size="small" icon="el-icon-refresh" @click="loadVehicles">刷新</el-button>
      </div>

      <el-table :data="filteredVehicles" border v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="plateNumber" label="车牌号" width="150">
          <template slot-scope="scope">
            <span class="plate-number">{{ scope.row.plateNumber }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="vehicleType" label="车辆类型" width="120">
          <template slot-scope="scope">
            <el-tag :type="getVehicleTypeTag(scope.row.vehicleType)" size="small">
              {{ getVehicleTypeText(scope.row.vehicleType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="vehicleCategory" label="车辆规格" width="110">
          <template slot-scope="scope">
            <el-tag :type="getVehicleCategoryTag(scope.row.vehicleCategory, scope.row.vehicleType)" size="small">
              {{ getVehicleCategoryText(scope.row.vehicleCategory, scope.row.vehicleType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="capacity" label="载重(kg)" width="120">
          <template slot-scope="scope">
            <span>{{ scope.row.capacity ? scope.row.capacity.toLocaleString() : '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="getStatusTag(scope.row.status)" size="small">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="currentLocation" label="当前位置" min-width="200" show-overflow-tooltip></el-table-column>
        <el-table-column label="坐标" width="180">
          <template slot-scope="scope">
            <span v-if="scope.row.longitude && scope.row.latitude">
              {{ scope.row.longitude }}, {{ scope.row.latitude }}
            </span>
            <span v-else class="text-muted">未设置</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template slot-scope="scope">
            <el-button size="mini" type="primary" icon="el-icon-edit" @click="openEditDialog(scope.row)">编辑</el-button>
            <el-button size="mini" type="danger" icon="el-icon-delete" @click="handleDelete(scope.row)" :disabled="scope.row.status !== 'idle'">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && filteredVehicles.length === 0" description="暂无车辆数据"></el-empty>
    </div>

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="500px" :close-on-click-modal="false">
      <el-form :model="vehicleForm" :rules="formRules" ref="vehicleFormRef" label-width="100px">
        <el-form-item label="车牌号" prop="plateNumber">
          <el-input v-model="vehicleForm.plateNumber" placeholder="请输入车牌号" :disabled="isEdit"></el-input>
        </el-form-item>
        <el-form-item label="车辆类型" prop="vehicleType">
          <el-select v-model="vehicleForm.vehicleType" placeholder="请选择车辆类型" style="width: 100%">
            <el-option label="厢式货车" value="van"></el-option>
            <el-option label="大型卡车" value="truck"></el-option>
            <el-option label="摩托车" value="motorcycle"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="车辆规格" prop="vehicleCategory">
          <el-select v-model="vehicleForm.vehicleCategory" placeholder="请选择车辆规格" style="width: 100%">
            <el-option label="小型车辆（可进市区）" value="small"></el-option>
            <el-option label="大型车辆（限城区）" value="large"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="载重(kg)" prop="capacity">
          <el-input-number v-model="vehicleForm.capacity" :min="0" :max="50000" :step="100" style="width: 100%"></el-input-number>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="vehicleForm.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="空闲" value="idle"></el-option>
            <el-option label="配送中" value="on_way"></el-option>
            <el-option label="维护中" value="maintenance"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="当前位置" prop="currentLocation">
          <el-input v-model="vehicleForm.currentLocation" placeholder="请输入当前位置描述"></el-input>
        </el-form-item>
        <el-form-item label="经度">
          <el-input v-model="vehicleForm.longitude" placeholder="例如：126.5500"></el-input>
        </el-form-item>
        <el-form-item label="纬度">
          <el-input v-model="vehicleForm.latitude" placeholder="例如：43.8400"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getAllVehicles, addVehicle, updateVehicle, deleteVehicle } from '@/api'

export default {
  name: 'VehiclesView',
  data() {
    return {
      vehicles: [],
      loading: false,
      submitting: false,
      filterStatus: '',
      searchKeyword: '',
      dialogVisible: false,
      isEdit: false,
      vehicleForm: {
        id: null,
        plateNumber: '',
        vehicleType: 'van',
        vehicleCategory: 'small',
        capacity: 5000,
        status: 'idle',
        currentLocation: '',
        longitude: '',
        latitude: ''
      },
      formRules: {
        plateNumber: [
          { required: true, message: '请输入车牌号', trigger: 'blur' }
        ],
        vehicleType: [
          { required: true, message: '请选择车辆类型', trigger: 'change' }
        ],
        status: [
          { required: true, message: '请选择状态', trigger: 'change' }
        ]
      }
    }
  },
  computed: {
    filteredVehicles() {
      let result = this.vehicles
      
      if (this.filterStatus) {
        result = result.filter(v => v.status === this.filterStatus)
      }
      
      if (this.searchKeyword) {
        const keyword = this.searchKeyword.toLowerCase()
        result = result.filter(v => 
          v.plateNumber.toLowerCase().includes(keyword)
        )
      }
      
      return result
    },
    stats() {
      const stats = {
        total: this.vehicles.length,
        idle: 0,
        working: 0,
        maintenance: 0
      }
      
      this.vehicles.forEach(v => {
        if (v.status === 'idle') stats.idle++
        else if (v.status === 'on_way') stats.working++
        else if (v.status === 'maintenance') stats.maintenance++
      })
      
      return stats
    },
    dialogTitle() {
      return this.isEdit ? '编辑车辆' : '添加车辆'
    }
  },
  mounted() {
    this.loadVehicles()
  },
  methods: {
    async loadVehicles() {
      this.loading = true
      try {
        const res = await getAllVehicles()
        this.vehicles = res.data || []
      } catch (error) {
        this.$message.error(`加载车辆列表失败: ${error.message}`)
      } finally {
        this.loading = false
      }
    },
    getVehicleTypeText(type) {
      const map = {
        'van': '厢式货车',
        'truck': '大型卡车',
        'motorcycle': '摩托车'
      }
      return map[type] || type
    },
    getVehicleTypeTag(type) {
      const map = {
        'van': 'primary',
        'truck': 'success',
        'motorcycle': 'warning'
      }
      return map[type] || 'info'
    },
    getVehicleCategoryText(category, vehicleType) {
      if (category === 'small') return '小型（可进市区）'
      if (category === 'large') return '大型（限城区）'
      if (vehicleType === 'truck') return '大型（限城区）'
      return '小型（可进市区）'
    },
    getVehicleCategoryTag(category, vehicleType) {
      if (category === 'large' || vehicleType === 'truck') return 'danger'
      return 'success'
    },
    getStatusText(status) {
      const map = {
        'idle': '空闲',
        'on_way': '配送中',
        'maintenance': '维护中'
      }
      return map[status] || status
    },
    getStatusTag(status) {
      const map = {
        'idle': 'success',
        'on_way': 'warning',
        'maintenance': 'danger'
      }
      return map[status] || 'info'
    },
    openAddDialog() {
      this.isEdit = false
      this.vehicleForm = {
        id: null,
        plateNumber: '',
        vehicleType: 'van',
        vehicleCategory: 'small',
        capacity: 5000,
        status: 'idle',
        currentLocation: '',
        longitude: '',
        latitude: ''
      }
      this.dialogVisible = true
      this.$nextTick(() => {
        if (this.$refs.vehicleFormRef) {
          this.$refs.vehicleFormRef.clearValidate()
        }
      })
    },
    openEditDialog(vehicle) {
      this.isEdit = true
      this.vehicleForm = {
        id: vehicle.id,
        plateNumber: vehicle.plateNumber,
        vehicleType: vehicle.vehicleType || 'van',
        vehicleCategory: vehicle.vehicleCategory || (vehicle.vehicleType === 'truck' ? 'large' : 'small'),
        capacity: vehicle.capacity || 5000,
        status: vehicle.status,
        currentLocation: vehicle.currentLocation || '',
        longitude: vehicle.longitude || '',
        latitude: vehicle.latitude || ''
      }
      this.dialogVisible = true
      this.$nextTick(() => {
        if (this.$refs.vehicleFormRef) {
          this.$refs.vehicleFormRef.clearValidate()
        }
      })
    },
    async handleSubmit() {
      this.$refs.vehicleFormRef.validate(async (valid) => {
        if (valid) {
          this.submitting = true
          try {
            const data = {
              ...this.vehicleForm
            }
            
            if (this.isEdit) {
              await updateVehicle(data)
              this.$message.success('车辆更新成功')
            } else {
              await addVehicle(data)
              this.$message.success('车辆添加成功')
            }
            
            this.dialogVisible = false
            await this.loadVehicles()
          } catch (error) {
            this.$message.error(`操作失败: ${error.message}`)
          } finally {
            this.submitting = false
          }
        }
      })
    },
    handleDelete(vehicle) {
      this.$confirm(`确定要删除车辆 "${vehicle.plateNumber}" 吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await deleteVehicle(vehicle.id)
          this.$message.success('车辆删除成功')
          await this.loadVehicles()
        } catch (error) {
          this.$message.error(`删除失败: ${error.message}`)
        }
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.vehicles-container {
  padding: 20px;
  min-height: calc(100vh - 60px);
  background: #f0f2f5;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  margin-right: 15px;
}

.stat-icon.total {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.stat-icon.idle {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  color: white;
}

.stat-icon.working {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
}

.stat-icon.maintenance {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: white;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}

.table-container {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.table-header {
  display: flex;
  margin-bottom: 15px;
  align-items: center;
}

.plate-number {
  font-weight: 600;
  color: #409EFF;
}

.text-muted {
  color: #c0c4cc;
}

.dialog-footer {
  text-align: right;
}
</style>
