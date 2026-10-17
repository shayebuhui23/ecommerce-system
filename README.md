# 小型电商订单系统（跨境贸易场景扩展版）

软件工程课程期末综合实践项目。当前处于**骨架阶段**：工程结构、数据库、统一响应与三角色权限已完成，商品、购物车、订单、支付、物流、售后等业务模块将在后续阶段接入。

## 技术栈

| 层次 | 技术 |
| ---- | ---- |
| 前端 | Vue 3、Vite 5、Element Plus、Pinia、Vue Router、Axios、Tailwind CSS |
| 后端 | Java 11、Spring Boot 2.7.18、MyBatis-Plus 3.5.3.2、Sa-Token 1.37.0 |
| 数据库 | MySQL 8（InnoDB / utf8mb4） |
| 构建 | Maven 3.6+、npm |

## 环境要求

- JDK 11（`JAVA_HOME` 指向 JDK 11，`mvn -v` 输出 Java version: 11.x）
- Maven 3.6+
- Node.js 18+（实测 v20.20.2）
- MySQL 8.0+

## 目录结构

```
ecommerce-system/
├── backend/            Spring Boot 后端
├── frontend/           Vue 3 前端
├── database/           schema.sql 建表脚本、seed.sql 演示数据
├── docs/               需求与开发计划文档
└── README.md
```

## 一、初始化数据库

```powershell
cd d:/ecommerce-system
mysql -u root -p < database/schema.sql
mysql -u root -p < database/seed.sql
```

执行后得到 `ecommerce` 库，包含 17 张业务表（users、roles、categories、products、product_skus、carts、cart_items、addresses、orders、order_items、payments、shipments、after_sales、reviews、inventory_logs、exchange_rates、operation_logs）。

> `schema.sql` 可重复执行（会先删除同名表），仅用于开发与课程演示环境。

## 二、启动后端

数据库密码**不写入仓库**，通过以下任一方式提供：

1. **环境变量（推荐）**

   ```powershell
   $env:MYSQL_PASSWORD="你的数据库密码"
   cd backend
   mvn spring-boot:run
   ```

2. **本地配置文件**

   复制 `backend/src/main/resources/application-local.yml.example` 为 `application-local.yml` 并填写密码，然后：

   ```powershell
   cd backend
   mvn spring-boot:run -Dspring-boot.run.profiles=local
   ```

后端默认端口 `8080`，接口前缀 `/api`。

## 三、启动前端

```powershell
cd frontend
npm install
npm run dev
```

访问 http://localhost:5173 。Vite 已把 `/api` 代理到 `http://127.0.0.1:8080`，无需额外配置跨域。

生产构建：`npm run build`，产物在 `frontend/dist`。

## 四、演示账号

| 用户名 | 密码 | 角色 | 说明 |
| ------ | ---- | ---- | ---- |
| admin | 123456 | 系统管理员 ADMIN | 用户管理、全量数据查询 |
| staff | 123456 | 商家运营 STAFF | 商品、库存、履约、售后审核 |
| customer | 123456 | 普通客户 CUSTOMER | 浏览下单、支付、售后申请 |

也可用邮箱登录（如 `admin@ecommerce.local`）。数据库中仅保存 BCrypt 哈希，不存明文。

## 五、已实现接口

| 方法 | 路径 | 权限 | 说明 |
| ---- | ---- | ---- | ---- |
| POST | `/api/auth/register` | 公开 | 注册普通客户账号（角色固定 CUSTOMER） |
| POST | `/api/auth/login` | 公开 | 用户名或邮箱 + 密码登录，返回 token 与用户信息 |
| POST | `/api/auth/logout` | 登录 | 注销当前登录态 |
| GET | `/api/user/me` | 登录 | 当前用户信息与角色 |
| GET | `/api/user/page` | ADMIN | 分页查询用户，支持关键字搜索 |
| PUT | `/api/user/status` | ADMIN | 启用 / 禁用账号 |

统一响应结构：

```json
{ "code": 200, "message": "操作成功", "data": {} }
```

HTTP 状态码与业务码分离：未登录返回 401、越权返回 403、参数校验失败返回 400，响应体中 `code` 为细分业务码。

## 六、权限说明

- 登录态由 Sa-Token 校验，除登录/注册外的所有接口默认需要登录。
- 管理员接口使用 `@SaCheckRole("ADMIN")` 在服务端强制校验，前端菜单隐藏不作为权限依据。
- 每次受保护请求都会校验账号状态，被禁用账号即使持有未过期令牌也无法继续操作。
- 管理员不能禁用自己的账号。

## 七、开发进度

- [x] 阶段一：项目初始化（工程、数据库、统一响应、登录与角色权限）
- [ ] 阶段二：商品与客户功能
- [ ] 阶段三：购物车与订单
- [ ] 阶段四：支付与履约
- [ ] 阶段五：售后与扩展功能
- [ ] 阶段六：测试、修复与交付

详细计划见 `docs/plan.md`，需求见 `docs/初始规划.md`。

## 八、常用命令

```powershell
# 后端编译
cd backend; mvn -DskipTests compile

# 后端测试
cd backend; mvn test

# 前端构建
cd frontend; npm run build
```

## 九、验证截图

骨架阶段已在浏览器中完成登录、角色菜单、越权拦截与退出流程验证，截图见 `docs/screenshots/`：

| 文件 | 内容 |
| ---- | ---- |
| `01-login.png` | 登录页（含演示账号一键填充） |
| `02-dashboard-admin.png` | 管理员工作台与角色菜单 |
| `03-user-management.png` | 管理员用户管理与启停入口 |
| `04-forbidden.png` | 普通客户访问管理员页面被拦截至 403 |

接口层验证结果（curl / PowerShell 实测）：

- 正确密码登录返回 token 与角色；错误密码返回 400。
- 未携带 token 访问 `/api/user/me` 返回 401。
- 普通客户访问 `/api/user/page` 返回 403。
- 管理员分页查询返回 3 个演示账号。
