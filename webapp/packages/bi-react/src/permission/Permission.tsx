import React from 'react';
import { usePermission } from './usePermission';

interface PermissionProps {
  /** 权限标识，支持单个字符串或字符串数组 */
  value: string | string[];
  /** 校验失败时渲染的内容，默认为 null */
  fallback?: React.ReactNode;
  /** 子元素 */
  children: React.ReactNode;
}

/**
 * 权限控制组件，类似 v-has-perm
 * @example
 * <Permission value="user:add">
 *   <Button>添加用户</Button>
 * </Permission>
 */
export const Permission: React.FC<PermissionProps> = ({ 
  value, 
  fallback = null, 
  children 
}) => {
  const { hasPermission } = usePermission();

  if (hasPermission(value)) {
    return <>{children}</>;
  }

  return <>{fallback}</>;
};

export default Permission;
