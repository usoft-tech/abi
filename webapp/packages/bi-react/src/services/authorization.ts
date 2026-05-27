import request from "./request";

export enum AuthorizationBizType {
  APP = "APP",
  SHARE = "SHARE",
  PAGE = "PAGE",
}

export enum AuthorizationScope {
  PUBLIC = "PUBLIC",
  TENANT = "TENANT",
  USER = "USER",
}

export type AuthorizationEntity = {
  id: string;
  tenantId: string;
  bizType: AuthorizationBizType;
  bizId: string;
  authorizerScope: AuthorizationScope;
  authorizerId?: string;
  isDeleted: boolean;
  createdAt: string;
  createdBy: string;
  updatedAt: string;
  updatedBy: string;
};

export type AuthItem = {
  scope: AuthorizationScope;
  authorizerId?: string;
};

export type BatchUpdateAuthRequest = {
  bizId: string;
  bizType: AuthorizationBizType;
  authorizations: AuthItem[];
};

/**
 * 根据业务ID查询授权列表
 */
export const listAuthorizations = (bizType: AuthorizationBizType, bizId: string) => {
  return request.get<AuthorizationEntity[]>("/bi/authorization/list", {
    bizType,
    bizId,
  });
};

/**
 * 批量更新授权
 */
export const batchUpdateAuthorizations = (data: BatchUpdateAuthRequest) => {
  return request.post<boolean>("/bi/authorization/batch-update", data);
};
