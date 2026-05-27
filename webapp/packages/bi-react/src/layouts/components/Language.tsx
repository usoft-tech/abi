import { config } from "@/store/appSlice";
import { CheckOutlined, GlobalOutlined } from "@ant-design/icons";
import { Dropdown, MenuProps } from "antd";
import React from "react";
import { useTranslation } from "react-i18next";
import { useSelector } from "react-redux";
import styled from "styled-components";

const StyledLanguage = styled.div`
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

const Language: React.FC = () => {
  const { layout, theme } = useSelector(config);
  const { t, i18n } = useTranslation();

  const changeLanguage = (lng: string) => {
    i18n.changeLanguage(lng);
  };

  const menuItems: MenuProps["items"] = [
    {
      key: "zh-CN",
      label: (
        <span>
          {t("setting.zhCN")}
          {i18n.language === "zh-CN" && (
            <CheckOutlined style={{ marginLeft: 8 }} />
          )}
        </span>
      ),
      onClick: () => changeLanguage("zh-CN"),
    },
    {
      key: "en-US",
      label: (
        <span>
          {t("setting.enUS")}
          {i18n.language === "en-US" && (
            <CheckOutlined style={{ marginLeft: 8 }} />
          )}
        </span>
      ),
      onClick: () => changeLanguage("en-US"),
    },
  ];

  return (
    <StyledLanguage
      className={theme === "dark" && layout === "top-menu" ? "dark" : ""}
    >
      <Dropdown menu={{ items: menuItems }} placement="bottom">
        <a className="ant-dropdown-link" onClick={(e) => e.preventDefault()}>
          <GlobalOutlined style={{ fontSize: 20, color: "#444444" }} />
          {/* <IconFont type="icon-magic-fill" style={{ fontSize: 20, color: '#444444' }} /> */}
        </a>
      </Dropdown>
    </StyledLanguage>
  );
};

export default Language;
