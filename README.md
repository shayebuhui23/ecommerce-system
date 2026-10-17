# 小型电商订单系统（跨境贸易场景扩展版）

软件工程课程期末综合实践项目。已完成阶段一（工程骨架、数据库与三角色权限）和阶段二（商品分类、商品与 SKU、商品浏览筛选、收货地址、个人资料）；购物车、订单、支付、物流与售后将在后续阶段接入。

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
├── src/                        源代码
│   ├── backend/                Spring Boot 后端（Java 单元测试位于 src/backend/src/test）
│   ├── frontend/               Vue 3 前端
│   └── database/               schema.sql 建表脚本、seed.sql 演示数据
├── docs/                       需求、设计文档、会议记录与验证截图
│   ├── 初始规划.md
│   ├── plan.md
│   ├── meetings/               会议记录
│   └── screenshots/            功能验证截图
├── test/                       测试用例与测试报告
│   ├── test-cases/             功能测试用例
│   └── api/                    接口冒烟测试脚本
├── .gitignore
└── README.md
```

> 说明：`src/backend/src/test` 是 Maven 约定的 Java 单元测试目录，随工程构建执行；
> 顶层 `test/` 用于存放课程要求的测试用例文档与接口测试脚本。

## 一、初始化数据库

```powershell
cd d:/ecommerce-system
mysql -u root -p < src/database/schema.sql
mysql -u root -p < src/database/seed.sql
```

执行后得到 `ecommerce` 库，包含 17 张业务表（users、roles、categories、products、product_skus、carts、cart_items、addresses、orders、order_items、payments、shipments、after_sales、reviews、inventory_logs、exchange_rates、operation_logs）。

> `schema.sql` 可重复执行（会先删除同名表），仅用于开发与课程演示环境。

## 二、启动后端

数据库密码**不写入仓库**，通过以下任一方式提供：

1. **环境变量（推荐）**

   ```powershell
   $env:MYSQL_PASSWORD="你的数据库密码"
   cd src/backend
   mvn spring-boot:run
   ```

   2. **本地配置文件**

    复制 `src/backend/src/main/resources/application-local.yml.example` 为同目录下的 `application-local.yml` 并填写密码，然后：

    ```powershell
    cd src/backend
    mvn spring-boot:run -Dspring-boot.run.profiles=local
    ```

后端默认端口 `8080`，接口前缀 `/api`。

## 三、启动前端

```powershell
cd src/frontend
npm install
npm run dev
```

访问 http://localhost:5173 。Vite 已把 `/api` 代理到 `http://127.0.0.1:8080`，无需额外配置跨域。

生产构建：`npm run build`，产物在 `src/frontend/dist`。

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
| PUT | `/api/user/profile` | 登录 | 修改昵称、联系电话与国家或地区 |
| GET | `/api/user/page` | ADMIN | 分页查询用户，支持关键字搜索 |
| PUT | `/api/user/status` | ADMIN | 启用 / 禁用账号 |
| GET | `/api/category/tree` | 登录 | 两级分类树 |
| POST | `/api/category` | ADMIN / STAFF | 新增分类 |
| PUT | `/api/category/{id}` | ADMIN / STAFF | 编辑分类 |
| DELETE | `/api/category/{id}` | ADMIN / STAFF | 删除分类（有子分类或商品时拒绝） |
| GET | `/api/product/page` | 登录 | 商品分页与搜索；客户仅能看到上架商品 |
| GET | `/api/product/{id}` | 登录 | 商品详情（含中英文描述与 SKU） |
| POST | `/api/product` | ADMIN / STAFF | 新增商品（可同时提交 SKU 列表） |
| PUT | `/api/product/{id}` | ADMIN / STAFF | 编辑商品与 SKU |
| PUT | `/api/product/{id}/status` | ADMIN / STAFF | 上架 / 下架 |
| DELETE | `/api/product/{id}` | ADMIN / STAFF | 逻辑删除商品 |
| GET | `/api/address/list` | 登录 | 当前用户的收货地址列表 |
| POST | `/api/address` | 登录 | 新增收货地址（首个自动设为默认） |
| PUT | `/api/address/{id}` | 登录 | 编辑地址（校验数据归属） |
| DELETE | `/api/address/{id}` | 登录 | 删除地址（校验数据归属） |
| PUT | `/api/address/{id}/default` | 登录 | 设为默认地址 |

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
- [x] 阶段二：商品与客户功能（分类、商品与 SKU、商品浏览筛选、收货地址、个人资料）
- [ ] 阶段三：购物车与订单
- [ ] 阶段四：支付与履约
- [ ] 阶段五：售后与扩展功能
- [ ] 阶段六：测试、修复与交付

详细计划见 `docs/plan.md`，需求见 `docs/初始规划.md`。

## 八、常用命令

```powershell
# 后端编译
cd src/backend; mvn -DskipTests compile

# 后端测试
cd src/backend; mvn test

# 前端构建
cd src/frontend; npm run build
```

## 九、验证截图

骨架阶段已在浏览器中完成登录、角色菜单、越权拦截与退出流程验证，截图见 `docs/screenshots/`：

| 文件 | 内容 |
| ---- | ---- |
| `01-login.png` | 登录页（含演示账号一键填充） |
| `02-dashboard-admin.png` | 管理员工作台与角色菜单 |
| `03-user-management.png` | 管理员用户管理与启停入口 |
| `04-forbidden.png` | 普通客户访问管理员页面被拦截至 403 |
| `05-product-manage.png` | 商家商品管理（8 个演示商品、缺货与上下架状态） |
| `06-category-manage.png` | 两级分类管理 |
| `07-shop.png` | 客户端跨境商品目录与分类筛选 |
| `08-product-detail.png` | 商品详情抽屉（中英文描述与 SKU 规格） |
| `09-address.png` | 国际收货地址管理 |

接口层验证结果（curl / PowerShell 实测）：

- 正确密码登录返回 token 与角色；错误密码返回 400。
- 未携带 token 访问 `/api/user/me` 返回 401。
- 普通客户访问 `/api/user/page` 返回 403。
- 管理员分页查询返回 3 个演示账号。

## 十、代码托管

课程要求源码托管在**头歌平台创建的项目仓库**，仓库地址需填写在报告 4.4 节，评审期间必须可访问。

| 平台 | 地址 | 状态 |
| ---- | ---- | ---- |
| GitHub（开发主仓库） | https://github.com/shayebuhui23/ecommerce-system | 已推送全部提交 |
| 头歌（课程要求仓库） | 待补充 | 期末提交前需推送并填写到报告 4.4 节 |

推送命令：

```powershell
# GitHub（开发期主仓库）
git push -u origin main

# 头歌（第二个远端，按课程要求补推）
git remote add educoder <头歌仓库地址>
git push educoder main
```

说明：

- 头歌的 Git 地址形式为 `https://git.educoder.net/<用户名>/<项目名>.git`；`code.educoder.net/<用户名>/<项目名>` 只是网页地址，不能用于推送。
- `node_modules/`、`dist/`、`target/`、`application-local.yml` 已在 `.gitignore` 中排除，仓库中不含依赖、构建产物与数据库密码。
- 后续每次功能完成后提交并推送，保持提交记录连续、均衡（过程考核依据）。
- 若远端仓库已存在初始 README 等文件，推送前先 `git pull --rebase origin main --allow-unrelated-histories` 合并。

## 十一、测试用例与报告

- 测试用例文档：`test/test-cases/`
- 接口冒烟脚本：`test/api/smoke.ps1`（本机 PowerShell 直接执行，覆盖登录、401、403 与商品可见性）
- Java 单元测试：`src/backend/src/test/`（`mvn test` 执行）

详见 `test/README.md`。
