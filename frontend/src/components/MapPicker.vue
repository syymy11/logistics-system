<template>
  <div class="map-picker-dialog">
    <el-dialog
      title="地图选址"
      :visible.sync="dialogVisible"
      width="900px"
      :close-on-click-modal="false"
      @close="handleClose"
    >
      <div class="map-picker-container">
        <div class="search-section">
          <el-input
            v-model="searchKeyword"
            placeholder="输入地址搜索（如：吉林市船营区）"
            class="search-input"
            @keyup.enter.native="searchAddress"
          >
            <el-button slot="append" icon="el-icon-search" @click="searchAddress">搜索</el-button>
          </el-input>
          <div class="search-tip">提示：也可以直接点击地图选点</div>
        </div>
        <div class="map-wrapper">
          <div id="pickerMap" class="picker-map"></div>
          <div class="marker-info" v-if="selectedAddress">
            <div class="info-title"><i class="el-icon-location"></i> 已选位置</div>
            <div class="info-address">{{ selectedAddress }}</div>
            <div class="info-coord">经度：{{ selectedLng }}，纬度：{{ selectedLat }}</div>
          </div>
        </div>
        <div class="result-section">
          <el-form label-width="80px">
            <el-form-item label="地址">
              <el-input v-model="resultAddress" placeholder="地址" style="width: 100%"></el-input>
            </el-form-item>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="经度">
                  <el-input v-model="resultLng" placeholder="经度"></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="纬度">
                  <el-input v-model="resultLat" placeholder="纬度"></el-input>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </div>
      </div>
      <span slot="footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="confirmSelect" :disabled="!selectedAddress">确定选择</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'MapPicker',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    defaultLng: {
      type: Number,
      default: 126.55
    },
    defaultLat: {
      type: Number,
      default: 43.84
    }
  },
  data() {
    return {
      map: null,
      marker: null,
      placeSearch: null,
      searchKeyword: '',
      selectedAddress: '',
      selectedLng: '',
      selectedLat: '',
      resultAddress: '',
      resultLng: '',
      resultLat: ''
    }
  },
  computed: {
    dialogVisible: {
      get() {
        return this.visible
      },
      set(val) {
        this.$emit('update:visible', val)
      }
    }
  },
  watch: {
    visible(val) {
      if (val) {
        this.$nextTick(() => {
          this.initMap()
        })
      }
    }
  },
  methods: {
    getAMap() {
      return window.AMap
    },
    initMap() {
      if (this.map) {
        this.map.destroy()
      }

      window._AMapSecurityConfig = {
        securityJsCode: "b3560386f4c1e5c7c0e62826e8ea8dd1"
      }

      if (this.getAMap()) {
        this.createMap()
      } else {
        const script = document.createElement('script')
        script.src = 'https://webapi.amap.com/maps?v=2.0&key=61867fc56137e8daa7e7a60a5e01c18b'
        script.onload = () => {
          this.createMap()
        }
        document.head.appendChild(script)
      }
    },
    createMap() {
      const AMap = this.getAMap()
      const centerLng = this.resultLng || this.defaultLng
      const centerLat = this.resultLat || this.defaultLat

      this.map = new AMap.Map('pickerMap', {
        zoom: 13,
        center: [centerLng, centerLat]
      })

      this.map.on('click', (e) => {
        this.handleMapClick(e)
      })

      this.map.addControl(new AMap.ToolBar())
      this.map.addControl(new AMap.Scale())

      if (this.resultLng && this.resultLat) {
        this.addMarker(this.resultLng, this.resultLat)
      }
    },
    handleMapClick(e) {
      const AMap = this.getAMap()
      const lng = e.lnglat.getLng()
      const lat = e.lnglat.getLat()

      this.selectedLng = lng.toFixed(6)
      this.selectedLat = lat.toFixed(6)
      this.resultLng = this.selectedLng
      this.resultLat = this.selectedLat

      this.addMarker(lng, lat)

      AMap.plugin('AMap.Geocoder', () => {
        const geocoder = new AMap.Geocoder()
        geocoder.getAddress([lng, lat], (status, result) => {
          if (status === 'complete' && result.regeocode) {
            this.selectedAddress = result.regeocode.formattedAddress
            this.resultAddress = this.selectedAddress
          } else {
            this.selectedAddress = '位置 (' + lng.toFixed(4) + ', ' + lat.toFixed(4) + ')'
            this.resultAddress = this.selectedAddress
          }
        })
      })
    },
    addMarker(lng, lat) {
      const AMap = this.getAMap()
      if (this.marker) {
        this.map.remove(this.marker)
      }

      this.marker = new AMap.Marker({
        position: [lng, lat],
        draggable: true
      })

      this.marker.on('dragend', (e) => {
        this.handleMapClick(e)
      })

      this.map.add(this.marker)
      this.map.setCenter([lng, lat])
    },
    searchAddress() {
      const AMap = this.getAMap()
      if (!this.searchKeyword.trim()) {
        this.$message.warning('请输入搜索关键词')
        return
      }

      AMap.plugin('AMap.PlaceSearch', () => {
        if (!this.placeSearch) {
          this.placeSearch = new AMap.PlaceSearch({
            pageSize: 5,
            pageIndex: 1,
            extensions: 'all'
          })
        }

        this.placeSearch.search(this.searchKeyword, (status, result) => {
          if (status === 'complete' && result.poiList && result.poiList.pois.length > 0) {
            const poi = result.poiList.pois[0]
            const location = poi.location

            this.selectedLng = location.lng.toFixed(6)
            this.selectedLat = location.lat.toFixed(6)
            this.resultLng = this.selectedLng
            this.resultLat = this.selectedLat
            this.selectedAddress = poi.address || poi.name
            this.resultAddress = this.selectedAddress

            this.addMarker(location.lng, location.lat)
            this.map.setZoomAndCenter(15, [location.lng, location.lat])

            this.$message.success('找到位置：' + this.selectedAddress)
          } else {
            this.$message.warning('未找到该地址，请尝试其他关键词')
          }
        })
      })
    },
    confirmSelect() {
      if (!this.resultAddress) {
        this.$message.warning('请选择地址')
        return
      }
      if (!this.resultLng || !this.resultLat) {
        this.$message.warning('请在地图上选择位置')
        return
      }

      this.$emit('confirm', {
        address: this.resultAddress,
        longitude: parseFloat(this.resultLng),
        latitude: parseFloat(this.resultLat)
      })
      this.$emit('update:visible', false)
    },
    handleClose() {
      this.$emit('update:visible', false)
    }
  }
}
</script>

<style scoped>
.map-picker-dialog {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.map-picker-container {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.search-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.search-input {
  flex: 1;
}

.search-tip {
  font-size: 12px;
  color: #909399;
}

.map-wrapper {
  position: relative;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.picker-map {
  height: 400px;
  width: 100%;
}

.marker-info {
  position: absolute;
  bottom: 15px;
  left: 15px;
  right: 15px;
  background: rgba(255, 255, 255, 0.95);
  padding: 15px;
  border-radius: 10px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.info-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 600;
  color: #409EFF;
  margin-bottom: 8px;
}

.info-address {
  font-size: 14px;
  color: #303133;
  margin-bottom: 4px;
  word-break: break-all;
}

.info-coord {
  font-size: 12px;
  color: #909399;
}

.result-section {
  background: #f8fafc;
  padding: 15px;
  border-radius: 10px;
}

.result-section >>> .el-form-item {
  margin-bottom: 10px;
}

.result-section >>> .el-form-item:last-child {
  margin-bottom: 0;
}
</style>
