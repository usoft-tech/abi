import React, { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useDispatch } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import { Modal, Spin } from 'antd';
import styled from 'styled-components';
import { AuthResponse } from '../services/auth';
import { setTenant as setTenantAction } from '../store/accountSlice';
import { useThirdAuthCallback } from '@/hooks/useThirdAuth';

const PageWrapper = styled.div`
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f0f2f5;
`;

const TenantSelectModal = styled(Modal)`
  ul {
    list-style-type: none;
    padding: 0;
    margin: 0;

    li {
      padding: 12px 16px;
      cursor: pointer;
      transition: all 0.3s;

      &:hover {
        background: #f0f7ff;
        color: #1677ff;
      }
    }
  }
`;

export const ThirdAuthCallback: React.FC = () => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const [tenants, setTenants] = useState<AuthResponse["tenants"]>([]);
  const [tenantVisible, setTenantVisible] = useState(false);

  useThirdAuthCallback(setTenants, setTenantVisible);

  return (
    <PageWrapper>
      <Spin tip="正在处理登录请求..." size="large" />
      <TenantSelectModal
        title={t("app.tenantSelection")}
        visible={tenantVisible}
        footer={null}
        closable={false}
        maskClosable={false}
      >
        <ul>
          {tenants.map((tenant) => (
            <li
              key={tenant.id}
              onClick={() => {
                dispatch(setTenantAction(tenant));
                navigate("/tenant");
              }}
            >
              {tenant.name}
            </li>
          ))}
        </ul>
      </TenantSelectModal>
    </PageWrapper>
  );
};
