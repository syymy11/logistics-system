<template>
  <div id="app">
    <div class="header" v-if="showHeader">
      <div class="header-content">
        <h1>物流配送管理系统</h1>
        <div class="nav-container">
          <div class="nav-wrapper" ref="navWrapper" @mouseleave="handleWrapperLeave">
            <div class="nav-item" v-for="item in navItems" :key="item.path"
              :class="{ active: activeMenu === item.path }" @click="navigateTo(item.path)"
              @mouseenter="handleMouseEnter($event)">
              <span class="nav-text">{{ item.name }}</span>
            </div>
            <div class="nav-indicator" ref="indicator"></div>
          </div>
        </div>
        <div class="user-info">
          <span class="user-name">{{ userName }}</span>
          <el-button type="text" @click="handleLogout">退出</el-button>
        </div>
      </div>
    </div>
    <main>
      <router-view />
    </main>
  </div>
</template>

<script>
export default {
  name: 'App',
  data() {
    return {
      allNavItems: [
        { path: '/', name: '首页', roles: ['admin', 'operator'] },
        { path: '/orders', name: '订单管理', roles: ['admin', 'operator'] },
        { path: '/tracking', name: '订单追踪', roles: ['admin', 'operator'] },
        { path: '/dispatch', name: '智能调度', roles: ['admin'] },
        { path: '/management', name: '配送管理', roles: ['admin'] }
      ]
    }
  },
  computed: {
    navItems() {
      const user = localStorage.getItem('user')
      if (!user) return []
      const userData = JSON.parse(user)
      const userRole = userData.role
      return this.allNavItems.filter(item => item.roles.includes(userRole))
    },
    activeMenu() {
      return this.$route.path
    },
    showHeader() {
      return this.$route.path !== '/login'
    },
    userName() {
      const user = localStorage.getItem('user')
      if (user) {
        const userData = JSON.parse(user)
        return `${userData.realName || userData.username} (${userData.role === 'admin' ? '管理员' : '操作员'})`
      }
      return ''
    }
  },
  watch: {
    activeMenu() {
      setTimeout(() => {
        this.updateIndicator()
      }, 50)
    }
  },
  methods: {
    navigateTo(path) {
      if (this.$route.path !== path) {
        this.$router.push(path)
      }
    },
    handleLogout() {
      localStorage.removeItem('user')
      localStorage.removeItem('token')
      this.$router.push('/login')
    },
    handleMouseEnter(e) {
      const indicator = this.$refs.indicator
      const navWrapper = this.$refs.navWrapper
      if (!indicator || !navWrapper) return

      const item = e.currentTarget
      const wrapperRect = navWrapper.getBoundingClientRect()
      const itemRect = item.getBoundingClientRect()

      const left = itemRect.left - wrapperRect.left
      indicator.style.left = `${left}px`
      indicator.style.width = `${itemRect.width}px`
      indicator.style.opacity = '1'
    },
    handleWrapperLeave() {
      setTimeout(() => {
        this.updateIndicator()
      }, 50)
    },
    updateIndicator() {
      const indicator = this.$refs.indicator
      const navWrapper = this.$refs.navWrapper
      if (!indicator || !navWrapper) return

      const activeItem = navWrapper.querySelector('.nav-item.active')
      if (activeItem) {
        const wrapperRect = navWrapper.getBoundingClientRect()
        const itemRect = activeItem.getBoundingClientRect()
        indicator.style.left = `${itemRect.left - wrapperRect.left}px`
        indicator.style.width = `${itemRect.width}px`
        indicator.style.opacity = '1'
      }
    }
  },
  mounted() {
    setTimeout(() => {
      this.updateIndicator()
    }, 100)
  }
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
  background-color: #f0f2f5;
  color: #333;
  line-height: 1.5;
}

#app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background: #f5f5f5;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
  padding: 4px 0;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header h1 {
  font-size: 20px;
  font-weight: 600;
  margin: 0;
  color: #2c3e50;
  flex-shrink: 0;
}

.nav-container {
  background: white;
  border-radius: 25px;
  padding: 5px 10px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
}

.nav-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  gap: 5px;
}

.nav-item {
  position: relative;
  padding: 8px 24px;
  font-size: 15px;
  font-weight: 500;
  color: #5a6a7a;
  cursor: pointer;
  border-radius: 20px;
  transition: color 0.3s ease;
  z-index: 1;
  background: transparent;
}

.nav-item:hover {
  color: #2c3e50;
}

.nav-item.active {
  color: #1a5f7a;
  font-weight: 600;
}

.nav-indicator {
  position: absolute;
  top: 0;
  height: 100%;
  background: rgba(26, 130, 196, 0.15);
  border-radius: 20px;
  opacity: 0;
  transition: left 0.5s cubic-bezier(0.25, 0.46, 0.45, 0.94),
    width 0.5s cubic-bezier(0.25, 0.46, 0.45, 0.94),
    opacity 0.3s ease;
  pointer-events: none;
}

.nav-item:hover~.nav-indicator {
  opacity: 1;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.user-name {
  font-size: 14px;
  color: #5a6a7a;
}

main {
  flex: 1;
  width: 100%;
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    gap: 10px;
    padding: 10px 16px;
  }

  .header h1 {
    font-size: 18px;
  }

  .nav-item {
    padding: 6px 16px;
    font-size: 13px;
  }
}
</style>