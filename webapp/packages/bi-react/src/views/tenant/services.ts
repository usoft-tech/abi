import { PageResponse } from "@/services/api";
import { AppMenuType, AppResponse } from "@/services/app";
import request, { EnableStatus } from "@/services/request";
import { PageSchema } from "bi-sdk-react";
import {
  ChatRequestType,
  ChatResponseType,
  SchemaItemType,
} from "bi-sdk-react/dist/types/components/typing";
import { TenantUserResponse } from "../sa/services";
import { ShareLinkCreateRequest, ShareLinkResponse } from "@/services/share";
import { SseOptions, startSSE } from "@/services/sse";
import config from "@/config";

/****************************** 用户管理 start ******************************/

export type UserResponse = {
  id: string;
  username: string;
  departmentId?: string;
  employeeNo?: string;
  displayName?: string;
  status?: EnableStatus;
};

export type UserQueryRequest = {
  page: number;
  size: number;
  username?: string;
  departmentId?: string;
  employeeNo?: string;
  keyword?: string;
};

/**
 * 获取当前租户的用户分页列表
 */
export const listTenantUsers = (params: UserQueryRequest) => {
  return request.get<PageResponse<TenantUserResponse>>(
    "/system/tenants/users",
    {
      ...params,
    },
  );
};

/**
 * 获取全局用户分页列表
 */
export const listUsersFromGlobal = (params: UserQueryRequest) => {
  return request.get<PageResponse<UserResponse>>("/system/users/global", {
    ...params,
  });
};

/**
 * 为当前租户添加用户
 */
export const addTenantUsers = (userIds: string[]) => {
  return request.post<boolean>("/system/tenants/users", userIds);
};

/**
 * 从当前租户移除用户
 */
export const removeTenantUser = (userId: string) => {
  return request.delete<boolean>(`/system/tenants/users/${userId}`);
};

/****************************** 用户管理 end ******************************/

/****************************** 图标管理 start ******************************/

export type SysIconQueryRequest = {
  name?: string;
  keyword?: string;
  page?: number;
  size?: number;
};

export type SysIconResponse = {
  id: string;
  name: string;
  svg: string;
  description?: string | null;
};

/**
 * 获取图标列表
 */
export const listIcons = (params: SysIconQueryRequest) => {
  return request.get<PageResponse<SysIconResponse>>("/icons/list", params, {
    headers: { Accept: "application/json" },
  });
};

/**
 * 上传图标
 */
export const uploadIcon = (
  file: File,
  keepFill: boolean,
  name?: string,
  description?: string,
) => {
  const formData = new FormData();
  formData.append("file", file);
  formData.append("keepFill", String(keepFill));
  if (name) formData.append("name", name);
  if (description) formData.append("description", description);
  return request.post<SysIconResponse>("/icons/upload", formData, {
    headers: { "Content-Type": "multipart/form-data" },
  });
};

/**
 * 删除图标
 */
export const deleteIcon = (id: string) => {
  return request.delete<SysIconResponse>(`/icons/${id}`);
};

/****************************** 图标管理 end ******************************/

/****************************** 页面管理 start ******************************/

export type PageQueryRequest = {
  name?: string;
  status?: string;
  keyword?: string;
  page?: number;
  size?: number;
};

export type PageCreateRequest = {
  name: string;
  description?: string;
  industry?: string;
  status?: string;
  cover?: string;
};

export type PageUpdateRequest = {
  name?: string;
  description?: string;
  industry?: string;
  status?: string;
  cover?: string;
};

export type PageUpdateSchemaRequest = {
  schemaJson?: string;
};

export type PageResp = {
  id: string;
  name: string;
  description?: string;
  industry?: string;
  hasWatermark?: boolean;
  schema?: PageSchema;
  status: string;
  cover?: string;
  excels?: any[] | null;
};

/**
 * 分页查询页面
 */
export const listPages = (params: PageQueryRequest) => {
  return request.get<PageResponse<PageResp>>("/bi/pages", params);
};

/**
 * 创建页面
 */
export const createPage = (data: PageCreateRequest) => {
  return request.post<PageResp>("/bi/pages", data);
};

/**
 * 更新页面
 */
export const updatePage = (id: string, data: PageUpdateRequest) => {
  return request.put<PageResp>(`/bi/pages/${id}`, data);
};

/**
 * 更新页面设计
 */
export const updatePageSchema = (id: string, data: PageSchema) => {
  return request.put<PageResp>(`/bi/pages/schema-${id}`, data);
};

/**
 * 删除页面
 */
export const deletePage = (id: string) => {
  return request.delete<boolean>(`/bi/pages/${id}`);
};

/**
 * 获取页面详情
 */
export const getPage = (id: string) => {
  return request.get<PageResp>(`/bi/pages/${id}`);
};

/**
 * 页面助手聊天
 */
export const assistantChat = (
  bizType: string,
  bizId: string,
  conversationId: string | null,
  requestData: ChatRequestType,
) => {
  return request.post<ChatResponseType>(
    `/bi/pages/assistant-chat`,
    {
      ...requestData,
      bizType,
      bizId,
      conversationId,
    },
    {
      timeout: 0,
    },
  );
};

/**
 * 获取页面分享链接
 */
export const getPageShareLink = (id: string) => {
  return request.get<ShareLinkResponse>(`/bi/pages/share/${id}`);
};

/**
 * 创建页面分享链接
 */
export const createPageShareLink = (data: ShareLinkCreateRequest) => {
  return request.post<ShareLinkResponse>(`/bi/pages/share`, data);
};

/**
 * 删除页面分享链接
 */
export const deletePageShareLink = (id: string) => {
  return request.delete<boolean>(`/bi/pages/share/${id}`);
};

/**
 * 获取分享页面详情
 */
export const getSharePage = (shareKey: string) => {
  return request.get<PageResp>(`/bi/pages/share-schema/${shareKey}`);
};

/**
 * 页面数据解读
 */
export const pageInterpretation = ({
  id,
  data,
  onOpen,
  onMessage,
  onError,
  onClose,
}: Omit<SseOptions, "url"> & { id: string }) => {
  return startSSE({
    url: config.apiBasePath + `/bi/pages/interpret/${id}`,
    data,
    onOpen,
    onMessage,
    onError,
    onClose,
  });
};

/****************************** 页面管理 end ******************************/

type PageExampleItem = {
  id: string;
  name: string;
  description?: string;
  cover?: string;
  item: any;
}

type PageTemplateItem = {
  id: string;
  name: string;
  description?: string;
  cover?: string;
  schema: PageSchema;
}

/**
 * 分页查询页面片段
 */
export const listPageExamples = (params: any) => {
  return request.get<PageResponse<PageExampleItem>>("/bi/page-examples", params);
};

/**
 * 获取页面片段详情
 */
export const getPageExample = (id: string) => {
  return request.get<PageExampleItem>(`/bi/page-examples/${id}`);
};

/**
 * 分页查询页面模板
 */
export const listPageTemplates = (params: any) => {
  return request.get<PageResponse<PageTemplateItem>>("/bi/page-templates", params);
};

/**
 * 获取页面模板详情
 */
export const getPageTemplate = (id: string) => {
  return request.get<PageTemplateItem>(`/bi/page-templates/${id}`);
};

/****************************** 数据源管理 start ******************************/

export type DataSourceQueryRequest = {
  name?: string;
  type?: string;
  keyword?: string;
  page?: number;
  size?: number;
};

export type DataSourceCreateRequest = {
  name: string;
  type: string;
  driverClassName?: string;
  url?: string;
  host?: string;
  port?: number;
  databaseName?: string;
  username?: string;
  password?: string;
  extProps?: string;
};

export type DataSourceUpdateRequest = {
  name?: string;
  type?: string;
  driverClassName?: string;
  url?: string;
  host?: string;
  port?: number;
  databaseName?: string;
  username?: string;
  password?: string;
  extProps?: string;
};

export type DataSourceResponse = {
  id: string;
  name: string;
  type: string;
  driverClassName?: string;
  url?: string;
  host?: string;
  port?: number;
  databaseName?: string;
  username?: string;
  extProps?: string;
};

/**
 * 分页查询数据源
 */
export const listDataSources = (params: DataSourceQueryRequest) => {
  return request.get<PageResponse<DataSourceResponse>>(
    "/bi/datasources",
    params,
  );
};

/**
 * 创建数据源
 */
export const createDataSource = (data: DataSourceCreateRequest) => {
  return request.post<DataSourceResponse>("/bi/datasources", data, {
    timeout: 0,
  });
};

/**
 * 更新数据源
 */
export const updateDataSource = (id: string, data: DataSourceUpdateRequest) => {
  return request.put<DataSourceResponse>(`/bi/datasources/${id}`, data, {
    timeout: 0,
  });
};

/**
 * 更新数据源
 */
export const testDataSourceConnection = (
  id: string,
  data: DataSourceUpdateRequest | DataSourceCreateRequest,
) => {
  return request.post<DataSourceResponse>(
    `/bi/datasources/test-connection/${id || ""}`,
    data,
  );
};

/**
 * 删除数据源
 */
export const deleteDataSource = (id: string) => {
  return request.delete<boolean>(`/bi/datasources/${id}`);
};

/**
 * 获取数据源详情
 */
export const getDataSource = (id: string) => {
  return request.get<DataSourceResponse>(`/bi/datasources/${id}`);
};

export type DatasourceDbResponse = {
  id: string;
  datasourceId: string;
  dbid: string;
  name: string;
  type: string;
  description?: string;
  status: string;
};

export type DatasourceTableResponse = {
  id: string;
  datasourceId: string;
  dbId: string;
  name: string;
  description?: string;
  status: string;
};

export type ChatBiAgentResponse = {
  id: string;
  type: string | null;
  name: string;
  group: boolean;
  children?: ChatBiAgentResponse[];
};

export type DatasourceFieldResponse = {
  id: string;
  tableId: string;
  name: string;
  type: string;
  description?: string;
  status: string;
};

export type DatasourceFieldQueryRequest = {
  datasourceId?: string;
  dbId?: string;
  tableId?: string;
  name?: string;
  page?: number;
  size?: number;
};

export type DatasourceStatusUpdateRequest = {
  status: string;
};

/**
 * 同步数据源结构
 */
export const syncDataSourceSchema = (id: string) => {
  return request.put<boolean>(`/bi/datasources/${id}/sync-schema`, undefined, {
    timeout: 0,
  });
};

/**
 * 获取数据库列表
 */
export const listDataSourceDbs = (datasourceId: string) => {
  return request.get<DatasourceDbResponse[]>(`/bi/datasources/dbs`, {
    datasourceId,
  });
};

/**
 * 获取表列表
 */
export const listDataSourceTables = (dbId: string) => {
  return request.get<DatasourceTableResponse[]>(`/bi/datasources/tables`, {
    dbId,
  });
};

/**
 * 获取表列表
 */
export const listChatBiAgents = (type?: string) => {
  return request.get<ChatBiAgentResponse[]>(`/bi/datasources/agents`, {
    type,
  });
};

/**
 * 分页查询字段
 */
export const pageDataSourceFields = (params: DatasourceFieldQueryRequest) => {
  return request.get<PageResponse<DatasourceFieldResponse>>(
    "/bi/datasources/fields",
    params,
  );
};

/**
 * 更新数据库状态
 */
export const updateDataSourceDbStatus = (id: string, status: string) => {
  return request.put<boolean>(`/bi/datasources/dbs/${id}/status`, { status });
};

/**
 * 更新表状态
 */
export const updateDataSourceTableStatus = (id: string, status: string) => {
  return request.put<boolean>(`/bi/datasources/tables/${id}/status`, {
    status,
  });
};

/**
 * 更新字段状态
 */
export const updateDataSourceFieldStatus = (id: string, status: string) => {
  return request.put<boolean>(`/bi/datasources/fields/${id}/status`, {
    status,
  });
};

/**
 * 更新数据库描述
 */
export const updateDataSourceDbDescription = (
  id: string,
  description: string,
) => {
  return request.put<boolean>(`/bi/datasources/dbs/${id}/description`, {
    description,
  });
};

/**
 * 更新表描述
 */
export const updateDataSourceTableDescription = (
  id: string,
  description: string,
) => {
  return request.put<boolean>(`/bi/datasources/tables/${id}/description`, {
    description,
  });
};

/**
 * 更新字段描述
 */
export const updateDataSourceFieldDescription = (
  id: string,
  description: string,
) => {
  return request.put<boolean>(`/bi/datasources/fields/${id}/description`, {
    description,
  });
};

/****************************** 数据源管理 end ******************************/

/****************************** 数据集管理 start ******************************/

export type DatasetFolderCreateRequest = {
  name: string;
  parentId?: string;
  sort?: number;
};

export type DatasetFolderUpdateRequest = {
  name?: string;
  sort?: number;
  parentId?: string;
};

export type DatasetFolderEntity = {
  id: string;
  tenantId: string;
  name: string;
  parentId?: string;
  ancestorIds?: string;
  sort?: number;
  ancestorSorts?: string;
  level?: number;
  isLeaf?: boolean;
  isDeleted?: boolean;
  createdAt?: string;
  createdBy?: string;
  updatedAt?: string;
  updatedBy?: string;
};

export enum DataSetType {
  SQL = "SQL",
  API = "API",
}

export enum FieldType {
  STRING = "STRING",
  NUMBER = "NUMBER",
  BOOLEAN = "BOOLEAN",
  OBJECT = "OBJECT",
  ARRAY = "ARRAY",
}

export type DataSetConfigApi = {
  url?: string;
  method?: string;
  headers?: Record<string, string>;
  params?: Record<string, string>;
  body?: string;
};

export type DataSetConfigSql = {
  sql?: string;
  dataSourceId?: string;
};

export type DataSetConfigInputField = {
  key?: string;
  type?: FieldType;
  required?: boolean;
};

export type DataSetConfigInput = {
  fields?: DataSetConfigInputField[];
};

export type DataSetConfigOutputField = {
  key?: string;
  name?: string;
  type?: FieldType;
  schema?: DataSetConfigOutput;
};

export type DataSetConfigOutput = {
  fields?: DataSetConfigOutputField[];
};

export type DataSetConfig = {
  api?: DataSetConfigApi;
  sql?: DataSetConfigSql;
  input?: DataSetConfigInput;
  output?: DataSetConfigOutput;
  script?: string;
};

export type DatasetCreateRequest = {
  folderId?: string;
  datasourceId?: string;
  name: string;
  type: DataSetType;
  config?: DataSetConfig;
  description?: string;
};

export type DatasetUpdateRequest = {
  folderId?: string;
  datasourceId?: string;
  name?: string;
  type?: DataSetType;
  config?: DataSetConfig;
  description?: string;
  status?: EnableStatus;
};

export type DatasetEntity = {
  id: string;
  tenantId: string;
  folderId?: string;
  datasourceId?: string;
  name: string;
  type: DataSetType;
  config?: string;
  description?: string;
  status: EnableStatus;
  isDeleted?: boolean;
  createdAt?: string;
  createdBy?: string;
  updatedAt?: string;
  updatedBy?: string;
};

export type DatasetResponse = {
  id: string;
  folderId?: string;
  datasourceId?: string;
  name: string;
  type: DataSetType;
  config?: DataSetConfig;
  description?: string;
  status: EnableStatus;
};

export type DatasetQueryRequest = {
  folderId?: string;
  datasourceId?: string;
  name?: string;
  type?: DataSetType;
  page?: number;
  size?: number;
  status?: EnableStatus;
};

export type GenerateSqlResponse = {
  sql: string;
  script: string;
  output: DataSetConfigOutput;
};

export type TestSqlRequest = {
  dbId: string;
  sql: string;
};

// --- Folder API ---

/**
 * 创建文件夹
 */
export const createDatasetFolder = (data: DatasetFolderCreateRequest) => {
  return request.post<DatasetFolderEntity>("/bi/dataset/folders", data);
};

/**
 * 更新文件夹
 */
export const updateDatasetFolder = (
  id: string,
  data: DatasetFolderUpdateRequest,
) => {
  return request.put<DatasetFolderEntity>(`/bi/dataset/folders/${id}`, data);
};

/**
 * 删除文件夹
 */
export const deleteDatasetFolder = (id: string) => {
  return request.delete<boolean>(`/bi/dataset/folders/${id}`);
};

/**
 * 获取文件夹列表
 */
export const listDatasetFolders = () => {
  return request.get<DatasetFolderEntity[]>("/bi/dataset/folders");
};

// --- Dataset API ---

/**
 * 创建数据集
 */
export const createDataset = (data: DatasetCreateRequest) => {
  return request.post<DatasetEntity>("/bi/dataset", data);
};

/**
 * 更新数据集
 */
export const updateDataset = (id: string, data: DatasetUpdateRequest) => {
  return request.put<DatasetEntity>(`/bi/dataset/${id}`, data);
};

/**
 * 删除数据集
 */
export const deleteDataset = (id: string) => {
  return request.delete<boolean>(`/bi/dataset/${id}`);
};

/**
 * 获取数据集详情
 */
export const getDataset = (id: string) => {
  return request.get<DatasetResponse>(`/bi/dataset/${id}`);
};

/**
 * 分页查询数据集
 */
export const listDatasets = (params: DatasetQueryRequest) => {
  return request.get<PageResponse<DatasetResponse>>("/bi/dataset", params);
};

/**
 * AI生成SQL
 */
export const generateSql = (data: {
  datasourceId: string;
  agentId: number;
  prompt: string;
}) => {
  return request.post<GenerateSqlResponse>(`/bi/dataset/generate-sql`, data, {
    headers: { "Content-Type": "application/json" },
    timeout: 0,
  });
};

/**
 * 助手聊天
 */
export const assistantChatToDataset = ({
  dbId,
  title,
  description,
  schemaItem,
}: {
  dbId: string;
  title: string;
  description: string;
  schemaItem: SchemaItemType;
}) => {
  return request.post<DatasetResponse>(
    `/bi/dataset/assistant-chat`,
    {
      dbId,
      title,
      description,
      schemaItem,
    },
    {
      timeout: 0,
    },
  );
};

/**
 * 测试SQL
 */
export const testSql = (data: TestSqlRequest) => {
  return request.post<Record<string, Object>[]>("/bi/dataset/test-sql", data);
};

type DataSetExeResponse = {
  result: Object;
  explain: string;
};

/**
 * 执行数据集
 */
export const executeDataset = (id: string, params: Record<string, Object>, aiPrompt?: string) => {
  return request.post<DataSetExeResponse>(`/bi/dataset/execute/${id}`, params, {
    headers: { "Content-Type": "application/json" },
    params: { aiPrompt: aiPrompt || "" },
    timeout: 0
  });
};

/****************************** 数据集管理 end ******************************/

/****************************** 应用管理 start ******************************/

export type AppCreateRequest = {
  menuType: AppMenuType;
  title: string;
  icon?: string;
  pageId?: string;
  appKey?: string;
  redirectUrl?: string;
  parentId?: string;
  sort?: number;
  hasWatermark?: boolean;
};

export type AppUpdateRequest = {
  menuType?: AppMenuType;
  title?: string;
  icon?: string;
  pageId?: string;
  appKey?: string;
  redirectUrl?: string;
  parentId?: string;
  sort?: number;
  hasWatermark?: boolean;
};

/**
 * 获取应用菜单列表
 */
export { listApps } from "@/services/app";

/**
 * 创建应用菜单
 */
export const createApp = (data: AppCreateRequest) => {
  return request.post<AppResponse>("/bi/apps", data);
};

/**
 * 更新应用菜单
 */
export const updateApp = (id: string, data: AppUpdateRequest) => {
  return request.put<AppResponse>(`/bi/apps/${id}`, data);
};

/**
 * 删除应用菜单
 */
export const deleteApp = (id: string) => {
  return request.delete<boolean>(`/bi/apps/${id}`);
};

/**
 * 获取应用菜单详情
 */
export const getApp = (id: string) => {
  return request.get<AppResponse>(`/bi/apps/${id}`);
};

/**
 * 获取应用菜单详情
 */
export const getAppSchema = (id: string) => {
  return request.get<PageResp>(`/bi/apps/${id}/schema`);
};

/****************************** 应用管理 end ******************************/

/****************************** 租户设置 start ******************************/

export enum LogoType {
  LOGO_NAME = "LOGO_NAME",
  ONLY_LOGO = "ONLY_LOGO",
  ONLY_NAME = "ONLY_NAME",
}

export type TenantSiteConfig = {
  logo?: string;
  name?: string;
  logoType?: LogoType;
  description?: string;
  keywords?: string;
  copyright?: string;
};

export type TenantSettingRequest = {
  contactUser?: string;
  contactPhone?: string;
  siteConfig?: TenantSiteConfig;
};

export type TenantResponse = {
  id: string;
  code: string;
  name: string;
  status: EnableStatus;
  cover?: string;
  description?: string;
  contactUser?: string;
  contactPhone?: string;
  siteConfig?: TenantSiteConfig;
};

/**
 * 获取租户设置
 */
export const getTenantSetting = () => {
  return request.get<TenantResponse>("/system/tenants/setting");
};

/**
 * 更新租户设置
 */
export const updateTenantSetting = (data: TenantSettingRequest) => {
  return request.put<TenantResponse>("/system/tenants/setting", data);
};

/****************************** 租户设置 end ******************************/


/****************************** 客户端凭证管理 start ******************************/
export type SysAuthClientQueryRequest = {
  clientId?: string;
  name?: string;
  keyword?: string;
  page?: number;
  size?: number;
};

export type SysAuthClientCreateRequest = {
  name: string;
  description?: string;
  expiredAt?: string;
};

export type SysAuthClientUpdateRequest = {
  name?: string;
  description?: string;
  expiredAt?: string;
};

export type SysAuthClientResponse = {
  tenantId: string;
  clientId: string;
  name: string;
  description?: string;
  clientSecret?: string;
  expiredAt?: string;
};

export const listSysAuthClients = (params: SysAuthClientQueryRequest) => {
  return request.get<PageResponse<SysAuthClientResponse>>(
    "/system/auth-clients",
    params,
  );
};

export const createSysAuthClient = (data: SysAuthClientCreateRequest) => {
  return request.post<SysAuthClientResponse>("/system/auth-clients", data);
};

export const updateSysAuthClient = (
  clientId: string,
  data: SysAuthClientUpdateRequest,
) => {
  return request.put<SysAuthClientResponse>(
    `/system/auth-clients/${clientId}`,
    data,
  );
};

export const deleteSysAuthClient = (clientId: string) => {
  return request.delete<boolean>(`/system/auth-clients/${clientId}`);
};

export const refreshSysAuthClientSecret = (clientId: string) => {
  return request.post<SysAuthClientResponse>(
    `/system/auth-clients/${clientId}/refresh-secret`,
  );
};

export const getSysAuthClient = (clientId: string) => {
  return request.get<SysAuthClientResponse>(`/system/auth-clients/${clientId}`);
};

/****************************** 客户端凭证管理 end ******************************/
