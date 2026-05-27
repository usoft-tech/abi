import logo from "@/assets/logo.png";
import { RootState } from "@/store";
import { config } from "@/store/appSlice";
import { selectString } from "@/store/settingSlice";
import { LogoType } from "@/views/tenant/services";
import { useSelector } from "react-redux";
import styled from "styled-components";

const LogoWrapper = styled.div`
  height: 61px;
  line-height: 28px;
  padding: 0 16px;
  font-size: 18px;
  font-weight: bold;
  text-align: center;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  position: sticky;
  top: 0;
  z-index: 1000;
  user-select: none;
  flex: none;

  img {
    height: 40px;
    margin-right: 12px;
  }

  &.dark {
    color: #fff;
  }

  &.light {
    // background: #ffffff;
  }

  &.vertical {
    &.light {
      color: #000;
      border-bottom: solid 1px #e8e8e8;
    }
  }

  &.horizontal {
    float: left;
    margin: 0 24px 0 0;
    color: #1890ff;
  }
`;

type LogoProps = {
  orientation?: "vertical" | "horizontal";
  type?: LogoType;
  logoImg?: string;
  style?: React.CSSProperties;
};

const Logo = ({
  orientation = "vertical",
  type,
  logoImg,
  style = {},
}: LogoProps) => {
  const { name, theme } = useSelector(config);
  const { tenant } = useSelector((state: RootState) => state.account);
  const siteName = useSelector((state: RootState) =>
    selectString(state, "site.name"),
  );
  const siteLogo = useSelector((state: RootState) =>
    selectString(state, "site.logo"),
  );
  const logoType = type || tenant?.siteConfig?.logoType || LogoType.LOGO_NAME;
  return (
    <LogoWrapper className={`logo ${orientation} ${theme}`} style={style}>
      {logoType !== LogoType.ONLY_NAME && (
        <img
          src={logoImg || tenant?.siteConfig?.logo || siteLogo || logo}
          alt="logo"
        />
      )}
      {logoType !== LogoType.ONLY_LOGO && (
        <span style={{ fontSize: 16, color: "var(--ant-color-primary)" }}>
          {tenant?.siteConfig?.name || siteName || name}
        </span>
      )}
    </LogoWrapper>
  );
};

export default Logo;
