import {
  AuthItem,
  AuthorizationBizType,
  AuthorizationScope,
  batchUpdateAuthorizations,
  listAuthorizations,
} from "@/services/authorization";
import { TenantUserResponse } from "@/views/sa/services";
import {
  Button,
  Drawer,
  Form,
  message,
  Radio,
  Space,
  Spin,
  Transfer,
} from "antd";
import React, { useEffect, useImperativeHandle, useRef, useState } from "react";
import { listTenantUsers } from "../services";

interface BaseAuthorizeProps {
  bizType: AuthorizationBizType;
  bizId: string;
  publicScopeEnabled?: boolean;
}

interface AuthorizeProps extends BaseAuthorizeProps {
  submitting: boolean;
  onChange?: (values: AuthItem[]) => void;
}

interface AuthorizeDrawerProps extends BaseAuthorizeProps {
  open: boolean;
  onClose: () => void;
}

export interface AuthorizeRef {
  validateFields: () => Promise<AuthItem[]>;
}

export const Authorize = React.forwardRef<AuthorizeRef, AuthorizeProps>(
  (
    { bizType, bizId, submitting, publicScopeEnabled = true, onChange },
    ref,
  ) => {
    const [loading, setLoading] = useState(false);

    const [userOptions, setUserOptions] = useState<TenantUserResponse[]>([]);

    const [form] = Form.useForm<{
      scope: AuthorizationScope;
      userIds: string[];
    }>();

    // 加载用户列表
    const loadUsers = async (keyword = "") => {
      try {
        const res = await listTenantUsers({
          page: 1,
          size: 50, // 限制显示数量，支持搜索
          keyword: keyword,
        });
        // 累加用户，避免已选用户因搜索而消失
        setUserOptions((prev) => {
          const newItems = res.data.items || [];
          const existingIds = new Set(prev.map((p) => p.id));
          const uniqueNewItems = newItems.filter(
            (item) => !existingIds.has(item.id),
          );
          return [...prev, ...uniqueNewItems];
        });
      } catch (error) {
        console.error(error);
      }
    };

    // 初始化加载
    useEffect(() => {
      loadUsers();
    }, []);
    useEffect(() => {
      loadAuthorizationInfo();
    }, [bizType, bizId]);

    // 加载授权信息
    const loadAuthorizationInfo = async () => {
      if (!bizId) return;
      setLoading(true);
      try {
        const { data: list } = await listAuthorizations(bizType, bizId);
        if (list && list.length > 0) {
          // 简单逻辑：根据第一条记录判断 scope
          // 实际逻辑可能更复杂，如果存在混合授权
          const first = list[0];

          form.setFieldsValue({
            scope: first.authorizerScope,
            userIds:
              first.authorizerScope === AuthorizationScope.USER
                ? (list
                    .map((item) => item.authorizerId)
                    .filter((id) => !!id) as string[])
                : [],
          });
        } else {
          // 默认公开
          form.setFieldsValue({
            scope: AuthorizationScope.TENANT,
            userIds: [],
          });
        }
      } catch (error) {
        message.error("加载授权信息失败");
      } finally {
        setLoading(false);
      }
    };

    // 保存
    const validateFields: AuthorizeRef["validateFields"] = async () => {
      try {
        const values = await form.validateFields();
        const { scope, userIds } = values;
        const authorizations: AuthItem[] = [];

        if (scope === AuthorizationScope.USER) {
          userIds.forEach((uid) => {
            authorizations.push({
              scope: AuthorizationScope.USER,
              authorizerId: uid,
            });
          });
        } else {
          authorizations.push({
            scope: scope,
            authorizerId: undefined,
          });
        }

        return Promise.resolve(authorizations);
      } catch (error) {
        message.error("保存失败");
        return Promise.reject(error);
      }
    };

    const handleChange = () => {
      try {
        const values = form.getFieldsValue();
        const { scope, userIds } = values;
        const authorizations: AuthItem[] = [];

        if (scope === AuthorizationScope.USER) {
          if (!userIds || userIds.length === 0) {
            message.error("请选择用户");
            return;
          }
          userIds.forEach((uid) => {
            authorizations.push({
              scope: AuthorizationScope.USER,
              authorizerId: uid,
            });
          });
        } else {
          authorizations.push({
            scope: scope,
            authorizerId: undefined,
          });
        }
        onChange?.(authorizations);
      } catch (error) {
        console.error(error);
        return Promise.reject(error);
      }
    };

    useImperativeHandle(ref, () => ({
      validateFields,
    }));

    const scope = Form.useWatch("scope", form);
    const userIds = Form.useWatch("userIds", form);

    useEffect(() => {
      if (scope !== AuthorizationScope.USER) {
        form.setFieldValue("userIds", []);
      }
    }, [scope]);

    return (
      <Spin spinning={loading || submitting}>
        <Form form={form} layout="vertical" onValuesChange={handleChange}>
          <Form.Item label="授权范围" name="scope">
            <Radio.Group
              options={[
                {
                  value: AuthorizationScope.TENANT,
                  label: "仅本租户成员可见",
                },
                ...(publicScopeEnabled
                  ? [
                      {
                        value: AuthorizationScope.PUBLIC,
                        label: "所有人可见",
                      },
                    ]
                  : []),
                {
                  value: AuthorizationScope.USER,
                  label: "仅指定用户可见",
                },
              ]}
              optionType="button"
            />
          </Form.Item>

          {scope === AuthorizationScope.USER && (
            <Form.Item
              label="选择用户"
              name="userIds"
              rules={[
                {
                  validator(_, value) {
                    if (!value || value.length === 0) {
                      return Promise.reject("请选择用户");
                    } else {
                      return Promise.resolve();
                    }
                  },
                  message: "请选择用户",
                },
              ]}
            >
              <Transfer
                dataSource={userOptions.map((user) => ({
                  key: user.id,
                  title: user.displayName || user.username,
                  description: user.username,
                }))}
                titles={["待选用户", "已选用户"]}
                targetKeys={userIds}
                onChange={(nextTargetKeys) =>
                  form.setFieldValue("userIds", nextTargetKeys as string[])
                }
                render={(item) => item.title}
                showSearch
                onSearch={(_, value) => loadUsers(value)}
                filterOption={(inputValue, item) =>
                  item.title.indexOf(inputValue) !== -1 ||
                  item.description?.indexOf(inputValue) !== -1
                }
                styles={{
                  section: {
                    width: 200,
                    height: 400,
                  },
                }}
              />
            </Form.Item>
          )}
        </Form>
      </Spin>
    );
  },
);

export const AuthorizeDrawer: React.FC<AuthorizeDrawerProps> = ({
  open,
  onClose,
  bizType,
  bizId,
  publicScopeEnabled = true,
}) => {
  const [submitting, setSubmitting] = useState(false);
  const ref = useRef<AuthorizeRef>(null);

  // 保存
  const handleSave = async () => {
    try {
      const values = await ref.current?.validateFields();
      if (!values) {
        return;
      }
      setSubmitting(true);

      await batchUpdateAuthorizations({
        bizId,
        bizType,
        authorizations: values,
      });

      message.success("授权保存成功");
      onClose();
    } catch (error) {
      message.error("保存失败");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Drawer
      title="授权配置"
      open={open}
      onClose={onClose}
      size={500}
      extra={
        <Space>
          <Button onClick={onClose}>取消</Button>
          <Button type="primary" onClick={handleSave} loading={submitting}>
            授权
          </Button>
        </Space>
      }
    >
      <Authorize
        ref={ref}
        bizType={bizType}
        bizId={bizId}
        submitting={submitting}
        publicScopeEnabled={publicScopeEnabled}
      />
    </Drawer>
  );
};
