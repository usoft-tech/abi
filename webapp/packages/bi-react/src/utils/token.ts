export const TENANT_ID_HEADER = "X-Tenant-ID";
export const TOKEN_HEADER = "Authorization";
export const TOKEN_PREFIX = "Bearer ";
export const TOKEN_KEY = "token";

export const getToken = () => {
  return localStorage.getItem(TOKEN_KEY);
};

export const setToken = (token: string) => {
  localStorage.setItem(TOKEN_KEY, token);
};

export const getTenantId = () => {
  return localStorage.getItem(TENANT_ID_HEADER);
};

export const setTenantId = (tenantId: string) => {
  localStorage.setItem(TENANT_ID_HEADER, tenantId);
};

export const clearToken = () => {
  localStorage.removeItem(TOKEN_KEY);
};

export const clearTenantId = () => {
  localStorage.removeItem(TENANT_ID_HEADER);
};