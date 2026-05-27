import RemoteTable from "@/components/table/RemoteTable";
import { PlusOutlined } from "@ant-design/icons";
import { useMutation } from "@tanstack/react-query";
import { Button, Input, Modal, Space, Tag, message } from "antd";
import type { ColumnsType } from "antd/es/table";
import React, { useMemo, useRef, useState } from "react";
import {
  addTenantUsers,
  listTenantUsers,
  listUsersFromGlobal,
  removeTenantUser,
  type UserQueryRequest,
  type UserResponse,
} from "./services";
import { TenantAuthKey, TenantUserResponse } from "../sa/services";
import { Permission } from "@/permission";

/**
 * 租户侧用户管理页面
 */
export const User: React.FC = () => {
  const [query, setQuery] = useState<UserQueryRequest>({ page: 1, size: 10 });
  const [userSelectVisible, setUserSelectVisible] = useState(false);
  const [userSelectQuery, setUserSelectQuery] = useState<UserQueryRequest>({
    page: 1,
    size: 10,
  });
  const [selectedUserIds, setSelectedUserIds] = useState<React.Key[]>([]);
  const tableRef = useRef<{ refresh: () => void } | null>(null);

  const removeUserMut = useMutation({
    mutationFn: (userId: string) => removeTenantUser(userId),
    onSuccess: () => {
      message.success("移除成功");
      tableRef.current?.refresh();
    },
  });

  const addUsersMut = useMutation({
    mutationFn: (userIds: string[]) => addTenantUsers(userIds),
    onSuccess: () => {
      message.success("添加成功");
      setUserSelectVisible(false);
      setSelectedUserIds([]);
      tableRef.current?.refresh();
    },
  });

  const handleRemoveUser = (userId: string) => {
    Modal.confirm({
      title: "确认移除",
      content: "确认从当前租户移除此用户？",
      onOk: () => removeUserMut.mutate(userId),
    });
  };

  const handleAddUsers = () => {
    if (!selectedUserIds.length) {
      message.warning("请先选择要添加的用户");
      return;
    }
    addUsersMut.mutate(selectedUserIds as string[]);
  };

  const columns: ColumnsType<TenantUserResponse> = useMemo(
    () => [
      { title: "用户名", dataIndex: "username", key: "username" },
      { title: "姓名", dataIndex: "displayName", key: "displayName" },
      { title: "工号", dataIndex: "employeeNo", key: "employeeNo" },
      {
        title: "角色",
        dataIndex: "authKey",
        key: "authKey",
        render: (authKey: TenantAuthKey) =>
          authKey === TenantAuthKey.ADMIN ? (
            <Tag color="red">管理员</Tag>
          ) : (
            <Tag color="green">普通用户</Tag>
          ),
      },
      {
        title: "操作",
        key: "action",
        render: (_, record) => (
          <Space>
            <Permission value={["tenant:sa:users:remove"]}>
              <Button
                type="link"
                danger
                onClick={() => handleRemoveUser(record.id)}
                loading={removeUserMut.isPending}
              >
                移除
              </Button>
            </Permission>
          </Space>
        ),
      },
    ],
    [removeUserMut.isPending],
  );

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
          placeholder="搜索用户"
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
        <Permission value={["tenant:sa:users:add"]}>
          <Button type="primary" onClick={() => setUserSelectVisible(true)}>
            <PlusOutlined /> 添加用户
          </Button>
        </Permission>
      </div>

      <RemoteTable<TenantUserResponse>
        ref={tableRef}
        title="用户管理"
        fetchKey={["tenant_users_local", query]}
        fetchData={() => listTenantUsers(query)}
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
        rowKey="id"
        columns={columns}
      />

      <Modal
        title="选择用户"
        width={800}
        open={userSelectVisible}
        onCancel={() => setUserSelectVisible(false)}
        onOk={handleAddUsers}
        confirmLoading={addUsersMut.isPending}
      >
        <div style={{ marginBottom: 12 }}>
          <Input.Search
            placeholder="搜索用户"
            allowClear
            style={{ width: 300 }}
            onSearch={(val) =>
              setUserSelectQuery({
                ...userSelectQuery,
                keyword: val,
                page: 1,
              })
            }
          />
        </div>
        <RemoteTable<UserResponse>
          fetchKey={["tenant_users_select_local", userSelectQuery]}
          fetchData={() => listUsersFromGlobal(userSelectQuery)}
          pagination={{
            current: userSelectQuery.page,
            pageSize: userSelectQuery.size,
            onChange: (page, size) =>
              setUserSelectQuery({
                ...userSelectQuery,
                page,
                size,
              }),
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

export default User;
