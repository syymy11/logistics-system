import Vue from 'vue'
import VueRouter from 'vue-router'
import DashboardView from '../views/DashboardView.vue'
import DispatchView from '../views/DispatchView.vue'
import ManagementView from '../views/ManagementView.vue'
import OrdersView from '../views/OrdersView.vue'
import OrderTrackingView from '../views/OrderTrackingView.vue'
import LoginView from '../views/LoginView.vue'

Vue.use(VueRouter)

const routes = [
  {
    path: '/login',
    name: 'login',
    component: LoginView,
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    name: 'dashboard',
    component: DashboardView,
    meta: { requiresAuth: true, roles: ['admin', 'operator'] }
  },
  {
    path: '/dispatch',
    name: 'dispatch',
    component: DispatchView,
    meta: { requiresAuth: true, roles: ['admin'] }
  },
  {
    path: '/management',
    name: 'management',
    component: ManagementView,
    meta: { requiresAuth: true, roles: ['admin'] }
  },
  {
    path: '/orders',
    name: 'orders',
    component: OrdersView,
    meta: { requiresAuth: true, roles: ['admin', 'operator'] }
  },
  {
    path: '/tracking',
    name: 'tracking',
    component: OrderTrackingView,
    meta: { requiresAuth: true, roles: ['admin', 'operator'] }
  }
]

const router = new VueRouter({
  mode: 'history',
  routes
})

router.beforeEach((to, from, next) => {
  const userStr = localStorage.getItem('user')
  const user = userStr ? JSON.parse(userStr) : null

  if (to.meta.requiresAuth && !user) {
    next('/login')
    return
  }

  if (to.path === '/login' && user) {
    next('/')
    return
  }

  if (to.meta.roles && user) {
    const userRole = user.role
    if (!to.meta.roles.includes(userRole)) {
      next('/')
      return
    }
  }

  next()
})

export default router