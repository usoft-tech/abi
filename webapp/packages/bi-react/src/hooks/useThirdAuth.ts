import { useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useDispatch } from 'react-redux';
import { message } from 'antd';
import { setToken } from '@/utils/token';
import { getAuthorizedUser, getThirdAuthUrl, AuthResponse } from '@/services/auth';
import { setTenant as setTenantAction, setTenants as setTenantsAction } from '@/store/accountSlice';
import config from '@/config';

export const useThirdAuthLogin = () => {
  /**
   * 处理第三方登录跳转
   */
  const handleThirdLogin = useCallback(async (platform: string) => {
    try {
      const res = await getThirdAuthUrl(platform);
      if (res.code === 200 || res.code === 'OK') {
         window.location.href = res.data;
      } else {
         message.error(res.message || '获取第三方登录地址失败');
      }
    } catch (error) {
      console.error(error);
      message.error('获取第三方登录地址失败');
    }
  }, []);

  return {
    handleThirdLogin
  };
};

export const useThirdAuthCallback = (
  setTenants: (tenants: AuthResponse["tenants"]) => void,
  setTenantVisible: (visible: boolean) => void
) => {
  const navigate = useNavigate();
  const dispatch = useDispatch();

  /**
   * 处理第三方登录回调
   */
  useEffect(() => {
    const searchParams = new URLSearchParams(window.location.search);
    const tokenFromQuery = searchParams.get("token");
    const errorFromQuery = searchParams.get("error");

    if (!tokenFromQuery && !errorFromQuery) {
      return;
    }

    const clearQuery = () => {
      window.history.replaceState({}, document.title, window.location.pathname);
    };

    if (errorFromQuery) {
      message.error(decodeURIComponent(errorFromQuery));
      clearQuery();
      // 如果出错，可以选择跳转回登录页
      navigate(config.basePath + '/login');
      return;
    }

    if (tokenFromQuery) {
      setToken(tokenFromQuery);
      getAuthorizedUser()
        .then((res) => {
          if (res.code === "OK" || res.code === 200) {
            const user = res.data;
            const tenantList = user.tenants || [];
            setTenants(tenantList);
            dispatch(setTenantsAction(tenantList));
            if (tenantList.length === 1) {
              dispatch(setTenantAction(tenantList[0]));
              navigate("/tenant");
            } else {
              dispatch(setTenantAction(null));
              setTenantVisible(tenantList.length > 1);
            }
          }
        })
        .catch((error) => {
            console.error(error);
            message.error('登录失败，请重试');
            navigate(config.basePath + '/login');
        })
        .finally(() => {
          clearQuery();
        });
    }
  }, [dispatch, navigate, setTenants, setTenantVisible]);
};
