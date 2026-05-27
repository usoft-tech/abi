import Header, { SectionTitle } from "@/components/header/Header";
import Upload from "@/components/upload/Upload";
import Logo from "@/layouts/components/Logo";
import { Validator } from "@/utils";
import { PhoneOutlined } from "@ant-design/icons";
import {
  Button,
  Col,
  Form,
  Input,
  message,
  Radio,
  Row,
  Skeleton,
  Spin,
  Typography,
} from "antd";
import { useEffect, useState } from "react";
import styled from "styled-components";
import {
  getTenantSetting,
  LogoType,
  TenantResponse,
  TenantSettingRequest,
  updateTenantSetting,
} from "./services";
import { useDispatch } from "react-redux";
import { setTenant as setTenantAction } from "@/store/accountSlice";
import { Permission } from "@/permission";

const CompactForm = styled(Form)`
  .ant-form-item {
    margin-bottom: 8px;
  }

  .section-title {
    margin-bottom: 8px;
  }
`;

const Setting = () => {
  const [tenant, setTenant] = useState<Partial<TenantResponse>>({
    name: "",
    contactUser: "",
    contactPhone: "",
    siteConfig: {
      logoType: LogoType.LOGO_NAME,
    },
  });
  const [form] = Form.useForm<TenantSettingRequest>();
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const dispatch = useDispatch();

  const logo = Form.useWatch(["siteConfig", "logo"], form);
  const logoType = Form.useWatch(["siteConfig", "logoType"], form);

  const loadData = async () => {
    setLoading(true);
    try {
      const { data: res } = await getTenantSetting();
      // siteConfig 是对象，转为字符串显示以便编辑
      const formData = {
        ...res,
        siteConfig: {
          ...(res.siteConfig || {}),
          logoType: res.siteConfig?.logoType || LogoType.LOGO_NAME,
        },
      };
      setTenant(res);
      form.setFieldsValue(formData);
    } catch (error) {
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleSubmit = async () => {
    setSubmitting(true);
    try {
      const values = await form.validateFields();
      let siteConfig = values.siteConfig;

      const req: TenantSettingRequest = {
        contactUser: values.contactUser,
        contactPhone: values.contactPhone,
        siteConfig: siteConfig,
      };
      await updateTenantSetting(req);
      // 更新 store 中的 tenant 信息
      setTenant({ ...tenant, ...values });
      dispatch(setTenantAction({ ...tenant, ...values }));
      message.success("保存成功");
      // 重新加载数据以确保同步
      loadData();
    } catch (error) {
      console.error(error);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <>
      <Header
        title="系统设置"
        subTitle="设置当前租户的信息，包括联系方式及站点信息"
        extra={
          <Permission value={["tenant:sa:setting"]}>
            <Button type="primary" onClick={handleSubmit}>
              保存设置
            </Button>
          </Permission>
        }
      />
      <Spin spinning={loading || submitting}>
        <Row gutter={48}>
          <Col xs={24} sm={24} md={10}>
            {loading ? (
              <Skeleton active />
            ) : (
              <CompactForm
                form={form as any}
                layout="vertical"
                onFinish={handleSubmit}
                initialValues={{
                  siteConfig: {
                    logoType: LogoType.LOGO_NAME,
                  },
                }}
              >
                <SectionTitle title="基本信息" />
                <Form.Item label="租户名称">
                  <Typography.Text type="secondary">
                    {tenant.name}
                  </Typography.Text>
                </Form.Item>

                <SectionTitle title="站点配置" />
                <Form.Item label="站点名称" name={["siteConfig", "name"]}>
                  <Input />
                </Form.Item>
                <Form.Item
                  label="站点描述"
                  name={["siteConfig", "description"]}
                >
                  <Input />
                </Form.Item>
                <Form.Item label="站点logo" name={["siteConfig", "logo"]}>
                  <Upload
                    bizType="logo"
                    maxCount={1}
                    fileList={logo ? [logo] : []}
                    accept="image/png,image/jpeg,image/svg+xml"
                    onChange={(fileList) => {
                      form.setFieldValue(
                        ["siteConfig", "logo"],
                        fileList?.length ? fileList[0].url : null,
                      );
                    }}
                  />
                </Form.Item>

                <Form.Item
                  label="站点logo类型"
                  name={["siteConfig", "logoType"]}
                  initialValue={LogoType.LOGO_NAME}
                >
                  <Radio.Group
                    optionType="button"
                    options={[
                      { label: "图片 + 名称", value: LogoType.LOGO_NAME },
                      { label: "仅图片", value: LogoType.ONLY_LOGO },
                      { label: "仅名称", value: LogoType.ONLY_NAME },
                    ]}
                  />
                </Form.Item>

                <SectionTitle title="联系方式" />
                <Row gutter={24}>
                  <Col xs={24} sm={24} md={12}>
                    <Form.Item label="联系人" name="contactUser">
                      <Input />
                    </Form.Item>
                  </Col>
                  <Col xs={24} sm={24} md={12}>
                    <Form.Item
                      label="联系电话"
                      name="contactPhone"
                      rules={[
                        {
                          validator: Validator.mobile,
                          message: "请输入联系电话",
                        },
                      ]}
                    >
                      <Input
                        type="tel"
                        prefix={
                          <>
                            <PhoneOutlined /> +86
                          </>
                        }
                      />
                    </Form.Item>
                  </Col>
                </Row>
              </CompactForm>
            )}
          </Col>
          <Col xs={0} sm={0} md={14}>
            <div style={{ marginBottom: 48 }}>
              <SectionTitle title="说明" />
              <Typography.Paragraph type="secondary">
                <ul>
                  <li>在此页面您可以配置系统的基本信息和外观。</li>
                  <li>站点配置修改后，请刷新页面以查看最新效果。</li>
                  <li>支持上传 PNG, JPG, SVG 格式的图片作为 Logo。</li>
                </ul>
              </Typography.Paragraph>
            </div>
            <Typography.Text type="secondary">整体效果预览</Typography.Text>
            <Logo
              type={logoType}
              logoImg={logo}
              style={{
                justifyContent: "flex-start",
                borderBottom: "none",
                padding: 0,
              }}
            />
          </Col>
        </Row>
      </Spin>
    </>
  );
};

export default Setting;
