# 测试说明

本目录存放课程要求的**测试用例与测试报告**，与 `src/backend/src/test` 下的 Java 单元测试互补。

## 目录

```
test/
├── test-cases/            功能测试用例（按阶段划分）
│   ├── 阶段一-认证与权限.md
│   └── 阶段二-商品与地址.md
└── api/
    └── smoke.ps1          接口冒烟脚本
```

## 测试分层

| 层次 | 位置 | 说明 |
| ---- | ---- | ---- |
| 单元测试 | `src/backend/src/test/` | JUnit 5 + Spring Boot Test，`mvn test` 执行 |
| 接口测试 | `test/api/smoke.ps1` | PowerShell 直接调用后端接口，覆盖认证、越权与数据可见性 |
| 功能测试 | `test/test-cases/` | 手工/半自动执行的用例与结果记录，对应报告第 5 章 |

## 执行方式

### 1. 单元测试

```powershell
cd src/backend
mvn test
```

### 2. 接口冒烟测试

前置条件：MySQL 已导入 `src/database/schema.sql` 与 `seed.sql`，后端已在 8080 端口启动。

```powershell
cd d:/ecommerce-system
powershell -ExecutionPolicy Bypass -File test/api/smoke.ps1
```

可选参数：

```powershell
powershell -ExecutionPolicy Bypass -File test/api/smoke.ps1 -BaseUrl http://127.0.0.1:8080 -Password 123456
```

脚本共 43 项断言，覆盖以下行为：

- 三角色登录与角色返回、错误密码、未登录、跨角色越权（管理员接口、统计接口、审计日志）
- 客户与商家在商品可见性上的差异（下架商品对客户不可见）
- 购物车加购、数量合并、后端算价、非法数量拦截
- 下单、库存扣减、购物车清空、金额由后端计算
- 模拟支付失败不改订单状态、支付成功流转待发货、重复支付拦截
- 客户越权发货拦截、商家发货、确认收货、重复收货拦截
- 商品评价与重复评价拦截、评分汇总
- 退款申请与重复申请拦截、商家驳回
- 经营统计、汇率列表、操作审计日志

注意：脚本会创建一笔订单并走完“支付→发货→收货→评价→退款申请（驳回）”流程，属于会写入业务数据的回归测试，建议在演示库或测试库执行。

## 用例维护约定

- 用例编号规则：`TC-<阶段>-<序号>`，例如 `TC-P2-05`
- 每次功能变更后补充或更新对应用例，并在“实际结果”列记录验证结论
- 缺陷单独记录在用例文档的“缺陷跟踪”小节，修复后在 Git 提交信息中引用编号
