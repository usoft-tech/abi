import QueryButton from "@/components/button/QueryButton";
import DeleteIcon from "@/components/icon/DeleteIcon";
import EditIcon from "@/components/icon/EditIcon";
import RemoteTable from "@/components/table/RemoteTable";
import { PlusOutlined } from "@ant-design/icons";
import { useMutation } from "@tanstack/react-query";
import { AutoComplete, Button, Form, Input, Modal, Space, message } from "antd";
import type { ColumnsType } from "antd/es/table";
import React, { useEffect, useMemo, useRef, useState } from "react";
import {
  createConfig,
  deleteConfig,
  listConfigs,
  updateConfig,
  type SysConfigCreateRequest,
  type SysConfigQueryRequest,
  type SysConfigResponse,
  type SysConfigUpdateRequest,
} from "./services";
import { Permission } from "@/permission";
import { useDispatch } from "react-redux";
import { reloadConfigAsync } from "@/store/settingSlice";
import { AppDispatch } from "@/store";

const columnsBuilder = (
  onEdit: (record: SysConfigResponse) => void,
  onDelete: (record: SysConfigResponse) => void,
): ColumnsType<SysConfigResponse> => [
  { title: "配置键", dataIndex: "configKey", key: "configKey" },
  { title: "配置值", dataIndex: "configValue", key: "configValue" },
  {
    title: "操作",
    key: "action",
    render: (_: any, record: SysConfigResponse) => (
      <Space size="middle">
        <Permission value="sa:setting:update">
          <EditIcon onEdit={() => onEdit(record)} />
        </Permission>
        <Permission value="sa:setting:delete">
          <DeleteIcon name="配置" onDelete={() => onDelete(record)} />
        </Permission>
      </Space>
    ),
  },
];

const QueryBar: React.FC<{
  query: SysConfigQueryRequest;
  onChange: (q: SysConfigQueryRequest) => void;
}> = ({ query, onChange }) => {
  const [form] = Form.useForm<SysConfigQueryRequest>();
  return (
    <Form<SysConfigQueryRequest>
      form={form}
      layout="inline"
      initialValues={{ configKey: query.configKey, keyword: query.keyword }}
      onFinish={(values: SysConfigQueryRequest) =>
        onChange({ ...query, ...values, page: 1 })
      }
      style={{ marginBottom: 12 }}
    >
      <Form.Item name="configKey" label="配置键">
        <Input placeholder="按配置键筛选" allowClear />
      </Form.Item>
      <Form.Item name="keyword" label="关键字">
        <Input placeholder="按键或值模糊搜索" allowClear />
      </Form.Item>
      <Form.Item>
        <QueryButton />
      </Form.Item>
    </Form>
  );
};

const EditModal: React.FC<{
  open: boolean;
  initial?: SysConfigResponse | null;
  onCancel: () => void;
  onSubmit: (values: SysConfigCreateRequest | SysConfigUpdateRequest) => void;
  loading?: boolean;
}> = ({ open, initial, onCancel, onSubmit, loading }) => {
  const [form] = Form.useForm();
  const isEdit = !!initial?.id;
  useEffect(() => {
    if (open) {
      form.setFieldsValue({
        configKey: initial?.configKey || "",
        configValue: initial?.configValue || "",
      });
    }
  }, [initial, open]);
  return (
    <Modal
      title={isEdit ? "编辑配置" : "新建配置"}
      open={open}
      onCancel={onCancel}
      onOk={() => {
        form.validateFields().then(onSubmit);
      }}
      confirmLoading={loading}
      destroyOnHidden
    >
      <Form
        form={form}
        layout="vertical"
        initialValues={{
          configKey: initial?.configKey,
          configValue: initial?.configValue,
        }}
      >
        <Form.Item
          name="configKey"
          label="配置键"
          rules={[{ required: true, message: "请输入配置键" }]}
        >
          <AutoComplete
            placeholder="例如: site.title"
            options={[
              {
                label: "站点名称（site.name）",
                value: "site.name",
              },
              {
                label: "站点logo（site.logo）",
                value: "site.logo",
              },
              {
                label: "语言切换（language.switch）",
                value: "language.switch",
              },
              {
                label: "主题切换（theme.switch）",
                value: "theme.switch",
              },
              {
                label: "布局切换（layout.switch）",
                value: "layout.switch",
              },
            ]}
          />
        </Form.Item>
        <Form.Item
          name="configValue"
          label="配置值"
          tooltip="布尔值支持选项：true/false、1/0、Y/N、y/n"
          rules={[{ required: true, message: "请输入配置值" }]}
        >
          <Input placeholder="例如: 智数报表" />
        </Form.Item>
      </Form>
    </Modal>
  );
};

const Setting: React.FC = () => {
  const [query, setQuery] = useState<SysConfigQueryRequest>({
    page: 1,
    size: 10,
    configKey: undefined,
    keyword: undefined,
  });
  const [editing, setEditing] = useState<SysConfigResponse | null>(null);
  const [modalOpen, setModalOpen] = useState(false);
  const tableRef = useRef<any>(null);
  const dispatch = useDispatch<AppDispatch>();

  const refreshTable = () => {
    tableRef.current?.refresh();
  };

  const onCreate = () => {
    setEditing(null);
    setModalOpen(true);
  };

  const createMut = useMutation({
    mutationFn: createConfig,
    onSuccess: () => {
      message.success("创建成功");
      setModalOpen(false);
      setEditing(null);
      refreshTable();
      dispatch(reloadConfigAsync());
    },
    onError: (err: any) => message.error(err?.message || "创建失败"),
  });

  const updateMut = useMutation({
    mutationFn: ({
      id,
      payload,
    }: {
      id: string;
      payload: SysConfigUpdateRequest;
    }) => updateConfig(id, payload),
    onSuccess: () => {
      message.success("更新成功");
      setModalOpen(false);
      setEditing(null);
      refreshTable();
      dispatch(reloadConfigAsync());
    },
    onError: (err: any) => message.error(err?.message || "更新失败"),
  });

  const deleteMut = useMutation({
    mutationFn: deleteConfig,
    onSuccess: () => {
      message.success("删除成功");
      refreshTable();
    },
    onError: (err: any) => message.error(err?.message || "删除失败"),
  });

  const columns = useMemo(
    () =>
      columnsBuilder(
        (record) => {
          setEditing(record);
          setModalOpen(true);
        },
        (record) => {
          deleteMut.mutate(record.id);
        },
      ),
    [],
  );

  return (
    <>
      <RemoteTable<SysConfigResponse>
        ref={tableRef}
        queryBar={<QueryBar query={query} onChange={setQuery} />}
        title="系统配置"
        titleExtra={
          <Permission value="sa:setting:create">
            <Button type="primary" onClick={onCreate}>
              <PlusOutlined /> 新建配置
            </Button>
          </Permission>
        }
        fetchKey={["sysConfigs", query]}
        fetchData={() => listConfigs(query)}
        pagination={{
          current: query.page,
          pageSize: query.size,
          onChange: (page, size) => setQuery({ ...query, page, size }),
        }}
        selection={false}
        rowKey="id"
        columns={columns}
      />

      <EditModal
        open={modalOpen}
        initial={editing}
        onCancel={() => {
          setModalOpen(false);
          setEditing(null);
        }}
        loading={createMut.isPending || updateMut.isPending}
        onSubmit={(values) => {
          if (editing?.id) {
            updateMut.mutate({
              id: editing.id,
              payload: values as SysConfigUpdateRequest,
            });
          } else {
            createMut.mutate(values as SysConfigCreateRequest);
          }
        }}
      />
    </>
  );
};

export default Setting;
