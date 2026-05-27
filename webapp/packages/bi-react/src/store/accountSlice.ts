import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import { clearTenantId, clearToken, setTenantId } from '@/utils/token';
import { EnableStatus } from '@/services/request';
import { TenantSiteConfig } from '@/views/tenant/services';

interface Tenant {
  id?: string | null;
  name?: string | null;
  code?: string | null;
  status?: EnableStatus;
  siteConfig?: TenantSiteConfig;
}

interface User {
  id: string;
  username: string;
  departmentId?: string | null;
  avatar?: string | null;
  displayName?: string | null;
  email?: string | null;
}

interface AccountState {
  activeAppId?: number | null;
  user?: User | null;
  tenant?: Tenant | null;
  tenants: Tenant[];
  permissions: string[];
  roles: string[];
}


const initialState: AccountState = {
  activeAppId: null,
  user: {
    id: '1',
    username: 'admin',
    displayName: '超级管理员',
    email: null,
    avatar: '',
  },
  tenant: null,
  tenants: [],
  permissions: [], // 示例：['user:add', 'user:edit']
  roles: [],      // 示例：['admin', 'editor']
};

export const accountSlice = createSlice({
  name: 'account',
  initialState,
  reducers: {
    setActiveAppId: (state, action: PayloadAction<number | null>) => {
      state.activeAppId = action.payload;
    },
    setUser: (state, action: PayloadAction<User | null>) => {
      state.user = action.payload;
    },
    setPermissions: (state, action: PayloadAction<string[]>) => {
      state.permissions = action.payload;
    },
    setRoles: (state, action: PayloadAction<string[]>) => {
      state.roles = action.payload;
    },
    setTenant: (state, action: PayloadAction<Tenant | null>) => {
      state.tenant = action.payload;
      if (action.payload?.id) {
        setTenantId(action.payload.id);
      } else {
        clearTenantId();
      }
    },
    setTenants: (state, action: PayloadAction<Tenant[]>) => {
      state.tenants = action.payload;
    },
    clearAccount: (state) => {
      state.activeAppId = null;
      state.user = null;
      state.tenant = null;
      state.tenants = [];
      state.permissions = [];
      state.roles = [];
      clearTenantId();
      clearToken();
    },
  },
});

export const { setActiveAppId, setUser, setPermissions, setRoles, setTenant, setTenants, clearAccount } = accountSlice.actions;

export default accountSlice.reducer;
