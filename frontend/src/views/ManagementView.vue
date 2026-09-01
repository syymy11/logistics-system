<template>
  <div class="management-container">
    <div class="header">
      <h2>配送管理</h2>
    </div>

    <div class="content">
      <el-tabs v-model="activeTab" type="border-card">
        <el-tab-pane label="仓库管理" name="warehouses">
          <div class="panel-header">
            <h3>仓库信息</h3>
            <div class="header-actions">
              <el-button type="primary" @click="showWarehouseDialog = true">添加仓库</el-button>
            </div>
          </div>
          <el-table :data="warehouses" v-loading="loading.warehouses" border stripe>
            <el-table-column prop="name" label="仓库名称" width="150">
              <template slot-scope="scope">
                <span style="font-weight: 600; color: #67C23A;">{{ scope.row.name }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="address" label="地址" min-width="200" show-overflow-tooltip></el-table-column>
            <el-table-column label="坐标" width="200">
              <template slot-scope="scope">
                <span v-if="scope.row.longitude && scope.row.latitude">
                  {{ scope.row.longitude }}, {{ scope.row.latitude }}
                </span>
                <span v-else class="text-muted">未设置</span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template slot-scope="scope">
                <el-tag :type="scope.row.status === 'active' ? 'success' : 'info'" size="small">
                  {{ scope.row.status === 'active' ? '启用' : '停用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template slot-scope="scope">
                <el-button type="danger" size="small" @click="deleteWarehouse(scope.row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="车辆管理" name="vehicles">
          <div class="page-header">
            <h3>车辆信息</h3>
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
                    <div class="stat-value">{{ vehicleStats.total }}</div>
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
                    <div class="stat-value">{{ vehicleStats.idle }}</div>
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
                    <div class="stat-value">{{ vehicleStats.working }}</div>
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
                    <div class="stat-value">{{ vehicleStats.maintenance }}</div>
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

            <el-table :data="filteredVehicles" border v-loading="loading.vehicles" stripe style="width: 100%">
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

            <el-empty v-if="!loading.vehicles && filteredVehicles.length === 0" description="暂无车辆数据"></el-empty>
          </div>
        </el-tab-pane>

        <el-tab-pane label="限行区域管理" name="restrictedAreas">
          <div class="panel-header">
            <h3>限行区域配置</h3>
            <div class="header-actions">
              <el-button type="primary" @click="openAddRestrictedAreaDialog">添加限行区域</el-button>
            </div>
          </div>
          <el-table :data="restrictedAreas" v-loading="loading.restrictedAreas" border stripe>
            <el-table-column prop="name" label="区域名称" width="180">
              <template slot-scope="scope">
                <span style="font-weight: 600; color: #E6A23C;">{{ scope.row.name }}</span>
              </template>
            </el-table-column>
            <el-table-column label="中心点坐标" width="200">
              <template slot-scope="scope">
                <span v-if="scope.row.centerLng && scope.row.centerLat">
                  {{ scope.row.centerLng }}, {{ scope.row.centerLat }}
                </span>
                <span v-else class="text-muted">未设置</span>
              </template>
            </el-table-column>
            <el-table-column label="限行半径(公里)" width="130">
              <template slot-scope="scope">
                <span>{{ scope.row.radius || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="restriction" label="限制规则" min-width="200">
              <template slot-scope="scope">
                <el-tag v-if="scope.row.restriction === 'large_vehicle'" type="danger" size="small">大型车辆限行</el-tag>
                <span v-else>{{ scope.row.restriction }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template slot-scope="scope">
                <el-tag :type="scope.row.status === 'active' ? 'success' : 'info'" size="small">
                  {{ scope.row.status === 'active' ? '启用' : '停用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200">
              <template slot-scope="scope">
                <el-button type="primary" size="small" @click="openEditRestrictedAreaDialog(scope.row)">编辑</el-button>
                <el-button type="danger" size="small" @click="handleDeleteRestrictedArea(scope.row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!loading.restrictedAreas && restrictedAreas.length === 0" description="暂无限行区域配置"></el-empty>
        </el-tab-pane>
      </el-tabs>
    </div>

    <el-dialog title="添加仓库" :visible.sync="showWarehouseDialog" width="500px">
      <el-form :model="warehouseForm" label-width="100px">
        <el-form-item label="仓库名称">
          <el-input v-model="warehouseForm.name" placeholder="请输入仓库名称"></el-input>
        </el-form-item>
        <el-form-item label="地址">
          <div style="display: flex; gap: 10px; width: 100%;">
            <el-input v-model="warehouseForm.address" placeholder="请输入仓库地址" style="flex: 1;"></el-input>
            <el-button type="primary" @click="showMapPicker('warehouse')">
              <i class="el-icon-location"></i> 地图选点
            </el-button>
          </div>
        </el-form-item>
        <el-form-item label="经度">
          <el-input v-model="warehouseForm.longitude" placeholder="例如：126.5500"></el-input>
        </el-form-item>
        <el-form-item label="纬度">
          <el-input v-model="warehouseForm.latitude" placeholder="例如：43.8400"></el-input>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="warehouseForm.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="启用" value="active"></el-option>
            <el-option label="停用" value="inactive"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="showWarehouseDialog = false">取消</el-button>
        <el-button type="primary" @click="addWarehouse">确定</el-button>
      </span>
    </el-dialog>

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

    <el-dialog :title="restrictedAreaDialogTitle" :visible.sync="showRestrictedAreaDialog" width="550px" :close-on-click-modal="false">
      <el-form :model="restrictedAreaForm" :rules="restrictedAreaFormRules" ref="restrictedAreaFormRef" label-width="100px">
        <el-form-item label="区域名称" prop="name">
          <el-input v-model="restrictedAreaForm.name" placeholder="例如：市中心限行区"></el-input>
        </el-form-item>
        <el-form-item label="中心经度" prop="centerLng">
          <el-input v-model="restrictedAreaForm.centerLng" placeholder="例如：126.5500"></el-input>
        </el-form-item>
        <el-form-item label="中心纬度" prop="centerLat">
          <el-input v-model="restrictedAreaForm.centerLat" placeholder="例如：43.8400"></el-input>
        </el-form-item>
        <el-form-item label="限行半径(公里)" prop="radius">
          <el-input-number v-model="restrictedAreaForm.radius" :min="0.1" :max="100" :step="0.5" :precision="2" style="width: 100%"></el-input-number>
        </el-form-item>
        <el-form-item label="限制规则" prop="restriction">
          <el-select v-model="restrictedAreaForm.restriction" placeholder="请选择限制规则" style="width: 100%">
            <el-option label="大型车辆限行" value="large_vehicle"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="restrictedAreaForm.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="启用" value="active"></el-option>
            <el-option label="停用" value="inactive"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="showRestrictedAreaDialog = false">取消</el-button>
        <el-button type="primary" @click="handleRestrictedAreaSubmit" :loading="submitting">确定</el-button>
      </span>
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
import MapPicker from '@/components/MapPicker.vue'
import { getAllVehicles, addVehicle, updateVehicle, deleteVehicle, getWarehouses, addWarehouse, deleteWarehouse, getRestrictedAreas, addRestrictedArea, updateRestrictedArea, deleteRestrictedArea } from '@/api'

export default {
  name: 'ManagementView',
  components: { MapPicker },
  data() {
    return {
      activeTab: 'warehouses',
      warehouses: [],
      showWarehouseDialog: false,
      warehouseForm: {
        name: '',
        address: '',
        longitude: '',
        latitude: '',
        status: 'active'
      },
      loading: {
        warehouses: false,
        vehicles: false,
        restrictedAreas: false
      },
      submitting: false,
      showMapPickerDialog: false,
      currentPickerType: '',
      currentPickerLng: 126.55,
      currentPickerLat: 43.84,

      vehicles: [],
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
      },

      restrictedAreas: [],
      showRestrictedAreaDialog: false,
      isEditRestrictedArea: false,
      restrictedAreaForm: {
        id: null,
        name: '',
        centerLng: '',
        centerLat: '',
        radius: 5,
        restriction: 'large_vehicle',
        status: 'active'
      },
      restrictedAreaFormRules: {
        name: [
          { required: true, message: '请输入区域名称', trigger: 'blur' }
        ],
        centerLng: [
          { required: true, message: '请输入中心经度', trigger: 'blur' }
        ],
        centerLat: [
          { required: true, message: '请输入中心纬度', trigger: 'blur' }
        ],
        radius: [
          { required: true, message: '请输入限行半径', trigger: 'blur' }
        ],
        restriction: [
          { required: true, message: '请选择限制规则', trigger: 'change' }
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
    vehicleStats() {
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
    },
    restrictedAreaDialogTitle() {
      return this.isEditRestrictedArea ? '编辑限行区域' : '添加限行区域'
    }
  },
  mounted() {
    this.loadWarehouses()
    this.loadVehicles()
    this.loadRestrictedAreas()
  },
  methods: {
    async loadWarehouses() {
      this.loading.warehouses = true
      try {
        const res = await getWarehouses()
        this.warehouses = res.data || []
      } catch (error) {
        console.error('加载仓库数据失败:', error)
      } finally {
        this.loading.warehouses = false
      }
    },
    async addWarehouse() {
      if (!this.warehouseForm.name || !this.warehouseForm.address) {
        this.$message.warning('请填写完整信息')
        return
      }
      try {
        await addWarehouse(this.warehouseForm)
        this.$message.success('添加成功')
        this.showWarehouseDialog = false
        this.warehouseForm = { name: '', address: '', longitude: '', latitude: '', status: 'active' }
        this.loadWarehouses()
      } catch (error) {
        this.$message.error('添加失败')
      }
    },
    async deleteWarehouse(id) {
      try {
        await this.$confirm('确认删除该仓库?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        await deleteWarehouse(id)
        this.$message.success('删除成功')
        this.loadWarehouses()
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('删除失败')
        }
      }
    },

    async loadVehicles() {
      this.loading.vehicles = true
      try {
        const res = await getAllVehicles()
        this.vehicles = res.data || []
      } catch (error) {
        this.$message.error(`加载车辆列表失败: ${error.message}`)
      } finally {
        this.loading.vehicles = false
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
            const data = { ...this.vehicleForm }
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
    },

    showMapPicker(type) {
      this.currentPickerType = type
      if (type === 'warehouse' && this.warehouseForm.longitude && this.warehouseForm.latitude) {
        this.currentPickerLng = parseFloat(this.warehouseForm.longitude)
        this.currentPickerLat = parseFloat(this.warehouseForm.latitude)
      } else {
        this.currentPickerLng = 126.55
        this.currentPickerLat = 43.84
      }
      this.showMapPickerDialog = true
    },
    handleMapConfirm(result) {
      if (this.currentPickerType === 'warehouse') {
        this.warehouseForm.address = result.address
        this.warehouseForm.longitude = result.longitude
        this.warehouseForm.latitude = result.latitude
      }
      this.showMapPickerDialog = false
    },

    async loadRestrictedAreas() {
      this.loading.restrictedAreas = true
      try {
        const res = await getRestrictedAreas()
        this.restrictedAreas = res.data || []
      } catch (error) {
        this.$message.error(`加载限行区域列表失败: ${error.message}`)
      } finally {
        this.loading.restrictedAreas = false
      }
    },
    openAddRestrictedAreaDialog() {
      this.isEditRestrictedArea = false
      this.restrictedAreaForm = {
        id: null,
        name: '',
        centerLng: '',
        centerLat: '',
        radius: 5,
        restriction: 'large_vehicle',
        status: 'active'
      }
      this.showRestrictedAreaDialog = true
      this.$nextTick(() => {
        if (this.$refs.restrictedAreaFormRef) {
          this.$refs.restrictedAreaFormRef.clearValidate()
        }
      })
    },
    openEditRestrictedAreaDialog(area) {
      this.isEditRestrictedArea = true
      this.restrictedAreaForm = {
        id: area.id,
        name: area.name,
        centerLng: area.centerLng || '',
        centerLat: area.centerLat || '',
        radius: area.radius || 5,
        restriction: area.restriction || 'large_vehicle',
        status: area.status || 'active'
      }
      this.showRestrictedAreaDialog = true
      this.$nextTick(() => {
        if (this.$refs.restrictedAreaFormRef) {
          this.$refs.restrictedAreaFormRef.clearValidate()
        }
      })
    },
    async handleRestrictedAreaSubmit() {
      this.$refs.restrictedAreaFormRef.validate(async (valid) => {
        if (valid) {
          this.submitting = true
          try {
            const data = { ...this.restrictedAreaForm }
            if (this.isEditRestrictedArea) {
              await updateRestrictedArea(data.id, data)
              this.$message.success('限行区域更新成功')
            } else {
              await addRestrictedArea(data)
              this.$message.success('限行区域添加成功')
            }
            this.showRestrictedAreaDialog = false
            await this.loadRestrictedAreas()
          } catch (error) {
            this.$message.error(`操作失败: ${error.message}`)
          } finally {
            this.submitting = false
          }
        }
      })
    },
    handleDeleteRestrictedArea(area) {
      this.$confirm(`确定要删除限行区域 "${area.name}" 吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await deleteRestrictedArea(area.id)
          this.$message.success('限行区域删除成功')
          await this.loadRestrictedAreas()
        } catch (error) {
          this.$message.error(`删除失败: ${error.message}`)
        }
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.management-container {
  padding: 20px;
  height: calc(100vh - 60px);
  box-sizing: border-box;
}

.header {
  margin-bottom: 20px;
}

.header h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 500;
  color: #303133;
}

.content {
  height: calc(100% - 60px);
}

.content .el-tabs {
  height: 100%;
}

.content .el-tabs>>>.el-tabs__content {
  padding: 20px;
  height: calc(100% - 80px);
  overflow: auto;
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

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h3 {
  margin: 0;
  font-size: 18px;
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

.text-muted {
  color: #c0c4cc;
}

.plate-number {
  font-weight: 600;
  color: #409EFF;
}

.dialog-footer {
  text-align: right;
}

.header-actions {
  display: flex;
  gap: 10px;
}
</style>
