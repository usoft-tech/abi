import IconFont from "@/components/icon/IconFont";
import { RootState } from "@/store";
import {
  config,
  setLayout,
  setTheme,
  toggleMultipleRoute,
} from "@/store/appSlice";
import { selectBoolean } from "@/store/settingSlice";
import { CheckOutlined } from "@ant-design/icons";
import { Dropdown, MenuProps } from "antd";
import React, { useMemo } from "react";
import { useTranslation } from "react-i18next";
import { useDispatch, useSelector } from "react-redux";
import styled from "styled-components";

const StyledSetting = styled.div`
  line-height: normal;

  &.dark {
    * {
      color: #ffffff !important;
    }
  }

  .ant-dropdown-link {
    cursor: pointer;
    display: inline-block;
  }
`;

const Setting: React.FC = () => {
  const dispatch = useDispatch();
  const { layout, theme, route } = useSelector(config);
  const { t } = useTranslation();

  const enableThemeSwitch = useSelector((state: RootState) =>
    selectBoolean(state, "theme.switch", true),
  );

  const enableLayoutSwitch = useSelector((state: RootState) =>
    selectBoolean(state, "layout.switch", true),
  );

  const handleSetTheme = (newTheme: "dark" | "light") => {
    dispatch(setTheme(newTheme));
  };

  const handleSetLayout = (
    newLayout: "side-menu" | "top-menu" | "top-side-menu",
  ) => {
    dispatch(setLayout(newLayout));
  };

  const menuItems: MenuProps["items"] = useMemo(() => {
    const list: MenuProps["items"] = [];
    if (enableThemeSwitch) {
      const themeItems = [
        {
          key: "dark",
          label: (
            <span>
              <IconFont type="icon-moon" style={{ marginRight: 8 }} />
              {t("setting.dark")}
              {theme === "dark" && <CheckOutlined style={{ marginLeft: 8 }} />}
            </span>
          ),
          onClick: () => handleSetTheme("dark"),
        },
        {
          key: "light",
          label: (
            <span>
              <IconFont type="icon-sun" style={{ marginRight: 8 }} />
              {t("setting.light")}
              {theme === "light" && <CheckOutlined style={{ marginLeft: 8 }} />}
            </span>
          ),
          onClick: () => handleSetTheme("light"),
        },
      ];
      list.push(...themeItems);
      list.push({
        type: "divider",
      });
    }
    list.push({
      key: "multiple-route",
      label: (
        <span>
          <IconFont type="icon-tabs" style={{ marginRight: 8 }} />
          {t("setting.multiTab")}
          {route.multiple && <CheckOutlined style={{ marginLeft: 8 }} />}
        </span>
      ),
      onClick: () => dispatch(toggleMultipleRoute()),
    });
    if (enableLayoutSwitch) {
      const layoutItems = [
        {
          key: "side-menu",
          label: (
            <span>
              <IconFont type="icon-sider-l" style={{ marginRight: 8 }} />
              {t("setting.side-menu")}
              {layout === "side-menu" && (
                <CheckOutlined style={{ marginLeft: 8 }} />
              )}
            </span>
          ),
          onClick: () => handleSetLayout("side-menu"),
        },
        {
          key: "top-menu",
          label: (
            <span>
              <IconFont
                type="icon-sider-l"
                style={{ marginRight: 8, transform: "rotate(90deg)" }}
              />
              {t("setting.top-menu")}
              {layout === "top-menu" && (
                <CheckOutlined style={{ marginLeft: 8 }} />
              )}
            </span>
          ),
          onClick: () => handleSetLayout("top-menu"),
        },
        {
          key: "top-side-menu",
          label: (
            <span>
              <IconFont
                type="icon-sider-l"
                style={{ marginRight: 8, transform: "rotate(90deg)" }}
              />
              {t("setting.top-side-menu")}
              {layout === "top-side-menu" && (
                <CheckOutlined style={{ marginLeft: 8 }} />
              )}
            </span>
          ),
          onClick: () => handleSetLayout("top-side-menu"),
        },
      ];
      list.push({
        type: "divider",
      });
      list.push(...layoutItems);
    }
    return list;
  }, [route.multiple, theme, layout, enableLayoutSwitch, enableThemeSwitch]);

  return (
    <StyledSetting
      className={theme === "dark" && layout === "top-menu" ? "dark" : ""}
    >
      <Dropdown menu={{ items: menuItems }} placement="bottom">
        <a className="ant-dropdown-link" onClick={(e) => e.preventDefault()}>
          <IconFont
            type="icon-magic-fill"
            style={{ fontSize: 20, color: "#444444" }}
          />
        </a>
      </Dropdown>
    </StyledSetting>
  );
};

export default Setting;
