// api/index.js
import axios from 'axios'

const request = axios.create({
  baseURL: 'http://localhost:8081/api',
  timeout: 120000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 可以在这里添加认证信息等
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    // 统一处理响应格式
    if (res.code !== 200) {
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  error => {
    console.error('响应错误:', error)
    let errorMessage = '网络请求失败'
    if (error.response) {
      switch (error.response.status) {
        case 400:
          errorMessage = '请求参数错误'
          break
        case 401:
          errorMessage = '未授权，请重新登录'
          break
        case 403:
          errorMessage = '拒绝访问'
          break
        case 404:
          errorMessage = '请求接口不存在'
          break
        case 500:
          errorMessage = '服务器内部错误'
          break
        default:
          errorMessage = `请求失败 (${error.response.status})`
      }
    }
    return Promise.reject(new Error(errorMessage))
  }
)

// 登录相关
export const login = (username, password) => request.post('/auth/login', { username, password })
export const logout = () => request.post('/auth/logout')
export const getUserInfo = (id) => request.get('/auth/info', { params: { id } })

// 订单相关
export const getPendingOrders = () => request.get('/order/pending')
export const getOrders = (page, size, status) => request.get('/order/list', { params: { page, size, status } })
export const getOrdersByStatus = (status) => request.get('/order/list', { params: { page: 1, size: 100, status } })
export const addOrder = (data) => request.post('/order/add', data)
export const deleteOrder = (id) => request.delete(`/order/delete/${id}`)
export const updateOrderStatus = (id, status) => request.put('/order/status', null, { params: { id, status } })
export const resetOrder = (id) => request.post(`/order/reset/${id}`)

// 调度相关
export const getVehicles = () => request.get('/dispatch/vehicles')
export const planDelivery = (orderIds, vehicleId) => request.post('/dispatch/plan', { orderIds, vehicleId })
export const getRoute = (taskId) => request.get(`/dispatch/route/${taskId}`)
export const getTaskDetails = (taskId) => request.get(`/dispatch/task/${taskId}`)
export const getTaskByOrderId = (orderId) => request.get(`/dispatch/task/by-order/${orderId}`)

// 车辆管理相关
export const getAllVehicles = () => request.get('/vehicle/list')
export const getAvailableVehicles = () => request.get('/vehicle/available')
export const addVehicle = (data) => request.post('/vehicle/add', data)
export const updateVehicle = (data) => request.put('/vehicle/update', data)
export const deleteVehicle = (id) => request.delete(`/vehicle/delete/${id}`)

// 仓库管理相关
export const getWarehouses = () => request.get('/warehouse/list')
export const addWarehouse = (data) => request.post('/warehouse/add', data)
export const deleteWarehouse = (id) => request.delete(`/warehouse/delete/${id}`)

// 限行区域管理相关
export const getRestrictedAreas = () => request.get('/dispatch/restricted-areas')
export const addRestrictedArea = (data) => request.post('/dispatch/restricted-areas', data)
export const updateRestrictedArea = (id, data) => request.put(`/dispatch/restricted-areas/${id}`, data)
export const deleteRestrictedArea = (id) => request.delete(`/dispatch/restricted-areas/${id}`)