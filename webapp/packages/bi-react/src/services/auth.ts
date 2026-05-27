import http, { EnableStatus } from "./request";

export interface AuthRequest {
  username?: string;
  password?: string;
}

export interface AuthResponse {
  token: string;
  type: string;
  tenants: {
    id: string;
    name: string;
    code: string;
    status: EnableStatus;
  }[];
}

export interface AuthorizedUser {
  id: string;
  username: string;
  displayName?: string | null;
  departmentId?: string | null;
  tenants: AuthResponse["tenants"];
  roles: string[];
  permissions: string[];
}

/**
 * 登录
 */
export const login = (params: AuthRequest) => {
  return http.post<AuthResponse>("/auth/login", params);
};

/**
 * 获取当前登录用户信息
 */
export const getAuthorizedUser = () => {
  return http.get<AuthorizedUser>("/auth/user-info");
};

/**
 * 获取第三方登录跳转地址
 */
export const getThirdAuthUrl = (platform: string) => {
  return http.get<string>(`/auth/third/url/${platform}`);
};
