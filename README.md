# 智能物流配送调度系统

> 基于**改进遗传算法**求解带载重与限行约束的车辆路径规划（VRP）问题的物流调度平台，自动为订单匹配最优车辆与配送路径。

## 技术栈

- **后端**：Spring Boot 4.0 · MyBatis · MySQL 8 · Java 21
- **前端**：Vue 2 · Vue Router · Element UI · ECharts 6 · 高德地图 JS API
- **算法**：改进遗传算法（最近邻启发式 + OX 交叉 + 自适应变异 + 2-opt 局部搜索）
- **测试**：JUnit 5 · Mockito · JaCoCo

## 核心特性

- **多重约束智能调度**：综合就近仓库分配、车辆载重、限行区域禁令，输出可执行的最优配送路径
- **事务一致性保障**：`@Transactional` 确保订单、任务、车辆多表状态变更原子提交
- **全链路可视化**：高德地图实时绘制配送轨迹，ECharts 统计看板数据可视化
- **自动坐标解析**：调用高德地理编码 API 完成地址→坐标转换，异常自动回退

## 功能模块

- 用户登录鉴权（admin/operator 角色控制）
- 订单管理（增删改查、状态流转、重置）
- 车辆/仓库/限行区域管理
- 智能调度（遗传算法路径规划）
- 配送轨迹可视化（高德地图）
- 订单追踪与统计看板（ECharts）

## 快速启动

### 1. 环境准备

- JDK 21+、Maven 3.8+、MySQL 8+、Node.js 16+

### 2. 后端启动

```bash
cd backend
# 创建数据库
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS logistics DEFAULT CHARSET utf8mb4;"
# 启动（首次启动自动初始化仓库、用户、车辆、限行区域数据）
mvnw.cmd spring-boot:run
```

服务地址：http://localhost:8081

### 3. 前端启动

```bash
cd frontend
npm install
npm run serve
```

访问地址：http://localhost:8080

### 4. 默认账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |
| 操作员 | operator | operator123 |

## 测试

```bash
cd backend
mvnw.cmd clean test
# 覆盖率报告：target/site/jacoco/index.html
```

测试覆盖：16 个用例全部通过，后端行覆盖率 72%，核心算法与服务类覆盖率 ≥ 90%。

## 目录结构

```
logistics-system/
├── backend/                # Spring Boot 后端
│   └── src/main/java/com/logistics/
│       ├── algorithm/      # 遗传算法
│       ├── controller/     # REST 控制器
│       ├── entity/         # 实体类
│       ├── mapper/         # MyBatis Mapper
│       ├── service/        # 业务服务
│       └── config/         # 数据初始化
└── frontend/               # Vue 2 前端
    └── src/
        ├── api/            # axios 接口封装
        ├── components/     # 通用组件
        ├── router/         # 路由配置
        └── views/          # 页面视图
```
