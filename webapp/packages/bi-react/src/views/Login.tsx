import LinkedAnimate from "@/components/motion/LinkedAnimate";
import config from "@/config";
import { useThirdAuthLogin } from "@/hooks/useThirdAuth";
import { CryptoUtils } from "@/utils/crypto";
import { setToken } from "@/utils/token";
import {
  ArrowRightOutlined,
  DingdingOutlined,
  GithubOutlined,
  LockOutlined,
  SafetyCertificateOutlined,
  UserOutlined,
  WechatOutlined,
} from "@ant-design/icons";
import { Button, Checkbox, Divider, Form, Input, message, Modal } from "antd";
import Cookies from "js-cookie";
import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useDispatch } from "react-redux";
import { useNavigate } from "react-router-dom";
import styled from "styled-components";
import { AuthResponse, login } from "../services/auth";
import {
  setTenant as setTenantAction,
  setTenants as setTenantsAction,
} from "../store/accountSlice";

const PageWrapper = styled.div`
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background-color: #f0f2f5;
  padding: 20px;
  position: relative;
`;

const LoginCard = styled.div`
  width: 420px;
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.05);
  overflow: hidden;
  z-index: 1;

  @media (max-width: 480px) {
    width: 100%;
  }
`;

const Header = styled.div`
  background: #1677ff;
  padding: 40px 20px;
  text-align: center;
  color: #ffffff;

  h1 {
    margin: 0 0 12px;
    font-size: 28px;
    font-weight: 700;
    color: #ffffff;
    letter-spacing: 2px;
  }

  p {
    margin: 0;
    font-size: 14px;
    opacity: 0.9;
  }
`;

const FormContainer = styled.div`
  padding: 40px;

  .ant-form-item {
    margin-bottom: 24px;
  }

  .ant-input-affix-wrapper {
    padding: 10px 11px;
  }

  .login-btn {
    height: 44px;
    font-size: 16px;
    background: #1677ff;

    &:hover {
      background: #4096ff;
    }
  }
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

const OtherLoginMethods = styled.div`
  display: flex;
  justify-content: center;
  gap: 24px;
  margin-top: 24px;

  .icon-btn {
    width: 40px;
    height: 40px;
    border: 1px solid #e5e5e5;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
    color: #666;
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      color: #1677ff;
      border-color: #1677ff;
      background: #f0f7ff;
    }

    &.cas:hover {
      color: #722ed1;
      border-color: #722ed1;
      background: #f9f0ff;
    }
    &.wechat:hover {
      color: #07c160;
      border-color: #07c160;
      background: #eafff3;
    }
    &.dingding:hover {
      color: #0089ff;
      border-color: #0089ff;
      background: #e6f4ff;
    }
    &.github:hover {
      color: #333;
      border-color: #333;
      background: #f5f5f5;
    }
  }
`;

const Footer = styled.div`
  margin-top: 40px;
  text-align: center;
  color: #888;
  font-size: 12px;
`;

export const Login: React.FC = () => {
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const [form] = Form.useForm();
  const { t } = useTranslation();
  const [tenants, setTenants] = useState<AuthResponse["tenants"]>([]);
  const [tenantVisible, setTenantVisible] = useState(false);

  const { handleThirdLogin } = useThirdAuthLogin();

  useEffect(() => {
    const paramsStr = Cookies.get("usoft_bi_login_params");
    if (paramsStr) {
      try {
        const params = JSON.parse(CryptoUtils.decodeBase64(paramsStr));
        if (params.remember) {
          form.setFieldsValue(params);
        }
      } catch (e) {
        console.error("Failed to parse login params", e);
        Cookies.remove("usoft_bi_login_params");
      }
    }
  }, [form]);

  /**
   * 处理用户名密码登录
   */
  const onFinish = async (values: any) => {
    try {
      const params = {
        username: values.username,
        password: values.password,
        remember: values.remember,
      };

      const publicKey = config.sm2.publicKey || "";
      if (publicKey) {
        values.password = CryptoUtils.encryptSM2(values.password, publicKey);
      }

      const res = await login(values);
      if (res.code === "OK" || res.code === 200) {
        message.success(t("app.loginSuccess"));

        if (params.remember) {
          const paramsStr = CryptoUtils.encodeBase64(JSON.stringify(params));
          Cookies.set("usoft_bi_login_params", paramsStr, { expires: 7 });
        } else {
          Cookies.remove("usoft_bi_login_params");
        }

        const { tenants, token } = res.data;
        setToken(token);
        setTenants(tenants || []);
        dispatch(setTenantsAction(tenants || []));
        if (tenants.length === 1) {
          dispatch(setTenantAction(tenants[0]));
          navigate("/app");
        } else {
          dispatch(setTenantAction(null));
          setTenantVisible(tenants.length > 1);
        }
      }
    } catch (error) {
      console.error(error);
    }
  };

  return (
    <PageWrapper>
      <LinkedAnimate />
      <TenantSelectModal
        title={t("app.tenantSelection")}
        visible={tenantVisible}
        footer={null}
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
      <LoginCard>
        <Header>
          <h1>{t("app.title")}</h1>
          <p>{t("app.subtitle")}</p>
        </Header>
        <FormContainer>
          <Form
            form={form}
            name="login"
            initialValues={{ remember: true }}
            onFinish={onFinish}
            layout="vertical"
          >
            <Form.Item
              label={t("app.username")}
              name="username"
              rules={[{ required: true, message: t("form.usernameRequired") }]}
            >
              <Input
                prefix={<UserOutlined style={{ color: "rgba(0,0,0,.25)" }} />}
                placeholder={t("app.username")}
              />
            </Form.Item>

            <Form.Item
              label={t("app.password")}
              name="password"
              rules={[{ required: true, message: t("form.passwordRequired") }]}
            >
              <div style={{ position: "relative" }}>
                <Input.Password
                  prefix={<LockOutlined style={{ color: "rgba(0,0,0,.25)" }} />}
                  placeholder={t("app.password")}
                />
                {config.forgotPassword && (
                  <a
                    style={{
                      position: "absolute",
                      right: 0,
                      top: -30,
                      fontSize: 12,
                      color: "#1677ff",
                    }}
                    href="#"
                  >
                    {t("app.forgotPassword")}
                  </a>
                )}
              </div>
            </Form.Item>

            <Form.Item name="remember" valuePropName="checked">
              <Checkbox>{t("app.rememberMe")}</Checkbox>
            </Form.Item>

            <Form.Item>
              <Button
                type="primary"
                htmlType="submit"
                block
                className="login-btn"
              >
                {t("app.login")} <ArrowRightOutlined />
              </Button>
            </Form.Item>
            {config.sso.third && (
              <>
                <Divider
                  plain
                  style={{ fontSize: 12, color: "#999", margin: "24px 0" }}
                >
                  {t("app.otherLoginMethods")}
                </Divider>

                <OtherLoginMethods>
                  {config.sso.cas && (
                    <div
                      className="icon-btn cas"
                      onClick={() =>
                        (window.location.href = config.apiBasePath + "/auth/cas/login")
                      }
                      title="CAS"
                    >
                      <SafetyCertificateOutlined />
                    </div>
                  )}
                  {config.sso.wechatOpen && (
                    <div
                      className="icon-btn wechat"
                      onClick={() => handleThirdLogin("wechat_open")}
                    >
                      <WechatOutlined />
                    </div>
                  )}
                  {config.sso.dingtalk && (
                    <div
                      className="icon-btn dingding"
                      onClick={() => handleThirdLogin("dingtalk")}
                    >
                      <DingdingOutlined />
                    </div>
                  )}
                  {config.sso.github && (
                    <div
                      className="icon-btn github"
                      onClick={() => handleThirdLogin("github")}
                    >
                      <GithubOutlined />
                    </div>
                  )}
                </OtherLoginMethods>
              </>
            )}
            {config.register && (
              <div style={{ textAlign: "center", marginTop: 24, fontSize: 14 }}>
                <span style={{ color: "#666" }}>{t("app.noAccount")} </span>
                <a href="#" style={{ color: "#1677ff", fontWeight: 500 }}>
                  {t("app.register")}
                </a>
              </div>
            )}
          </Form>
        </FormContainer>
      </LoginCard>

      <Footer>{t("app.copyright")}</Footer>
    </PageWrapper>
  );
};
