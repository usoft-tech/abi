# ABI

[English](./README.md) | [简体中文](./README.zh-CN.md)

ABI 是由 Usoft 维护的 AI 驱动型商业智能平台，面向数据源、数据集、
报表、交互式 BI 页面、分享协作、租户管理和 AI 辅助分析等场景，提供
一套可扩展的一体化产品能力。

本项目作为开源产品发布，使用 [Apache License 2.0](./LICENSE) 开源协议。

## 功能特性

- 多租户系统管理：用户、角色、权限、租户、字典、系统配置、文件上传、
  图标资源和租户站点设置。
- 安全认证能力：账号密码登录、客户端认证、JWT 访问控制、第三方登录和
  CAS 集成。
- BI 管理能力：数据源、数据集、应用、页面、页面模板、报表、分享和授权范围。
- AI 辅助能力：模型配置、会话、技能导入、SQL 生成、页面助手和报表解读。
- 多数据库初始化脚本：支持 MySQL、PostgreSQL、H2、Oracle 和 SQL Server。
- 基于 React 的管理控制台和 BI 设计/运行界面。

## 项目结构

本仓库由 Maven 多模块后端和独立 React 前端组成：

| 路径 | 说明 |
| --- | --- |
| `usoft-common` | 通用 API 模型、异常、工具类、SSE 支持和共享基础能力。 |
| `usoft-core` | Spring 基础设施、MyBatis 工具、租户支持、安全上下文和全局异常处理。 |
| `usoft-security*` | 认证、授权、JWT、第三方登录和 CAS 支持。 |
| `usoft-system*` | 用户、角色、租户、权限、文件、图标和系统配置等系统管理能力。 |
| `usoft-ai*` | AI 模型、会话、技能、提示词和 Agent 集成能力。 |
| `usoft-bi*` | BI 数据源、数据集、页面、报表、分享和授权能力。 |
| `usoft-launcher` | Spring Boot 应用入口和运行配置。 |
| `webapp/packages/bi-react` | 基于 Rsbuild 构建的 React 前端。 |
| `assembly` | 运行包脚本、Docker 资源和二进制打包描述。 |

## 环境要求

- Java 21
- Maven 3.9 或更高版本
- Node.js 22
- npm 10
- 非 H2 内存模式运行时，需要准备受支持的数据库

构建后端前，请确认 Maven 正在使用 Java 21：

```bash
java -version
mvn -v
```

## 后端

后端启动入口位于 `usoft-launcher` 模块。

### 配置说明

默认服务端口为 `8080`，后端上下文路径为 `/jgsz-api`。

数据库配置通过 `DB_TYPE` 选择：

| `DB_TYPE` | 配置文件 |
| --- | --- |
| `mysql` | `application-mysql.yml` |
| `pgsql` | `application-pgsql.yml` |
| `h2` | `application-h2.yml` |
| `oracle` | `application-oracle.yml` |
| `sqlserver` | `application-sqlserver.yml` |

常用环境变量：

| 变量 | 说明 |
| --- | --- |
| `DB_TYPE` | 数据库类型，默认值为 `mysql`。 |
| `JWT_SECRET` | JWT 签名密钥，正常认证流程必须配置。 |
| `JWT_TTL_SECONDS` | JWT 有效期，单位秒，默认 `3600`。 |
| `MYSQL_HOST`, `MYSQL_PORT`, `MYSQL_DATABASE` | MySQL 连接配置。 |
| `MYSQL_USERNAME`, `MYSQL_PASSWORD` | MySQL 账号和密码。配置中包含开发默认值。 |
| `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD`, `REDIS_DATABASE` | Redis 连接配置。 |
| `OPENAI_API_BASE_URL`, `OPENAI_API_KEY` | OpenAI 兼容模型服务配置。 |
| `OPENAI_DEFAULT_MODEL`, `OPENAI_CODE_MODEL`, `OPENAI_EMBEDDING_MODEL` | 默认模型名称配置。 |
| `DASHSCOPE_API_BASE_URL`, `DASHSCOPE_API_KEY`, `DASHSCOPE_CODE_MODEL` | DashScope 模型服务配置。 |
| `SM2_PRIVATE_KEY` | 登录密码解密使用的 SM2 私钥。 |
| `BI_CHAT_FILE_DB_PATH` | BI 对话文件缓存数据库路径。 |
| `FILE_STORAGE_PATH`, `FILE_VISIT_PATH_PREFIX` | 上传文件存储路径和访问前缀。 |

### 本地运行

轻量本地运行可使用 H2 配置：

```bash
export DB_TYPE=h2
export JWT_SECRET=change-this-local-secret
export OPENAI_API_KEY=your-openai-compatible-api-key

mvn -pl usoft-launcher -am spring-boot:run
```

使用 MySQL 时，请先准备本地数据库，然后运行：

```bash
export DB_TYPE=mysql
export MYSQL_HOST=localhost
export MYSQL_PORT=3306
export MYSQL_DATABASE=bi
export MYSQL_USERNAME=your-database-user
export MYSQL_PASSWORD=your-database-password
export JWT_SECRET=change-this-local-secret
export OPENAI_API_KEY=your-openai-compatible-api-key

mvn -pl usoft-launcher -am spring-boot:run
```

数据库结构和初始化数据位于 `usoft-launcher/src/main/resources/db`。

### 构建

```bash
mvn -DskipTests package
```

启动包由 `usoft-launcher` 模块生成。

## 前端

前端代码位于 `webapp/packages/bi-react`。

### 配置说明

前端运行行为通过公开环境变量控制：

| 变量 | 说明 |
| --- | --- |
| `PUBLIC_BASE_PATH` | 前端基础路径，默认 `/webapp`。 |
| `PUBLIC_API_URL` | 前端 API 基础路径，默认 `/api`。 |
| `PUBLIC_SM2_PUBLIC_KEY` | 提交密码前使用的 SM2 公钥。 |
| `PUBLIC_REGISTER_ENABLED` | 设置为 `true` 时启用注册入口。 |
| `PUBLIC_FORGOT_PASSWORD_ENABLED` | 设置为 `true` 时启用忘记密码入口。 |
| `PUBLIC_SSO_THIRD_ENABLED` | 设置为 `true` 时启用第三方 SSO 入口。 |
| `PUBLIC_SSO_CAS_ENABLED` | 设置为 `true` 时启用 CAS SSO 入口。 |
| `PUBLIC_SSO_WECHAT_OPEN_ENABLED` | 设置为 `true` 时启用微信开放平台 SSO 入口。 |
| `PUBLIC_SSO_DINGTALK_ENABLED` | 设置为 `true` 时启用钉钉 SSO 入口。 |
| `PUBLIC_SSO_GITHUB_ENABLED` | 设置为 `true` 时启用 GitHub SSO 入口。 |

### 本地运行

```bash
cd webapp/packages/bi-react
npm ci
npm run dev
```

开发服务器默认将 API 请求代理到 `http://localhost:8080`。如需指向其它后端实例，
可设置 `PROXY_TARGET`。

### 构建

```bash
cd webapp/packages/bi-react
npm ci
npm run build
```

构建产物输出到 `webapp/packages/bi-react/dist`。

## 生产部署注意事项

部分默认配置是为了降低本地开发门槛。生产部署前必须完成以下检查：

- 配置高强度 `JWT_SECRET`。
- 覆盖 `SM2_PRIVATE_KEY`，并配置匹配的前端 `PUBLIC_SM2_PUBLIC_KEY`；
  内置 SM2 fallback 仅用于本地开发。
- 不要依赖默认数据库账号或密码。MySQL 配置默认使用本地开发账号
  `root`/`root`，生产环境必须通过环境变量覆盖。
- 评估目标环境是否允许暴露 H2 Console；它默认面向本地开发启用。
- 通过环境变量或部署密钥配置 Redis、上传存储、文件访问地址、模型服务和模型 API Key。
- 确认数据库初始化模式符合部署环境要求。

## 商务合作

商务合作请联系 qiwei@lzfsd.com。

## 开源协议

ABI 使用 Apache License 2.0 开源协议。完整协议内容见 [LICENSE](./LICENSE)。
