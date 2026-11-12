# 贡献指南

本项目为软件工程课程实践项目，同时保持可协作的工程规范。以下约定由现有提交历史与目录结构整理而来，供后续参与开发的同学参照。

## 环境准备

```bash
git clone https://github.com/shayebuhui23/ecommerce-system.git
cd ecommerce-system
mysql -u root -p < src/database/schema.sql
mysql -u root -p < src/database/seed.sql
```

后端与前端启动方式见 `README.md` 的「快速开始」。数据库密码通过 `MYSQL_PASSWORD` 环境变量或 `application-local.yml` 提供，任何情况下都不要提交到仓库。

## 分支约定

| 类型 | 命名示例 | 用途 |
| ---- | -------- | ---- |
| 功能 | `feat/order-timeout` | 新增业务能力 |
| 修复 | `fix/cart-quantity-limit` | 缺陷修复 |
| 文档 | `docs/readme-structure` | 文档补充与整理 |
| 工程 | `chore/ci-workflow` | 构建、依赖、配置调整 |

分支从 `main` 切出，合入前保持与 `main` 同步。

## 提交信息

格式为 `类型: 中文摘要`，与现有提交历史保持一致。

```
feat: 实现订单超时自动关闭
fix: 修复购物车数量上限校验缺失
docs: 补充订单状态流转说明
test: 补充阶段六回归用例
chore: 忽略 IDE 助手数据目录
```

一次提交只做一件事。功能提交与对应的用例补充可以分两次提交，便于评审时定位。

## 提交前自检

```bash
# 后端编译
cd src/backend && mvn -DskipTests compile

# 后端单元测试，需 MySQL 已建库
cd src/backend && mvn test

# 前端构建
cd src/frontend && npm run build
```

涉及接口行为的改动，还需在数据库与后端就绪的前提下执行接口回归。

```powershell
powershell -ExecutionPolicy Bypass -File test/api/smoke.ps1
```

## 改动约定

- 金额与库存相关逻辑集中在 Service 层，Controller 只做参数接收与权限注解。
- 订单状态流转必须在 Service 层通过条件更新或显式状态校验完成，禁止无条件覆盖状态字段；调整状态机时同步更新 `OrderStatus`、相关 Service 与回归用例。
- 库存扣减必须使用带条件的更新语句，先查后改会在并发下超卖。
- 新增或调整接口时，同步更新 `README.md` 的接口清单与权限列。
- 数据库脚本保持可重复执行，`schema.sql` 先删表再建表，`seed.sql` 只重建演示数据。
- 新增页面时在 `src/frontend/src/router/routes.js` 中声明 `meta.roles` 与 `meta.group`，菜单由角色自动过滤。

## 提交方式

外部贡献走 Fork 与 Pull Request。

1. Fork 本仓库并在自己的分支上完成改动。
2. 按上文自检清单验证通过。
3. 发起 PR，说明改动动机、影响范围与验证方式。
4. 评审通过后由维护者合并到 `main`。
