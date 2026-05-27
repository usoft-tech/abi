import QueryButton from "@/components/button/QueryButton";
import RemoteTable from "@/components/table/RemoteTable";
import {
  CloudServerOutlined,
  DatabaseOutlined,
  DeleteOutlined,
  EditOutlined,
  MoreOutlined,
  PlusOutlined,
  SyncOutlined,
  TableOutlined,
  LockOutlined,
  LoadingOutlined,
} from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  Button,
  Dropdown,
  Form,
  Input,
  InputNumber,
  Layout,
  MenuProps,
  Modal,
  Radio,
  Select,
  Space,
  Tag,
  Tree,
  message,
} from "antd";
import type { ColumnsType } from "antd/es/table";
import type { DataNode } from "antd/es/tree";
import React, { useEffect, useMemo, useRef, useState } from "react";
import {
  createDataSource,
  deleteDataSource,
  listDataSourceDbs,
  listDataSourceTables,
  listDataSources,
  pageDataSourceFields,
  syncDataSourceSchema,
  testDataSourceConnection,
  updateDataSource,
  updateDataSourceDbStatus,
  updateDataSourceTableStatus,
  updateDataSourceDbDescription,
  updateDataSourceTableDescription,
  updateDataSourceFieldDescription,
  type DatasourceFieldQueryRequest,
  type DatasourceFieldResponse,
  type DataSourceCreateRequest,
  type DataSourceResponse,
  type DataSourceUpdateRequest,
} from "./services";
import styled from "styled-components";
import { Permission, usePermission } from "@/permission";

const { Sider, Content } = Layout;

const dsTypes = [
  {
    type: "ChatBI",
    label: "ChatBI",
    driver: "",
    urlTemplate: "{protocol}://{host}:{port}",
  },
  {
    type: "MySQL",
    label: "MySQL",
    driver: "com.mysql.cj.jdbc.Driver",
    urlTemplate:
      "jdbc:mysql://{host}:{port}/{database}?useUnicode=true&characterEncoding=UTF-8&useSSL=false&allowMultiQueries=true&allowPublicKeyRetrieval=true",
  },
  {
    type: "PostgreSQL",
    label: "PostgreSQL",
    driver: "org.postgresql.Driver",
    urlTemplate: "jdbc:postgresql://{host}:{port}/{database}",
  },
  {
    type: "Oracle",
    label: "Oracle",
    driver: "oracle.jdbc.OracleDriver",
    urlTemplate: "jdbc:oracle:thin:@{host}:{port}:{database}",
  },
  {
    type: "SQLServer",
    label: "SQLServer",
    driver: "com.microsoft.sqlserver.jdbc.SQLServerDriver",
    urlTemplate: "jdbc:sqlserver://{host}:{port};databaseName={database}",
  },
];

const typeSelectOptions = dsTypes.map((item) => ({
  value: item.type,
  label: item.label,
}));

const StyledTree = styled(Tree)`
  .ant-tree-node-content-wrapper {
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
`;

/**
 * 字段查询条件工具条
 */
const FieldQueryBar: React.FC<{
  query: DatasourceFieldQueryRequest;
  onChange: (q: DatasourceFieldQueryRequest) => void;
}> = ({ query, onChange }) => {
  const [form] = Form.useForm<DatasourceFieldQueryRequest>();

  // 当 query.tableId 变化时，重置表单
  useEffect(() => {
    form.setFieldsValue({ name: query.name });
  }, [query.datasourceId, query.dbId, query.tableId, form]);

  return (
    <Form<DatasourceFieldQueryRequest>
      form={form}
      layout="inline"
      initialValues={{ name: query.name }}
      onFinish={(values) => onChange({ ...query, ...values, page: 1 })}
      style={{ marginBottom: 12 }}
    >
      <Form.Item name="name" label="字段名">
        <Input placeholder="按字段名筛选" allowClear />
      </Form.Item>
      <Form.Item>
        <QueryButton />
      </Form.Item>
    </Form>
  );
};

/**
 * 编辑数据源弹窗组件
 * 支持多种数据库类型的连接配置，包括 Oracle 的 SID 和服务名切换
 */
const EditModal: React.FC<{
  open: boolean;
  initial?: DataSourceResponse | null;
  onCancel: () => void;
  onSubmit: (values: DataSourceCreateRequest | DataSourceUpdateRequest) => void;
  onTestConnection: (
    values: DataSourceCreateRequest | DataSourceUpdateRequest,
  ) => void;
  loading?: boolean;
}> = ({ open, initial, onCancel, onSubmit, onTestConnection, loading }) => {
  const [form] = Form.useForm();
  const [dbType, setDbType] = useState(initial?.type || "ChatBI");
  const isEdit = !!initial?.id;

  useEffect(() => {
    if (!open) {
      form.resetFields();
    } else {
      let oracleConnectionType = "SID";
      if (initial?.type === "Oracle" && initial?.url) {
        if (initial.url.includes("@//")) {
          oracleConnectionType = "SERVICE_NAME";
        }
      }

      form.setFieldsValue({
        name: initial?.name,
        type: initial?.type || "ChatBI",
        host: initial?.host,
        port: initial?.port,
        url: initial?.url,
        username: initial?.username,
        databaseName: initial?.databaseName,
        // password: initial?.password, // 密码不回显
        extProps: initial?.extProps,
        oracleConnectionType,
      });
      setDbType(initial?.type || "ChatBI");
    }
  }, [open, form, initial]);

  const dbBaseProps = (
    <>
      <Space>
        <Form.Item
          name="host"
          label="主机地址"
          rules={[{ required: true, message: "请输入主机地址" }]}
          style={{ width: 300 }}
        >
          <Input placeholder="localhost" />
        </Form.Item>
        <Form.Item
          name="port"
          label="端口"
          rules={[{ required: true, message: "请输入端口" }]}
        >
          <InputNumber placeholder="" style={{ width: 120 }} />
        </Form.Item>
      </Space>
      {dbType === "Oracle" && (
        <Form.Item
          name="oracleConnectionType"
          label="连接方式"
          initialValue="SID"
        >
          <Radio.Group>
            <Radio value="SID">SID</Radio>
            <Radio value="SERVICE_NAME">服务名</Radio>
          </Radio.Group>
        </Form.Item>
      )}
      <Form.Item
        noStyle
        shouldUpdate={(prev, curr) =>
          prev.oracleConnectionType !== curr.oracleConnectionType
        }
      >
        {({ getFieldValue }) => {
          const connectionType = getFieldValue("oracleConnectionType");
          const isOracle = dbType === "Oracle";
          const label = isOracle
            ? connectionType === "SERVICE_NAME"
              ? "服务名"
              : "SID"
            : "数据库/服务名";
          const placeholder = isOracle
            ? connectionType === "SERVICE_NAME"
              ? "service_name"
              : "sid"
            : "db_name";

          return (
            <Form.Item
              name="databaseName"
              label={label}
              rules={[{ required: true, message: `请输入${label}` }]}
            >
              <Input placeholder={placeholder} />
            </Form.Item>
          );
        }}
      </Form.Item>
      <Form.Item
        name="username"
        label="用户名"
        rules={[{ required: true, message: "请输入用户名" }]}
      >
        <Input placeholder="root" />
      </Form.Item>
      <Form.Item
        name="password"
        label="密码"
        rules={[{ required: !isEdit, message: "请输入密码" }]}
        extra={isEdit ? "留空则不修改密码" : undefined}
      >
        <Input.Password placeholder="******" />
      </Form.Item>
    </>
  );

  const handleSubmit = (
    callback: (
      values: DataSourceCreateRequest | DataSourceUpdateRequest,
    ) => void,
  ) => {
    form.validateFields().then((values) => {
      const dsType = dsTypes.find((item) => item.type === values.type);
      const driverClassName = dsType?.driver || "";
      let urlTemplate = dsType?.urlTemplate || "";

      // Oracle 特殊处理连接方式
      if (values.type === "Oracle") {
        if (values.oracleConnectionType === "SERVICE_NAME") {
          urlTemplate = "jdbc:oracle:thin:@//{host}:{port}/{database}";
        } else {
          urlTemplate = "jdbc:oracle:thin:@{host}:{port}:{database}";
        }
      }

      let url = urlTemplate
        .replace("{host}", values.host)
        .replace("{port}", values.port.toString())
        .replace("{database}", values.databaseName)
        .replace(
          "{protocol}",
          values.type === "ChatBI" ? values.extProps?.protocol || "https" : "",
        );
      if ([80, 443].includes(values.port)) {
        url = url.replace(values.host + ":" + values.port, values.host);
      }
      callback({
        ...values,
        driverClassName,
        url,
        id: initial?.id || undefined,
      });
    });
  };

  const handleOk = () => {
    handleSubmit(onSubmit);
  };

  const handleTestConnection = () => {
    handleSubmit(onTestConnection);
  };

  return (
    <Modal
      title={isEdit ? "编辑数据源" : "新建数据源"}
      open={open}
      onCancel={onCancel}
      confirmLoading={loading}
      destroyOnHidden
      width={600}
      footer={
        <Space>
          <Button loading={loading} disabled={loading} onClick={onCancel}>
            取消
          </Button>
          <Button
            loading={loading}
            disabled={loading}
            onClick={handleTestConnection}
          >
            测试连接
          </Button>
          <Button
            type="primary"
            loading={loading}
            disabled={loading}
            onClick={handleOk}
          >
            确定
          </Button>
        </Space>
      }
    >
      <Form
        form={form}
        layout="vertical"
        onValuesChange={(_, values) => setDbType(values.type)}
      >
        <Form.Item
          name="name"
          label="名称"
          rules={[{ required: true, message: "请输入数据源名称" }]}
        >
          <Input placeholder="例如: 业务数据库" />
        </Form.Item>
        <Form.Item
          name="type"
          label="类型"
          rules={[{ required: true, message: "请选择数据库类型" }]}
        >
          <Select
            options={typeSelectOptions}
            onChange={(value) => {
              if (value === "ChatBI") {
                form.setFieldsValue({
                  extProps: JSON.stringify({ protocol: "https" }),
                });
              } else if (value === "Oracle") {
                form.setFieldsValue({
                  extProps: "",
                  oracleConnectionType: "SID",
                });
              } else {
                form.setFieldsValue({
                  extProps: "",
                });
              }
            }}
          />
        </Form.Item>
        {dbType !== "ChatBI" ? (
          <>
            {dbBaseProps}
            <Form.Item name="extProps" label="扩展属性">
              <Input.TextArea placeholder="JSON格式的扩展属性" rows={3} />
            </Form.Item>
          </>
        ) : (
          <>
            <Space>
              <Form.Item
                name="extProps"
                label="HTTP协议"
                rules={[{ required: true, message: "请选择HTTP协议" }]}
                style={{ width: 100 }}
                initialValue="https"
              >
                <Select
                  options={[
                    { label: "HTTP", value: "http" },
                    { label: "HTTPS", value: "https" },
                  ]}
                />
              </Form.Item>
              <Form.Item
                name="host"
                label="主机地址"
                rules={[{ required: true, message: "请输入主机地址" }]}
                style={{ width: 300 }}
              >
                <Input placeholder="localhost" />
              </Form.Item>
              <Form.Item
                name="port"
                label="端口"
                rules={[{ required: true, message: "请输入端口" }]}
              >
                <InputNumber placeholder="" style={{ width: 120 }} />
              </Form.Item>
            </Space>
            <Form.Item
              name="password"
              label="密钥"
              rules={[{ required: !isEdit, message: "请输入密钥" }]}
              extra={isEdit ? "留空则不修改密钥" : undefined}
            >
              <Input.Password placeholder="******" />
            </Form.Item>
          </>
        )}
      </Form>
    </Modal>
  );
};

/**
 * 编辑描述弹窗
 */
const EditDescriptionModal: React.FC<{
  open: boolean;
  initialContent?: string;
  title: string;
  onCancel: () => void;
  onSubmit: (content: string) => void;
  loading?: boolean;
}> = ({ open, initialContent, title, onCancel, onSubmit, loading }) => {
  const [form] = Form.useForm();

  useEffect(() => {
    if (open) {
      form.setFieldsValue({ description: initialContent });
    } else {
      form.resetFields();
    }
  }, [open, initialContent, form]);

  const handleOk = () => {
    form.validateFields().then((values) => {
      onSubmit(values.description);
    });
  };

  return (
    <Modal
      title={title}
      open={open}
      onCancel={onCancel}
      confirmLoading={loading}
      onOk={handleOk}
      destroyOnHidden
    >
      <Form form={form} layout="vertical">
        <Form.Item name="description" label="描述">
          <Input.TextArea rows={4} placeholder="请输入描述" />
        </Form.Item>
      </Form>
    </Modal>
  );
};

/**
 * 数据源管理页面
 */
const Datasource: React.FC = () => {
  const queryClient = useQueryClient();
  const [treeData, setTreeData] = useState<DataNode[]>([]);
  const [expandedKeys, setExpandedKeys] = useState<React.Key[]>([]);
  const [treeKeyword, setTreeKeyword] = useState("");
  const didInitExpandRef = useRef(false);
  const [fieldQuery, setFieldQuery] = useState<DatasourceFieldQueryRequest>({
    page: 1,
    size: 10,
  });
  const [syncingKeys, setSyncingKeys] = useState<Set<React.Key>>(new Set());

  const [editing, setEditing] = useState<DataSourceResponse | null>(null);
  const [modalOpen, setModalOpen] = useState(false);
  const [descEditing, setDescEditing] = useState<{
    id: string;
    type: "db" | "table" | "field";
    content?: string;
    title: string;
  } | null>(null);
  const tableRef = useRef<any>(null);

  // 右键菜单状态
  const [contextMenu, setContextMenu] = useState<{
    open: boolean;
    x: number;
    y: number;
    node: any;
  } | null>(null);

  const { hasPermission } = usePermission();

  // 关闭右键菜单
  useEffect(() => {
    const closeMenu = () => setContextMenu(null);
    document.addEventListener("click", closeMenu);
    return () => document.removeEventListener("click", closeMenu);
  }, []);

  // 加载第一层数据源
  const { data: dsList } = useQuery({
    retry: false,
    queryKey: ["datasources", "list"],
    queryFn: () => listDataSources({ page: 1, size: 1000 }),
  });

  // 更新树数据（第一层）
  useEffect(() => {
    if (dsList?.data?.items) {
      setTreeData((prev) => {
        // 保留现有树的展开状态太复杂，这里简化为如果有子节点则保留，或者直接全量更新
        // 为了支持 loadData，我们需要将新数据合并到旧数据中，或者只更新第一层
        // 这里采用：如果是初始化，直接设置；如果是更新，尝试保留子节点
        const newNodes = dsList.data.items.map((ds) => {
          const existingNode = prev.find((n) => n.key === ds.id);
          return {
            title: ds.name,
            key: ds.id,
            icon: <CloudServerOutlined />,
            isLeaf: false,
            data: { ...ds, nodeType: "datasource" },
            children: existingNode?.children,
          };
        });
        return newNodes;
      });

      if (!didInitExpandRef.current) {
        didInitExpandRef.current = true;
        setExpandedKeys(dsList.data.items.map((ds) => ds.id));
      }
    }
  }, [dsList]);

  // 异步加载子节点
  const onLoadData = ({ key, children, data }: any) =>
    new Promise<void>(async (resolve) => {
      if (children && children.length > 0) {
        resolve();
        return;
      }

      try {
        if (data.nodeType === "datasource") {
          const { data: dbs } = await listDataSourceDbs(key);
          setTreeData((origin) =>
            updateTreeData(
              origin,
              key,
              dbs.map((db) => ({
                title: db.name,
                key: db.id,
                icon: <DatabaseOutlined />,
                isLeaf: false,
                data: { ...db, nodeType: "db" },
              })),
            ),
          );
        } else if (data.nodeType === "db") {
          const { data: tables } = await listDataSourceTables(key);
          setTreeData((origin) =>
            updateTreeData(
              origin,
              key,
              tables.map((t) => ({
                title: t.name,
                key: t.id,
                icon: <TableOutlined />,
                isLeaf: true,
                data: { ...t, nodeType: "table" },
              })),
            ),
          );
        }
      } catch (e) {
        message.error("加载失败");
      }
      resolve();
    });

  const filteredTreeData = useMemo(() => {
    const kw = treeKeyword.trim().toLowerCase();
    if (!kw) return treeData;

    const filterNodes = (nodes: DataNode[]): DataNode[] => {
      const result: DataNode[] = [];
      for (const node of nodes) {
        const titleText = String((node as any).title ?? "").toLowerCase();
        const children = node.children ? filterNodes(node.children) : undefined;
        if (titleText.includes(kw) || (children && children.length > 0)) {
          result.push({ ...node, children });
        }
      }
      return result;
    };

    return filterNodes(treeData);
  }, [treeData, treeKeyword]);

  // 选择节点
  const onSelect = (selectedKeys: React.Key[], info: any) => {
    const nodeType = info?.node?.data?.nodeType;
    const data = info?.node?.data;

    if (nodeType === "datasource") {
      setFieldQuery((prev) => ({
        ...prev,
        datasourceId: data.id,
        dbId: undefined,
        tableId: undefined,
        page: 1,
      }));
      return;
    }

    if (nodeType === "db") {
      setFieldQuery((prev) => ({
        ...prev,
        datasourceId: data.datasourceId,
        dbId: data.id,
        tableId: undefined,
        page: 1,
      }));
      return;
    }

    if (nodeType === "table") {
      setFieldQuery((prev) => ({
        ...prev,
        datasourceId: data.datasourceId,
        dbId: data.dbId,
        tableId: data.id,
        page: 1,
      }));
    }
  };

  // 数据源操作 mutations
  const createMut = useMutation({
    mutationFn: createDataSource,
    onSuccess: () => {
      message.success("创建成功");
      setModalOpen(false);
      setEditing(null);
      queryClient.invalidateQueries({ queryKey: ["datasources", "list"] });
    },
    onError: (err: any) => message.error(err?.message || "创建失败"),
  });

  const updateMut = useMutation({
    mutationFn: ({
      id,
      payload,
    }: {
      id: string;
      payload: DataSourceUpdateRequest;
    }) => updateDataSource(id, payload),
    onSuccess: () => {
      message.success("更新成功");
      setModalOpen(false);
      setEditing(null);
      queryClient.invalidateQueries({ queryKey: ["datasources", "list"] });
    },
    onError: (err: any) => message.error(err?.message || "更新失败"),
  });

  const deleteMut = useMutation({
    mutationFn: deleteDataSource,
    onSuccess: () => {
      message.success("删除成功");
      queryClient.invalidateQueries({ queryKey: ["datasources", "list"] });
    },
    onError: (err: any) => message.error(err?.message || "删除失败"),
  });

  const syncMut = useMutation({
    mutationFn: syncDataSourceSchema,
    onMutate: (id) => {
      setSyncingKeys((prev) => {
        const next = new Set(prev);
        next.add(id);
        return next;
      });
    },
    onSettled: (_, __, id) => {
      setSyncingKeys((prev) => {
        const next = new Set(prev);
        next.delete(id);
        return next;
      });
    },
    onSuccess: async (_, id) => {
      message.success("同步触发成功");
      try {
        const { data: dbs } = await listDataSourceDbs(id);
        setTreeData((origin) =>
          updateTreeData(
            origin,
            id,
            dbs.map((db) => ({
              title: db.name,
              key: db.id,
              icon: <DatabaseOutlined />,
              isLeaf: false,
              data: { ...db, nodeType: "db" },
            })),
          ),
        );
      } catch (e) {
        console.error(e);
      }
    },
    onError: (err: any) => message.error(err?.message || "同步失败"),
  });

  const testConnectionMut = useMutation({
    mutationFn: ({
      id,
      payload,
    }: {
      id: string;
      payload: DataSourceUpdateRequest | DataSourceCreateRequest;
    }) => testDataSourceConnection(id, payload),
    onSuccess: ({data}) => {
      if (data) {
        message.success("测试连接成功")
      } else {
        message.error("测试连接失败")
      }
    },
    onError: (err: any) => message.error(err?.message || "测试连接失败"),
  });

  const updateDbStatusMut = useMutation({
    mutationFn: ({ id, status }: { id: string; status: string }) =>
      updateDataSourceDbStatus(id, status),
    onSuccess: (_, { id, status }) => {
      message.success(status === "ENABLE" ? "已启用" : "已停用");
      setTreeData((origin) =>
        updateTreeData(origin, id, undefined, { status }),
      );
    },
    onError: (err: any) => message.error(err?.message || "操作失败"),
  });

  const updateTableStatusMut = useMutation({
    mutationFn: ({ id, status }: { id: string; status: string }) =>
      updateDataSourceTableStatus(id, status),
    onSuccess: (_, { id, status }) => {
      message.success(status === "ENABLE" ? "已启用" : "已停用");
      setTreeData((origin) =>
        updateTreeData(origin, id, undefined, { status }),
      );
    },
    onError: (err: any) => message.error(err?.message || "操作失败"),
  });

  const updateDbDescMut = useMutation({
    mutationFn: ({ id, description }: { id: string; description: string }) =>
      updateDataSourceDbDescription(id, description),
    onSuccess: (_, { id, description }) => {
      message.success("更新成功");
      setDescEditing(null);
      setTreeData((origin) =>
        updateTreeData(origin, id, undefined, { description }),
      );
    },
    onError: (err: any) => message.error(err?.message || "更新失败"),
  });

  const updateTableDescMut = useMutation({
    mutationFn: ({ id, description }: { id: string; description: string }) =>
      updateDataSourceTableDescription(id, description),
    onSuccess: (_, { id, description }) => {
      message.success("更新成功");
      setDescEditing(null);
      setTreeData((origin) =>
        updateTreeData(origin, id, undefined, { description }),
      );
    },
    onError: (err: any) => message.error(err?.message || "更新失败"),
  });

  const updateFieldDescMut = useMutation({
    mutationFn: ({ id, description }: { id: string; description: string }) =>
      updateDataSourceFieldDescription(id, description),
    onSuccess: () => {
      message.success("更新成功");
      setDescEditing(null);
      if (tableRef.current) {
        tableRef.current.refresh();
      }
    },
    onError: (err: any) => message.error(err?.message || "更新失败"),
  });

  // 更新树数据通用方法
  const updateTreeData = (
    list: (DataNode & { data?: any })[],
    key: React.Key,
    children?: (DataNode & { data?: any })[],
    partialData?: any,
  ): (DataNode & { data?: any })[] =>
    list.map((node) => {
      if (node.key === key) {
        return {
          ...node,
          children: children !== undefined ? children : node.children,
          data: partialData ? { ...node.data, ...partialData } : node.data, // 更新 data 中的状态
        };
      }
      if (node.children) {
        return {
          ...node,
          children: updateTreeData(node.children, key, children, partialData),
        };
      }
      return node;
    });

  // 字段表格列
  const fieldColumns = useMemo<ColumnsType<DatasourceFieldResponse>>(
    () => [
      { title: "字段名", dataIndex: "name", key: "name" },
      {
        title: "描述",
        dataIndex: "description",
        key: "description",
        render: (desc, record) => (
          <Space>
            {desc}
            <Permission value="tenant:sa:datasource:update">
              <Button
                type="text"
                icon={<EditOutlined />}
                size="small"
                onClick={() =>
                  setDescEditing({
                    id: record.id,
                    type: "field",
                    content: desc,
                    title: `修改字段描述: ${record.name}`,
                  })
                }
              />
            </Permission>
          </Space>
        ),
      },
      { title: "类型", dataIndex: "type", key: "type", width: 120 },
      {
        title: "状态",
        dataIndex: "status",
        key: "status",
        width: 80,
        align: "center",
        render: (status) => (
          <Tag color={status === "ENABLE" ? "success" : "default"}>
            {status === "ENABLE" ? "启用" : "停用"}
          </Tag>
        ),
      },
    ],
    [],
  );

  // 节点渲染
  const titleRender = (nodeData: any) => {
    const { title, key, data } = nodeData;
    const isDisabled =
      data.status !== "ENABLE" && data.nodeType !== "datasource";
    const isSyncing = syncingKeys.has(key);

    // 通用样式处理
    const titleContent = (
      <span style={{ color: isDisabled ? "#999" : "inherit" }}>
        {isDisabled && <LockOutlined style={{ marginRight: 4 }} />}
        {title}
        {isSyncing && <LoadingOutlined style={{ marginLeft: 8 }} spin />}
      </span>
    );

    if (data.nodeType === "datasource") {
      const menuItems = [
        hasPermission("tenant:sa:datasource:update")
          ? {
              key: "edit",
              label: "编辑",
              icon: <EditOutlined />,
              onClick: (e: any) => {
                e.domEvent.stopPropagation();
                setEditing(data);
                setModalOpen(true);
              },
            }
          : null,
        hasPermission("tenant:sa:datasource:update")
          ? {
              key: "sync",
              label: "同步",
              icon: <SyncOutlined />,
              onClick: (e: any) => {
                e.domEvent.stopPropagation();
                syncMut.mutate(key);
              },
            }
          : null,
        hasPermission("tenant:sa:datasource:update")
          ? {
              key: "delete",
              label: "删除",
              icon: <DeleteOutlined />,
              danger: true,
              onClick: (e: any) => {
                e.domEvent.stopPropagation();
                Modal.confirm({
                  title: "确认删除",
                  content: `确定要删除数据源 "${title}" 吗？`,
                  onOk: () => deleteMut.mutate(key),
                });
              },
            }
          : null,
      ].filter(Boolean) as MenuProps["items"];

      return (
        <div
          className="group inline-flex items-center justify-between"
          style={{ width: "calc(100% - 24px)" }}
        >
          {titleContent}
          <Dropdown menu={{ items: menuItems }} trigger={["click"]}>
            <Button
              type="text"
              size="small"
              icon={<MoreOutlined />}
              onClick={(e) => e.stopPropagation()}
            />
          </Dropdown>
          <EditDescriptionModal
            open={!!descEditing}
            title={descEditing?.title || "修改描述"}
            initialContent={descEditing?.content}
            onCancel={() => setDescEditing(null)}
            loading={
              updateDbDescMut.isPending ||
              updateTableDescMut.isPending ||
              updateFieldDescMut.isPending
            }
            onSubmit={(content) => {
              if (!descEditing) return;
              if (descEditing.type === "db") {
                updateDbDescMut.mutate({
                  id: descEditing.id,
                  description: content,
                });
              } else if (descEditing.type === "table") {
                updateTableDescMut.mutate({
                  id: descEditing.id,
                  description: content,
                });
              } else if (descEditing.type === "field") {
                updateFieldDescMut.mutate({
                  id: descEditing.id,
                  description: content,
                });
              }
            }}
          />
        </div>
      );
    }
    return titleContent;
  };

  const handleRightClick = ({ event, node }: any) => {
    const data = node.data;
    if (data.nodeType === "db" || data.nodeType === "table") {
      setContextMenu({
        open: true,
        x: event.clientX,
        y: event.clientY,
        node: node,
      });
    }
  };

  return (
    <div style={{ height: "calc(100% + 40px)", margin: -20 }}>
      <Layout style={{ height: "100%", background: "#fff" }}>
        <Sider
          width={300}
          theme="light"
          style={{ borderRight: "1px solid #f0f0f0", overflow: "auto" }}
        >
          <div
            style={{
              padding: "16px",
              borderBottom: "1px solid #f0f0f0",
              display: "flex",
              justifyContent: "space-between",
              alignItems: "center",
            }}
          >
            <span style={{ fontWeight: 500 }}>数据源列表</span>
            <Permission value="tenant:sa:datasource:create">
              <Button
                type="primary"
                size="small"
                icon={<PlusOutlined />}
                onClick={() => {
                  setEditing(null);
                  setModalOpen(true);
                }}
              >
                新建
              </Button>
            </Permission>
          </div>
          <div
            style={{ padding: "12px 16px", borderBottom: "1px solid #f0f0f0" }}
          >
            <Input
              allowClear
              placeholder="搜索数据源/库/表"
              value={treeKeyword}
              onChange={(e) => setTreeKeyword(e.target.value)}
            />
          </div>
          <StyledTree
            treeData={filteredTreeData}
            loadData={onLoadData}
            onSelect={onSelect}
            titleRender={titleRender}
            blockNode
            showIcon
            showLine
            expandedKeys={expandedKeys}
            onExpand={(keys) => setExpandedKeys(keys)}
            onRightClick={handleRightClick}
            style={{ padding: "8px" }}
          />
          {contextMenu && (
            <div
              className="ant-dropdown-menu ant-dropdown-menu-light"
              style={{
                position: "fixed",
                top: contextMenu.y,
                left: contextMenu.x,
                zIndex: 1050,
                boxShadow:
                  "0 3px 6px -4px rgba(0, 0, 0, 0.12), 0 6px 16px 0 rgba(0, 0, 0, 0.08), 0 9px 28px 8px rgba(0, 0, 0, 0.05)",
                borderRadius: 8,
                padding: 4,
                backgroundColor: "#fff",
                minWidth: 120,
              }}
              onClick={(e) => e.stopPropagation()}
            >
              <Permission value="tenant:sa:datasource:update">
                <div
                  className="ant-dropdown-menu-item"
                  style={{
                    padding: "5px 12px",
                    cursor: "pointer",
                    transition: "all 0.3s",
                  }}
                  onMouseEnter={(e) =>
                    (e.currentTarget.style.backgroundColor =
                      "rgba(0, 0, 0, 0.04)")
                  }
                  onMouseLeave={(e) =>
                    (e.currentTarget.style.backgroundColor = "transparent")
                  }
                  onClick={() => {
                    const { node } = contextMenu;
                    setDescEditing({
                      id: node.key as string,
                      type: node.data.nodeType,
                      content: node.data.description,
                      title: `修改描述: ${node.title}`,
                    });
                    setContextMenu(null);
                  }}
                >
                  修改描述
                </div>
              </Permission>
              <Permission value="tenant:sa:datasource:update">
                <div
                  className="ant-dropdown-menu-item"
                  style={{
                    padding: "5px 12px",
                    cursor: "pointer",
                    transition: "all 0.3s",
                  }}
                  onMouseEnter={(e) =>
                    (e.currentTarget.style.backgroundColor =
                      "rgba(0, 0, 0, 0.04)")
                  }
                  onMouseLeave={(e) =>
                    (e.currentTarget.style.backgroundColor = "transparent")
                  }
                  onClick={() => {
                    const { node } = contextMenu;
                    const newStatus =
                      node.data.status === "ENABLE" ? "DISABLE" : "ENABLE";
                    if (node.data.nodeType === "db") {
                      updateDbStatusMut.mutate({
                        id: node.key,
                        status: newStatus,
                      });
                    } else if (node.data.nodeType === "table") {
                      updateTableStatusMut.mutate({
                        id: node.key,
                        status: newStatus,
                      });
                    }
                    setContextMenu(null);
                  }}
                >
                  {contextMenu.node.data.status === "ENABLE" ? "停用" : "启用"}
                </div>
              </Permission>
            </div>
          )}
        </Sider>
        <Content style={{ padding: "16px", overflow: "auto" }}>
          <RemoteTable<DatasourceFieldResponse>
            ref={tableRef}
            queryBar={
              <FieldQueryBar query={fieldQuery} onChange={setFieldQuery} />
            }
            title="字段列表"
            fetchKey={["datasource-fields", fieldQuery]}
            fetchData={() => pageDataSourceFields(fieldQuery)}
            pagination={{
              current: fieldQuery.page,
              pageSize: fieldQuery.size,
              onChange: (page, size) =>
                setFieldQuery({ ...fieldQuery, page, size }),
            }}
            rowKey="id"
            columns={fieldColumns}
            selection={false}
          />
        </Content>
      </Layout>

      <EditModal
        open={modalOpen}
        initial={editing}
        onCancel={() => {
          setModalOpen(false);
          setEditing(null);
        }}
        loading={
          createMut.isPending ||
          updateMut.isPending ||
          testConnectionMut.isPending
        }
        onSubmit={(values) => {
          if (editing?.id) {
            updateMut.mutate({
              id: editing.id,
              payload: values as DataSourceUpdateRequest,
            });
          } else {
            createMut.mutate(values as DataSourceCreateRequest);
          }
        }}
        onTestConnection={(values) => {
          testConnectionMut.mutate({
            id: editing?.id || "",
            payload: values as
              | DataSourceCreateRequest
              | DataSourceUpdateRequest,
          });
        }}
      />

      <EditDescriptionModal
        open={!!descEditing}
        title={descEditing?.title || "修改描述"}
        initialContent={descEditing?.content}
        onCancel={() => setDescEditing(null)}
        loading={
          updateDbDescMut.isPending ||
          updateTableDescMut.isPending ||
          updateFieldDescMut.isPending
        }
        onSubmit={(content) => {
          if (!descEditing) return;
          if (descEditing.type === "db") {
            updateDbDescMut.mutate({
              id: descEditing.id,
              description: content,
            });
          } else if (descEditing.type === "table") {
            updateTableDescMut.mutate({
              id: descEditing.id,
              description: content,
            });
          } else if (descEditing.type === "field") {
            updateFieldDescMut.mutate({
              id: descEditing.id,
              description: content,
            });
          }
        }}
      />
    </div>
  );
};

export default Datasource;
