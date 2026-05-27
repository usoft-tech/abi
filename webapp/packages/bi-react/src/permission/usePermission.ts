import { useCallback } from 'react';
import { useSelector } from 'react-redux';
import { RootState } from '@/store';

/**
 * 权限检查 Hook
 * @returns { hasPermission: (permission: string | string[]) => boolean }
 */
export const usePermission = () => {
  const permissions = useSelector((state: RootState) => state.account.permissions);
  const roles = useSelector((state: RootState) => state.account.roles);

  const hasPermission = useCallback((value: string | string[]) => {
    if (!value || value.length === 0) return true;
    
    // 超级管理员直接放行
    if (roles.includes('admin')) return true;

    const needPermissions = Array.isArray(value) ? value : [value];
    
    // 检查是否包含所需权限
    return permissions.some(permission => {
      // 支持通配符，如 user:*
      if (permission.includes('*')) {
        const prefix = permission.split(':')[0];
        return needPermissions.some(np => np.startsWith(prefix));
      }
      return needPermissions.includes(permission);
    });
  }, [permissions, roles]);

  return { hasPermission };
};
