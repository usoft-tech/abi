import { config, setCollapsed, toggleCollapsed } from "@/store/appSlice";
import {
  MenuFoldOutlined,
  MenuOutlined,
  MenuUnfoldOutlined,
} from "@ant-design/icons";
import { Drawer, Layout } from "antd";
import React, { useEffect, useMemo } from "react";
import { useDispatch, useSelector } from "react-redux";
import { Outlet, RouteObject, useMatches } from "react-router-dom";
import styled from "styled-components";

// Components
import { reloadConfigAsync, selectBoolean } from "@/store/settingSlice";
import { AppDispatch } from "@/store";

import { isMobile } from "@/utils";
import AuthorizedHandler from "./AuthorizedHandler";
import Account from "./components/Account";
import AppMenu from "./components/AppMenu";
import Language from "./components/Language";
import Logo from "./components/Logo";
import MultiTab from "./components/MultiTab";
import RouteMenu from "./components/RouteMenu";
import Setting from "./components/Setting";
import { RootState } from "@/store";

const { Header, Sider, Content } = Layout;

// Styled Components
const StyledLayout = styled(Layout)`
  min-height: 100vh;

  .ant-layout-sider.sider-menu {
    height: calc(100vh - 61px);
    position: sticky;
    top: 61px;

    .ant-layout-sider-children {
      height: 100vh;
    }
  }

  .trigger {
    font-size: 18px;
    line-height: 60px;
    padding: 0 24px;
    cursor: pointer;
    transition: color 0.3s;
    &:hover {
      color: #1890ff;
    }
  }

  .header {
    height: 61px;
    line-height: 60px;
    background: #fff;
    padding: 0 24px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-bottom: solid 1px #e8e8e8;
    position: sticky;
    top: 0;
    z-index: 1000;

    &.dark {
      color: #fff;
      background: #001529;
    }
    &.light {
      color: #000;
    }
  }

  .header-left {
    display: flex;
    align-items: center;
    flex: 1;
  }

  .header-right {
    display: flex;
    align-items: center;
    flex-direction: row;
    gap: 16px;
  }

  .top-menu-wrapper {
    display: flex;
    align-items: center;
    flex: 1 1 auto;
  }

  .ant-layout-sider-trigger {
    height: 24px;
    line-height: 24px;
    color: #a5a5a5;
    border-right: solid 1px rgba(5, 5, 5, 0.06);
  }

  .ant-layout-sider {
    padding-bottom: 0;
  }

  .ant-layout-sider-children {
    display: flex;
    flex-direction: column;
  }

  &.mobile {
    .top-menu-wrapper {
      justify-content: center;

      .logo {
        margin-right: 0;
      }
    }
  }
`;

const MobileMenuWrapper = styled(Drawer)`
  .ant-drawer-header-title {
    flex-direction: row-reverse;
    justify-content: space-between;

    .ant-drawer-close {
      margin: 0;
    }
  }
`;

const BasicLayout: React.FC = () => {
  const dispatch = useDispatch<AppDispatch>();
  const matches = useMatches();
  const root = matches[0];
  const sider =
    (root?.handle as RouteObject["handle"] | null | undefined)?.sider ?? true;

  const { layout, theme, collapsed, route } = useSelector(config);
  const enableLanguage = useSelector((state: RootState) =>
    selectBoolean(state, "language.switch", true),
  );

  const handleToggleCollapsed = () => {
    dispatch(toggleCollapsed());
  };

  const handleSetCollapsed = (value: boolean) => {
    dispatch(setCollapsed(value));
  };

  const isMobileDevice = useMemo(() => isMobile(), [isMobile]);

  useEffect(() => {
    dispatch(reloadConfigAsync());
  }, []);

  return (
    <StyledLayout className={`basic-layout ${isMobileDevice ? "mobile" : ""}`}>
      <Header
        className={`header ${
          theme === "dark" && layout === "top-menu" ? "dark" : "light"
        }`}
      >
        {!isMobileDevice && (
          <div className={`header-left`}>
            <Logo orientation="horizontal" />
            {/* 顶部菜单模式 */}
            {layout === "top-menu" ? (
              <div className="top-menu-wrapper">
                <RouteMenu
                  theme={theme}
                  mode="horizontal"
                  style={{ flex: "1 1 auto" }}
                />
              </div>
            ) : layout === "top-side-menu" ? (
              <div style={{ display: "flex", alignItems: "center" }}>
                <AppMenu theme="light" mode="horizontal" />
              </div>
            ) : (
              <div style={{ display: "flex", alignItems: "center" }}>
                <div className="trigger" onClick={handleToggleCollapsed}>
                  {collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
                </div>
              </div>
            )}
          </div>
        )}

        {isMobileDevice && (
          <>
            <MobileMenuWrapper
              open={collapsed}
              onClose={handleToggleCollapsed}
              footer={null}
              size={200}
              placement="left"
              title={
                <>
                  <MenuOutlined /> 导航
                </>
              }
              styles={{
                wrapper: { width: "90%" },
                body: {
                  padding: 0,
                  height: "calc(100vh - 41px)",
                  overflow: "hidden",
                },
              }}
            >
              <RouteMenu
                theme={theme}
                mode="inline"
                style={{ borderInlineEnd: "none" }}
              />
            </MobileMenuWrapper>
            <div style={{ display: "flex", alignItems: "center" }}>
              <div
                className="trigger"
                onClick={handleToggleCollapsed}
                style={{ padding: 0 }}
              >
                {collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
              </div>
            </div>
            <div className="header-left">
              <div className="top-menu-wrapper">
                <Logo orientation="horizontal" />
              </div>
            </div>
          </>
        )}

        <div className="header-right">
          {!isMobileDevice && enableLanguage && <Language />}
          {!isMobileDevice && <Setting />}
          <Account />
        </div>
      </Header>

      <Layout style={{ background: "#f0f2f5" }}>
        {/* 侧边菜单模式 */}
        {sider &&
          ["side-menu", "top-side-menu"].includes(layout) &&
          !isMobileDevice && (
            <Sider
              collapsible
              collapsed={collapsed}
              onCollapse={handleSetCollapsed}
              theme={theme}
              trigger={
                collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />
              }
              style={{
                display: "flex",
                flexDirection: "column",
              }}
              className="sider-menu"
            >
              {/* <Logo orientation="vertical" /> */}
              <div
                style={{
                  borderRight: "solid 1px rgba(5, 5, 5, 0.06)",
                  paddingTop: 20,
                  flex: "1 1 auto",
                  overflowY: "auto",
                }}
              >
                <RouteMenu
                  theme={theme}
                  mode="inline"
                  style={{ borderInlineEnd: "none" }}
                />
              </div>
            </Sider>
          )}

        <Layout style={{ background: "#f0f2f5" }}>
          {route.multiple && !isMobileDevice && (
            <MultiTab
              style={{
                position: "sticky",
                top: 61,
                background: "#fff",
                zIndex: 100,
                margin: "8px 8px -8px 8px",
                borderBottom: "1px solid var(--ant-color-border-secondary)",
              }}
            />
          )}

          <Content
            style={{
              margin: "8px",
              padding: "20px",
              background: "#fff",
              minHeight: "280px",
            }}
          >
            <Outlet />
          </Content>
        </Layout>
      </Layout>
      <AuthorizedHandler redirect={true} />
    </StyledLayout>
  );
};

export default BasicLayout;
