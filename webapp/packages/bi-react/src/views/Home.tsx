import Ad1 from "@/assets/images/ad/1.png";
import Ad2 from "@/assets/images/ad/2.png";
import Ad3 from "@/assets/images/ad/3.png";
import Ad4 from "@/assets/images/ad/4.png";
import Ad5 from "@/assets/images/ad/5.png";
import {
  ApiOutlined,
  AppstoreOutlined,
  BarChartOutlined,
  CheckCircleOutlined,
  CloudServerOutlined,
  EditOutlined,
  LaptopOutlined,
  LockOutlined,
  MobileOutlined,
  PlayCircleOutlined,
  RightOutlined,
  RobotOutlined,
  RocketOutlined,
  SafetyCertificateOutlined,
  ThunderboltOutlined,
  ToolOutlined
} from "@ant-design/icons";
import { Button, Col, Row, Space, Typography } from "antd";
import { useNavigate } from "react-router-dom";
import styled from "styled-components";

const { Title, Paragraph, Text } = Typography;


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

const HeroSection = styled(Section)`
  padding-top: 20px;
  padding-bottom: 20px;
  position: relative;
  overflow: hidden;

  // &::before {
  //   content: "";
  //   position: absolute;
  //   top: -100px;
  //   right: -100px;
  //   width: 600px;
  //   height: 600px;
  //   background: radial-gradient(
  //     circle,
  //     rgba(22, 119, 255, 0.1) 0%,
  //     rgba(255, 255, 255, 0) 70%
  //   );
  //   border-radius: 50%;
  // }

  .ant-row {
    background: linear-gradient(135deg, #f0f7ff 0%, #ffffff 100%);

    .ant-col {
      padding: 36px 24px;

      &:last-child:before {
        content: "";
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: linear-gradient(135deg, #165dff 0%, #36cfc9 100%);
        transform: translate(0, 0) rotate(0) skewX(-12deg) skewY(0) scaleX(1)
          scaleY(1);
        transform-origin: top right;
        opacity: 0.05;
      }
    }
  }
`;

const HeroImage = styled.div`
  width: 80%;
  height: 300px;
  border-radius: 12px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 10% 0;
  transition-duration: 500ms;

  &.deg-2 {
    transform: translate(0, 0) rotate(2deg) skewX(0) skewY(0) scaleX(1)
      scaleY(1);
  }

  &:hover {
    transform: translate(0, 0) rotate(0deg) skewX(0) skewY(0) scaleX(1)
      scaleY(1);
  }

  img {
    width: 100%;
    height: 100%;
  }
`;

const FeatureCard = styled.div`
  background: #f8faff;
  padding: 32px;
  border-radius: 12px;
  transition: all 0.3s;
  height: 100%;
  cursor: pointer;

  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 10px 30px rgba(22, 119, 255, 0.1);
    background: #ffffff;
  }

  .icon-wrapper {
    width: 48px;
    height: 48px;
    background: #e6f4ff;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 24px;
    color: #1677ff;
    font-size: 24px;
  }
`;

const BlueBanner = styled(Section)`
  color: #ffffff;
  padding: 0 0 60px 0;

  & > div {
    background: linear-gradient(90deg, #1677ff 0%, #4096ff 100%);
    border-radius: 12px;
  }

  h2 {
    color: #ffffff !important;
  }

  .list-item {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 16px;
    font-size: 16px;
  }

  .ant-col:last-child {
    padding-inline: 0 !important;
  }

  .banner-image {
    background: rgba(255, 255, 255, 0.1);
    height: 250px;
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: rgba(255, 255, 255, 0.5);

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      border-top-right-radius: 12px;
      border-bottom-right-radius: 12px;
    }
  }
`;

const SectionTitle = styled.div<{ align?: "left" | "center" }>`
  text-align: ${(props) => props.align || "center"};
  margin-bottom: 60px;

  h2 {
    font-size: 32px;
    font-weight: 700;
    margin-bottom: 16px;
    color: #1f2329;
  }

  p {
    font-size: 16px;
    color: #646a73;
    max-width: 600px;
    margin: 0 auto;
    line-height: 1.6;
  }
`;

const GridCard = styled.div`
  background: #ffffff;
  padding: 24px;
  border-radius: 8px;
  border: 1px solid #eee;
  display: flex;
  gap: 16px;
  transition: all 0.3s;

  &:hover {
    border-color: #1677ff;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  }

  .icon {
    font-size: 24px;
    color: #1677ff;
    margin-top: 4px;
  }
`;

export const Home = () => {
  const navigate = useNavigate();

  const handleLogin = () => {
    navigate("/login");
  };

  return (
    <>

      {/* Hero Section */}
      <HeroSection id="home">
        <Container>
          <Row gutter={[48, 48]} align="middle">
            <Col xs={24} lg={12}>
              <Title level={1} style={{ fontSize: 48, marginBottom: 24 }}>
                AI驱动的
                <br />
                智能BI分析平台
              </Title>
              <Paragraph
                style={{ fontSize: 18, color: "#646a73", marginBottom: 32 }}
              >
                即可探索您的数据，利用AI能力快速洞察业务趋势。拖拽操作，自动设计，多端适配，让数据分析变得前所未有的简单。
              </Paragraph>
              <Space size={16}>
                <Button
                  type="primary"
                  size="large"
                  style={{ height: 48, padding: "0 32px" }}
                  onClick={handleLogin}
                >
                  开始免费试用
                </Button>
                <Button
                  size="large"
                  icon={<PlayCircleOutlined />}
                  style={{ height: 48 }}
                >
                  观看演示
                </Button>
              </Space>
              <div
                style={{
                  marginTop: 40,
                  display: "flex",
                  alignItems: "center",
                  gap: 16,
                }}
              >
                {/* Avatars placeholder */}
                <div style={{ display: "flex" }}>
                  {[1, 2, 3].map((i) => (
                    <div
                      key={i}
                      style={{
                        width: 32,
                        height: 32,
                        borderRadius: "50%",
                        background: "#ddd",
                        border: "2px solid #fff",
                        marginLeft: i > 1 ? -8 : 0,
                      }}
                    ></div>
                  ))}
                </div>
                <Text type="secondary">1000+ 企业正在使用</Text>
              </div>
            </Col>
            <Col xs={24} lg={12}>
              <HeroImage className="deg-2">
                <img src={Ad1} alt="BI Dashboard Preview" />
              </HeroImage>
            </Col>
          </Row>
        </Container>
      </HeroSection>

      {/* Features Cards */}
      <Section id="features" bg="#ffffff">
        <Container>
          <SectionTitle>
            <h2>AI驱动的BI系统</h2>
            <p>
              替代传统的人工报表开发模式，赋予业务人员自主分析能力，让数据价值触手可及。
            </p>
          </SectionTitle>
          <Row gutter={[24, 24]}>
            <Col xs={24} md={8}>
              <FeatureCard>
                <div className="icon-wrapper">
                  <RobotOutlined />
                </div>
                <Title level={4}>AI智能分析</Title>
                <Paragraph type="secondary">
                  集成AI大模型能力，智能生成分析报告，洞察业务趋势，辅助决策。
                </Paragraph>
              </FeatureCard>
            </Col>
            <Col xs={24} md={8}>
              <FeatureCard>
                <div className="icon-wrapper">
                  <LaptopOutlined />
                </div>
                <Title level={4}>多端协同</Title>
                <Paragraph type="secondary">
                  PC、平板、手机多端适配，随时随地查看报表，数据尽在掌握。
                </Paragraph>
              </FeatureCard>
            </Col>
            <Col xs={24} md={8}>
              <FeatureCard>
                <div className="icon-wrapper">
                  <ToolOutlined />
                </div>
                <Title level={4}>自助设计能力</Title>
                <Paragraph type="secondary">
                  拖拽式报表设计，所见即所得，内置丰富的图表组件，满足各种分析需求。
                </Paragraph>
              </FeatureCard>
            </Col>
          </Row>
        </Container>
      </Section>

      {/* Blue Banner */}
      <BlueBanner>
        <Container>
          <Row gutter={[48, 48]} align="middle">
            <Col xs={24} md={12}>
              <Title level={2} style={{ color: "#fff" }}>
                快速发布与分享
              </Title>
              <Paragraph
                style={{ color: "rgba(255,255,255,0.85)", marginBottom: 32 }}
              >
                一键发布分享报表，支持链接、二维码等多种分享方式，让数据协作无障碍。
              </Paragraph>
              <div className="list-item">
                <CheckCircleOutlined />{" "}
                <span>支持多种导出格式 (Excel, PDF, Image)</span>
              </div>
              <div className="list-item">
                <CheckCircleOutlined /> <span>支持私有化部署，数据更安全</span>
              </div>
              <div className="list-item">
                <CheckCircleOutlined />{" "}
                <span>灵活的权限控制，确保数据安全</span>
              </div>
            </Col>
            <Col xs={24} md={12}>
              <div className="banner-image">
                <img src={Ad2} alt="BI Dashboard Preview" />
              </div>
            </Col>
          </Row>
        </Container>
      </BlueBanner>

      {/* Architecture */}
      <Section bg="#f8faff">
        <Container>
          <Row gutter={[48, 48]} align="middle">
            <Col xs={24} md={12}>
              <HeroImage style={{ height: 300 }}>
                <img src={Ad3} alt="BI Dashboard Preview" />
              </HeroImage>
            </Col>
            <Col xs={24} md={12}>
              <Title level={2}>多租户架构优势</Title>
              <Paragraph type="secondary" style={{ marginBottom: 32 }}>
                采用领先的SaaS多租户架构设计，为企业提供安全、灵活、高效的数据分析环境。
              </Paragraph>

              <div
                style={{ display: "flex", flexDirection: "column", gap: 24 }}
              >
                <div style={{ display: "flex", gap: 16 }}>
                  <SafetyCertificateOutlined
                    style={{ fontSize: 24, color: "#1677ff" }}
                  />
                  <div>
                    <Title level={5} style={{ marginTop: 0 }}>
                      数据隔离安全
                    </Title>
                    <Text type="secondary">
                      严格的逻辑隔离机制，确保不同租户数据互不可见，保障数据隐私安全。
                    </Text>
                  </div>
                </div>
                <div style={{ display: "flex", gap: 16 }}>
                  <CloudServerOutlined
                    style={{ fontSize: 24, color: "#1677ff" }}
                  />
                  <div>
                    <Title level={5} style={{ marginTop: 0 }}>
                      资源弹性伸缩
                    </Title>
                    <Text type="secondary">
                      基于云原生架构，满足不同规模企业的性能和存储需求。
                    </Text>
                  </div>
                </div>
                <div style={{ display: "flex", gap: 16 }}>
                  <RocketOutlined style={{ fontSize: 24, color: "#1677ff" }} />
                  <div>
                    <Title level={5} style={{ marginTop: 0 }}>
                      成本控制优势
                    </Title>
                    <Text type="secondary">
                      共享基础设施，降低运维成本，按需付费更加灵活。
                    </Text>
                  </div>
                </div>
              </div>
            </Col>
          </Row>
        </Container>
      </Section>

      {/* Design Capability */}
      <Section>
        <Container>
          <SectionTitle>
            <h2>自助设计与发布能力</h2>
            <p>从简单的图表管理，到复杂的大屏展示，一切尽在掌握。</p>
          </SectionTitle>

          {/* Sub Section 1 */}
          <Row gutter={[64, 48]} align="middle">
            <Col xs={24} md={10}>
              <Title level={3}>拖拽式报表设计</Title>
              <Paragraph type="secondary">
                真正的所见即所得，丰富的图表组件，支持多种数据源接入方式。
              </Paragraph>
              <Space direction="vertical" size={12}>
                <Text>
                  <CheckCircleOutlined
                    style={{ color: "#1677ff", marginRight: 8 }}
                  />{" "}
                  海量图表库
                </Text>
                <Text>
                  <CheckCircleOutlined
                    style={{ color: "#1677ff", marginRight: 8 }}
                  />{" "}
                  自由布局模式
                </Text>
                <Text>
                  <CheckCircleOutlined
                    style={{ color: "#1677ff", marginRight: 8 }}
                  />{" "}
                  智能配色推荐
                </Text>
              </Space>
              <div style={{ marginTop: 24 }}>
                <a
                  href="#"
                  style={{ display: "flex", alignItems: "center", gap: 4 }}
                >
                  了解更多设计功能 <RightOutlined style={{ fontSize: 12 }} />
                </a>
              </div>
            </Col>
            <Col xs={24} md={14}>
              <HeroImage style={{ height: 320 }}>
                <img src={Ad4} alt="BI Dashboard Preview" />
              </HeroImage>
            </Col>
          </Row>

          {/* Sub Section 2 */}
          <Row
            gutter={[64, 48]}
            align="middle"
            style={{ flexDirection: "row-reverse" }}
          >
            <Col xs={24} md={10}>
              <Title level={3}>一键发布与分享</Title>
              <Paragraph type="secondary">
                设计好的报表一键发布，支持链接、二维码、嵌入等多种分享方式。
              </Paragraph>
              <Space direction="vertical" size={12}>
                <div style={{ display: "flex", gap: 12 }}>
                  <MobileOutlined style={{ fontSize: 20, color: "#1677ff" }} />
                  <div>
                    <Text strong>多端适配</Text>
                    <br />
                    <Text type="secondary" style={{ fontSize: 12 }}>
                      PC、Tablet、Mobile 完美自适应
                    </Text>
                  </div>
                </div>
                <div style={{ display: "flex", gap: 12 }}>
                  <ApiOutlined style={{ fontSize: 20, color: "#1677ff" }} />
                  <div>
                    <Text strong>无缝嵌入</Text>
                    <br />
                    <Text type="secondary" style={{ fontSize: 12 }}>
                      支持 Iframe / SDK 嵌入第三方系统
                    </Text>
                  </div>
                </div>
              </Space>
            </Col>
            <Col xs={24} md={14}>
              <HeroImage style={{ height: 320 }}>
                <img src={Ad5} alt="BI Dashboard Preview" />
              </HeroImage>
            </Col>
          </Row>
        </Container>
      </Section>

      {/* Core Features Grid */}
      <Section bg="#f8faff">
        <Container>
          <SectionTitle>
            <h2>产品核心特性概览</h2>
            <p>全方位的BI能力支持，满足企业级数据分析需求。</p>
          </SectionTitle>
          <Row gutter={[24, 24]}>
            {[
              {
                icon: <BarChartOutlined />,
                title: "多维数据分析",
                desc: "支持下钻、联动、跳转等交互分析能力。",
              },
              {
                icon: <EditOutlined />,
                title: "可视化设计",
                desc: "所见即所得的画布，自由布局，简单易用。",
              },
              {
                icon: <CloudServerOutlined />,
                title: "高性能引擎",
                desc: "亿级数据秒级响应，稳定流畅的分析体验。",
              },
              {
                icon: <MobileOutlined />,
                title: "移动端适配",
                desc: "原生H5体验，随时随地查看数据报表。",
              },
              {
                icon: <ThunderboltOutlined />,
                title: "智能监控",
                desc: "支持数据预警，异常情况实时通知触达。",
              },
              {
                icon: <LockOutlined />,
                title: "安全控制",
                desc: "精细化权限管理，保障企业数据资产安全。",
              },
              {
                icon: <ApiOutlined />,
                title: "数据集成",
                desc: "支持主流数据库、API等多种数据源接入。",
              },
              {
                icon: <AppstoreOutlined />,
                title: "门户集成",
                desc: "提供完善的API接口，轻松集成到现有系统。",
              },
            ].map((item, index) => (
              <Col xs={24} sm={12} md={6} key={index}>
                <GridCard>
                  <div className="icon">{item.icon}</div>
                  <div>
                    <Title level={5} style={{ marginTop: 0, marginBottom: 8 }}>
                      {item.title}
                    </Title>
                    <Text type="secondary" style={{ fontSize: 13 }}>
                      {item.desc}
                    </Text>
                  </div>
                </GridCard>
              </Col>
            ))}
          </Row>
        </Container>
      </Section>

    </>
  );
};
