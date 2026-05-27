import { RootState } from "@/store";
import { config } from "@/store/appSlice";
import { isMobile } from "@/utils";
import { clearToken } from "@/utils/token";
import {
  DownOutlined,
  LogoutOutlined,
  UserOutlined
} from "@ant-design/icons";
import { Avatar, Divider, Dropdown, Modal } from "antd";
import React, { useMemo } from "react";
import { useTranslation } from "react-i18next";
import { useSelector } from "react-redux";
import { useNavigate } from "react-router-dom";
import styled from "styled-components";
import setting from "@/config";

interface Props {
  avatar?: string;
}

const StyledAccount = styled.div`
  display: flex;
  align-items: center;
  flex-direction: row;
  gap: 8px;
  cursor: pointer;

  .account-info {
    display: flex;
    align-items: flex-start;
    flex-direction: column;
    font-size: 12px;
    text-align: center;
    gap: 4px;
    max-width: 100px;

    > div {
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      line-height: 1em;
      text-align: left;
      width: 100%;
      color: #444444;
    }
  }

  &.dark {
    .account-info > div {
      color: #ffffff !important;
    }
    .anticon {
      color: #ffffff !important;
    }
  }
`;

const AccountOverlay = styled.div`
  width: 200px;
  background-color: #ffffff;
  border-radius: 4px;
  border: 1px solid #e5e5e5;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
  padding: 12px;

  .account-info {
    display: flex;
    align-items: center;
    flex-direction: column;
    font-size: 12px;
    text-align: center;
    gap: 8px;

    > div {
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      line-height: 1em;
      width: 100%;
      color: #444444;
    }
  }

  .ant-divider-horizontal {
    margin: 12px 0;
  }

  ul {
    list-style: none;
    padding: 0;
    margin: 0;

    li {
      display: flex;
      align-items: center;
      flex-direction: row;
      gap: 8px;
      padding: 2px 12px;
      cursor: pointer;
      color: #444;

      &:hover {
        background-color: #f5f5f5;
      }
    }
  }
`;

const Account: React.FC<Props> = ({ avatar = "" }) => {
  const { layout, theme } = useSelector(config);
  const { user, tenant } = useSelector((state: RootState) => state.account);
  const { t } = useTranslation();
  const isMobileDevice = useMemo(() => isMobile(), [isMobile]);
  const navigate = useNavigate();

  const handleLogout = () => {
    Modal.confirm({
      title: t("account.logout"),
      content: t("account.logoutConfirm"),
      okText: t("common.confirm"),
      cancelText: t("common.cancel"),
      onOk: () => {
        clearToken();
        window.location.href = setting.basePath + "/login";
      },
    });
  };

  const handleCenterClick = () => {
    navigate("/account/center");
  };

  const menuContent = (
    <AccountOverlay>
      <div className="account-info">
        <Avatar
          src={user?.avatar || avatar}
          icon={!user?.avatar && <UserOutlined />}
        />
        <div>{user?.displayName || user?.username || ""}</div>
        <div>{tenant?.name || ""}</div>
      </div>
      <Divider />
      <ul>
        <li onClick={handleCenterClick}>
          <UserOutlined style={{ fontSize: 12 }} />
          <div>{t("account.center")}</div>
        </li>
        {/* <Permission value="system:setting">
          <li>
            <SettingOutlined style={{ fontSize: 12 }} />
            <div>{t("account.settings")}</div>
          </li>
        </Permission> */}
        <li onClick={handleLogout}>
          <LogoutOutlined style={{ fontSize: 12 }} />
          <div>{t("account.logout")}</div>
        </li>
      </ul>
    </AccountOverlay>
  );

  return (
    <Dropdown popupRender={() => menuContent} placement="bottomLeft">
      <StyledAccount
        className={theme === "dark" && layout === "top-menu" ? "dark" : ""}
      >
        <Avatar
          src={user?.avatar || avatar}
          icon={!user?.avatar && <UserOutlined />}
        />
        {!isMobileDevice && (
          <>
            <div className="account-info">
              <div>{user?.displayName || user?.username || ""}</div>
              <div>{tenant?.name || ""}</div>
            </div>
            <DownOutlined style={{ fontSize: 8 }} />
          </>
        )}
      </StyledAccount>
    </Dropdown>
  );
};

export default Account;
