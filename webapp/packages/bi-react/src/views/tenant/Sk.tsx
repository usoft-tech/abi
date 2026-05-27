import DeleteIcon from "@/components/icon/DeleteIcon";
import EditIcon from "@/components/icon/EditIcon";
import RemoteTable from "@/components/table/RemoteTable";
import { KeyOutlined, PlusOutlined } from "@ant-design/icons";
import { useMutation } from "@tanstack/react-query";
import {
  Button,
  DatePicker,
  Descriptions,
  Form,
  Input,
  Modal,
  Popconfirm,
  Space,
  Tooltip,
  Typography,
  message,
} from "antd";
import type { ColumnsType } from "antd/es/table";
import dayjs, { Dayjs } from "dayjs";
import React, { useMemo, useRef, useState } from "react";
import {
  createSysAuthClient,
  deleteSysAuthClient,
  listSysAuthClients,
  refreshSysAuthClientSecret,
  updateSysAuthClient,
  type SysAuthClientCreateRequest,
  type SysAuthClientQueryRequest,
  type SysAuthClientResponse,
  type SysAuthClientUpdateRequest,
} from "./services";
import { Permission } from "@/permission";

type SysAuthClientFormValues = {
  name?: string;
  description?: string;
  expiredAt?: Dayjs;
};

const Sk: React.FC = () => {
  const [query, setQuery] = useState<SysAuthClientQueryRequest>({
    page: 1,
    size: 10,
  });
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<SysAuthClientResponse | null>(null);
  const [form] = Form.useForm<SysAuthClientFormValues>();
  const tableRef = useRef<{ refresh: () => void } | null>(null);

  const createMut = useMutation({
    mutationFn: (payload: SysAuthClientCreateRequest) =>
      createSysAuthClient(payload),
    onSuccess: (res) => {
      const data = res.data;
      message.success("创建成功");
      setModalOpen(false);
      setEditing(null);
      tableRef.current?.refresh();
      if (data) {
        Modal.info({
          title: "客户端密钥已生成",
          content: (
            <Descriptions
              column={1}
              layout="vertical"
              items={[
                {
                  key: "clientId",
                  label: "客户端ID",
                  children: (
                    <Typography.Text copyable>{data.clientId}</Typography.Text>
                  ),
                },
                {
                  key: "clientSecret",
                  label: "客户端密钥",
                  children: (
                    <Typography.Text copyable>
                      {data.clientSecret}
                    </Typography.Text>
                  ),
                },
                {
                  key: "tip",
                  children: (
                    <Typography.Paragraph type="danger">
                      请妥善保管本次显示的客户端密钥，该密钥只会显示一次。
                    </Typography.Paragraph>
                  ),
                },
              ]}
            />
          ),
        });
      }
    },
    onError: (err: any) => message.error(err?.message || "创建失败"),
  });

  const updateMut = useMutation({
    mutationFn: ({
      clientId,
      payload,
    }: {
      clientId: string;
      payload: SysAuthClientUpdateRequest;
    }) => updateSysAuthClient(clientId, payload),
    onSuccess: () => {
      message.success("更新成功");
      setModalOpen(false);
      setEditing(null);
      tableRef.current?.refresh();
    },
    onError: (err: any) => message.error(err?.message || "更新失败"),
  });

  const deleteMut = useMutation({
    mutationFn: deleteSysAuthClient,
    onSuccess: () => {
      message.success("删除成功");
      tableRef.current?.refresh();
    },
    onError: (err: any) => message.error(err?.message || "删除失败"),
  });

  const refreshMut = useMutation({
    mutationFn: refreshSysAuthClientSecret,
    onSuccess: (res) => {
      const data = res.data;
      if (data) {
        Modal.info({
          title: "客户端密钥已刷新",
          content: (
            <Descriptions
              column={1}
              layout="vertical"
              items={[
                {
                  key: "clientId",
                  label: "客户端ID",
                  children: (
                    <Typography.Text copyable>{data.clientId}</Typography.Text>
                  ),
                },
                {
                  key: "clientSecret",
                  label: "客户端密钥",
                  children: (
                    <Typography.Text copyable>
                      {data.clientSecret}
                    </Typography.Text>
                  ),
                },
                {
                  key: "tip",
                  children: (
                    <Typography.Paragraph type="danger">
                      请妥善保管本次显示的客户端密钥，该密钥只会显示一次。
                    </Typography.Paragraph>
                  ),
                },
              ]}
            />
          ),
        });
      }
      tableRef.current?.refresh();
    },
    onError: (err: any) => message.error(err?.message || "刷新失败"),
  });

  const columns: ColumnsType<SysAuthClientResponse> = useMemo(
    () => [
      {
        title: "客户端ID",
        dataIndex: "clientId",
        key: "clientId",
        width: 100,
        render: (text: string) => (
          <Typography.Text
            copyable={true}
            ellipsis={{
              tooltip: {
                overlay: text,
                styles: {
                  container: {
                    width: 260,
                  },
                },
              },
            }}
          >
            {text}
          </Typography.Text>
        ),
      },
      {
        title: "名称",
        dataIndex: "name",
        key: "name",
      },
      {
        title: "描述",
        dataIndex: "description",
        key: "description",
      },
      {
        title: "过期时间",
        dataIndex: "expiredAt",
        key: "expiredAt",
        width: 150,
        align: "center",
        render: (text?: string) =>
          text ? dayjs(text).format("YYYY-MM-DD HH:mm:ss") : "-",
      },
      {
        title: "操作",
        key: "action",
        width: 100,
        align: "center",
        render: (_, record) => (
          <Space>
            <Permission value={["tenant:sa:sk:update"]}>
              <EditIcon onEdit={() => handleEdit(record)} />
            </Permission>
            <Permission value={["tenant:sa:sk:refresh-secret"]}>
              <Popconfirm
                title={`确定刷新该${record.name || "数据"}的密钥吗?`}
                onConfirm={() => handleRefresh(record.clientId)}
                okText="确定"
                cancelText="取消"
              >
                <Button
                  type="link"
                  loading={refreshMut.isPending}
                  disabled={refreshMut.isPending}
                >
                  <Tooltip title="刷新密钥">
                    <KeyOutlined />
                  </Tooltip>
                </Button>
              </Popconfirm>
            </Permission>
            <Permission value={["tenant:sa:sk:delete"]}>
              <DeleteIcon
                loading={deleteMut.isPending}
                onDelete={() => handleDelete(record.clientId)}
              />
            </Permission>
          </Space>
        ),
      },
    ],
    [deleteMut.isPending, refreshMut.isPending],
  );

  const handleAdd = () => {
    setEditing(null);
    setModalOpen(true);
    form.resetFields();
  };

  const handleEdit = (record: SysAuthClientResponse) => {
    setEditing(record);
    setModalOpen(true);
    form.setFieldsValue({
      name: record.name,
      description: record.description,
      expiredAt: record.expiredAt ? dayjs(record.expiredAt) : undefined,
    });
  };

  const handleDelete = (clientId: string) => {
    Modal.confirm({
      title: "确认删除",
      content: "删除后该客户端将无法再使用，是否继续？",
      onOk: () => deleteMut.mutate(clientId),
    });
  };

  const handleRefresh = (clientId: string) => {
    refreshMut.mutate(clientId);
  };

  const handleSubmit = async () => {
    const values = await form.validateFields();
    const payload: SysAuthClientCreateRequest | SysAuthClientUpdateRequest = {
      name: values.name,
      description: values.description,
      expiredAt: values.expiredAt
        ? (values.expiredAt as Dayjs).toISOString()
        : undefined,
    };
    if (editing) {
      updateMut.mutate({ clientId: editing.clientId, payload });
    } else {
      createMut.mutate(payload as SysAuthClientCreateRequest);
    }
  };

  return (
    <>
      <div
        style={{
          marginBottom: 12,
          display: "flex",
          justifyContent: "space-between",
        }}
      >
        <Input.Search
          placeholder="按名称或ID搜索"
          allowClear
          style={{ width: 260 }}
          onSearch={(val) =>
            setQuery({
              ...query,
              keyword: val,
              page: 1,
            })
          }
        />
        <Permission value={["tenant:sa:sk:create"]}>
          <Button type="primary" onClick={handleAdd}>
            <PlusOutlined /> 新建客户端
          </Button>
        </Permission>
      </div>

      <RemoteTable<SysAuthClientResponse>
        ref={tableRef}
        title="密钥管理"
        fetchKey={["sys_auth_clients", query]}
        fetchData={() => listSysAuthClients(query)}
        pagination={{
          current: query.page,
          pageSize: query.size,
          onChange: (page, size) =>
            setQuery({
              ...query,
              page,
              size,
            }),
        }}
        selection={false}
        rowKey="clientId"
        columns={columns}
      />

      <Modal
        title={editing ? "编辑客户端" : "新建客户端"}
        open={modalOpen}
        onCancel={() => setModalOpen(false)}
        onOk={handleSubmit}
        confirmLoading={createMut.isPending || updateMut.isPending}
        destroyOnClose
      >
        <Form<SysAuthClientFormValues> form={form} layout="vertical">
          <Form.Item
            name="name"
            label="名称"
            rules={[{ required: true, message: "请输入名称" }]}
          >
            <Input placeholder="请输入客户端名称" />
          </Form.Item>
          <Form.Item name="description" label="描述">
            <Input.TextArea rows={3} placeholder="请输入描述信息" />
          </Form.Item>
          <Form.Item name="expiredAt" label="过期时间">
            <DatePicker
              showTime
              style={{ width: "100%" }}
              placeholder="请选择过期时间"
            />
          </Form.Item>
        </Form>
      </Modal>
    </>
  );
};

export default Sk;
