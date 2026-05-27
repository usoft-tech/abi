import React, { useEffect, useState } from "react";
import {
  Tabs,
  Form,
  Input,
  Button,
  Upload,
  message,
  Avatar,
  Card,
  Typography,
  Row,
  Col,
} from "antd";
import {
  UserOutlined,
  SafetyOutlined,
  CameraOutlined,
  ProfileOutlined,
} from "@ant-design/icons";
import styled from "styled-components";
import { useTranslation } from "react-i18next";
import { useSelector, useDispatch } from "react-redux";
import { RootState } from "@/store";
import { setUser } from "@/store/accountSlice";
import { updateProfile } from "@/services/user";
import ImgCrop from "antd-img-crop";
import { uploadFile } from "@/services/app";

const { Title, Text } = Typography;

const PageContainer = styled.div`
  padding: 24px;
  min-height: 100%;
`;

const SettingsCard = styled(Card)`
  border-radius: 8px;
  box-shadow:
    0 1px 2px 0 rgba(0, 0, 0, 0.03),
    0 1px 6px -1px rgba(0, 0, 0, 0.02),
    0 2px 4px 0 rgba(0, 0, 0, 0.02);

  .ant-card-body {
    padding: 0;
    display: flex;
    flex-direction: row;
  }

  .ant-tabs {
    width: 100%;
  }

  .ant-tabs-nav {
    padding: 16px 0;
    width: 224px;
    
    @media screen and (max-width: 768px) {
      width: 100%;
      padding: 0;
    }
  }

  .ant-tabs-content-holder {
    border-left: 1px solid #f0f0f0;
    padding: 24px 40px;
    min-height: 500px;

    @media screen and (max-width: 768px) {
      border-left: none;
      padding: 24px;
    }
  }
`;

const FormTitle = styled(Title)`
  margin-bottom: 24px !important;
`;

const AvatarView = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;

  .avatar-wrapper {
    position: relative;
    width: 144px;
    height: 144px;
    border-radius: 50%;
    overflow: hidden;
    border: 1px solid #f0f0f0;
    
    &:hover .upload-mask {
      opacity: 1;
    }
  }

  .upload-mask {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.4);
    display: flex;
    align-items: center;
    justify-content: center;
    opacity: 0;
    transition: opacity 0.3s;
    cursor: pointer;
    color: #fff;
    font-size: 24px;
  }
`;

const UserProfile: React.FC = () => {
  const { t } = useTranslation();
  const dispatch = useDispatch();
  const { user } = useSelector((state: RootState) => state.account);
  const [form] = Form.useForm();
  const [passwordForm] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [avatarUrl, setAvatarUrl] = useState<string>("");

  useEffect(() => {
    if (user) {
      form.setFieldsValue({
        displayName: user.displayName,
        username: user.username,
      });
      setAvatarUrl(user.avatar || "");
    }
  }, [user, form]);

  const handleUpdateProfile = async (values: any) => {
    setLoading(true);
    try {
      const { data } = await updateProfile({
        displayName: values.displayName,
        avatar: avatarUrl,
      });
      message.success(t("common.saveSuccess") || "保存成功");
      if (data && user) {
        dispatch(setUser({ ...user, ...data }));
      }
    } catch (error) {
      // Error handled by interceptor
    } finally {
      setLoading(false);
    }
  };

  const handleChangePassword = async (values: any) => {
    setLoading(true);
    try {
      await updateProfile({
        oldPassword: values.oldPassword,
        newPassword: values.newPassword,
      });
      message.success(t("common.saveSuccess") || "保存成功");
      passwordForm.resetFields();
    } catch (error) {
      // Error handled by interceptor
    } finally {
      setLoading(false);
    }
  };

  const handleUpload = async (file: File) => {
    try {
      const { data: res } = await uploadFile("avatar", file);
      if (res && res.url) {
        setAvatarUrl(res.url);
        message.success(t("common.uploadSuccess") || "上传成功");
      }
      return false;
    } catch (e) {
      return false;
    }
  };

  const BasicInfo = () => (
    <>
      <FormTitle level={4}>{t("account.basicInfo") || "基本信息"}</FormTitle>
      <Row gutter={64}>
        <Col xs={24} md={12}>
          <Form
            form={form}
            layout="vertical"
            onFinish={handleUpdateProfile}
            initialValues={{ displayName: user?.displayName }}
          >
            <Form.Item label={t("user.username") || "用户名"} name="username">
              <Input disabled />
            </Form.Item>
            <Form.Item
              label={t("user.displayName") || "显示名称"}
              name="displayName"
              rules={[
                { required: true, message: t("common.required") || "请输入" },
              ]}
            >
              <Input />
            </Form.Item>
            <Form.Item>
              <Button type="primary" htmlType="submit" loading={loading}>
                {t("common.save") || "保存"}
              </Button>
            </Form.Item>
          </Form>
        </Col>
        <Col xs={24} md={12}>
          <AvatarView>
            <Text type="secondary">{t("account.changeAvatar") || "更换头像"}</Text>
            <ImgCrop rotationSlider>
              <Upload showUploadList={false} beforeUpload={handleUpload as any}>
                <div className="avatar-wrapper">
                  <Avatar size={144} src={avatarUrl} icon={<UserOutlined />} />
                  <div className="upload-mask">
                    <CameraOutlined />
                  </div>
                </div>
              </Upload>
            </ImgCrop>
          </AvatarView>
        </Col>
      </Row>
    </>
  );

  const SecuritySettings = () => (
    <>
      <FormTitle level={4}>{t("account.security") || "安全设置"}</FormTitle>
      <Row>
        <Col xs={24} md={12}>
          <Form
            form={passwordForm}
            layout="vertical"
            onFinish={handleChangePassword}
          >
            <Form.Item
              label={t("user.oldPassword") || "旧密码"}
              name="oldPassword"
              rules={[
                { required: true, message: t("common.required") || "请输入" },
              ]}
            >
              <Input.Password placeholder={t("user.oldPassword") || "旧密码"} />
            </Form.Item>
            <Form.Item
              label={t("user.newPassword") || "新密码"}
              name="newPassword"
              rules={[
                { required: true, message: t("common.required") || "请输入" },
                { min: 6, message: "密码长度不能少于6位" }
              ]}
            >
              <Input.Password placeholder={t("user.newPassword") || "新密码"} />
            </Form.Item>
            <Form.Item
              label={t("user.confirmPassword") || "确认密码"}
              name="confirmPassword"
              dependencies={["newPassword"]}
              rules={[
                { required: true, message: t("common.required") || "请输入" },
                ({ getFieldValue }) => ({
                  validator(_, value) {
                    if (!value || getFieldValue("newPassword") === value) {
                      return Promise.resolve();
                    }
                    return Promise.reject(
                      new Error(t("user.passwordMismatch") || "密码不一致"),
                    );
                  },
                }),
              ]}
            >
              <Input.Password placeholder={t("user.confirmPassword") || "确认密码"} />
            </Form.Item>
            <Form.Item>
              <Button type="primary" htmlType="submit" loading={loading}>
                {t("common.save") || "保存"}
              </Button>
            </Form.Item>
          </Form>
        </Col>
      </Row>
    </>
  );

  const items = [
    {
      key: "1",
      label: (
        <span>
          <ProfileOutlined />
          {t("account.basicInfo") || "基本信息"}
        </span>
      ),
      children: <BasicInfo />,
    },
    {
      key: "2",
      label: (
        <span>
          <SafetyOutlined />
          {t("account.security") || "安全设置"}
        </span>
      ),
      children: <SecuritySettings />,
    },
  ];

  return (
    <PageContainer>
        <Tabs
          tabPosition="left"
          items={items}
        />
    </PageContainer>
  );
};

export default UserProfile;
