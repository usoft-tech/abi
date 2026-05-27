import type { PageResponse } from "@/services/api";
import request, { EnableStatus } from "@/services/request";

/****************************** 系统配置 start ******************************/

export type SysConfigResponse = {
  id: string;
  configKey: string;
  configValue: string;
};

export type SysConfigCreateRequest = {
  configKey: string;
  configValue: string;
};

export type SysConfigUpdateRequest = {
  configKey: string;
  configValue: string;
};

export type SysConfigQueryRequest = {
  page: number;
  size: number;
  configKey?: string;
  keyword?: string;
};

/**
 * 获取系统配置分页列表
 */
export const listConfigs = (params: SysConfigQueryRequest) => {
  return request.get<PageResponse<SysConfigResponse>>("/system/configs", {
    ...params,
  });
};

/**
 * 创建系统配置
 */
export const createConfig = (payload: SysConfigCreateRequest) => {
  return request.post<SysConfigResponse>("/system/configs", payload);
};

/**
 * 更新系统配置
 */
export const updateConfig = (id: string, payload: SysConfigUpdateRequest) => {
  return request.put<SysConfigResponse>(`/system/configs/${id}`, payload);
};

/**
 * 删除系统配置
 */
export const deleteConfig = (id: string) => {
  return request.delete<boolean>(`/system/configs/${id}`);
};

/****************************** 系统配置 end ******************************/

/****************************** 租户管理 start ******************************/

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

export type TenantCreateRequest = {
  code: string;
  name: string;
  status: EnableStatus;
  cover?: string;
  description?: string;
  contactUser?: string;
  contactPhone?: string;
};

export type TenantUpdateRequest = {
  code: string;
  name: string;
  status: EnableStatus;
  cover?: string;
  description?: string;
  contactUser?: string;
  contactPhone?: string;
};

export type TenantQueryRequest = {
  page: number;
  size: number;
  code?: string;
  name?: string;
  status?: EnableStatus;
  keyword?: string;
};

/**
 * 获取租户分页列表
 */
export const listTenants = (params: TenantQueryRequest) => {
  return request.get<PageResponse<TenantResponse>>("/system/tenants", {
    ...params,
  });
};

/**
 * 创建租户
 */
export const createTenant = (payload: TenantCreateRequest) => {
  return request.post<TenantResponse>("/system/tenants", payload);
};

/**
 * 更新租户
 */
export const updateTenant = (id: string, payload: TenantUpdateRequest) => {
  return request.put<TenantResponse>(`/system/tenants/${id}`, payload);
};

/**
 * 删除租户
 */
export const deleteTenant = (id: string) => {
  return request.delete<boolean>(`/system/tenants/${id}`);
};

/**
 * 获取租户用户分页列表
 */
export const listTenantUsers = (tenantId: string, params: UserQueryRequest) => {
  return request.get<PageResponse<TenantUserResponse>>(
    `/system/tenants/${tenantId}/users`,
    { ...params },
  );
};

/**
 * 为租户添加用户
 */
export const addTenantUsers = (tenantId: string, userIds: string[]) => {
  return request.post<void>(`/system/tenants/${tenantId}/users`, userIds);
};

/**
 * 从租户移除用户
 */
export const removeTenantUser = (tenantId: string, userId: string) => {
  return request.delete<void>(`/system/tenants/${tenantId}/users/${userId}`);
};

/**
 * 更新租户用户角色
 */
export const updateTenantUserAuthKey = (
  tenantId: string,
  userId: string,
  authKey: TenantAuthKey,
) => {
  return request.put<void>(
    `/system/tenants/${tenantId}/auth/${userId}/${authKey}`,
  );
};



/****************************** 租户管理 end ******************************/

/****************************** 角色管理 start ******************************/

export type RoleResponse = {
  id: string;
  name: string;
  code: string;
};

export type RoleCreateRequest = {
  name: string;
  code: string;
};

export type RoleUpdateRequest = {
  name: string;
  code: string;
};

export type RoleQueryRequest = {
  page: number;
  size: number;
  name?: string;
  code?: string;
  keyword?: string;
};

/**
 * 获取角色分页列表
 */
export const listRoles = (params: RoleQueryRequest) => {
  return request.get<PageResponse<RoleResponse>>("/system/roles", {
    ...params,
  });
};

/**
 * 创建角色
 */
export const createRole = (payload: RoleCreateRequest) => {
  return request.post<RoleResponse>("/system/roles", payload);
};

/**
 * 更新角色
 */
export const updateRole = (id: string, payload: RoleUpdateRequest) => {
  return request.put<RoleResponse>(`/system/roles/${id}`, payload);
};

/**
 * 删除角色
 */
export const deleteRole = (id: string) => {
  return request.delete<boolean>(`/system/roles/${id}`);
};

/**
 * 获取角色权限ID列表
 */
export const listRolePermissions = (roleId: string) => {
  return request.get<string[]>(`/system/roles/${roleId}/permissions`);
};

/**
 * 分配角色权限
 */
export const assignRolePermissions = (
  roleId: string,
  permissionIds: string[],
) => {
  return request.post<void>(
    `/system/roles/${roleId}/permissions`,
    permissionIds,
  );
};

/**
 * 获取角色下的用户列表
 */
export const listRoleUsers = (
  roleId: string,
  params: { page: number; size: number },
) => {
  return request.get<PageResponse<UserResponse>>(
    `/system/roles/${roleId}/users`,
    { ...params },
  );
};

/**
 * 给角色添加用户
 */
export const addRoleUsers = (roleId: string, userIds: string[]) => {
  return request.post<void>(`/system/roles/${roleId}/users`, userIds);
};

/**
 * 从角色移除用户
 */
export const removeRoleUser = (roleId: string, userId: string) => {
  return request.delete<void>(`/system/roles/${roleId}/users/${userId}`);
};

/****************************** 角色管理 end ******************************/

/****************************** 用户管理 start ******************************/

export enum TenantAuthKey {
  ADMIN = "ADMIN",
  USER = "USER",
}

export interface UserResponse {
  id: string;
  username: string;
  departmentId: string;
  employeeNo?: string;
  displayName?: string;
  status?: EnableStatus;
};

export interface TenantUserResponse extends UserResponse {
  authKey: TenantAuthKey;
}

export type UserCreateRequest = {
  username: string;
  password: string;
  departmentId: string;
  employeeNo?: string;
  displayName?: string;
  status?: EnableStatus;
};

export type UserUpdateRequest = {
  username: string;
  password?: string;
  departmentId: string;
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
 * 获取用户分页列表
 */
export const listUsers = (params: UserQueryRequest) => {
  return request.get<PageResponse<UserResponse>>("/system/users", {
    ...params,
  });
};

/**
 * 获取用户分页列表（全局）
 */
export const listUsersFromGlobal = (params: UserQueryRequest) => {
  return request.get<PageResponse<UserResponse>>("/system/users/global", {
    ...params,
  });
};

/**
 * 创建用户
 */
export const createUser = (payload: UserCreateRequest) => {
  return request.post<UserResponse>("/system/users", payload);
};

/**
 * 更新用户
 */
export const updateUser = (id: string, payload: UserUpdateRequest) => {
  return request.put<UserResponse>(`/system/users/${id}`, payload);
};

/**
 * 删除用户
 */
export const deleteUser = (id: string) => {
  return request.delete<boolean>(`/system/users/${id}`);
};

/****************************** 用户管理 end ******************************/

/****************************** 权限管理 start ******************************/

export enum PermissionType {
  GROUP = "GROUP",
  PERM = "PERM",
}

export type PermissionResponse = {
  id: string;
  parentId?: string;
  name: string;
  code: string;
  type: PermissionType;
  description?: string;
  sort?: number;
  children?: PermissionResponse[];
  builtIn?: boolean;
};

export type PermissionCreateRequest = {
  parentId?: string;
  name: string;
  code: string;
  type: PermissionType;
  description?: string;
  sort?: number;
};

export type PermissionUpdateRequest = {
  parentId?: string;
  name: string;
  code: string;
  type: PermissionType;
  description?: string;
  sort?: number;
};

export type PermissionQueryRequest = {
  keyword?: string;
};

/**
 * 获取权限列表
 */
export const listPermissions = (params: PermissionQueryRequest) => {
  return request.get<PermissionResponse[]>("/system/permission/list", {
    ...params,
  });
};

/**
 * 创建权限
 */
export const createPermission = (payload: PermissionCreateRequest) => {
  return request.post<PermissionResponse>("/system/permission/create", payload);
};

/**
 * 更新权限
 */
export const updatePermission = (
  id: string,
  payload: PermissionUpdateRequest,
) => {
  return request.post<PermissionResponse>(
    `/system/permission/update/${id}`,
    payload,
  );
};

/**
 * 删除权限
 */
export const deletePermission = (id: string) => {
  return request.post<void>(`/system/permission/delete/${id}`);
};

/****************************** 权限管理 end ******************************/


/****************************** 技能管理 start ******************************/

export type SkillListItemResponse = {
  name: string;
  description: string;
};

export type AgentSkill = {
  name: string;
  description: string;
  skillContent: string;
  resources: Record<string, string>;
};

/**
 * 获取技能列表
 */
export const listSkills = () => {
  return request.get<SkillListItemResponse[]>("/ai/skills");
};

/**
 * 创建技能
 */
export const createSkill = (payload: AgentSkill) => {
  return request.post<boolean>("/ai/skills", payload);
};

/**
 * 更新技能
 */
export const updateSkill = (payload: AgentSkill) => {
  return request.put<boolean>(`/ai/skills`, payload);
};

/**
 * 导入技能
 */
export const importSkill = (file: File) => {
  const formData = new FormData();
  formData.append("file", file);
  return request.post<boolean>("/ai/skills/import", formData);
};

/**
 * 获取技能
 */
export const getSkill = (name: string) => {
  return request.get<AgentSkill>(`/ai/skills/${name}`);
};

/**
 * 删除技能
 */
export const deleteSkill = (name: string) => {
  return request.delete<boolean>(`/ai/skills/${name}`);
};

/****************************** 技能管理 end ******************************/

/****************************** 大模型管理 start ******************************/

export type ModelResponse = {
  id: string;
  name: string;
  provider: string;
  model: string;
  baseUrl?: string;
  extProps?: Record<string, any>;
  createdAt: string;
};

export type ModelCreateRequest = {
  name: string;
  provider: string;
  model: string;
  baseUrl?: string;
  apiKey?: string;
  extProps?: Record<string, any>;
};

export type ModelUpdateRequest = {
  name: string;
  provider: string;
  model: string;
  baseUrl?: string;
  apiKey?: string;
  extProps?: Record<string, any>;
};

export type ModelQueryRequest = {
  page: number;
  size: number;
  name?: string;
  provider?: string;
  keyword?: string;
};

/**
 * 获取大模型分页列表
 */
export const listModels = (params: ModelQueryRequest) => {
  return request.get<PageResponse<ModelResponse>>("/ai/models", {
    ...params,
  });
};

/**
 * 创建大模型
 */
export const createModel = (payload: ModelCreateRequest) => {
  return request.post<ModelResponse>("/ai/models", payload);
};

/**
 * 更新大模型
 */
export const updateModel = (id: string, payload: ModelUpdateRequest) => {
  return request.put<ModelResponse>(`/ai/models/${id}`, payload);
};

/**
 * 删除大模型
 */
export const deleteModel = (id: string) => {
  return request.delete<boolean>(`/ai/models/${id}`);
};

/**
 * 获取大模型详情
 */
export const getModel = (id: string) => {
  return request.get<ModelResponse>(`/ai/models/${id}`);
};

/****************************** 大模型管理 end ******************************/

/****************************** 字典管理 start ******************************/

export type DictTypeResponse = {
  id: string;
  dictName: string;
  dictType: string;
  status: EnableStatus;
  remark?: string;
  createdAt: string;
};

export type DictTypeCreateRequest = {
  dictName: string;
  dictType: string;
  status: EnableStatus;
  remark?: string;
};

export type DictTypeUpdateRequest = {
  dictName: string;
  dictType: string;
  status: EnableStatus;
  remark?: string;
};

export type DictTypeQueryRequest = {
  page: number;
  size: number;
  dictName?: string;
  dictType?: string;
  status?: EnableStatus;
  keyword?: string;
};

export type DictDataResponse = {
  id: string;
  dictSort: number;
  dictLabel: string;
  dictValue: string;
  dictType: string;
  cssClass?: string;
  listClass?: string;
  isDefault: string;
  status: EnableStatus;
  remark?: string;
  createdAt: string;
};

export type DictDataCreateRequest = {
  dictSort?: number;
  dictLabel: string;
  dictValue: string;
  dictType: string;
  cssClass?: string;
  listClass?: string;
  isDefault?: string;
  status: EnableStatus;
  remark?: string;
};

export type DictDataUpdateRequest = {
  dictSort?: number;
  dictLabel: string;
  dictValue: string;
  dictType: string;
  cssClass?: string;
  listClass?: string;
  isDefault?: string;
  status: EnableStatus;
  remark?: string;
};

export type DictDataQueryRequest = {
  page: number;
  size: number;
  dictType?: string;
  dictLabel?: string;
  status?: EnableStatus;
  keyword?: string;
};

/**
 * 获取字典类型分页列表
 */
export const listDictTypes = (params: DictTypeQueryRequest) => {
  return request.get<PageResponse<DictTypeResponse>>("/system/dict/type", {
    ...params,
  });
};

/**
 * 创建字典类型
 */
export const createDictType = (payload: DictTypeCreateRequest) => {
  return request.post<DictTypeResponse>("/system/dict/type", payload);
};

/**
 * 更新字典类型
 */
export const updateDictType = (id: string, payload: DictTypeUpdateRequest) => {
  return request.put<DictTypeResponse>(`/system/dict/type/${id}`, payload);
};

/**
 * 删除字典类型
 */
export const deleteDictType = (id: string) => {
  return request.delete<boolean>(`/system/dict/type/${id}`);
};

/**
 * 获取字典类型详情
 */
export const getDictType = (id: string) => {
  return request.get<DictTypeResponse>(`/system/dict/type/${id}`);
};

/**
 * 获取字典数据分页列表
 */
export const listDictDatas = (params: DictDataQueryRequest) => {
  return request.get<PageResponse<DictDataResponse>>("/system/dict/data", {
    ...params,
  });
};

/**
 * 创建字典数据
 */
export const createDictData = (payload: DictDataCreateRequest) => {
  return request.post<DictDataResponse>("/system/dict/data", payload);
};

/**
 * 更新字典数据
 */
export const updateDictData = (id: string, payload: DictDataUpdateRequest) => {
  return request.put<DictDataResponse>(`/system/dict/data/${id}`, payload);
};

/**
 * 删除字典数据
 */
export const deleteDictData = (id: string) => {
  return request.delete<boolean>(`/system/dict/data/${id}`);
};

/**
 * 获取字典数据详情
 */
export const getDictData = (id: string) => {
  return request.get<DictDataResponse>(`/system/dict/data/${id}`);
};

/**
 * 根据字典类型获取字典数据列表
 */
export const listDictDatasByType = (dictType: string) => {
  return request.get<DictDataResponse[]>(`/system/dict/data/type/${dictType}`);
};

/****************************** 字典管理 end ******************************/

/****************************** 页面片段管理 start ******************************/

import { PageSchema } from "bi-sdk-react";

export type PageExampleResponse = {
  id: string;
  name: string;
  description?: string;
  cover?: string;
  item?: any;
};

export type PageExampleCreateRequest = {
  name: string;
  description?: string;
  cover?: string;
  item?: any;
};

export type PageExampleUpdateRequest = {
  id: string;
  name: string;
  description?: string;
  cover?: string;
  item?: any;
};

export type PageExampleQueryRequest = {
  page: number;
  size: number;
  name?: string;
  keyword?: string;
};

/**
 * 获取页面片段分页列表
 */
export const listPageExamples = (params: PageExampleQueryRequest) => {
  return request.get<PageResponse<PageExampleResponse>>("/bi/page-examples", {
    ...params,
  });
};

/**
 * 创建页面片段
 */
export const createPageExample = (payload: PageExampleCreateRequest) => {
  return request.post<PageExampleResponse>("/bi/page-examples", payload);
};

/**
 * 更新页面片段
 */
export const updatePageExample = (payload: PageExampleUpdateRequest) => {
  return request.put<PageExampleResponse>(`/bi/page-examples`, payload);
};

/**
 * 删除页面片段
 */
export const deletePageExample = (id: string) => {
  return request.delete<void>(`/bi/page-examples/${id}`);
};

/**
 * 获取页面片段详情
 */
export const getPageExample = (id: string) => {
  return request.get<PageExampleResponse>(`/bi/page-examples/${id}`);
};

/****************************** 页面片段管理 end ******************************/

/****************************** 页面模板管理 start ******************************/

export type PageTemplateResponse = {
  id: string;
  name: string;
  description?: string;
  cover?: string;
  schema?: PageSchema;
};

export type PageTemplateCreateRequest = {
  name: string;
  description?: string;
  cover?: string;
  schema?: PageSchema;
};

export type PageTemplateUpdateRequest = {
  id: string;
  name: string;
  description?: string;
  cover?: string;
  schema?: PageSchema;
};

export type PageTemplateQueryRequest = {
  page: number;
  size: number;
  name?: string;
  keyword?: string;
};

/**
 * 获取页面模板分页列表
 */
export const listPageTemplates = (params: PageTemplateQueryRequest) => {
  return request.get<PageResponse<PageTemplateResponse>>("/bi/page-templates", {
    ...params,
  });
};

/**
 * 创建页面模板
 */
export const createPageTemplate = (payload: PageTemplateCreateRequest) => {
  return request.post<PageTemplateResponse>("/bi/page-templates", payload);
};

/**
 * 更新页面模板
 */
export const updatePageTemplate = (payload: PageTemplateUpdateRequest) => {
  return request.put<PageTemplateResponse>(`/bi/page-templates`, payload);
};

/**
 * 删除页面模板
 */
export const deletePageTemplate = (id: string) => {
  return request.delete<void>(`/bi/page-templates/${id}`);
};

/**
 * 获取页面模板详情
 */
export const getPageTemplate = (id: string) => {
  return request.get<PageTemplateResponse>(`/bi/page-templates/${id}`);
};

/****************************** 页面模板管理 end ******************************/
