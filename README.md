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

执行后得到 `ecommerce` 库，包含 18 张业务表（users、roles、categories、products、product_skus、carts、cart_items、addresses、orders、order_items、payments、shipments、after_sales、reviews、inventory_logs、exchange_rates、operation_logs、notifications）。

> `schema.sql` 可重复执行（会先删除同名表），仅用于开发与课程演示环境。

已有数据库升级到多商家版本时，额外执行一次迁移脚本（新增 `products.seller_id`、第二个商家账号与更多商品分类）：

```powershell
mysql -u root -p < src/database/migration-multimerchant.sql
mysql -u root -p < src/database/migration-product-cost.sql
mysql -u root -p < src/database/migration-username-scheme.sql
mysql -u root -p < src/database/migration-notifications.sql
mysql -u root -p < src/database/migration-hardening.sql
mysql -u root -p < src/database/migration-aftersale-return.sql
```

## 一键启动（推荐）

项目根目录提供了 `start-dev.ps1`，会自动检查 MySQL，并在两个新窗口中分别启动后端与前端，等待端口就绪后打开浏览器：

```powershell
.\start-dev.ps1
# 不自动打开浏览器
.\start-dev.ps1 -NoBrowser
```

如果提示脚本被禁止运行，可改用：

```powershell
powershell -ExecutionPolicy Bypass -File .\start-dev.ps1
```

脚本打开的「电商后端 8080」「电商前端 5173」两个窗口需要保持开启，关闭窗口即停止对应服务。也可以按下面章节手动启动。

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
| seller000001 | 123456 | 商家运营 STAFF（小林优选） | 本店商品、库存、履约、售后审核 |
| seller000002 | 123456 | 商家运营 STAFF（云帆优选） | 第二个演示商家，用于验证商家隔离 |
| user000001 | 123456 | 普通客户 CUSTOMER | 浏览下单、支付、售后申请 |

用户名由系统按角色分配：客户 `user` + 6 位序号、商家 `seller` + 6 位序号、系统管理员固定 `admin`，注册后不可修改。
也可用邮箱登录（如 `admin@ecommerce.local`）。数据库中仅保存 BCrypt 哈希，不存明文。

## 五、已实现接口

| 方法 | 路径 | 权限 | 说明 |
| ---- | ---- | ---- | ---- |
| POST | `/api/auth/register` | 公开 | 注册账号，`registerType` 为 `CUSTOMER`（默认，客户）或 `MERCHANT`（商家，服务端映射 STAFF）；用户名由系统分配并返回 |
| POST | `/api/auth/login` | 公开 | 用户名或邮箱 + 密码登录，返回 token 与用户信息 |
| POST | `/api/auth/logout` | 登录 | 注销当前登录态 |
| GET | `/api/user/me` | 登录 | 当前用户信息与角色 |
| PUT | `/api/user/profile` | 登录 | 修改邮箱、昵称、联系电话与国家或地区（用户名、账号状态与角色不可改） |
| GET | `/api/user/page` | ADMIN | 分页查询用户，支持关键字搜索与 `roleCode` 角色筛选 |
| PUT | `/api/user/status` | ADMIN | 启用 / 禁用账号 |
| GET | `/api/category/tree` | 登录 | 两级分类树 |
| POST | `/api/category` | ADMIN | 新增分类 |
| PUT | `/api/category/{id}` | ADMIN | 编辑分类 |
| DELETE | `/api/category/{id}` | ADMIN | 删除分类（有子分类或商品时拒绝） |
| GET | `/api/product/page` | 登录 | 商品分页与搜索；`sellerId` 按商家筛选，`mine=true` 仅看本店；客户仅能看到上架商品；成本价仅管理员与商家可见 |
| GET | `/api/product/{id}` | 登录 | 商品详情（含中英文描述、商家与 SKU）；成本价仅管理员与商家可见 |
| POST | `/api/product` | ADMIN / STAFF | 新增商品（管理员需指定 `sellerId`，商家自动归属本店） |
| PUT | `/api/product/{id}` | ADMIN / STAFF | 编辑商品与 SKU（商家只能操作本店商品） |
| PUT | `/api/product/{id}/status` | ADMIN / STAFF | 上架 / 下架（商家只能操作本店商品） |
| DELETE | `/api/product/{id}` | ADMIN / STAFF | 逻辑删除商品（商家只能操作本店商品） |
| GET | `/api/address/list` | 登录 | 当前用户的收货地址列表 |
| POST | `/api/address` | 登录 | 新增收货地址（首个自动设为默认） |
| PUT | `/api/address/{id}` | 登录 | 编辑地址（校验数据归属） |
| DELETE | `/api/address/{id}` | 登录 | 删除地址（校验数据归属） |
| PUT | `/api/address/{id}/default` | 登录 | 设为默认地址 |
| GET | `/api/cart` | 登录 | 购物车列表，金额与可用性由后端实时计算 |
| POST | `/api/cart/items` | 登录 | 加入购物车（同一商品+规格累加数量，商品有 SKU 时必须传 `skuId`） |
| PUT | `/api/cart/items/{id}` | 登录 | 修改数量（校验上架状态、SKU 与库存） |
| DELETE | `/api/cart/items/{id}` | 登录 | 移出购物车 |
| DELETE | `/api/cart/items` | 登录 | 清空购物车 |
| POST | `/api/order` | 登录 | 从购物车创建订单，或传 `productId + skuId + quantity` 立即购买；一单只允许一个商家；30 分钟未支付会自动取消并回补库存 |
| GET | `/api/order/page` | 登录 | 订单分页；客户看自己的订单，商家看本店商品相关订单，管理员可按 `sellerId` 筛选；`afterSale=true` 仅返回售后处理中/已退款订单 |
| GET | `/api/order/{id}` | 登录 | 订单详情（含商品快照与地址快照） |
| PUT | `/api/order/{id}/cancel` | 登录 | 取消待付款订单并恢复库存 |
| POST | `/api/payment/pay` | 登录 | 模拟支付（可模拟成功/失败），成功后订单转待发货并累计销量；退款写入负数退款流水 |
| GET | `/api/payment/order/{orderId}` | 登录 | 查询订单支付记录 |
| GET | `/api/payment/page` | ADMIN | 管理员分页查询支付记录 |
| POST | `/api/order/{id}/ship` | STAFF | 商家发货并录入物流公司与单号 |
| POST | `/api/order/{id}/urge` | 登录 | 客户催发货（仅待发货订单，写入操作日志并给商家发送站内提醒） |
| GET | `/api/notification/list` | 登录 | 当前用户的通知列表 |
| GET | `/api/notification/unread-count` | 登录 | 当前用户未读通知数量 |
| PUT | `/api/notification/{id}/read` | 登录 | 将一条通知标记为已读 |
| PUT | `/api/notification/read-all` | 登录 | 全部标记为已读 |
| GET | `/api/order/{id}/shipment` | 登录 | 查询订单物流信息 |
| POST | `/api/order/{id}/receive` | 登录 | 客户确认收货 |
| POST | `/api/after-sale` | 登录 | 提交售后申请，`type` 为 `REFUND_ONLY`（仅退款）或 `RETURN_REFUND`（退货退款）；待发货只能仅退款，已发货/已完成只能退货退款 |
| PUT | `/api/after-sale/{id}/audit` | STAFF | 商家审核：仅退款通过即退款；退货退款通过后等待买家寄回 |
| PUT | `/api/after-sale/{id}/return-shipment` | 登录 | 买家填写退货物流，售后进入待商家收货 |
| PUT | `/api/after-sale/{id}/confirm-receipt` | STAFF | 商家确认收到退货，完成退款并恢复库存 |
| GET | `/api/after-sale/order/{orderId}` | 登录 | 查询订单的售后单 |
| GET | `/api/after-sale/page` | 登录 | 售后单分页；客户看自己的，商家看本店商品的售后，管理员可按 `sellerId` 筛选 |
| POST | `/api/review` | 登录 | 提交商品评价（仅已完成订单） |
| GET | `/api/review/product/{productId}` | 登录 | 商品评价列表 |
| GET | `/api/review/product/{productId}/summary` | 登录 | 商品评分汇总 |
| GET | `/api/statistics/overview` | ADMIN / STAFF | 经营统计概览（含成本、毛利与毛利率）；管理员统计全平台，商家仅统计本店数据 |
| GET | `/api/exchange-rate/list` | 登录 | 汇率列表 |
| PUT | `/api/exchange-rate/{id}` | ADMIN | 调整模拟汇率 |
| GET | `/api/operation-log/page` | ADMIN | 关键操作审计日志 |

统一响应结构：

```json
{ "code": 200, "message": "操作成功", "data": {} }
```

HTTP 状态码与业务码分离：未登录返回 401、越权返回 403、参数校验失败返回 400，响应体中 `code` 为细分业务码。

## 六、权限说明

- 登录态由 Sa-Token 校验，除登录/注册外的所有接口默认需要登录。
- 注册接口只接受 `CUSTOMER` / `MERCHANT` 两种注册类型，商家注册由服务端映射为 STAFF，客户端无法指定角色 ID 提权。
- 商家运营只能维护本店商品，也只能看到包含本店商品的订单与售后；商品、订单、售后均按 `products.seller_id` 做服务端隔离。
- 管理员接口使用 `@SaCheckRole("ADMIN")` 在服务端强制校验，前端菜单隐藏不作为权限依据。
- 每次受保护请求都会校验账号状态，被禁用账号即使持有未过期令牌也无法继续操作。
- 管理员不能禁用自己的账号。

## 七、币种与金额规则

| 规则 | 说明 |
| ---- | ---- |
| 基础币种 | 商品价格统一以 **CNY** 记录，商品表 `currency` 字段标记 |
| 订单币种 | 下单时可选 CNY 或 USD，默认 CNY |
| 汇率来源 | 管理员在「汇率管理」维护模拟汇率，不接实时外汇接口 |
| 汇率快照 | 下单时把当时的汇率写入订单 `exchange_rate`，后续调价不影响历史订单 |
| 金额计算 | 商品金额与运费先按 CNY 计算并判断是否包邮（满 500 免运费），再按汇率换算为订单币种 |
| 统计口径 | 统计金额使用订单的 `base_amount`（下单时折算的 CNY 金额），不同币种不会直接相加 |

## 八、开发进度

- [x] 阶段一：项目初始化（工程、数据库、统一响应、登录与角色权限）
- [x] 阶段二：商品与客户功能（分类、商品与 SKU、商品浏览筛选、收货地址、个人资料）
- [x] 阶段三：购物车与订单（购物车、下单、库存预扣、订单查询与取消）
- [x] 阶段四：支付与履约（模拟支付、发货、物流跟踪、确认收货）
- [x] 阶段五：售后与扩展功能（退款审核、商品评价、币种汇率、经营统计、操作审计）
- [x] 阶段六：测试、修复与交付（回归 43 项通过、部署说明、代码统计与交付清单）

详细计划见 `docs/plan.md`，需求见 `docs/初始规划.md`。

## 九、常用命令

```powershell
# 后端编译
cd src/backend; mvn -DskipTests compile

# 后端测试
cd src/backend; mvn test

# 前端构建
cd src/frontend; npm run build
```

## 十、验证截图

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
| `10-cart.png` | 购物车（实时金额与数量调整） |
| `11-checkout.png` | 确认订单（地址选择与金额明细） |
| `12-orders.png` | 我的订单与状态筛选 |
| `13-order-detail.png` | 订单详情与取消入口 |
| `14-order-manage.png` | 商家订单管理 |
| `15-payment.png` | 模拟支付弹窗（含失败分支演示） |
| `16-ship.png` | 商家发货弹窗（物流公司与单号） |
| `17-shipment.png` | 客户查看物流信息与确认收货 |
| `18-refund.png` | 订单退款申请与售后状态 |
| `19-after-sale-audit.png` | 商家售后审核（通过/驳回） |
| `20-statistics.png` | 经营统计看板 |
| `21-exchange-rate.png` | 汇率管理 |
| `22-operation-log.png` | 操作审计日志 |
| `23-e2e-regression.png` | 阶段六端到端回归（下单→支付→发货→收货全链路） |

接口层验证结果（curl / PowerShell 实测）：

- 正确密码登录返回 token 与角色；错误密码返回 400。
- 未携带 token 访问 `/api/user/me` 返回 401。
- 普通客户访问 `/api/user/page` 返回 403。
- 管理员分页查询返回 3 个演示账号。

## 十一、代码托管

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

## 十二、测试用例与报告

| 材料 | 位置 | 说明 |
| ---- | ---- | ---- |
| 功能测试用例 | `test/test-cases/` | 5 个阶段共 127 条用例，含缺陷跟踪 |
| 接口回归脚本 | `test/api/smoke.ps1` | 43 项断言，覆盖认证、越权、购物车、下单、支付、发货、收货、评价、售后、统计 |
| Java 单元测试 | `src/backend/src/test/` | `mvn test` 执行，3 个用例 |
| 代码统计与交付清单 | `docs/代码统计与交付清单.md` | 分层规模、接口数量、测试统计、交付物清单 |
| 验证截图 | `docs/screenshots/` | 23 张，覆盖各阶段关键界面 |

执行方式：

```powershell
# 单元测试
cd src/backend; mvn test

# 接口回归（需后端已启动、数据库已导入）
cd d:/ecommerce-system
powershell -ExecutionPolicy Bypass -File test/api/smoke.ps1
```

最近一次回归结果：`mvn test` 3/3 通过，`smoke.ps1` 43/43 通过，前端构建成功，浏览器端到端链路通过。

## 十三、常见问题

**Q1：启动后端报 `Access denied for user 'root'`**
数据库密码没有注入。检查 `src/backend/src/main/resources/application-local.yml` 是否存在且密码正确，或启动前设置环境变量：

```powershell
$env:MYSQL_PASSWORD="你的密码"
cd src/backend; mvn spring-boot:run -Dspring-boot.run.profiles=local
```

**Q2：8080 或 5173 端口被占用**
先结束占用进程再启动：

```powershell
Get-NetTCPConnection -LocalPort 8080,5173 -State Listen -ErrorAction SilentlyContinue |
  ForEach-Object { Stop-Process -Id $_.OwningProcess -Force }
```

**Q3：前端页面能打开但接口 404 / 跨域**
确认后端已在 8080 运行；Vite 已把 `/api` 代理到 `http://127.0.0.1:8080`，不要直接修改 `baseURL`。

**Q4：`mvn` 用的是 JDK 8 导致编译失败**
`JAVA_HOME` 必须指向 JDK 11 及以上，改完需重开终端：

```powershell
java -version; mvn -v
```

**Q5：演示账号登录提示被禁用**
管理员 `admin` 在「用户管理」中重新启用即可；禁用后该账号的 token 会被后端拦截并返回 401。

**Q6：想恢复初始演示数据**
重新执行脚本（会清空并重建演示数据，不影响表结构）：

```powershell
mysql -u root -p < src/database/seed.sql
```

**Q7：终端里中文输出乱码**
属于 PowerShell 代码页问题，不影响数据本身，可先执行 `chcp 65001` 再运行脚本。
