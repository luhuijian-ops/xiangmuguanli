# 项目管理系统

基于钉钉 Teambition 功能参考的项目管理系统，支持微信和钉钉登录。

# 本项目规则
## 基础诚实约束
- 所有代码修改、逻辑分析、问题解答，必须先读取对应原文件；
- 未读取的文件、未执行的命令、未查看的日志，严禁猜测内容、结构、报错和运行结果；
- 对业务逻辑、代码细节、配置含义不确定时，直接回复「我不确定」，禁止强行编造解释；
- 给出所有结论、代码建议、问题原因时，必须附带：文件名 + 大致行号 + 原文片段引用；
- 禁止虚构命令执行输出、虚构报错信息、虚构项目依赖与技术栈；
- 只能基于真实文件内容、真实命令输出、真实日志进行回答与修改。

## 代码修改约束
- 禁止擅自进行全局重构、架构调整、批量改名、调整目录结构；
- 禁止大范围改动代码、一次性修改多个无关文件；
- 只允许**最小范围修复**：只改问题相关代码，不改动无关逻辑、注释、格式、变量命名；
- 任何重构、结构调整、多文件修改，必须先向我说明修改范围和影响，**获得同意后再操作**；
- 不得私自删除原有业务逻辑、注释、配置项；
- 如需新增功能，先给出方案和涉及文件列表，确认后再编写代码；
- 严格遵循**只写增量代码**原则，严禁擅自修改、删减、变更已有成熟业务逻辑；原有业务代码若无明确指令，一律保持原样不动。


## 项目概述

这是一个功能完整的企业级项目管理系统，采用现代化技术栈构建，前后端分离架构，具备完善的项目管理、任务管理、日程管理等功能。

## 技术栈

### 后端
- **Java 17+** - 运行时环境
- **Spring Boot 3.2** - Web 框架
- **Spring Security** - 安全框架
- **Spring Data JPA** - 数据访问层
- **MySQL 8.0** - 数据库
- **JWT 认证** - 用户身份验证
- **OAuth 2.0** - 微信和钉钉第三方登录

### 前端
- **Vue 3** - 前端框架
- **Element Plus** - UI 组件库
- **Pinia** - 状态管理
- **Vite** - 构建工具
- **TypeScript** - 类型安全
- **Vue Router 4** - 路由管理

## 功能特性

- ✅ 微信扫码登录
- ✅ 钉钉扫码登录
- ✅ 项目管理 (CRUD、成员管理、权限控制)
- ✅ 任务管理 (看板视图、状态流转、优先级)
- ✅ 文件管理 (上传、下载、管理)
- ✅ 评论模块 (嵌套回复、@提及)
- ✅ 统计分析 (数据可视化、报表)
- ✅ 日程管理 (日历视图、提醒功能)
- ✅ 工时记录 (时间跟踪、统计)
- ✅ 审计日志 (操作记录、安全审计)
- ✅ 响应式设计 (支持多设备访问)

## 快速开始

### 环境要求
- Java 17+
- MySQL 8.0+
- Node.js 20+
- Maven 3.9+

### 安装步骤

1. **克隆项目**
   ```bash
   git clone <repository-url>
   cd xiangmuguanli
   ```

2. **配置数据库**
   ```sql
   -- 创建数据库
   CREATE DATABASE IF NOT EXISTS xiangmuguanli DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

3. **配置后端**
   ```bash
   cd backend-spring
   # 编辑 src/main/resources/application.yml 配置数据库和 OAuth 参数
   mvn clean compile
   ```

4. **配置前端**
   ```bash
   cd ../frontend
   npm install
   ```

5. **启动服务**
   ```bash
   # 方法1: 使用启动脚本 (推荐)
   ./start-project.sh  # Linux/Mac
   start-project.bat   # Windows

   # 方法2: 手动启动
   # 启动后端 (在 backend-spring 目录)
   mvn spring-boot:run

   # 启动前端 (在 frontend 目录)
   npm run dev
   ```

6. **访问应用**
   - 前端: http://localhost:5173
   - 后端: http://localhost:6200
   - 健康检查: http://localhost:6200/health

## 项目结构

```
xiangmuguanli/
├── backend-spring/            # Spring Boot 后端项目
│   └── src/main/java/com/xiangmuguanli/
│       ├── config/          # 配置类
│       ├── controller/     # 控制器
│       ├── service/        # 业务逻辑层
│       ├── repository/     # 数据访问层
│       ├── entity/         # 实体类
│       ├── dto/            # 数据传输对象
│       ├── security/       # 安全相关
│       └── exception/      # 异常处理
├── frontend/                 # Vue 3 前端项目
│   └── src/
│       ├── api/            # API 接口定义
│       ├── components/     # 组件库
│       ├── composables/    # 组合式函数
│       ├── layouts/        # 布局组件
│       ├── pages/          # 页面组件
│       ├── router/         # 路由配置
│       ├── stores/         # Pinia 状态管理
│       └── utils/          # 工具函数
├── docs/                    # 文档
├── start-project.sh         # Linux/Mac 启动脚本
├── start-project.bat        # Windows 启动脚本
└── README.md
```

## API 接口

主要 API 接口包括：

- `/api/v1/auth/*` - 认证相关
- `/api/v1/projects/*` - 项目管理
- `/api/v1/tasks/*` - 任务管理
- `/api/v1/files/*` - 文件管理
- `/api/v1/comments/*` - 评论管理
- `/api/v1/statistics/*` - 统计分析
- `/api/v1/schedule/*` - 日程管理
- `/api/v1/audit/*` - 审计日志
- `/api/v1/alerts/*` - 告警管理

## 部署

### 生产环境构建
```bash
# 前端构建
cd frontend
npm run build

# 后端构建
cd ../backend-spring
mvn clean package -DskipTests
java -jar target/xiangmuguanli-1.0.0.jar
```

### 环境变量配置
生产环境需要配置相应的环境变量，包括：
- 数据库连接
- JWT 密钥
- OAuth 配置
- 服务器地址

## 安全特性

- JWT Token 认证
- OAuth 2.0 第三方登录
- Spring Security 权限控制
- 输入验证和清理
- 安全头部配置
- CORS 配置

## 贡献

欢迎提交 Issue 和 Pull Request。

## 许可证

MIT
