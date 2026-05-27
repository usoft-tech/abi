import QueryButton from "@/components/button/QueryButton";
import DeleteIcon from "@/components/icon/DeleteIcon";
import EditIcon from "@/components/icon/EditIcon";
import RemoteTable from "@/components/table/RemoteTable";
import { EnableStatus } from "@/services/request";
import { PlusOutlined } from "@ant-design/icons";
import { useMutation } from "@tanstack/react-query";
import {
  Button,
  Drawer,
  Form,
  Input,
  InputNumber,
  message,
  Modal,
  Radio,
  Select,
  Space,
  Tag,
  Tooltip,
} from "antd";
import type { ColumnsType } from "antd/es/table";
import React, { useEffect, useRef, useState } from "react";
import {
  createDictData,
  createDictType,
  deleteDictData,
  deleteDictType,
  listDictDatas,
  listDictTypes,
  updateDictData,
  updateDictType,
  type DictDataCreateRequest,
  type DictDataQueryRequest,
  type DictDataResponse,
  type DictDataUpdateRequest,
  type DictTypeCreateRequest,
  type DictTypeQueryRequest,
  type DictTypeResponse,
  type DictTypeUpdateRequest,
} from "./services";
import IconFont from "@/components/icon/IconFont";
import { Permission } from "@/permission";

/**
 * 字典类型编辑弹窗
 */
const DictTypeModal: React.FC<{
  open: boolean;
  initial?: DictTypeResponse | null;
  onOk: (values: DictTypeCreateRequest | DictTypeUpdateRequest) => void;
  onCancel: () => void;
  loading?: boolean;
}> = ({ open, initial, onOk, onCancel, loading }) => {
  const [form] = Form.useForm();

  useEffect(() => {
    if (open) {
      if (initial) {
        form.setFieldsValue(initial);
      } else {
        form.resetFields();
        form.setFieldsValue({ status: EnableStatus.ENABLE });
      }
    }
  }, [open, initial, form]);

  const handleOk = () => {
    form.validateFields().then((values) => {
      onOk(values);
    });
  };

  return (
    <Modal
      title={initial ? "编辑字典类型" : "新建字典类型"}
      open={open}
      onOk={handleOk}
      onCancel={onCancel}
      confirmLoading={loading}
    >
      <Form form={form} layout="vertical">
        <Form.Item
          name="dictName"
          label="字典名称"
          rules={[{ required: true, message: "请输入字典名称" }]}
        >
          <Input placeholder="请输入字典名称" />
        </Form.Item>
        <Form.Item
          name="dictType"
          label="字典类型"
          rules={[{ required: true, message: "请输入字典类型" }]}
        >
          <Input placeholder="请输入字典类型" disabled={!!initial} />
        </Form.Item>
        <Form.Item
          name="status"
          label="状态"
          rules={[{ required: true, message: "请选择状态" }]}
        >
          <Radio.Group
            optionType="button"
            options={[
              { label: "启用", value: EnableStatus.ENABLE },
              { label: "禁用", value: EnableStatus.DISABLE },
            ]}
          />
        </Form.Item>
        <Form.Item name="remark" label="备注">
          <Input.TextArea placeholder="请输入备注" />
        </Form.Item>
      </Form>
    </Modal>
  );
};

/**
 * 字典数据编辑弹窗
 */
const DictDataModal: React.FC<{
  open: boolean;
  initial?: DictDataResponse | null;
  dictType: string;
  onOk: (values: DictDataCreateRequest | DictDataUpdateRequest) => void;
  onCancel: () => void;
  loading?: boolean;
}> = ({ open, initial, dictType, onOk, onCancel, loading }) => {
  const [form] = Form.useForm();

  useEffect(() => {
    if (open) {
      if (initial) {
        form.setFieldsValue(initial);
      } else {
        form.resetFields();
        form.setFieldsValue({
          dictType,
          status: EnableStatus.ENABLE,
          dictSort: 0,
          isDefault: "N",
        });
      }
    }
  }, [open, initial, dictType, form]);

  const handleOk = () => {
    form.validateFields().then((values) => {
      onOk(values);
    });
  };

  return (
    <Modal
      title={initial ? "编辑字典数据" : "新建字典数据"}
      open={open}
      onOk={handleOk}
      onCancel={onCancel}
      confirmLoading={loading}
    >
      <Form form={form} layout="vertical">
        <Form.Item name="dictType" label="字典类型">
          <Input disabled />
        </Form.Item>
        <Form.Item
          name="dictLabel"
          label="数据标签"
          rules={[{ required: true, message: "请输入数据标签" }]}
        >
          <Input placeholder="请输入数据标签" />
        </Form.Item>
        <Form.Item
          name="dictValue"
          label="数据键值"
          rules={[{ required: true, message: "请输入数据键值" }]}
        >
          <Input placeholder="请输入数据键值" />
        </Form.Item>
        <Form.Item name="dictSort" label="显示排序">
          <InputNumber min={0} style={{ width: "100%" }} />
        </Form.Item>
        <Form.Item name="isDefault" label="系统默认">
          <Radio.Group
            optionType="button"
            options={[
              { label: "是", value: "Y" },
              { label: "否", value: "N" },
            ]}
          />
        </Form.Item>
        <Form.Item
          name="status"
          label="状态"
          rules={[{ required: true, message: "请选择状态" }]}
        >
          <Radio.Group
            optionType="button"
            options={[
              { label: "启用", value: EnableStatus.ENABLE },
              { label: "禁用", value: EnableStatus.DISABLE },
            ]}
          />
        </Form.Item>
        <Form.Item name="remark" label="备注">
          <Input.TextArea placeholder="请输入备注" />
        </Form.Item>
      </Form>
    </Modal>
  );
};

/**
 * 字典数据管理抽屉
 */
const DictDataDrawer: React.FC<{
  open: boolean;
  dictType: string;
  onClose: () => void;
}> = ({ open, dictType, onClose }) => {
  const tableRef = useRef<any>(null);
  const [query, setQuery] = useState<DictDataQueryRequest>({
    page: 1,
    size: 10,
    dictType,
  });

  const [modalOpen, setModalOpen] = useState(false);
  const [editingRecord, setEditingRecord] = useState<DictDataResponse | null>(
    null,
  );

  const createMutation = useMutation({
    mutationFn: (values: DictDataCreateRequest) =>
      createDictData({ ...values, dictType }),
    onSuccess: () => {
      message.success("创建成功");
      setModalOpen(false);
      tableRef.current?.refresh();
    },
    onError: () => message.error("创建失败"),
  });

  const updateMutation = useMutation({
    mutationFn: (payload: { id: string; values: DictDataUpdateRequest }) =>
      updateDictData(payload.id, payload.values),
    onSuccess: () => {
      message.success("更新成功");
      setModalOpen(false);
      setEditingRecord(null);
      tableRef.current?.refresh();
    },
    onError: () => message.error("更新失败"),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => deleteDictData(id),
    onSuccess: () => {
      message.success("删除成功");
      tableRef.current?.refresh();
    },
    onError: () => message.error("删除失败"),
  });

  const handleCreate = (values: any) => {
    createMutation.mutate(values);
  };

  const handleUpdate = (values: any) => {
    if (!editingRecord) return;
    updateMutation.mutate({ id: editingRecord.id, values });
  };

  const handleDelete = (id: string) => {
    deleteMutation.mutate(id);
  };

  const columns: ColumnsType<DictDataResponse> = [
    { title: "字典标签", dataIndex: "dictLabel", key: "dictLabel" },
    { title: "字典键值", dataIndex: "dictValue", key: "dictValue" },
    { title: "备注", dataIndex: "remark", key: "remark", ellipsis: true },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      width: 100,
      align: "center",
      render: (status: EnableStatus) => (
        <Tag color={status === EnableStatus.ENABLE ? "success" : "error"}>
          {status === EnableStatus.ENABLE ? "启用" : "禁用"}
        </Tag>
      ),
    },
    {
      title: "操作",
      key: "action",
      width: 120,
      render: (_, record) => (
        <Space>
          <Permission value="sa:dict:data:update">
            <EditIcon
              onEdit={() => {
                setEditingRecord(record);
                setModalOpen(true);
              }}
            />
          </Permission>

          <Permission value="sa:dict:data:delete">
            <DeleteIcon onDelete={() => handleDelete(record.id)} />
          </Permission>
        </Space>
      ),
    },
  ];

  return (
    <Drawer
      title={`字典数据管理 - ${dictType}`}
      size={800}
      open={open}
      onClose={onClose}
    >
      <RemoteTable<DictDataResponse>
        ref={tableRef}
        size="small"
        fetchKey={["dictData", dictType, query]}
        fetchData={() => listDictDatas({ ...query, dictType })}
        columns={columns}
        rowKey="id"
        title="字典值选项"
        titleExtra={
          <Permission value="sa:dict:data:create">
            <Button
              type="primary"
              icon={<PlusOutlined />}
              onClick={() => {
                setEditingRecord(null);
                setModalOpen(true);
              }}
            >
              新增
            </Button>
          </Permission>
        }
        pagination={{
          current: query.page,
          pageSize: query.size,
          onChange: (page, size) => setQuery({ ...query, page, size }),
        }}
      />
      <DictDataModal
        open={modalOpen}
        initial={editingRecord}
        dictType={dictType}
        loading={createMutation.isPending || updateMutation.isPending}
        onCancel={() => {
          setModalOpen(false);
          setEditingRecord(null);
        }}
        onOk={(values) => {
          if (editingRecord) {
            handleUpdate(values);
          } else {
            handleCreate(values);
          }
        }}
      />
    </Drawer>
  );
};

/**
 * 字典管理页面
 */
const Dict: React.FC = () => {
  const tableRef = useRef<any>(null);
  const [query, setQuery] = useState<DictTypeQueryRequest>({
    page: 1,
    size: 10,
    keyword: "",
  });

  const [modalOpen, setModalOpen] = useState(false);
  const [editingRecord, setEditingRecord] = useState<DictTypeResponse | null>(
    null,
  );

  const createMutation = useMutation({
    mutationFn: (values: DictTypeCreateRequest) => createDictType(values),
    onSuccess: () => {
      message.success("创建成功");
      setModalOpen(false);
      tableRef.current?.refresh();
    },
    onError: () => message.error("创建失败"),
  });

  const updateMutation = useMutation({
    mutationFn: (payload: { id: string; values: DictTypeUpdateRequest }) =>
      updateDictType(payload.id, payload.values),
    onSuccess: () => {
      message.success("更新成功");
      setModalOpen(false);
      setEditingRecord(null);
      tableRef.current?.refresh();
    },
    onError: () => message.error("更新失败"),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => deleteDictType(id),
    onSuccess: () => {
      message.success("删除成功");
      tableRef.current?.refresh();
    },
    onError: () => message.error("删除失败"),
  });

  const [drawerOpen, setDrawerOpen] = useState(false);
  const [selectedDictType, setSelectedDictType] = useState("");

  const handleCreate = (values: any) => {
    createMutation.mutate(values);
  };

  const handleUpdate = (values: any) => {
    if (!editingRecord) return;
    updateMutation.mutate({ id: editingRecord.id, values });
  };

  const handleDelete = (id: string) => {
    deleteMutation.mutate(id);
  };

  const columns: ColumnsType<DictTypeResponse> = [
    { title: "字典名称", dataIndex: "dictName", key: "dictName" },
    {
      title: "字典类型",
      dataIndex: "dictType",
      key: "dictType",
      render: (text) => (
        <Button
          type="link"
          style={{ padding: 0 }}
          onClick={() => {
            setSelectedDictType(text);
            setDrawerOpen(true);
          }}
        >
          {text}
        </Button>
      ),
    },
    { title: "备注", dataIndex: "remark", key: "remark", ellipsis: true },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      width: 100,
      align: "center",
      render: (status: EnableStatus) => (
        <Tag color={status === EnableStatus.ENABLE ? "success" : "error"}>
          {status === EnableStatus.ENABLE ? "启用" : "禁用"}
        </Tag>
      ),
    },
    {
      title: "操作",
      key: "action",
      width: 150,
      render: (_, record) => (
        <Space>
          <Permission value="sa:dict:data">
            <Tooltip title="字典值">
              <Button
                type="link"
                onClick={() => {
                  setSelectedDictType(record.dictType);
                  setDrawerOpen(true);
                }}
              >
                <IconFont type="icon-dict" />
              </Button>
            </Tooltip>
          </Permission>
          <Permission value="sa:dict:type:update">
            <Tooltip title="编辑">
              <EditIcon
                onEdit={() => {
                  setEditingRecord(record);
                  setModalOpen(true);
                }}
              />
            </Tooltip>
          </Permission>
          <Permission value="sa:dict:type:delete">
            <Tooltip title="删除">
              <DeleteIcon onDelete={() => handleDelete(record.id)} />
            </Tooltip>
          </Permission>
        </Space>
      ),
    },
  ];

  return (
    <>
      <RemoteTable
        ref={tableRef}
        queryBar={
          <Form
            layout="inline"
            onFinish={(values) => setQuery({ ...query, ...values, page: 1 })}
          >
            <Form.Item name="keyword" label="关键字">
              <Input placeholder="字典名称或类型" allowClear />
            </Form.Item>
            <Form.Item name="status" label="状态">
              <Select
                placeholder="请选择状态"
                allowClear
                style={{ width: 120 }}
                options={[
                  { label: "启用", value: EnableStatus.ENABLE },
                  { label: "禁用", value: EnableStatus.DISABLE },
                ]}
              />
            </Form.Item>
            <Form.Item>
              <QueryButton />
            </Form.Item>
          </Form>
        }
        title="字典管理"
        titleExtra={
          <Permission value="sa:dict:type:create">
            <Button
              type="primary"
              icon={<PlusOutlined />}
              onClick={() => {
                setEditingRecord(null);
                setModalOpen(true);
              }}
            >
              新增
            </Button>
          </Permission>
        }
        fetchKey={["dictType", query]}
        fetchData={() => listDictTypes(query)}
        columns={columns}
        rowKey="id"
        pagination={{
          current: query.page,
          pageSize: query.size,
          onChange: (page, size) => setQuery({ ...query, page, size }),
        }}
      />

      <DictTypeModal
        open={modalOpen}
        initial={editingRecord}
        loading={createMutation.isPending || updateMutation.isPending}
        onCancel={() => {
          setModalOpen(false);
          setEditingRecord(null);
        }}
        onOk={(values) => {
          if (editingRecord) {
            handleUpdate(values);
          } else {
            handleCreate(values);
          }
        }}
      />

      <DictDataDrawer
        open={drawerOpen}
        dictType={selectedDictType}
        onClose={() => {
          setDrawerOpen(false);
          setSelectedDictType("");
        }}
      />
    </>
  );
};

export default Dict;
