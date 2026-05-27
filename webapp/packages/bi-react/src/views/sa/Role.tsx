import QueryButton from "@/components/button/QueryButton";
import DeleteIcon from "@/components/icon/DeleteIcon";
import EditIcon from "@/components/icon/EditIcon";
import RemoteTable from "@/components/table/RemoteTable";
import { KeyOutlined, PlusOutlined, UserOutlined } from "@ant-design/icons";
import { useMutation } from "@tanstack/react-query";
import {
  Button,
  Drawer,
  Form,
  Input,
  Modal,
  Space,
  Tooltip,
  message,
} from "antd";
import type { ColumnsType } from "antd/es/table";
import React, { useMemo, useRef, useState } from "react";
import { PermissionTree } from "./Permission";
import {
  addRoleUsers,
  assignRolePermissions,
  createRole,
  deleteRole,
  listRolePermissions,
  listRoleUsers,
  listRoles,
  listUsers,
  removeRoleUser,
  updateRole,
  type RoleCreateRequest,
  type RoleQueryRequest,
  type RoleResponse,
  type RoleUpdateRequest,
  type UserQueryRequest,
  type UserResponse,
} from "./services";
import { Permission } from "@/permission";

const columnsBuilder = (
  onEdit: (record: RoleResponse) => void,
  onDelete: (record: RoleResponse) => void,
  onAuth: (record: RoleResponse) => void,
  onManageMembers: (record: RoleResponse) => void,
): ColumnsType<RoleResponse> => [
  { title: "角色名称", dataIndex: "name", key: "name" },
  { title: "角色标识", dataIndex: "code", key: "code" },
  {
    title: "操作",
    key: "action",
    render: (_: any, record: RoleResponse) => (
      <Space size="middle">
        <Permission value="sa:role:update">
          <Tooltip title="成员管理">
            <Button type="link" onClick={() => onManageMembers(record)}>
              <UserOutlined />
            </Button>
          </Tooltip>
        </Permission>
        <Permission value="sa:role:update">
          <Tooltip title="授权">
            <Button type="link" onClick={() => onAuth(record)}>
              <KeyOutlined />
            </Button>
          </Tooltip>
        </Permission>
        <Permission value="sa:role:update">
          <EditIcon onEdit={() => onEdit(record)} />
        </Permission>
        <Permission value="sa:role:delete">
          <DeleteIcon name="角色" onDelete={() => onDelete(record)} />
        </Permission>
      </Space>
    ),
  },
];

const QueryBar: React.FC<{
  query: RoleQueryRequest;
  onChange: (q: RoleQueryRequest) => void;
}> = ({ query, onChange }) => {
  const [form] = Form.useForm<RoleQueryRequest>();
  return (
    <Form<RoleQueryRequest>
      form={form}
      layout="inline"
      initialValues={{
        name: query.name,
        code: query.code,
        keyword: query.keyword,
      }}
      onFinish={(values: RoleQueryRequest) =>
        onChange({ ...query, ...values, page: 1 })
      }
      style={{ marginBottom: 12 }}
    >
      <Form.Item name="name" label="名称">
        <Input placeholder="按名称筛选" allowClear />
      </Form.Item>
      <Form.Item name="code" label="标识">
        <Input placeholder="按标识筛选" allowClear />
      </Form.Item>
      <Form.Item>
        <QueryButton />
      </Form.Item>
    </Form>
  );
};

export const Role: React.FC = () => {
  const [query, setQuery] = useState<RoleQueryRequest>({ page: 1, size: 10 });
  const [columns, setColumns] = useState<ColumnsType<RoleResponse>>([]);
  const [editing, setEditing] = useState<RoleResponse | null>(null);
  const [visible, setVisible] = useState(false);
  const [form] = Form.useForm<RoleCreateRequest | RoleUpdateRequest>();
  const tableRef = useRef<any>(null);

  const [authDrawerVisible, setAuthDrawerVisible] = useState(false);
  const [authRole, setAuthRole] = useState<RoleResponse | null>(null);
  const [checkedKeys, setCheckedKeys] = useState<string[]>([]);
  const [saveAuthLoading, setSaveAuthLoading] = useState(false);

  const [memberDrawerVisible, setMemberDrawerVisible] = useState(false);
  const [memberRole, setMemberRole] = useState<RoleResponse | null>(null);

  const [userSelectVisible, setUserSelectVisible] = useState(false);
  const [selectedUserIds, setSelectedUserIds] = useState<React.Key[]>([]);
  const [userQuery, setUserQuery] = useState<UserQueryRequest>({
    page: 1,
    size: 10,
  });

  const [roleUsersQuery, setRoleUsersQuery] = useState<UserQueryRequest>({
    page: 1,
    size: 10,
  });
  const roleUsersTableRef = useRef<any>(null);

  const refreshTable = () => {
    tableRef.current?.refresh();
  };

  const createMutation = useMutation({
    mutationFn: (payload: RoleCreateRequest) => createRole(payload),
    onSuccess: () => {
      message.success("创建成功");
      setVisible(false);
      form.resetFields();
      refreshTable();
    },
  });

  const updateMutation = useMutation({
    mutationFn: (payload: { id: string; data: RoleUpdateRequest }) =>
      updateRole(payload.id, payload.data),
    onSuccess: () => {
      message.success("更新成功");
      setVisible(false);
      setEditing(null);
      form.resetFields();
      refreshTable();
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => deleteRole(id),
    onSuccess: () => {
      message.success("删除成功");
      refreshTable();
    },
  });

  const onCreate = () => {
    setEditing(null);
    form.resetFields();
    setVisible(true);
  };

  const onEdit = (record: RoleResponse) => {
    setEditing(record);
    form.setFieldsValue({
      name: record.name,
      code: record.code,
    } as any);
    setVisible(true);
  };

  const onDelete = (record: RoleResponse) => {
    deleteMutation.mutate(record.id);
  };

  const onAuth = async (record: RoleResponse) => {
    setAuthRole(record);
    setAuthDrawerVisible(true);
    setCheckedKeys([]);
    try {
      const { data } = await listRolePermissions(record.id);
      setCheckedKeys(data);
    } catch (e) {
      console.error(e);
      message.error("加载角色权限失败");
    }
  };

  const handleSaveAuth = async () => {
    if (!authRole) return;
    setSaveAuthLoading(true);
    try {
      await assignRolePermissions(authRole.id, checkedKeys);
      message.success("授权成功");
      setAuthDrawerVisible(false);
    } catch (e) {
      console.error(e);
      message.error("授权失败");
    } finally {
      setSaveAuthLoading(false);
    }
  };

  const onManageMembers = (record: RoleResponse) => {
    setMemberRole(record);
    setMemberDrawerVisible(true);
    roleUsersTableRef.current?.refresh();
  };

  const handleRemoveMember = async (userId: string) => {
    if (!memberRole) return;
    try {
      await removeRoleUser(memberRole.id, userId);
      message.success("移除成功");
      roleUsersTableRef.current?.refresh();
    } catch (e) {
      console.error(e);
      message.error("移除失败");
    }
  };

  const handleAddMembers = async () => {
    if (!memberRole) return;
    try {
      await addRoleUsers(memberRole.id, selectedUserIds as string[]);
      message.success("添加成功");
      setUserSelectVisible(false);
      setSelectedUserIds([]);
      roleUsersTableRef.current?.refresh();
    } catch (e) {
      console.error(e);
      message.error("添加失败");
    }
  };

  const onSubmit = () => {
    form
      .validateFields()
      .then((values: RoleCreateRequest | RoleUpdateRequest) => {
        if (editing) {
          updateMutation.mutate({ id: editing.id, data: values });
        } else {
          createMutation.mutate(values);
        }
      })
      .catch(() => {});
  };

  useMemo(() => {
    setColumns(columnsBuilder(onEdit, onDelete, onAuth, onManageMembers));
  }, []);

  return (
    <>
      <RemoteTable<RoleResponse>
        ref={tableRef}
        queryBar={<QueryBar query={query} onChange={setQuery} />}
        title="角色管理"
        titleExtra={
          <Permission value="sa:role:create">
            <Button type="primary" onClick={onCreate}>
              <PlusOutlined /> 新建角色
            </Button>
          </Permission>
        }
        fetchKey={["roles", query]}
        fetchData={() => listRoles(query)}
        pagination={{
          current: query.page,
          pageSize: query.size,
          onChange: (page, size) => setQuery({ ...query, page, size }),
        }}
        selection={false}
        rowKey="id"
        columns={columns}
      />

      <Modal
        title={editing ? "编辑角色" : "新建角色"}
        open={visible}
        onCancel={() => {
          setVisible(false);
          setEditing(null);
          form.resetFields();
        }}
        onOk={onSubmit}
        okText="保存"
        cancelText="取消"
      >
        <Form form={form} labelCol={{ span: 5 }}>
          <Form.Item
            name="name"
            label="角色名称"
            rules={[{ required: true, message: "请输入角色名称" }]}
          >
            <Input placeholder="请输入角色名称" />
          </Form.Item>
          <Form.Item
            name="code"
            label="角色标识"
            rules={[{ required: true, message: "请输入角色标识" }]}
          >
            <Input placeholder="请输入角色标识" />
          </Form.Item>
        </Form>
      </Modal>

      <Drawer
        title={`角色授权 - ${authRole?.name}`}
        open={authDrawerVisible}
        onClose={() => setAuthDrawerVisible(false)}
        size={500}
        extra={
          <Button
            type="primary"
            onClick={handleSaveAuth}
            loading={saveAuthLoading}
          >
            保存
          </Button>
        }
      >
        <PermissionTree
          checkable
          checkedKeys={checkedKeys}
          onCheck={(keys) => setCheckedKeys(keys)}
        />
      </Drawer>

      <Drawer
        title={`成员管理 - ${memberRole?.name}`}
        size={800}
        open={memberDrawerVisible}
        onClose={() => setMemberDrawerVisible(false)}
      >
        <div style={{ marginBottom: 16 }}>
          <Button type="primary" onClick={() => setUserSelectVisible(true)}>
            <PlusOutlined /> 添加成员
          </Button>
        </div>
        <RemoteTable<UserResponse>
          ref={roleUsersTableRef}
          fetchKey={["roleUsers", memberRole?.id, roleUsersQuery]}
          fetchData={() => {
            if (!memberRole)
              return Promise.resolve({
                code: "OK",
                message: "OK",
                success: true,
                timestamp: Date.now().toLocaleString(),
                data: {
                  items: [],
                  total: 0,
                  page: roleUsersQuery.page,
                  size: roleUsersQuery.size,
                },
              });
            return listRoleUsers(memberRole?.id as string, roleUsersQuery);
          }}
          selection={false}
          pagination={{
            current: roleUsersQuery.page,
            pageSize: roleUsersQuery.size,
            onChange: (page, size) =>
              setRoleUsersQuery({ ...roleUsersQuery, page, size }),
          }}
          rowKey="id"
          columns={[
            { title: "用户名", dataIndex: "username" },
            { title: "姓名", dataIndex: "displayName" },
            { title: "工号", dataIndex: "employeeNo" },
            {
              title: "操作",
              render: (_, record) => (
                <Button
                  type="link"
                  danger
                  onClick={() => handleRemoveMember(record.id)}
                >
                  移除
                </Button>
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
        onOk={handleAddMembers}
      >
        <div style={{ marginBottom: 12 }}>
          <Input.Search
            placeholder="搜索用户"
            onSearch={(val) =>
              setUserQuery({ ...userQuery, keyword: val, page: 1 })
            }
            style={{ width: 300 }}
          />
        </div>
        <RemoteTable<UserResponse>
          fetchKey={["users_select", userQuery]}
          fetchData={() => listUsers(userQuery)}
          pagination={{
            current: userQuery.page,
            pageSize: userQuery.size,
            onChange: (page, size) =>
              setUserQuery({ ...userQuery, page, size }),
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

export default Role;
