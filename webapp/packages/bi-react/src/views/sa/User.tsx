import QueryButton from "@/components/button/QueryButton";
import DeleteIcon from "@/components/icon/DeleteIcon";
import EditIcon from "@/components/icon/EditIcon";
import RemoteTable from "@/components/table/RemoteTable";
import { EnableStatus } from "@/services/request";
import { Pattern, Validator } from "@/utils";
import { PlusOutlined } from "@ant-design/icons";
import { useMutation } from "@tanstack/react-query";
import { Button, Form, Input, Modal, Radio, Space, message } from "antd";
import type { ColumnsType } from "antd/es/table";
import React, { useMemo, useRef, useState } from "react";
import {
  createUser,
  deleteUser,
  listUsersFromGlobal,
  updateUser,
  type UserCreateRequest,
  type UserQueryRequest,
  type UserResponse,
  type UserUpdateRequest,
} from "./services";
import { Permission } from "@/permission";

const columnsBuilder = (
  onEdit: (record: UserResponse) => void,
  onDelete: (record: UserResponse) => void,
): ColumnsType<UserResponse> => [
  { title: "用户名", dataIndex: "username", key: "username" },
  { title: "昵称", dataIndex: "displayName", key: "displayName" },
  // { title: "部门ID", dataIndex: "departmentId", key: "departmentId" },
  { title: "工号", dataIndex: "employeeNo", key: "employeeNo" },
  {
    title: "状态",
    dataIndex: "status",
    key: "status",
    render: (val: EnableStatus) =>
      val === EnableStatus.ENABLE ? "启用" : "禁用",
  },
  {
    title: "操作",
    key: "action",
    render: (_: any, record: UserResponse) => (
      <Space size="middle">
        <Permission value="sa:user:update">
          <EditIcon
            disabled={record.id === "1"}
            onEdit={() => onEdit(record)}
          />
        </Permission>
        <Permission value="sa:user:delete">
          <DeleteIcon
            disabled={record.id === "1"}
            name="用户"
            onDelete={() => onDelete(record)}
          />
        </Permission>
      </Space>
    ),
  },
];

const QueryBar: React.FC<{
  query: UserQueryRequest;
  onChange: (q: UserQueryRequest) => void;
}> = ({ query, onChange }) => {
  const [form] = Form.useForm<UserQueryRequest>();
  return (
    <Form<UserQueryRequest>
      form={form}
      layout="inline"
      initialValues={{
        username: query.username,
        departmentId: query.departmentId,
        employeeNo: query.employeeNo,
      }}
      onFinish={(values: UserQueryRequest) =>
        onChange({ ...query, ...values, page: 1 })
      }
    >
      <Form.Item name="username" label="用户名">
        <Input placeholder="按用户名筛选" allowClear />
      </Form.Item>
      {/* <Form.Item name="departmentId" label="部门ID">
        <Input placeholder="按部门ID筛选" allowClear />
      </Form.Item> */}
      <Form.Item name="employeeNo" label="工号">
        <Input placeholder="按工号筛选" allowClear />
      </Form.Item>
      <Form.Item>
        <QueryButton />
      </Form.Item>
    </Form>
  );
};

/**
 * 用户管理页面
 */
export const User: React.FC = () => {
  const [query, setQuery] = useState<UserQueryRequest>({ page: 1, size: 10 });
  const [columns, setColumns] = useState<ColumnsType<UserResponse>>([]);
  const [editing, setEditing] = useState<UserResponse | null>(null);
  const [visible, setVisible] = useState(false);
  const [form] = Form.useForm<UserCreateRequest | UserUpdateRequest>();
  const tableRef = useRef<any>(null);

  const refreshTable = () => {
    tableRef.current?.refresh();
  };

  /**
   * 创建用户
   */
  const createMutation = useMutation({
    mutationFn: (payload: UserCreateRequest) => createUser(payload),
    onSuccess: () => {
      message.success("创建成功");
      setVisible(false);
      form.resetFields();
      refreshTable();
    },
  });

  /**
   * 更新用户
   */
  const updateMutation = useMutation({
    mutationFn: (payload: { id: string; data: UserUpdateRequest }) =>
      updateUser(payload.id, payload.data),
    onSuccess: () => {
      message.success("更新成功");
      setVisible(false);
      setEditing(null);
      form.resetFields();
      refreshTable();
    },
  });

  /**
   * 删除用户
   */
  const deleteMutation = useMutation({
    mutationFn: (id: string) => deleteUser(id),
    onSuccess: () => {
      message.success("删除成功");
      refreshTable();
    },
  });

  /**
   * 打开创建弹窗
   */
  const onCreate = () => {
    setEditing(null);
    form.resetFields();
    setVisible(true);
  };

  /**
   * 打开编辑弹窗
   */
  const onEdit = (record: UserResponse) => {
    setEditing(record);
    form.setFieldsValue({
      username: record.username,
      displayName: record.displayName,
      status: record.status,
      departmentId: record.departmentId,
      employeeNo: record.employeeNo,
    } as any);
    setVisible(true);
  };

  /**
   * 删除记录
   */
  const onDelete = (record: UserResponse) => {
    deleteMutation.mutate(record.id);
  };

  /**
   * 提交保存
   */
  const onSubmit = () => {
    form
      .validateFields()
      .then((values: UserCreateRequest | UserUpdateRequest) => {
        if (editing) {
          const data: UserUpdateRequest = {
            username: values.username,
            displayName: values.displayName,
            status: values.status,
            departmentId: values.departmentId,
            employeeNo: values.employeeNo,
            password: values.password,
          };
          updateMutation.mutate({ id: editing.id, data });
        } else {
          const data: UserCreateRequest = {
            username: values.username,
            displayName: values.displayName,
            status: values.status,
            password: values.password as string,
            departmentId: values.departmentId,
            employeeNo: values.employeeNo,
          };
          createMutation.mutate(data);
        }
      })
      .catch(() => {});
  };

  useMemo(() => {
    setColumns(columnsBuilder(onEdit, onDelete));
  }, []);

  return (
    <>
      <RemoteTable<UserResponse>
        ref={tableRef}
        queryBar={<QueryBar query={query} onChange={setQuery} />}
        title="用户管理"
        titleExtra={
          <Permission value="sa:user:create">
            <Button type="primary" onClick={onCreate}>
              <PlusOutlined /> 新建用户
            </Button>
          </Permission>
        }
        fetchKey={["users", query]}
        fetchData={() => listUsersFromGlobal(query)}
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
        title={editing ? "编辑用户" : "新建用户"}
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
        <Form form={form} layout="vertical">
          <Form.Item
            name="username"
            label="用户名"
            rules={[
              {
                required: true,
                message: "请输入用户名",
                pattern: Pattern.username,
              },
            ]}
          >
            <Input placeholder="请输入用户名" />
          </Form.Item>
          <Form.Item name="displayName" label="昵称">
            <Input placeholder="请输入昵称" />
          </Form.Item>
          <Form.Item
            name="status"
            label="状态"
            initialValue={EnableStatus.ENABLE}
          >
            <Radio.Group
              optionType="button"
              options={[
                { label: "启用", value: EnableStatus.ENABLE },
                { label: "禁用", value: EnableStatus.DISABLE },
              ]}
            ></Radio.Group>
          </Form.Item>
          <Form.Item
            name="password"
            label="密码"
            rules={
              editing
                ? []
                : [
                    {
                      required: true,
                      message: "请输入密码",
                      validator: Validator.password,
                    },
                  ]
            }
          >
            <Input.Password placeholder="请输入密码" />
          </Form.Item>
          {/* <Form.Item
            name="departmentId"
            label="部门ID"
            rules={[{ required: true, message: "请输入部门ID" }]}
          >
            <Input placeholder="请输入部门ID" />
          </Form.Item> */}
          <Form.Item name="employeeNo" label="工号">
            <Input placeholder="请输入工号" />
          </Form.Item>
        </Form>
      </Modal>
    </>
  );
};

export default User;
