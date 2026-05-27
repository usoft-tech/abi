import QueryButton from "@/components/button/QueryButton";
import DeleteIcon from "@/components/icon/DeleteIcon";
import EditIcon from "@/components/icon/EditIcon";
import RemoteTable from "@/components/table/RemoteTable";
import CropUpload from "@/components/upload/CropUpload";
import { EnableStatus } from "@/services/request";
import { PlusOutlined, UserOutlined } from "@ant-design/icons";
import { useMutation } from "@tanstack/react-query";
import {
  Button,
  Drawer,
  Form,
  Input,
  Modal,
  Radio,
  Select,
  Space,
  Tooltip,
  message,
} from "antd";
import type { ColumnsType } from "antd/es/table";
import React, {
  useCallback,
  useEffect,
  useMemo,
  useRef,
  useState,
} from "react";
import {
  TenantAuthKey,
  TenantUserResponse,
  addTenantUsers,
  createTenant,
  deleteTenant,
  listTenantUsers,
  listTenants,
  listUsersFromGlobal,
  removeTenantUser,
  updateTenant,
  updateTenantUserAuthKey,
  type TenantCreateRequest,
  type TenantQueryRequest,
  type TenantResponse,
  type TenantUpdateRequest,
  type UserQueryRequest,
  type UserResponse,
} from "./services";
import { Permission } from "@/permission";

/**
 * 构建表格列
 */
const columnsBuilder = (
  onEdit: (record: TenantResponse) => void,
  onDelete: (record: TenantResponse) => void,
  onManageUsers: (record: TenantResponse) => void,
): ColumnsType<TenantResponse> => [
  { title: "租户编码", dataIndex: "code", key: "code" },
  { title: "租户名称", dataIndex: "name", key: "name" },
  {
    title: "状态",
    dataIndex: "status",
    key: "status",
    render: (status: EnableStatus) => (
      <span>{status === EnableStatus.ENABLE ? "启用" : "停用"}</span>
    ),
  },
  {
    title: "操作",
    key: "action",
    width: 50,
    align: "center",
    render: (_: any, record: TenantResponse) => (
      <Space size="middle">
        <Permission value="sa:tenant:users">
          <Tooltip title="用户管理">
            <Button type="link" onClick={() => onManageUsers(record)}>
              <UserOutlined />
            </Button>
          </Tooltip>
        </Permission>
        <Permission value="sa:tenant:update">
          <EditIcon onEdit={() => onEdit(record)} />
        </Permission>
        <Permission value="sa:tenant:delete">
          <DeleteIcon name="租户" onDelete={() => onDelete(record)} />
        </Permission>
      </Space>
    ),
  },
];

/**
 * 查询条件工具条
 */
const QueryBar: React.FC<{
  query: TenantQueryRequest;
  onChange: (q: TenantQueryRequest) => void;
}> = ({ query, onChange }) => {
  const [form] = Form.useForm<TenantQueryRequest>();
  return (
    <Form<TenantQueryRequest>
      form={form}
      layout="inline"
      initialValues={{
        code: query.code,
        name: query.name,
        status: query.status,
        keyword: query.keyword,
      }}
      onFinish={(values: TenantQueryRequest) =>
        onChange({ ...query, ...values, page: 1 })
      }
      style={{ marginBottom: 12 }}
    >
      <Form.Item name="name" label="名称">
        <Input placeholder="按名称筛选" allowClear />
      </Form.Item>
      <Form.Item name="status" label="状态">
        <Select
          allowClear
          placeholder="请选择状态"
          options={[
            { value: EnableStatus.ENABLE, label: "启用" },
            { value: EnableStatus.DISABLE, label: "停用" },
          ]}
          style={{ width: 140 }}
        />
      </Form.Item>
      <Form.Item>
        <QueryButton />
      </Form.Item>
    </Form>
  );
};

/**
 * 编辑弹窗
 */
const EditModal: React.FC<{
  open: boolean;
  initial?: TenantResponse | null;
  onCancel: () => void;
  onSubmit: (values: TenantCreateRequest | TenantUpdateRequest) => void;
  loading?: boolean;
}> = ({ open, initial, onCancel, onSubmit, loading }) => {
  const [form] = Form.useForm();
  const isEdit = !!initial?.id;
  useEffect(() => {
    if (!open) {
      form.resetFields();
    } else {
      form.setFieldsValue({
        code: initial?.code,
        name: initial?.name,
        status: initial?.status || EnableStatus.ENABLE,
        cover: initial?.cover,
        description: initial?.description,
        contactUser: initial?.contactUser,
        contactPhone: initial?.contactPhone,
      });
    }
  }, [open, form]);
  return (
    <Modal
      title={isEdit ? "编辑租户" : "新建租户"}
      open={open}
      onCancel={onCancel}
      onOk={() => {
        form.validateFields().then(onSubmit);
      }}
      confirmLoading={loading}
      destroyOnHidden
    >
      <Form form={form} layout="vertical">
        <Form.Item
          name="code"
          label="租户编码"
          rules={[{ required: true, message: "请输入租户编码" }]}
        >
          <Input placeholder="例如: t001" />
        </Form.Item>
        <Form.Item
          name="name"
          label="租户名称"
          rules={[{ required: true, message: "请输入租户名称" }]}
        >
          <Input placeholder="例如: 某某公司" />
        </Form.Item>
        <Form.Item
          name="status"
          label="状态"
          rules={[{ required: true, message: "请选择状态" }]}
        >
          <Radio.Group
            optionType="button"
            options={[
              { value: EnableStatus.ENABLE, label: "启用" },
              { value: EnableStatus.DISABLE, label: "停用" },
            ]}
          />
        </Form.Item>
        <Form.Item name="cover" label="封面">
          <CropUpload
            bizType="cover"
            maxCount={1}
            aspectSlider={false}
            aspect={1.78}
            fileList={initial?.cover ? [initial.cover] : []}
            onChange={(fileList) => {
              form.setFieldValue(
                "cover",
                fileList?.length ? fileList[0].url : null,
              );
            }}
          />
        </Form.Item>
        <Form.Item name="description" label="描述">
          <Input.TextArea placeholder="请输入租户描述" rows={3} />
        </Form.Item>
        <Form.Item name="contactUser" label="联系人">
          <Input placeholder="例如: 张三" />
        </Form.Item>
        <Form.Item
          name="contactPhone"
          label="联系电话"
          rules={[
            {
              pattern: /^[0-9+\-\s()]{6,20}$/,
              message: "请输入有效的联系电话",
            },
          ]}
        >
          <Input placeholder="例如: 13800000000 或 010-88888888" />
        </Form.Item>
      </Form>
    </Modal>
  );
};

/**
 * 租户管理页面
 */
const Tenant: React.FC = () => {
  const [query, setQuery] = useState<TenantQueryRequest>({
    page: 1,
    size: 10,
    code: undefined,
    name: undefined,
    status: undefined,
    keyword: undefined,
  });
  const [editing, setEditing] = useState<TenantResponse | null>(null);
  const [modalOpen, setModalOpen] = useState(false);
  const tableRef = useRef<any>(null);
  const [userDrawerOpen, setUserDrawerOpen] = useState(false);
  const [userSelectVisible, setUserSelectVisible] = useState(false);
  const [currentTenant, setCurrentTenant] = useState<TenantResponse | null>(
    null,
  );
  const [tenantUserQuery, setTenantUserQuery] = useState<UserQueryRequest>({
    page: 1,
    size: 10,
  });
  const [userSelectQuery, setUserSelectQuery] = useState<UserQueryRequest>({
    page: 1,
    size: 10,
  });
  const [selectedUserIds, setSelectedUserIds] = useState<React.Key[]>([]);
  const tenantUsersTableRef = useRef<any>(null);

  const refreshTable = () => {
    tableRef.current?.refresh();
  };

  const onManageUsers = useCallback((tenant: TenantResponse) => {
    setCurrentTenant(tenant);
    setUserDrawerOpen(true);
    setTenantUserQuery((prev) => ({ ...prev, page: 1 }));
  }, []);

  const onCreate = () => {
    setEditing(null);
    setModalOpen(true);
  };

  const createMut = useMutation({
    mutationFn: createTenant,
    onSuccess: () => {
      message.success("创建成功");
      setModalOpen(false);
      setEditing(null);
      refreshTable();
    },
    onError: (err: any) => message.error(err?.message || "创建失败"),
  });

  const updateMut = useMutation({
    mutationFn: ({
      id,
      payload,
    }: {
      id: string;
      payload: TenantUpdateRequest;
    }) => updateTenant(id, payload),
    onSuccess: () => {
      message.success("更新成功");
      setModalOpen(false);
      setEditing(null);
      refreshTable();
    },
    onError: (err: any) => message.error(err?.message || "更新失败"),
  });

  const deleteMut = useMutation({
    mutationFn: deleteTenant,
    onSuccess: () => {
      message.success("删除成功");
      refreshTable();
    },
    onError: (err: any) => message.error(err?.message || "删除失败"),
  });

  const handleRemoveTenantUser = async (userId: string) => {
    if (!currentTenant) return;
    try {
      await removeTenantUser(currentTenant.id, userId);
      message.success("移除成功");
      tenantUsersTableRef.current?.refresh();
    } catch (e) {
      message.error("移除失败");
    }
  };

  const handleAddTenantUsers = async () => {
    if (!currentTenant) return;
    if (!selectedUserIds.length) {
      setUserSelectVisible(false);
      return;
    }
    try {
      await addTenantUsers(currentTenant.id, selectedUserIds as string[]);
      message.success("添加成功");
      setUserSelectVisible(false);
      setSelectedUserIds([]);
      tenantUsersTableRef.current?.refresh();
    } catch (e) {
      message.error("添加失败");
    }
  };

  const handleUpdateTenantUserAuthKey = async (
    userId: string,
    authKey: TenantAuthKey,
  ) => {
    if (!currentTenant) return;
    try {
      await updateTenantUserAuthKey(currentTenant.id, userId, authKey);
      message.success("更新成功");
      tenantUsersTableRef.current?.refresh();
    } catch (e) {
      message.error("更新失败");
    }
  };

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
        (record) => {
          onManageUsers(record);
        },
      ),
    [deleteMut, onManageUsers],
  );

  return (
    <>
      <RemoteTable<TenantResponse>
        ref={tableRef}
        queryBar={<QueryBar query={query} onChange={setQuery} />}
        title="租户管理"
        titleExtra={
          <Permission value="sa:tenant:create">
            <Button type="primary" onClick={onCreate}>
              <PlusOutlined /> 新建租户
            </Button>
          </Permission>
        }
        fetchKey={["tenants", query]}
        fetchData={() => listTenants(query)}
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
              payload: values as TenantUpdateRequest,
            });
          } else {
            createMut.mutate(values as TenantCreateRequest);
          }
        }}
      />

      <Drawer
        title={`用户管理 - ${currentTenant?.name || ""}`}
        size={800}
        open={userDrawerOpen}
        onClose={() => setUserDrawerOpen(false)}
      >
        <div style={{ marginBottom: 16 }}>
          <Permission value="sa:tenant:users:add">
            <Button type="primary" onClick={() => setUserSelectVisible(true)}>
              <PlusOutlined /> 添加用户
            </Button>
          </Permission>
        </div>
        <RemoteTable<TenantUserResponse>
          ref={tenantUsersTableRef}
          fetchKey={["tenantUsers", currentTenant?.id, tenantUserQuery]}
          fetchData={() => {
            if (!currentTenant) {
              return Promise.resolve({
                code: "OK",
                message: "OK",
                success: true,
                timestamp: Date.now().toString(),
                data: {
                  items: [],
                  total: 0,
                  page: tenantUserQuery.page,
                  size: tenantUserQuery.size,
                },
              });
            }
            return listTenantUsers(currentTenant.id, tenantUserQuery);
          }}
          selection={false}
          pagination={{
            current: tenantUserQuery.page,
            pageSize: tenantUserQuery.size,
            onChange: (page, size) =>
              setTenantUserQuery({ ...tenantUserQuery, page, size }),
          }}
          rowKey="id"
          columns={[
            { title: "用户名", dataIndex: "username" },
            { title: "昵称", dataIndex: "displayName" },
            { title: "工号", dataIndex: "employeeNo" },
            {
              title: "角色",
              dataIndex: "authKey",
              render: (authKey: TenantAuthKey, record: TenantUserResponse) => (
                <Permission value="sa:tenant:users:update">
                  <Select
                    size="small"
                    options={[
                      { label: "管理员", value: TenantAuthKey.ADMIN },
                      { label: "普通用户", value: TenantAuthKey.USER },
                    ]}
                    value={authKey}
                    onChange={(val) =>
                      handleUpdateTenantUserAuthKey(record.id!, val)
                    }
                    style={{ width: 100 }}
                  />
                </Permission>
              ),
            },
            {
              title: "操作",
              render: (_, record) => (
                <Permission value="sa:tenant:users:remove">
                  <Button
                    type="link"
                    danger
                    onClick={() => handleRemoveTenantUser(record.id)}
                  >
                    移除
                  </Button>
                </Permission>
              ),
            },
          ]}
        />
      </Drawer>

      <Modal
        title="选择用户"
        width={800}
        open={userSelectVisible}
        onCancel={() => setUserSelectVisible(false)}
        onOk={handleAddTenantUsers}
      >
        <div style={{ marginBottom: 12 }}>
          <Input.Search
            placeholder="搜索用户"
            onSearch={(val) =>
              setUserSelectQuery({ ...userSelectQuery, keyword: val, page: 1 })
            }
            style={{ width: 300 }}
          />
        </div>
        <RemoteTable<UserResponse>
          fetchKey={["tenant_users_select", userSelectQuery]}
          fetchData={() => listUsersFromGlobal(userSelectQuery)}
          pagination={{
            current: userSelectQuery.page,
            pageSize: userSelectQuery.size,
            onChange: (page, size) =>
              setUserSelectQuery({ ...userSelectQuery, page, size }),
          }}
          rowKey="id"
          columns={[
            { title: "用户名", dataIndex: "username" },
            { title: "姓名", dataIndex: "displayName" },
            { title: "工号", dataIndex: "employeeNo" },
          ]}
          selection={{
            type: "checkbox",
            selectedRowKeys: selectedUserIds,
            onChange: (keys) => setSelectedUserIds(keys),
          }}
        />
      </Modal>
    </>
  );
};

export default Tenant;
