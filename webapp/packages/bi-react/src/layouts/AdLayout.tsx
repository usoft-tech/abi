import logo from "@/assets/logo.png";
import { config } from "@/store/appSlice";
import {
  GithubOutlined,
  WechatOutlined,
  WeiboOutlined,
  ZhihuOutlined
} from "@ant-design/icons";
import { Button, Col, Divider, Row, Space, Typography } from "antd";
import { useTranslation } from "react-i18next";
import { useSelector } from "react-redux";
import { Outlet, useNavigate } from "react-router-dom";
import styled from "styled-components";
import AuthorizedHandler from "./AuthorizedHandler";

const { Title, Paragraph, Text } = Typography;

// Styled Components
const PageWrapper = styled.div`
  width: 100%;
  overflow-x: hidden;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto,
    "Helvetica Neue", Arial, "Noto Sans", sans-serif, "Apple Color Emoji",
    "Segoe UI Emoji", "Segoe UI Symbol", "Noto Color Emoji";
`;

const HeaderWrapper = styled.header`
  position: sticky;
  top: 0;
  z-index: 1000;
  width: 100%;
  height: 64px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(8px);
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.03),
    0 1px 6px -1px rgba(0, 0, 0, 0.02), 0 2px 4px 0 rgba(0, 0, 0, 0.02);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 40px;

  .logo {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 20px;
    font-weight: bold;
    color: #1677ff;
    cursor: pointer;
  }

  .nav-links {
    display: flex;
    gap: 32px;

    a {
      color: #1f2329;
      font-size: 14px;
      font-weight: 500;
      text-decoration: none;
      transition: color 0.3s;

      &:hover {
        color: #1677ff;
      }
    }
  }

  .actions {
    display: flex;
    gap: 16px;
  }
`;

const Section = styled.section<{ bg?: string; padding?: string }>`
  background: ${(props) => props.bg || "#ffffff"};
  padding: ${(props) => props.padding || "80px 0"};
  width: 100%;
`;

const Container = styled.div`
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
`;

const FooterWrapper = styled.footer`
  background: #001529;
  color: rgba(255, 255, 255, 0.65);
  padding: 80px 0 40px;

  .footer-title {
    color: #ffffff;
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 24px;
  }

  .footer-link {
    display: block;
    color: rgba(255, 255, 255, 0.65);
    margin-bottom: 12px;
    transition: color 0.3s;
    cursor: pointer;

    &:hover {
      color: #1677ff;
    }
  }

  .social-icon {
    font-size: 24px;
    margin-right: 20px;
    cursor: pointer;
    transition: color 0.3s;

    &:hover {
      color: #ffffff;
    }
  }
`;

const CTASection = styled(Section)`
  background: #1677ff;
  text-align: center;

  h2 {
    color: #ffffff !important;
    font-size: 36px;
    margin-bottom: 20px;
  }

  p {
    color: rgba(255, 255, 255, 0.85);
    font-size: 18px;
    margin-bottom: 40px;
  }
`;

export const AdLayout = () => {
  const { t } = useTranslation();
  const { name } = useSelector(config);
  const navigate = useNavigate();

  const handleLogin = () => {
    navigate("/login");
  };

  return (
    <PageWrapper>
      {/* Header */}
      <HeaderWrapper>
        <div className="logo" onClick={() => window.location.reload()}>
          <img src={logo} alt={name} style={{ width: 48, height: 48 }} />
          <span>{name}</span>
        </div>
        <nav className="nav-links">
          <a href="#home">首页</a>
          <a href="#features">产品功能</a>
          <a href="#solutions">解决方案</a>
          <a href="#cases">客户案例</a>
          <a href="#about">关于我们</a>
        </nav>
        <div className="actions">
          <Button type="text" onClick={handleLogin}>
            登录
          </Button>
          <Button type="primary" onClick={handleLogin}>
            免费试用
          </Button>
        </div>
      </HeaderWrapper>

      <Outlet />

      {/* CTA Footer */}
      <CTASection>
        <Container>
          <Title level={2} style={{ color: "#fff" }}>
            开始您的智能数据分析之旅
          </Title>
          <Paragraph style={{ color: "rgba(255,255,255,0.8)" }}>
            立即注册体验全功能的BI平台，让数据驱动业务价值。
          </Paragraph>
          <Space size={24}>
            <Button
              type="default"
              size="large"
              style={{
                width: 160,
                height: 48,
                color: "#1677ff",
                fontWeight: "bold",
              }}
              onClick={handleLogin}
            >
              免费试用
            </Button>
            <Button ghost size="large" style={{ width: 160, height: 48 }}>
              联系我们
            </Button>
          </Space>
        </Container>
      </CTASection>

      {/* Footer */}
      <FooterWrapper>
        <Container>
          <Row gutter={[48, 48]}>
            <Col xs={24} md={8}>
              <div
                className="logo"
                style={{
                  display: "flex",
                  alignItems: "center",
                  gap: 10,
                  marginBottom: 20,
                }}
              >
                <img src={logo} alt={name} style={{ width: 32, height: 32 }} />
                <span
                  style={{ fontSize: 20, fontWeight: "bold", color: "#fff" }}
                >
                  {name}
                </span>
              </div>
              <Text style={{ color: "rgba(255,255,255,0.45)" }}>
                AI驱动的智能BI分析平台，让数据分析更简单、更智能。
              </Text>
              <div style={{ marginTop: 24 }}>
                <GithubOutlined className="social-icon" />
                <WeiboOutlined className="social-icon" />
                <WechatOutlined className="social-icon" />
                <ZhihuOutlined className="social-icon" />
              </div>
            </Col>
            <Col xs={12} md={4}>
              <div className="footer-title">产品</div>
              <a className="footer-link">功能特性</a>
              <a className="footer-link">解决方案</a>
              <a className="footer-link">更新日志</a>
              <a className="footer-link">私有化部署</a>
            </Col>
            <Col xs={12} md={4}>
              <div className="footer-title">资源</div>
              <a className="footer-link">帮助文档</a>
              <a className="footer-link">视频教程</a>
              <a className="footer-link">开发者API</a>
              <a className="footer-link">社区问答</a>
            </Col>
            <Col xs={12} md={4}>
              <div className="footer-title">关于</div>
              <a className="footer-link">关于我们</a>
              <a className="footer-link">加入我们</a>
              <a className="footer-link">联系方式</a>
              <a className="footer-link">合作伙伴</a>
            </Col>
          </Row>
          <Divider style={{ borderColor: "rgba(255,255,255,0.1)" }} />
          <div style={{ textAlign: "center", color: "rgba(255,255,255,0.45)" }}>
            {t('app.copyright')}
          </div>
        </Container>
      </FooterWrapper>
      <AuthorizedHandler />
    </PageWrapper>
  );
};
