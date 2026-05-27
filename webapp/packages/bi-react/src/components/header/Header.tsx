import React from "react";
import styled from "styled-components";

export type HeaderProps = {
  title?: string;
  subTitle?: string;
  extra?: React.ReactNode;
  style?: React.CSSProperties;
  className?: string;
};

const HeaderWrapper = styled.header`
  width: 100%;
  margin-bottom: 20px;
  // border-bottom: solid 1px var(--ant-color-border);
  background: #ffffff;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  .title {
    display: flex;
    flex-direction: column;
  }
  .title .main {
    font-size: 16px;
    font-weight: 500;
  }
  .title .sub {
    color: #999;
    font-size: 12px;
  }
`;

export const Header: React.FC<HeaderProps> = ({
  title,
  subTitle,
  extra,
  style = {},
  className,
}) => {
  return (
    <HeaderWrapper style={{ ...style }} className={className}>
      <div className="title">
        <span className="main">{title}</span>
        {subTitle && <span className="sub">{subTitle}</span>}
      </div>
      {extra && <div className="extra">{extra}</div>}
    </HeaderWrapper>
  );
};

const SectionTitleWrapper = styled.header`
  width: 100%;
  margin-bottom: 20px;
  // border-bottom: solid 1px var(--ant-color-border);
  background: #ffffff;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  .title {
    display: flex;
    flex-direction: row;
    border-left: solid 4px var(--ant-blue-6);
    padding-left: 8px;
    align-items: center;
  }
  .title .main {
    font-size: 14px;
    font-weight: 500;
    margin-right: 8px;
  }
  .title .sub {
    color: #999;
    font-size: 12px;
  }
`;

export const SectionTitle: React.FC<HeaderProps> = ({
  title,
  subTitle,
  extra,
  style = {},
  className,
}) => {
  return (
    <SectionTitleWrapper style={{ ...style }} className={`section-title ${className}`}>
      <div className="title">
        <span className="main">{title}</span>
        {subTitle && <span className="sub">{subTitle}</span>}
      </div>
      {extra && <div className="extra">{extra}</div>}
    </SectionTitleWrapper>
  );
};

export default Header;
