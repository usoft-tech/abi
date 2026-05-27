import QueryButton from "@/components/button/QueryButton";
import DeleteIcon from "@/components/icon/DeleteIcon";
import EditIcon from "@/components/icon/EditIcon";
import RemoteTable from "@/components/table/RemoteTable";
import { Permission } from "@/permission";
import { PlusOutlined, DeleteOutlined } from "@ant-design/icons";
import { useMutation } from "@tanstack/react-query";
import {
  Button,
  Form,
  Input,
  Modal,
  Space,
  Select,
  Row,
  Col,
  Tag,
  InputNumber,
  message,
  AutoComplete,
} from "antd";
import type { ColumnsType } from "antd/es/table";
import React, { useMemo, useRef, useState } from "react";
import {
  createModel,
  deleteModel,
  listModels,
  updateModel,
  type ModelCreateRequest,
  type ModelQueryRequest,
  type ModelResponse,
  type ModelUpdateRequest,
} from "./services";

const { Option } = Select;

const columnsBuilder = (
  onEdit: (record: ModelResponse) => void,
  onDelete: (record: ModelResponse) => void,
): ColumnsType<ModelResponse> => [
  { title: "名称", dataIndex: "name", key: "name" },
  {
    title: "提供商",
    dataIndex: "provider",
    key: "provider",
    render: (text: string) => <Tag color="blue">{text}</Tag>,
  },
  { title: "模型标识", dataIndex: "model", key: "model" },
  { title: "Base URL", dataIndex: "baseUrl", key: "baseUrl", ellipsis: true },
  {
    title: "操作",
    key: "action",
    render: (_: any, record: ModelResponse) => (
      <Space size="middle">
        <Permission value="ai:model:update">
          <EditIcon onEdit={() => onEdit(record)} />
        </Permission>
        <Permission value="ai:model:delete">
          <DeleteIcon name="模型" onDelete={() => onDelete(record)} />
        </Permission>
      </Space>
    ),
  },
];

const QueryBar: React.FC<{
  query: ModelQueryRequest;
  onChange: (q: ModelQueryRequest) => void;
}> = ({ query, onChange }) => {
  const [form] = Form.useForm<ModelQueryRequest>();
  return (
    <Form<ModelQueryRequest>
      form={form}
      layout="inline"
      initialValues={{
        name: query.name,
        provider: query.provider,
        keyword: query.keyword,
      }}
      onFinish={(values: ModelQueryRequest) =>
        onChange({ ...query, ...values, page: 1 })
      }
      style={{ marginBottom: 12 }}
    >
      <Form.Item name="name" label="名称">
        <Input placeholder="按名称筛选" allowClear />
      </Form.Item>
      <Form.Item name="provider" label="提供商">
        <Select placeholder="按提供商筛选" allowClear style={{ width: 150 }}>
          <Option value="openai">OpenAI</Option>
          <Option value="ollama">Ollama</Option>
          <Option value="dashscope">Aliyun DashScope</Option>
        </Select>
      </Form.Item>
      <Form.Item>
        <QueryButton />
      </Form.Item>
    </Form>
  );
};

export const ModelPage: React.FC = () => {
  const [query, setQuery] = useState<ModelQueryRequest>({ page: 1, size: 10 });
  const [columns, setColumns] = useState<ColumnsType<ModelResponse>>([]);
  const [editing, setEditing] = useState<ModelResponse | null>(null);
  const [visible, setVisible] = useState(false);
  // Need to handle ModelCreateRequest | ModelUpdateRequest plus extProps form logic
  const [form] = Form.useForm();
  const tableRef = useRef<any>(null);

  const refreshTable = () => {
    tableRef.current?.refresh();
  };

  const createMutation = useMutation({
    mutationFn: (payload: ModelCreateRequest) => createModel(payload),
    onSuccess: () => {
      message.success("创建成功");
      setVisible(false);
      form.resetFields();
      refreshTable();
    },
  });

  const updateMutation = useMutation({
    mutationFn: (payload: { id: string; data: ModelUpdateRequest }) =>
      updateModel(payload.id, payload.data),
    onSuccess: () => {
      message.success("更新成功");
      setVisible(false);
      setEditing(null);
      form.resetFields();
      refreshTable();
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => deleteModel(id),
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

  const onEdit = (record: ModelResponse) => {
    setEditing(record);
    // Convert extProps map to array for Form.List, inferring type
    const extPropsArray = record.extProps
      ? Object.entries(record.extProps).map(([key, value]) => {
          let type = "string";
          if (typeof value === "boolean") type = "boolean";
          else if (typeof value === "number") type = "number";
          return { key, value, type };
        })
      : [];

    form.setFieldsValue({
      ...record,
      extProps: extPropsArray,
    });
    setVisible(true);
  };

  const onDelete = (record: ModelResponse) => {
    deleteMutation.mutate(record.id);
  };

  const onSubmit = () => {
    form
      .validateFields()
      .then((values: any) => {
        // Convert extProps array back to map
        const extPropsMap: Record<string, any> = {};
        if (values.extProps && Array.isArray(values.extProps)) {
          values.extProps.forEach(
            (item: { key: string; value: any; type: string }) => {
              if (item.key) {
                extPropsMap[item.key] = item.value;
              }
            },
          );
        }

        const payload = {
          ...values,
          extProps: extPropsMap,
        };

        if (editing) {
          updateMutation.mutate({ id: editing.id, data: payload });
        } else {
          createMutation.mutate(payload);
        }
      })
      .catch(() => {});
  };

  useMemo(() => {
    setColumns(columnsBuilder(onEdit, onDelete));
  }, []);

  return (
    <>
      <RemoteTable<ModelResponse>
        ref={tableRef}
        queryBar={<QueryBar query={query} onChange={setQuery} />}
        title="大模型管理"
        titleExtra={
          <Permission value="ai:model:create">
            <Button type="primary" onClick={onCreate}>
              <PlusOutlined /> 新建模型
            </Button>
          </Permission>
        }
        fetchKey={["models", query]}
        fetchData={() => listModels(query)}
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
        title={editing ? "编辑模型" : "新建模型"}
        open={visible}
        onCancel={() => {
          setVisible(false);
          setEditing(null);
          form.resetFields();
        }}
        onOk={onSubmit}
        okText="保存"
        cancelText="取消"
        width={700}
        maskClosable={false}
      >
        <Form
          form={form}
          layout="vertical"
          initialValues={{
            provider: "openai",
            extProps: [],
          }}
        >
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="name"
                label="模型名称"
                rules={[{ required: true, message: "请输入模型名称" }]}
              >
                <Input placeholder="例如：GPT-4o" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="provider"
                label="提供商"
                rules={[{ required: true, message: "请选择或输入提供商" }]}
              >
                <Select
                  showSearch
                  placeholder="选择或输入提供商"
                  optionFilterProp="children"
                  allowClear
                >
                  <Option value="openai">OpenAI</Option>
                  <Option value="ollama">Ollama</Option>
                  <Option value="dashscope">Aliyun DashScope</Option>
                </Select>
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="model"
                label="模型标识 (Model ID)"
                rules={[{ required: true, message: "请输入模型标识" }]}
                tooltip="API调用时使用的模型ID，如 gpt-4o, claude-3-opus-20240229"
              >
                <Input placeholder="例如：gpt-4o" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                noStyle
                shouldUpdate={(prev, curr) => prev.provider !== curr.provider}
              >
                {({ getFieldValue }) => {
                  const provider = getFieldValue("provider");
                  if (provider === "dashscope") return null;
                  return (
                    <Form.Item
                      name="baseUrl"
                      label="API Base URL"
                      tooltip="可选，用于覆盖默认API地址"
                      rules={[
                        { required: true, message: "请输入API Base URL" },
                      ]}
                    >
                      <Input placeholder="例如：https://api.openai.com/v1" />
                    </Form.Item>
                  );
                }}
              </Form.Item>
            </Col>
          </Row>

          <Form.Item
            noStyle
            shouldUpdate={(prev, curr) => prev.provider !== curr.provider}
          >
            {({ getFieldValue }) => {
              const provider = getFieldValue("provider");
              if (provider === "ollama") return null;
              return (
                <Form.Item
                  name="apiKey"
                  label="API Key"
                  rules={[{ required: !editing, message: "请输入API Key" }]}
                  tooltip="创建时必填，编辑时留空则不修改"
                >
                  <Input.Password placeholder="请输入API Key" />
                </Form.Item>
              );
            }}
          </Form.Item>

          <Form.Item label="扩展属性">
            <Form.List name="extProps">
              {(fields, { add, remove }) => (
                <>
                  {fields.map(({ key, name, ...restField }) => (
                    <Space
                      key={key}
                      style={{ display: "flex", marginBottom: 8 }}
                      align="baseline"
                    >
                      <Form.Item
                        {...restField}
                        name={[name, "key"]}
                        rules={[{ required: true, message: "请输入Key" }]}
                        style={{ width: 150 }}
                      >
                        <AutoComplete
                          placeholder="Key"
                          options={[
                            { value: "maxTokens", label: "Max Tokens" },
                            { value: "temperature", label: "Temperature" },
                            { value: "stream", label: "Stream" },
                            {
                              value: "enableThinking",
                              label: "Enable Thinking",
                            },
                            { value: "enableSearch", label: "Enable Search" },
                          ]}
                          onChange={(value) => {
                            if (
                              value === "maxTokens" ||
                              value === "temperature"
                            ) {
                              const currentExtProps =
                                form.getFieldValue("extProps");
                              if (currentExtProps && currentExtProps[name]) {
                                currentExtProps[name].type = "number";
                                // Only clear value if type changed from something else to number,
                                // but here we are forcing it so maybe clear is safer if value is not compatible
                                if (
                                  typeof currentExtProps[name].value !==
                                  "number"
                                ) {
                                  currentExtProps[name].value = undefined;
                                }
                                form.setFieldsValue({
                                  extProps: currentExtProps,
                                });
                              }
                            }
                            if (
                              value === "stream" ||
                              value === "enableThinking" ||
                              value === "enableSearch"
                            ) {
                              const currentExtProps =
                                form.getFieldValue("extProps");
                              if (currentExtProps && currentExtProps[name]) {
                                currentExtProps[name].type = "boolean";
                                // Only clear value if type changed from something else to boolean,
                                // but here we are forcing it so maybe clear is safer if value is not compatible
                                if (
                                  typeof currentExtProps[name].value !==
                                  "boolean"
                                ) {
                                  currentExtProps[name].value = undefined;
                                }
                                form.setFieldsValue({
                                  extProps: currentExtProps,
                                });
                              }
                            }
                          }}
                        />
                      </Form.Item>

                      <Form.Item
                        noStyle
                        shouldUpdate={(prev, curr) => {
                          return (
                            prev.extProps?.[name]?.key !==
                            curr.extProps?.[name]?.key
                          );
                        }}
                      >
                        {({ getFieldValue }) => {
                          const keyVal = getFieldValue([
                            "extProps",
                            name,
                            "key",
                          ]);
                          const isFixedType =
                            keyVal === "maxTokens" ||
                            keyVal === "temperature" ||
                            keyVal === "stream" ||
                            keyVal === "enableThinking" ||
                            keyVal === "enableSearch";

                          return (
                            <Form.Item
                              {...restField}
                              name={[name, "type"]}
                              initialValue="string"
                              style={{ width: 100 }}
                            >
                              <Select
                                disabled={isFixedType}
                                onChange={() => {
                                  const currentExtProps =
                                    form.getFieldValue("extProps");
                                  if (
                                    currentExtProps &&
                                    currentExtProps[name]
                                  ) {
                                    currentExtProps[name].value = undefined;
                                    form.setFieldsValue({
                                      extProps: currentExtProps,
                                    });
                                  }
                                }}
                              >
                                <Option value="string">String</Option>
                                <Option value="number">Number</Option>
                                <Option value="boolean">Boolean</Option>
                              </Select>
                            </Form.Item>
                          );
                        }}
                      </Form.Item>

                      <Form.Item
                        noStyle
                        shouldUpdate={(prev, curr) => {
                          return (
                            prev.extProps?.[name]?.type !==
                            curr.extProps?.[name]?.type
                          );
                        }}
                      >
                        {({ getFieldValue }) => {
                          const type =
                            getFieldValue(["extProps", name, "type"]) ||
                            "string";
                          return (
                            <Form.Item
                              {...restField}
                              name={[name, "value"]}
                              rules={[
                                { required: true, message: "请输入Value" },
                              ]}
                              style={{ width: 250 }}
                            >
                              {type === "number" ? (
                                <InputNumber
                                  style={{ width: "100%" }}
                                  placeholder="Value"
                                />
                              ) : type === "boolean" ? (
                                <Select placeholder="Select boolean">
                                  <Option value={true}>True</Option>
                                  <Option value={false}>False</Option>
                                </Select>
                              ) : (
                                <Input placeholder="Value" />
                              )}
                            </Form.Item>
                          );
                        }}
                      </Form.Item>
                      <DeleteOutlined
                        onClick={() => remove(name)}
                        style={{ color: "red", cursor: "pointer" }}
                      />
                    </Space>
                  ))}
                  <Form.Item>
                    <Button
                      type="dashed"
                      onClick={() => add({ type: "string" })}
                      block
                      icon={<PlusOutlined />}
                    >
                      添加扩展属性
                    </Button>
                  </Form.Item>
                </>
              )}
            </Form.List>
          </Form.Item>
        </Form>
      </Modal>
    </>
  );
};

export default ModelPage;
