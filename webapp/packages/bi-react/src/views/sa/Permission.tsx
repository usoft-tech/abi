import QueryButton from "@/components/button/QueryButton";
import DeleteIcon from "@/components/icon/DeleteIcon";
import EditIcon from "@/components/icon/EditIcon";
import StrongTable from "@/components/table/StrongTable";
import { Permission as PermissionComponent } from "@/permission";
import {
  MinusSquareOutlined,
  PlusOutlined,
  PlusSquareOutlined,
} from "@ant-design/icons";
import {
  Button,
  Checkbox,
  Flex,
  Form,
  Input,
  InputNumber,
  message,
  Modal,
  Radio,
  Space,
  Spin,
  Tooltip,
  Tree,
  TreeSelect,
} from "antd";
import type { ColumnsType } from "antd/es/table";
import { BasicDataNode, DataNode } from "antd/es/tree";
import React, { useEffect, useMemo, useState } from "react";
import {
  createPermission,
  deletePermission,
  listPermissions,
  PermissionQueryRequest,
  PermissionType,
  updatePermission,
  type PermissionCreateRequest,
  type PermissionResponse,
  type PermissionUpdateRequest,
} from "./services";
import { CheckboxChangeEvent } from "antd/lib";

interface TreeDataType extends PermissionResponse, BasicDataNode, DataNode {
  children?: TreeDataType[];
}

/**
 * 列表转树
 */
const listToTree = (list: PermissionResponse[]): TreeDataType[] => {
  const map: Record<string, TreeDataType> = {};
  const roots: TreeDataType[] = [];

  list.forEach((item) => {
    map[item.id] = { ...item, key: item.id, title: item.name, children: [] };
  });

  list.forEach((item) => {
    const node = map[item.id];
    if (item.parentId && map[item.parentId]) {
      map[item.parentId].children?.push(node);
    } else {
      roots.push(node);
    }
  });

  console.log(list.filter(i => !i.parentId))

  // Clean up empty children arrays
  const cleanChildren = (nodes: PermissionResponse[]) => {
    nodes.forEach((node) => {
      if (node.children && node.children.length === 0) {
        delete node.children;
      } else if (node.children) {
        cleanChildren(node.children);
      }
    });
  };
  cleanChildren(roots);

  return roots;
};

/**
 * 编辑弹窗
 */
const EditModal: React.FC<{
  open: boolean;
  initial?: PermissionResponse | null;
  treeData: PermissionResponse[];
  onOk: (values: PermissionCreateRequest | PermissionUpdateRequest) => void;
  onCancel: () => void;
  loading?: boolean;
}> = ({ open, initial, treeData, onOk, onCancel, loading }) => {
  const [form] = Form.useForm();

  useEffect(() => {
    if (open) {
      if (initial) {
        form.setFieldsValue(initial);
      } else {
        form.resetFields();
        form.setFieldsValue({ type: PermissionType.PERM, sort: 0 });
      }
    }
  }, [open, initial, form]);

  const handleOk = () => {
    form.validateFields().then((values) => {
      onOk(values);
    });
  };

  // TreeSelect data needs to be formatted
  const formatTreeSelectData = (nodes: PermissionResponse[]): any[] => {
    return nodes
      .filter((node) => node.type === PermissionType.GROUP)
      .map((node) => ({
        title: node.name,
        value: node.id,
        children: node.children ? formatTreeSelectData(node.children) : [],
      }));
  };

  const treeSelectData = useMemo(
    () => formatTreeSelectData(treeData),
    [treeData],
  );

  return (
    <Modal
      title={initial ? "编辑权限" : "新建权限"}
      open={open}
      onOk={handleOk}
      onCancel={onCancel}
      confirmLoading={loading}
    >
      <Form
        form={form}
        layout="vertical"
        initialValues={{ type: PermissionType.GROUP, sort: 0 }}
      >
        <Form.Item name="type" hidden>
          <Input />
        </Form.Item>
        <Form.Item name="parentId" label="上级权限">
          <TreeSelect
            treeData={treeSelectData}
            allowClear
            placeholder="请选择上级权限（留空为根节点）"
            treeDefaultExpandAll
          />
        </Form.Item>
        <Form.Item
          name="name"
          label="权限名称"
          rules={[{ required: true, message: "请输入权限名称" }]}
        >
          <Input placeholder="请输入权限名称" />
        </Form.Item>
        <Form.Item
          name="code"
          label="权限编码"
          rules={[{ required: true, message: "请输入权限编码" }]}
        >
          <Input placeholder="请输入权限编码" disabled={initial?.builtIn} />
        </Form.Item>
        <Form.Item
          name="type"
          label="类型"
          rules={[{ required: true, message: "请选择类型" }]}
        >
          <Radio.Group
            optionType="button"
            options={[
              { label: "分组", value: PermissionType.GROUP },
              { label: "权限", value: PermissionType.PERM },
            ]}
          />
        </Form.Item>
        <Form.Item name="description" label="描述">
          <Input.TextArea placeholder="请输入描述" />
        </Form.Item>
        <Form.Item name="sort" label="排序">
          <InputNumber style={{ width: "100%" }} />
        </Form.Item>
      </Form>
    </Modal>
  );
};

const QueryBar: React.FC<{
  query: PermissionQueryRequest;
  onChange: (q: PermissionQueryRequest) => void;
}> = ({ query, onChange }) => {
  const [form] = Form.useForm<PermissionQueryRequest>();
  return (
    <Form<PermissionQueryRequest>
      form={form}
      layout="inline"
      initialValues={{
        keyword: query.keyword,
      }}
      onFinish={(values: PermissionQueryRequest) =>
        onChange({ ...query, ...values })
      }
      style={{ marginBottom: 12 }}
    >
      <Form.Item name="keyword" label="关键字">
        <Input placeholder="按键或值模糊搜索" allowClear />
      </Form.Item>
      <Form.Item>
        <QueryButton />
      </Form.Item>
    </Form>
  );
};

const Permission = () => {
  const [loading, setLoading] = useState(false);
  const [treeData, setTreeData] = useState<PermissionResponse[]>([]);
  const [query, setQuery] = useState<PermissionQueryRequest>({ keyword: "" });

  const [modalOpen, setModalOpen] = useState(false);
  const [editingRecord, setEditingRecord] = useState<PermissionResponse | null>(
    null,
  );
  const [submitLoading, setSubmitLoading] = useState(false);

  const loadData = async () => {
    setLoading(true);
    try {
      const { data: list } = await listPermissions(query);
      // res is PermissionResponse[] (flat list)
      const tree = listToTree(list);
      setTreeData(tree);
    } catch (error) {
      console.error(error);
      message.error("加载权限列表失败");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [query]);

  const handleCreate = async (values: PermissionCreateRequest) => {
    setSubmitLoading(true);
    try {
      await createPermission(values);
      message.success("创建成功");
      setModalOpen(false);
      loadData();
    } catch (error) {
      console.error(error);
      message.error("创建失败");
    } finally {
      setSubmitLoading(false);
    }
  };

  const handleUpdate = async (values: PermissionUpdateRequest) => {
    if (!editingRecord) return;
    setSubmitLoading(true);
    try {
      await updatePermission(editingRecord.id, values);
      message.success("更新成功");
      setModalOpen(false);
      setEditingRecord(null);
      loadData();
    } catch (error) {
      console.error(error);
      message.error("更新失败");
    } finally {
      setSubmitLoading(false);
    }
  };

  const handleDelete = async (record: PermissionResponse) => {
    try {
      await deletePermission(record.id);
      message.success("删除成功");
      loadData();
    } catch (error) {
      console.error(error);
      message.error("删除失败");
    }
  };

  const columns: ColumnsType<PermissionResponse> = [
    { title: "权限名称", dataIndex: "name", key: "name" },
    { title: "权限编码", dataIndex: "code", key: "code" },
    {
      title: "类型",
      dataIndex: "type",
      key: "type",
      width: 100,
      render: (text) => (text === PermissionType.GROUP ? "分组" : "权限"),
    },
    { title: "描述", dataIndex: "description", key: "description" },
    { title: "排序", dataIndex: "sort", key: "sort", width: 80 },
    {
      title: "操作",
      key: "action",
      width: 120,
      align: "center",
      render: (_: any, record: PermissionResponse) => (
        <Space size="middle">
          <PermissionComponent value="sa:permission:update">
            <EditIcon
              onEdit={() => {
                setEditingRecord(record);
                setModalOpen(true);
              }}
            />
          </PermissionComponent>
          <PermissionComponent value="sa:permission:delete">
            <DeleteIcon name="权限" onDelete={() => handleDelete(record)} />
          </PermissionComponent>
        </Space>
      ),
    },
  ];

  return (
    <>
      <StrongTable<PermissionResponse>
        title="权限管理"
        titleExtra={
          <PermissionComponent value="sa:permission:create">
            <Button
              type="primary"
              icon={<PlusOutlined />}
              onClick={() => {
                setEditingRecord(null);
                setModalOpen(true);
              }}
            >
              新建权限
            </Button>
          </PermissionComponent>
        }
        showIndex={false}
        queryBar={<QueryBar query={query} onChange={setQuery} />}
        columns={columns}
        dataSource={treeData}
        rowKey="id"
        loading={loading}
        pagination={false}
        selection={false}
        expandable={{
          defaultExpandAllRows: true,
        }}
      />

      <EditModal
        open={modalOpen}
        initial={editingRecord}
        treeData={treeData}
        loading={submitLoading}
        onOk={(values) => {
          if (editingRecord) {
            handleUpdate(values);
          } else {
            handleCreate(values);
          }
        }}
        onCancel={() => {
          setModalOpen(false);
          setEditingRecord(null);
        }}
      />
    </>
  );
};

export const PermissionTree: React.FC<{
  selectedKeys?: string[];
  onSelect?: (keys: string[]) => void;
  checkable?: boolean;
  checkedKeys?: string[];
  onCheck?: (keys: string[]) => void;
}> = ({ selectedKeys, onSelect, checkable, checkedKeys, onCheck }) => {
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState<PermissionResponse[]>([]);
  const [treeData, setTreeData] = useState<TreeDataType[]>([]);
  const [query, setQuery] = useState<PermissionQueryRequest>({ keyword: "" });

  const loadData = async () => {
    setLoading(true);
    try {
      const { data: list } = await listPermissions(query);
      setData(list);
      // res is PermissionResponse[] (flat list)
      const tree = listToTree(list);
      setTreeData(tree);
    } catch (error) {
      console.error(error);
      message.error("加载权限列表失败");
    } finally {
      setLoading(false);
    }
  };

  // Actually, I should probably manage expandedKeys internally to support the buttons properly.
  const [expandedKeys, setExpandedKeys] = useState<string[]>([]);

  const loop = (nodes: TreeDataType[], keys: string[]) => {
    nodes.forEach((node) => {
      keys.push(node.id);
      if (node.children) {
        loop(node.children, keys);
      }
    });
  };

  const handleCheckAllInternal = (e: CheckboxChangeEvent) => {
    const keys: string[] = [];
    if (e.target.checked) {
      loop(treeData, keys);
    }
    onCheck?.(keys);
  };

  // Override handleExpandAll to use internal state
  const handleExpandAllInternal = () => {
    const keys: string[] = [];
    loop(treeData, keys);

    setExpandedKeys(keys);
  };

  const handleCollapseAllInternal = () => {
    setExpandedKeys([]);
  };

  useEffect(() => {
    loadData();
  }, []);

  return (
    <Spin spinning={loading}>
      <Flex
        justify="space-between"
        gap={12}
        align="center"
        style={{ marginBottom: 12 }}
      >
        <Input.Search
          size="small"
          placeholder="搜索权限"
          value={query.keyword}
          onChange={(e) => {
            setQuery({ keyword: e.target.value });
          }}
          onSearch={loadData}
        />
        <Space>
          <Checkbox
            checked={
              !!checkedKeys?.length && checkedKeys?.length === data.length
            }
            onChange={handleCheckAllInternal}
            style={{ width: 60 }}
          >
            全选
          </Checkbox>
          <Tooltip title="展开">
            <Button type="link" onClick={handleExpandAllInternal}>
              <PlusSquareOutlined />
            </Button>
          </Tooltip>
          <Tooltip title="收起">
            <Button type="link" onClick={handleCollapseAllInternal}>
              <MinusSquareOutlined />
            </Button>
          </Tooltip>
        </Space>
      </Flex>
      <Tree
        treeData={treeData}
        blockNode={true}
        showLine={true}
        showIcon={true}
        checkable={checkable}
        checkedKeys={checkedKeys}
        onCheck={(keys) => {
          onCheck?.(keys as string[]);
        }}
        selectedKeys={selectedKeys}
        onSelect={(keys) => {
          onSelect?.(keys as string[]);
        }}
        expandedKeys={expandedKeys}
        onExpand={(keys) => {
          setExpandedKeys(keys as string[]);
        }}
      />
    </Spin>
  );
};

export default Permission;
