import { SectionTitle } from "@/components/header/Header";
import { AuthItem, AuthorizationBizType } from "@/services/authorization";
import {
  ShareLinkCreateRequest,
  ShareLinkExpireType,
  ShareLinkResponse,
} from "@/services/share";
import { useMutation } from "@tanstack/react-query";
import {
  DatePicker,
  Drawer,
  Form,
  message,
  Select,
  Space,
  Switch,
  Typography,
} from "antd";
import dayjs from "dayjs";
import { useEffect, useRef, useState } from "react";
import {
  createPageShareLink,
  deletePageShareLink,
  getPageShareLink,
} from "../services";
import { Authorize, AuthorizeRef } from "./AuthorizeDrawer";

interface ShareDrawerProps {
  baseUrl: string;
  bizType: AuthorizationBizType;
  bizId: string;
  open: boolean;
  onClose: () => void;
}

export const ShareDrawer = ({
  baseUrl,
  bizType,
  bizId,
  open,
  onClose,
}: ShareDrawerProps) => {
  const ref = useRef<AuthorizeRef>(null);
  const [form] = Form.useForm<ShareLinkCreateRequest>();
  const [shareLink, setShareLink] = useState<ShareLinkResponse>();

  const expireType = Form.useWatch("expireType", form);

  const createMutation = useMutation({
    mutationFn: (payload: ShareLinkCreateRequest) =>
      createPageShareLink(payload),
    onSuccess: ({ data }) => {
      setShareLink(data);
      message.success("分享链接成功");
    },
  });
  const deleteMutation = useMutation({
    mutationFn: (id: string) => deletePageShareLink(id),
    onSuccess: () => {
      setShareLink(undefined);
      message.success("取消分享成功");
    },
  });

  const handleSwitch = (checked: boolean) => {
    if (!checked) {
      deleteMutation.mutate(bizId);
    } else {
      createMutation.mutate({
        bizType,
        bizId,
        expireType: ShareLinkExpireType.Permanent,
        authorizations: [],
      });
    }
  };

  const handleUpdate = async (
    values: ShareLinkCreateRequest,
    authorizations?: AuthItem[],
  ) => {
    createMutation.mutate({
      ...values,
      bizType,
      bizId,
      authorizations:
        authorizations || (await ref.current?.validateFields()) || [],
    });
  };

  useEffect(() => {
    if (!open) {
      return;
    }
    getPageShareLink(bizId).then(({ data }) => {
      setShareLink(data);
      form.setFieldsValue({
        ...data,
        expireType: data.expireAt ? ShareLinkExpireType.Custom : ShareLinkExpireType.Permanent,
        expireAt: data.expireAt ? dayjs(data.expireAt) : undefined,
      });
    });
  }, [open, bizId]);

  return (
    <Drawer title="分享" open={open} onClose={onClose} size={500}>
      <SectionTitle
        title="分享"
        extra={
          <Switch
            value={!!shareLink}
            onChange={handleSwitch}
            checkedChildren="取消分享"
            unCheckedChildren="分享链接"
          />
        }
      />
      <Form
        form={form}
        layout="vertical"
        onValuesChange={(changedValues, values) => {
          if (changedValues.expireType === ShareLinkExpireType.Custom && !values.expireAt) {
            values.expireAt = dayjs().add(7, "day");
            form.setFieldsValue({ expireAt: values.expireAt });
          }
          handleUpdate(values)
        }}
      >
        <Form.Item>
          <Typography.Text copyable={!!shareLink} disabled={!shareLink}>
            {baseUrl}/{shareLink?.shareKey || "********"}
          </Typography.Text>
        </Form.Item>
        {shareLink && (
          <>
            <Form.Item label="链接有效期" noStyle style={{ marginTop: 24 }} />
            <Space.Compact>
              <Form.Item name="expireType" noStyle>
                <Select
                  options={[
                    {
                      label: "永久有效",
                      value: ShareLinkExpireType.Permanent,
                    },
                    {
                      label: "1天",
                      value: ShareLinkExpireType.OneDay,
                    },
                    {
                      label: "7天",
                      value: ShareLinkExpireType.SevenDay,
                    },
                    {
                      label: "30天",
                      value: ShareLinkExpireType.ThirtyDay,
                    },
                    {
                      label: "自定义有效期",
                      value: ShareLinkExpireType.Custom,
                    },
                  ]}
                  defaultValue={ShareLinkExpireType.Permanent}
                  style={{ width: 140 }}
                />
              </Form.Item>
              {expireType === ShareLinkExpireType.Custom && (
                <Form.Item name="expireAt" noStyle>
                  <DatePicker minDate={dayjs()} format="YYYY-MM-DD" />
                </Form.Item>
              )}
            </Space.Compact>
          </>
        )}
      </Form>
      <SectionTitle title="授权范围" />
      {shareLink && (
        <Authorize
          ref={ref}
          bizType={AuthorizationBizType.SHARE}
          bizId={shareLink.shareKey}
          submitting={createMutation.isPending || deleteMutation.isPending}
          onChange={(authorizations) =>
            handleUpdate(form.getFieldsValue(), authorizations)
          }
        />
      )}
    </Drawer>
  );
};
