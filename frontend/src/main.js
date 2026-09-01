import Vue from 'vue'
import App from './App.vue'
import router from './router'
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
import axios from 'axios'

Vue.config.productionTip = false
Vue.use(ElementUI)

Vue.prototype.$http = axios.create({
  baseURL: 'http://localhost:8081/api',
  timeout: 30000
})

const isRedirectedError = (err) => {
  if (!err) return false
  if (err.name === 'NavigationRedirected') return true
  if (err.message && err.message.includes('Redirected when going')) return true
  return false
}

const originalPush = router.push
const originalReplace = router.replace
router.push = function push(location) {
  return originalPush.call(this, location).catch(err => {
    if (isRedirectedError(err)) {
      Vue.prototype.$message.warning('您没有权限访问此页面')
      return Promise.resolve()
    }
    return Promise.reject(err)
  })
}
router.replace = function replace(location) {
  return originalReplace.call(this, location).catch(err => {
    if (isRedirectedError(err)) {
      Vue.prototype.$message.warning('您没有权限访问此页面')
      return Promise.resolve()
    }
    return Promise.reject(err)
  })
}

new Vue({
  router,
  render: h => h(App)
}).$mount('#app')