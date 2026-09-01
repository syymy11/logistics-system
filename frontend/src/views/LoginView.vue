<template>
  <div class="login-container">
    <div class="login-wrapper">
      <div class="scale-wrapper">
        <div class="login-content" :style="{ transform: `scale(${scale})` }">
          <div class="login-card">
            <div class="brand-section">
              <div class="brand-icon">
                <svg viewBox="0 0 64 64" fill="none">
                  <circle cx="32" cy="32" r="28" fill="url(#gradient)" />
                  <path d="M22 32h20M32 22v20" stroke="white" stroke-width="3" stroke-linecap="round" />
                  <circle cx="32" cy="32" r="8" fill="white" opacity="0.3" />
                  <defs>
                    <linearGradient id="gradient" x1="0%" y1="0%" x2="100%" y2="100%">
                      <stop offset="0%" style="stop-color:#ff6b6b" />
                      <stop offset="100%" style="stop-color:#ee5a24" />
                    </linearGradient>
                  </defs>
                </svg>
              </div>
              <h1 class="brand-title">智能物流系统</h1>
              <p class="brand-subtitle">基于遗传算法的路径规划解决方案</p>
            </div>

            <form @submit.prevent="handleLogin" class="login-form">
              <div class="form-group">
                <label class="form-label">用户名</label>
                <div class="input-group">
                  <svg class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                    <circle cx="12" cy="7" r="4" />
                  </svg>
                  <input type="text" v-model="loginForm.username" placeholder="请输入用户名" class="form-input" />
                </div>
              </div>

              <div class="form-group">
                <label class="form-label">密码</label>
                <div class="input-group">
                  <svg class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
                    <path d="M7 11V7a5 5 0 0 1 10 0v4" />
                  </svg>
                  <input :type="showPassword ? 'text' : 'password'" v-model="loginForm.password" placeholder="请输入密码"
                    class="form-input" />
                  <button type="button" class="toggle-password" @click="showPassword = !showPassword">
                    <svg v-if="!showPassword" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                      <circle cx="12" cy="12" r="3" />
                    </svg>
                    <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M17 17H2m0 0h2m-2 0v2m0-2v-2" />
                    </svg>
                  </button>
                </div>
              </div>

              <div class="form-group">
                <label class="form-label">用户角色</label>
                <div class="role-selector">
                  <div class="role-option" :class="{ active: loginForm.role === 'admin' }"
                    @click="loginForm.role = 'admin'">
                    <div class="role-icon admin">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M12 2l3 6 7 1-5 5 1 7-6-3-6 3 1-7-5-5 7-1z" />
                      </svg>
                    </div>
                    <div class="role-info">
                      <span class="role-name">管理员</span>
                      <span class="role-desc">拥有全部权限</span>
                    </div>
                    <div class="role-check" v-if="loginForm.role === 'admin'">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3">
                        <polyline points="20 6 9 17 4 12" />
                      </svg>
                    </div>
                  </div>
                  <div class="role-option" :class="{ active: loginForm.role === 'operator' }"
                    @click="loginForm.role = 'operator'">
                    <div class="role-icon operator">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2" />
                        <circle cx="9" cy="7" r="4" />
                        <path d="M22 21v-2a4 4 0 0 0-3-3.87" />
                        <path d="M16 3.13a4 4 0 0 1 0 7.75" />
                      </svg>
                    </div>
                    <div class="role-info">
                      <span class="role-name">操作员</span>
                      <span class="role-desc">基础操作权限</span>
                    </div>
                    <div class="role-check" v-if="loginForm.role === 'operator'">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3">
                        <polyline points="20 6 9 17 4 12" />
                      </svg>
                    </div>
                  </div>
                </div>
              </div>

              <div class="form-options">
                <label class="checkbox-wrapper">
                  <input type="checkbox" v-model="rememberMe" />
                  <span class="checkmark"></span>
                  <span class="checkbox-text">记住我</span>
                </label>
              </div>

              <button type="submit" class="login-button" :disabled="loading">
                <span v-if="loading" class="loading-spinner"></span>
                <span>{{ loading ? '登录中...' : '登 录' }}</span>
              </button>
            </form>

            <div class="error-message" v-if="loginStatus === 'fail'">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10" />
                <line x1="15" y1="9" x2="9" y2="15" />
                <line x1="9" y1="9" x2="15" y2="15" />
              </svg>
              <span>用户名或密码错误</span>
            </div>

            <div class="footer">
              <div class="test-accounts">
                <div class="test-account">
                  <span class="badge admin">管理员</span>
                  <span class="account-text">admin / admin123</span>
                </div>
                <div class="test-account">
                  <span class="badge operator">操作员</span>
                  <span class="account-text">operator / operator123</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="background-decoration">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
      <div class="circle circle-4"></div>
    </div>
  </div>
</template>

<script>
import { login } from '@/api'

export default {
  name: 'LoginView',
  data() {
    return {
      loginForm: { username: '', password: '', role: 'operator' },
      loading: false,
      loginStatus: null,
      showPassword: false,
      rememberMe: false,
      scale: 1
    }
  },
  mounted() {
    this.updateScale()
    window.addEventListener('resize', this.updateScale)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.updateScale)
  },
  methods: {
    updateScale() {
      const baseWidth = 1440
      const baseHeight = 900
      const scaleX = window.innerWidth / baseWidth
      const scaleY = window.innerHeight / baseHeight
      this.scale = Math.min(scaleX, scaleY, 1)
    },
    async handleLogin() {
      if (!this.loginForm.username || !this.loginForm.password) {
        this.$message.error('请输入用户名和密码')
        return
      }

      this.loading = true
      this.loginStatus = null

      try {
        const response = await login(this.loginForm.username, this.loginForm.password)
        if (response.code === 200) {
          const userRole = response.data.role
          if (userRole !== this.loginForm.role) {
            this.$message.error(`该账号角色为 ${userRole === 'admin' ? '管理员' : '操作员'}，请选择正确的角色类型`)
            this.loginStatus = 'fail'
            this.loading = false
            return
          }

          this.loginStatus = 'success'
          localStorage.setItem('user', JSON.stringify(response.data))
          this.$router.push('/')
        } else {
          this.loginStatus = 'fail'
        }
      } catch (error) {
        this.loginStatus = 'fail'
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 50%, #f39c12 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  position: relative;
  overflow: hidden;
}

.background-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
}

.circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.15;
}

.circle-1 {
  width: 300px;
  height: 300px;
  background: white;
  top: -100px;
  left: -100px;
  animation: float 6s ease-in-out infinite;
}

.circle-2 {
  width: 200px;
  height: 200px;
  background: rgba(255, 255, 255, 0.8);
  bottom: -50px;
  right: -50px;
  animation: float 8s ease-in-out infinite reverse;
}

.circle-3 {
  width: 150px;
  height: 150px;
  background: rgba(255, 255, 255, 0.6);
  top: 50%;
  left: 10%;
  animation: float 5s ease-in-out infinite 1s;
}

.circle-4 {
  width: 100px;
  height: 100px;
  background: rgba(255, 255, 255, 0.4);
  bottom: 20%;
  right: 20%;
  animation: float 7s ease-in-out infinite 2s;
}

@keyframes float {

  0%,
  100% {
    transform: translateY(0) scale(1);
  }

  50% {
    transform: translateY(-20px) scale(1.05);
  }
}

.login-wrapper {
  position: relative;
  z-index: 10;
  width: 100%;
  max-width: 420px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.scale-wrapper {
  position: relative;
  width: 420px;
  height: auto;
}

.login-content {
  transform-origin: top center;
  width: 100%;
}

.login-card {
  background: white;
  border-radius: 24px;
  padding: 48px 40px;
  box-shadow: 0 25px 80px rgba(0, 0, 0, 0.15);
  backdrop-filter: blur(20px);
}

.brand-section {
  text-align: center;
  margin-bottom: 40px;
}

.brand-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 20px;
}

.brand-title {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0 0 8px 0;
}

.brand-subtitle {
  font-size: 14px;
  color: #666;
  margin: 0;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-label {
  font-size: 13px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.input-group {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 16px;
  width: 18px;
  height: 18px;
  color: #9ca3af;
}

.form-input {
  width: 100%;
  padding: 14px 16px 14px 48px;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  font-size: 15px;
  transition: all 0.3s ease;
  outline: none;
  background: #fafafa;
}

.form-input:focus {
  border-color: #ff6b6b;
  background: white;
  box-shadow: 0 0 0 3px rgba(255, 107, 107, 0.1);
}

.form-input::placeholder {
  color: #9ca3af;
}

.toggle-password {
  position: absolute;
  right: 16px;
  background: none;
  border: none;
  color: #9ca3af;
  cursor: pointer;
  padding: 0;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.toggle-password svg {
  width: 18px;
  height: 18px;
}

.form-options {
  display: flex;
  justify-content: flex-start;
}

.checkbox-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  position: relative;
}

.checkbox-wrapper input {
  position: absolute;
  opacity: 0;
  cursor: pointer;
  height: 0;
  width: 0;
}

.checkmark {
  width: 18px;
  height: 18px;
  border: 2px solid #d1d5db;
  border-radius: 4px;
  transition: all 0.2s ease;
}

.checkbox-wrapper input:checked~.checkmark {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%);
  border-color: #ff6b6b;
}

.checkmark::after {
  content: '';
  position: absolute;
  left: 6px;
  top: 2px;
  width: 5px;
  height: 10px;
  border: solid white;
  border-width: 0 2px 2px 0;
  transform: rotate(45deg);
  opacity: 0;
  transition: opacity 0.2s ease;
}

.checkbox-wrapper input:checked~.checkmark::after {
  opacity: 1;
}

.checkbox-text {
  font-size: 14px;
  color: #666;
}

.login-button {
  width: 100%;
  padding: 15px;
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  transition: all 0.3s ease;
  margin-top: 10px;
}

.login-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 10px 30px rgba(255, 107, 107, 0.4);
}

.login-button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.loading-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.error-message {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  background: #fee2e2;
  color: #dc2626;
  padding: 12px 16px;
  border-radius: 10px;
  margin-top: 16px;
  font-size: 14px;
}

.error-message svg {
  width: 18px;
  height: 18px;
}

.footer {
  text-align: center;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.test-accounts {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: center;
}

.test-account {
  display: flex;
  align-items: center;
  gap: 10px;
}

.badge {
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 600;
}

.badge.admin {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.badge.operator {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  color: white;
}

.account-text {
  font-size: 12px;
  color: #9ca3af;
  font-family: monospace;
}

.role-selector {
  display: flex;
  gap: 12px;
}

.role-option {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border: 2px solid #e5e7eb;
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: #fafafa;
  position: relative;
}

.role-option:hover {
  border-color: #ff6b6b;
  background: #fff5f5;
}

.role-option.active {
  border-color: #ff6b6b;
  background: linear-gradient(135deg, rgba(255, 107, 107, 0.08) 0%, rgba(238, 90, 36, 0.08) 100%);
  box-shadow: 0 4px 12px rgba(255, 107, 107, 0.15);
}

.role-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.role-icon.admin {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.role-icon.operator {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  color: white;
}

.role-icon svg {
  width: 22px;
  height: 22px;
}

.role-info {
  display: flex;
  flex-direction: column;
  flex: 1;
}

.role-name {
  font-size: 14px;
  font-weight: 600;
  color: #1a1a2e;
}

.role-desc {
  font-size: 11px;
  color: #9ca3af;
  margin-top: 2px;
}

.role-check {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.role-check svg {
  width: 14px;
  height: 14px;
}
</style>
