import HelpIcon from "@/components/icon/HelpIcon";
import IconFont from "@/components/icon/IconFont";
import { EnableStatus } from "@/services/request";
import {
  CloudServerOutlined,
  ConsoleSqlOutlined,
  DatabaseOutlined,
  DeleteOutlined,
  FolderOpenOutlined,
  FolderOutlined,
  MinusCircleOutlined,
  PlusOutlined,
  SaveOutlined,
} from "@ant-design/icons";
import { Editor } from "@monaco-editor/react";
import { useMutation, useQuery } from "@tanstack/react-query";
import {
  Alert,
  Button,
  Card,
  Col,
  Drawer,
  Empty,
  Form,
  Input,
  InputNumber,
  message,
  Modal,
  Radio,
  Row,
  Select,
  Space,
  Switch,
  Table,
  Tabs,
  TreeSelect,
  TreeSelectProps,
} from "antd";
import { DataNode } from "antd/es/tree";
import { FC, useEffect, useRef, useState } from "react";
import { format } from "sql-formatter";
import styled from "styled-components";
import {
  createDataset,
  DatasetCreateRequest,
  DatasetFolderEntity,
  DatasetResponse,
  DataSetType,
  DatasetUpdateRequest,
  DataSourceResponse,
  FieldType,
  generateSql,
  GenerateSqlResponse,
  listDatasetFolders,
  listDataSourceDbs,
  listDataSources,
  testSql,
  updateDataset,
} from "../services";
import { Permission } from "@/permission";

const updateTreeData = (
  list: (DataNode & { data?: any })[],
  key: React.Key,
  children: (DataNode & { data?: any })[],
): (DataNode & { data?: any })[] =>
  list.map((node) => {
    if (node.key === key) {
      return {
        ...node,
        children,
      };
    }
    if (node.children) {
      return {
        ...node,
        children: updateTreeData(
          node.children as (DataNode & { data?: any })[],
          key,
          children,
        ),
      };
    }
    return node;
  });
const convertFoldersToTree = (folders: DatasetFolderEntity[]): any[] => {
  const map = new Map<string, any>();
  const roots: any[] = [];

  // Create nodes
  folders.forEach((f) => {
    map.set(f.id, {
      key: f.id,
      value: f.id,
      title: f.name,
      icon: ({ expanded }: any) =>
        expanded ? <FolderOpenOutlined /> : <FolderOutlined />,
      children: [],
    });
  });

  // Build tree
  folders.forEach((f) => {
    const node = map.get(f.id)!;
    if (f.parentId && map.has(f.parentId)) {
      map.get(f.parentId)!.children.push(node);
    } else {
      roots.push(node);
    }
  });

  return roots;
};

const BlockLabelFormItem = styled(Form.Item)`
  label {
    width: 100%;
  }
`;

const SchemaFieldEditor = ({
  name,
  fieldTypes,
  prefix = [],
}: {
  name: (string | number)[];
  fieldTypes: { label: string; value: string }[];
  prefix?: (string | number)[];
}) => {
  return (
    <Form.List name={name}>
      {(fields, { add, remove }) => (
        <div style={{ display: "flex", flexDirection: "column", gap: 4 }}>
          {fields.map(({ key, name: fieldName, ...restField }) => (
            <Card
              key={key}
              size="small"
              style={{
                borderLeft: "3px solid #1890ff",
                background: "#fafafa",
              }}
              bodyStyle={{ padding: "4px" }}
            >
              {/* Basic Fields Row */}
              <Row gutter={4} align="middle">
                <Col span={6}>
                  <Form.Item
                    {...restField}
                    name={[fieldName, "key"]}
                    rules={[{ required: true, message: "Key必填" }]}
                    style={{ marginBottom: 0 }}
                  >
                    <Input placeholder="Key" />
                  </Form.Item>
                </Col>
                <Col span={6}>
                  <Form.Item
                    {...restField}
                    name={[fieldName, "name"]}
                    style={{ marginBottom: 0 }}
                  >
                    <Input placeholder="名称" />
                  </Form.Item>
                </Col>
                <Col span={6}>
                  <Form.Item
                    {...restField}
                    name={[fieldName, "type"]}
                    style={{ marginBottom: 0 }}
                  >
                    <Select options={fieldTypes} placeholder="类型" />
                  </Form.Item>
                </Col>
                <Col span={6} style={{ textAlign: "right" }}>
                  <Button
                    type="text"
                    danger
                    icon={<DeleteOutlined />}
                    onClick={() => remove(fieldName)}
                  />
                </Col>
              </Row>

              {/* Recursive Schema for Object/Array */}
              <Form.Item
                noStyle
                dependencies={[[...prefix, ...name, fieldName, "type"]]}
              >
                {({ getFieldValue }) => {
                  // Construct path to the current field's type
                  // Use absolute path for getFieldValue
                  const typePath = [...prefix, ...name, fieldName, "type"];
                  const type = getFieldValue(typePath);

                  if (type === FieldType.OBJECT || type === FieldType.ARRAY) {
                    return (
                      <div
                        style={{
                          marginLeft: 24,
                          marginTop: 8,
                          paddingLeft: 12,
                          borderLeft: "2px dashed #d9d9d9",
                        }}
                      >
                        <div
                          style={{
                            marginBottom: 4,
                            fontSize: 12,
                            color: "#999",
                          }}
                        >
                          子字段配置:
                        </div>
                        <SchemaFieldEditor
                          name={[fieldName, "schema", "fields"]}
                          prefix={[...prefix, ...name]}
                          fieldTypes={fieldTypes}
                        />
                      </div>
                    );
                  }
                  return null;
                }}
              </Form.Item>
            </Card>
          ))}
          <Button
            type="dashed"
            onClick={() => add({ type: FieldType.STRING })}
            block
            icon={<PlusOutlined />}
            style={{ marginTop: 4 }}
          >
            添加字段
          </Button>
        </div>
      )}
    </Form.List>
  );
};

const AiGenerateSqlDrawer: FC<{
  open: boolean;
  datasources: DataSourceResponse[];
  onClose: () => void;
  onSubmit: (data: GenerateSqlResponse) => void;
}> = ({ open, datasources, onClose, onSubmit }) => {
  const [form] = Form.useForm<{
    datasourceId: string;
    agentId: number;
    prompt: string;
  }>();
  const [loading, setLoading] = useState(false);
  const [treeData, setTreeData] = useState<(DataNode & { data?: any })[]>([]);

  useEffect(() => {
    if (open) {
      form.resetFields();
      setTreeData(
        datasources
          .filter((ds) => ds.type === "ChatBI")
          .map((ds) => ({
            title: ds.name,
            value: ds.id,
            key: ds.id,
            icon: <CloudServerOutlined />,
            isLeaf: false,
            selectable: false,
            data: { ...ds, nodeType: "datasource" },
          })),
      );
    }
  }, [open, datasources]);

  const onLoadData = ({ key, children }: any) =>
    new Promise<void>(async (resolve) => {
      if (children && children.length > 0) {
        resolve();
        return;
      }
      try {
        const { data: dbs } = await listDataSourceDbs(key);
        setTreeData((origin) =>
          updateTreeData(
            origin,
            key,
            dbs.map((db) => ({
              title: db.name,
              value: db.id,
              key: db.id,
              icon: <DatabaseOutlined />,
              isLeaf: true,
              selectable: true,
              data: { ...db, nodeType: "db" },
            })),
          ),
        );
      } catch (e) {
        console.error(e);
        message.error("加载数据库失败");
      }
      resolve();
    });

  const handleSelect: TreeSelectProps["onSelect"] = (_, node) => {
    const { datasourceId, dbid } = node.data || {};
    form.setFieldsValue({
      datasourceId,
      agentId: Number(dbid),
    });
    console.log({ datasourceId, dbid }, form.getFieldsValue());
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      console.log(form.getFieldsValue());
      setLoading(true);
      const res = await generateSql(values);
      setLoading(false);
      if (res) {
        onSubmit(res.data);
        onClose();
      }
    } catch (error) {
      console.error(error);
      setLoading(false);
    }
  };

  return (
    <Modal
      title="创建AI数据集"
      open={open}
      okText="生成"
      cancelText="取消"
      onCancel={onClose}
      onOk={handleSubmit}
      confirmLoading={loading}
    >
      <Form form={form} layout="vertical">
        <Form.Item
          label="数据助理"
          rules={[{ required: true, message: "请选择数据助理" }]}
        >
          <TreeSelect
            treeData={treeData}
            loadData={onLoadData}
            placeholder="请选择数据助理"
            treeIcon
            styles={{
              popup: {
                root: { maxHeight: 400, overflow: "auto" },
              },
            }}
            onSelect={handleSelect}
          />
        </Form.Item>
        <Form.Item name="prompt" label="提示词" rules={[{ required: true }]}>
          <Input.TextArea
            rows={4}
            placeholder="请输入提示词，例如：查询所有用户的订单金额"
          />
        </Form.Item>
        <Form.Item name="datasourceId" hidden>
          <Input />
        </Form.Item>
        <Form.Item name="agentId" hidden>
          <InputNumber />
        </Form.Item>
      </Form>
    </Modal>
  );
};

export const DatasetDrawer = ({
  open,
  initial,
  folderId,
  tempDbs,
  onClose,
  onSuccess,
}: {
  open: boolean;
  initial: Partial<DatasetResponse> | null;
  folderId?: string;
  tempDbs?: {
    id: string;
    name: string;
  }[];
  onClose: () => void;
  onSuccess: (dataset: DatasetResponse) => void;
}) => {
  const [form] = Form.useForm();
  const type = Form.useWatch("type", form);
  const [sqlGenerateModalOpen, setSqlGenerateModalOpen] = useState(false);
  const refSql = useRef<any>(null);
  const refScript = useRef<any>(null);
  const dataSourceId = Form.useWatch("dataSourceId", form);
  const sql = Form.useWatch("sql", form);
  const [error, setError] = useState<string>("");

  const [testSqlResult, setTestSqlResult] = useState<
    Record<string, Object>[] | null
  >(null);

  // Fetch datasources for select
  const { data: datasources } = useQuery<DataSourceResponse[]>({
    retry: false,
    queryKey: ["datasources", "list-all"],
    queryFn: async () => {
      const res = await listDataSources({ page: 1, size: 1000 });
      return res.data?.items || [];
    },
    enabled: open && type === DataSetType.SQL,
  });

  // Fetch folders for select
  const { data: folders } = useQuery({
    retry: false,
    queryKey: ["dataset-folders"],
    queryFn: async () => {
      const res = await listDatasetFolders();
      return res.data || [];
    },
    enabled: open,
  });

  // Mutations
  const createMut = useMutation({
    mutationFn: createDataset,
    onSuccess: (dataset) => {
      message.success("创建成功");
      onSuccess(dataset.data as DatasetResponse);
      handleClose();
    },
  });

  const updateMut = useMutation({
    mutationFn: (data: { id: string; req: DatasetUpdateRequest }) =>
      updateDataset(data.id, data.req),
    onSuccess: (dataset) => {
      message.success("更新成功");
      onSuccess(dataset.data as DatasetResponse);
      handleClose();
    },
  });

  useEffect(() => {
    if (open) {
      form.resetFields();
      if (initial) {
        // Prepare API Headers/Params lists
        const apiHeaders = initial.config?.api?.headers
          ? Object.entries(initial.config.api.headers).map(([k, v]) => ({
              key: k,
              value: v,
            }))
          : [];
        const useParams = initial.config?.api?.params
          ? Object.entries(initial.config.api.params).map(([k, v]) => ({
              key: k,
              value: v,
            }))
          : [];

        form.setFieldsValue({
          name: initial.name,
          type: initial.type,
          folderId: initial.folderId || folderId,
          description: initial.description,
          status: initial.status || EnableStatus.ENABLE,
          // SQL Config
          sql: initial.config?.sql?.sql,
          dataSourceId: initial.config?.sql?.dataSourceId,
          // API Config
          apiUrl: initial.config?.api?.url,
          apiMethod: initial.config?.api?.method || "GET",
          apiHeaders,
          apiParams: useParams,
          apiBody: initial.config?.api?.body,
          // Input
          inputFields: initial.config?.input?.fields || [],
          // Output
          outputFields: initial.config?.output?.fields || [],
          // Script
          script: initial.config?.script,
        });
      } else {
        form.setFieldsValue({
          type: DataSetType.SQL,
          folderId: folderId,
          status: EnableStatus.ENABLE,
          apiMethod: "GET",
          inputFields: [],
          outputFields: [],
        });
      }
    }
  }, [open, initial, folderId, form]);

  const handleOk = async () => {
    try {
      const values = await form.validateFields();
      const config: any = {
        input: {
          fields: values.inputFields,
        },
        output: {
          fields: values.outputFields,
        },
        script: values.script,
      };

      if (values.type === DataSetType.SQL) {
        config.sql = { sql: values.sql, dataSourceId: values.dataSourceId };
      } else {
        const headers: Record<string, string> = {};
        values.apiHeaders?.forEach((h: any) => {
          if (h.key) headers[h.key] = h.value;
        });
        const params: Record<string, string> = {};
        values.apiParams?.forEach((p: any) => {
          if (p.key) params[p.key] = p.value;
        });

        config.api = {
          url: values.apiUrl,
          method: values.apiMethod,
          headers,
          params,
          body: values.apiBody,
        };
      }

      const payload = {
        name: values.name,
        type: values.type,
        folderId: values.folderId,
        description: values.description,
        status: values.status,
        config,
      };

      if (initial?.id) {
        updateMut.mutate({ id: initial.id, req: payload });
      } else {
        createMut.mutate(payload as DatasetCreateRequest);
      }
    } catch (e) {
      console.error(e);
    }
  };

  const fieldTypes = Object.values(FieldType).map((t) => ({
    label: t,
    value: t,
  }));

  // Input types are restricted to simple types
  const inputFieldTypes = [
    { label: FieldType.STRING, value: FieldType.STRING },
    { label: FieldType.NUMBER, value: FieldType.NUMBER },
    { label: FieldType.BOOLEAN, value: FieldType.BOOLEAN },
  ];

  const testSqlMutation = useMutation({
    mutationFn: testSql,
    onSuccess: (res) => {
      setError("");
      setTestSqlResult(res?.data || []);
    },
    onError: (error: any) => {
      const { response } = error;
      setError(response?.data?.message || "测试SQL执行失败");
      setTestSqlResult(null);
    },
  });

  const renderConfig = () => {
    if (type === DataSetType.SQL) {
      return (
        <>
          <Form.Item
            name="dataSourceId"
            label="数据源"
            rules={[{ required: true, message: "请选择数据源" }]}
          >
            <Select
              options={[
                ...(tempDbs?.length
                  ? [
                      {
                        label: "临时数据库",
                        value: "tempDb",
                        options: tempDbs.map((db) => ({
                          label: db.name,
                          value: db.id,
                        })),
                      },
                    ]
                  : []),
                {
                  label: "数据库",
                  value: "db",
                  options: datasources
                    ?.filter((item) => item.type !== "ChatBI")
                    ?.map((d: DataSourceResponse) => ({
                      label: d.name,
                      value: d.id,
                    })),
                },
              ]}
              placeholder="请选择数据源"
            />
          </Form.Item>
          <BlockLabelFormItem
            name="sql"
            colon={false}
            layout="vertical"
            label={
              <div
                style={{
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "space-between",
                  width: "100%",
                }}
              >
                <Space>
                  SQL 脚本 <HelpIcon title="支持 Mybatis 语法" />
                </Space>
                <a
                  onClick={() => {
                    const sql = form.getFieldValue("sql");
                    if (sql) {
                      const newSql = format(sql, {
                        language: "mysql",
                        useTabs: true,
                        tabWidth: 2,
                        keywordCase: "upper",
                        functionCase: "upper",
                      });
                      form.setFieldsValue({
                        sql: newSql,
                      });
                    }
                  }}
                >
                  <IconFont type="icon-formate" /> 格式化
                </a>
              </div>
            }
            rules={[{ required: true, message: "请输入SQL" }]}
          >
            <div style={{ border: "1px solid #d9d9d9" }}>
              <Editor
                height="500px"
                onMount={(editor) => {
                  (refSql.current as any) = { editor };
                }}
                defaultLanguage="sql"
                value={sql}
                onChange={(v) => form.setFieldsValue({ sql: v || "" })}
                options={{
                  minimap: { enabled: false },
                  scrollBeyondLastLine: false,
                  tabSize: 2,
                }}
              />
            </div>
          </BlockLabelFormItem>

          <Button
            icon={<ConsoleSqlOutlined />}
            disabled={
              !dataSourceId?.length || !sql?.length || testSqlMutation.isPending
            }
            loading={testSqlMutation.isPending}
            onClick={() => {
              testSqlMutation.mutate({
                dbId: dataSourceId,
                sql,
              });
            }}
            style={{ marginBottom: 16 }}
          >
            执行测试
          </Button>

          {error && <Alert title={error} type="error" showIcon />}
          {testSqlResult && testSqlResult.length > 0 && (
            <Table
              title={() => "测试结果"}
              rowKey={(_, index) => index + ""}
              columns={[
                {
                  title: "序号",
                  dataIndex: "index",
                  key: "index",
                  width: 100,
                  align: "center",
                  render: (_, __, index) => index + 1,
                },
                ...Object.keys(testSqlResult[0]).map((k) => ({
                  title: k,
                  dataIndex: k,
                  key: k,
                  width: 150,
                  ellipsis: { showTitle: true },
                })),
              ]}
              dataSource={testSqlResult}
              pagination={false}
              size="small"
              bordered={true}
              scroll={{ x: 735 }}
            />
          )}
          {testSqlResult && testSqlResult.length === 0 && (
            <Empty description="暂无数据" />
          )}
        </>
      );
    }
    return (
      <>
        <Form.Item
          name="apiUrl"
          label="API URL"
          rules={[{ required: true, message: "请输入URL" }]}
        >
          <Input placeholder="请输入API地址" />
        </Form.Item>
        <Form.Item
          name="apiMethod"
          label="请求方法"
          rules={[{ required: true }]}
        >
          <Radio.Group
            optionType="button"
            options={["GET", "POST", "PUT", "DELETE"].map((m) => ({
              label: m,
              value: m,
            }))}
          />
        </Form.Item>

        <Form.Item label="请求头">
          <Form.List name="apiHeaders">
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
                      rules={[{ required: true, message: "Key必填" }]}
                    >
                      <Input placeholder="Key" />
                    </Form.Item>
                    <Form.Item
                      {...restField}
                      name={[name, "value"]}
                      rules={[{ required: true, message: "Value必填" }]}
                    >
                      <Input placeholder="Value" />
                    </Form.Item>
                    <MinusCircleOutlined onClick={() => remove(name)} />
                  </Space>
                ))}
                <Form.Item>
                  <Button
                    type="dashed"
                    onClick={() => add()}
                    block
                    icon={<PlusOutlined />}
                  >
                    添加请求头
                  </Button>
                </Form.Item>
              </>
            )}
          </Form.List>
        </Form.Item>

        <Form.Item label="请求参数">
          <Form.List name="apiParams">
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
                      rules={[{ required: true, message: "Key必填" }]}
                    >
                      <Input placeholder="Key" />
                    </Form.Item>
                    <Form.Item
                      {...restField}
                      name={[name, "value"]}
                      rules={[{ required: true, message: "Value必填" }]}
                    >
                      <Input placeholder="Value" />
                    </Form.Item>
                    <MinusCircleOutlined onClick={() => remove(name)} />
                  </Space>
                ))}
                <Form.Item>
                  <Button
                    type="dashed"
                    onClick={() => add()}
                    block
                    icon={<PlusOutlined />}
                  >
                    添加参数
                  </Button>
                </Form.Item>
              </>
            )}
          </Form.List>
        </Form.Item>

        <Form.Item name="apiBody" label="请求体">
          <Input.TextArea rows={4} placeholder="JSON Body" />
        </Form.Item>
      </>
    );
  };

  const renderInput = () => (
    <Form.List name="inputFields">
      {(fields, { add, remove }) => (
        <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
          {fields.map(({ key, name, ...restField }) => (
            <Card
              size="small"
              key={key}
              style={{ marginBottom: 0 }}
              bodyStyle={{ padding: 12 }}
            >
              <Row gutter={16} align="middle">
                <Col span={8}>
                  <Form.Item
                    {...restField}
                    name={[name, "key"]}
                    label="Key"
                    rules={[{ required: true }]}
                    style={{ marginBottom: 0 }}
                  >
                    <Input placeholder="key" />
                  </Form.Item>
                </Col>
                <Col span={8}>
                  <Form.Item
                    {...restField}
                    name={[name, "type"]}
                    label="类型"
                    style={{ marginBottom: 0 }}
                  >
                    <Select options={inputFieldTypes} />
                  </Form.Item>
                </Col>
                <Col span={4}>
                  <Form.Item
                    {...restField}
                    name={[name, "required"]}
                    label="必填"
                    valuePropName="checked"
                    style={{ marginBottom: 0 }}
                  >
                    <Switch size="small" />
                  </Form.Item>
                </Col>
                <Col span={4} style={{ textAlign: "right" }}>
                  <Button
                    type="text"
                    danger
                    icon={<DeleteOutlined />}
                    onClick={() => remove(name)}
                  />
                </Col>
              </Row>
            </Card>
          ))}
          <Button
            type="dashed"
            onClick={() => add({ type: FieldType.STRING, required: true })}
            block
            icon={<PlusOutlined />}
          >
            添加输入参数
          </Button>
        </div>
      )}
    </Form.List>
  );

  const renderOutput = () => (
    <SchemaFieldEditor name={["outputFields"]} fieldTypes={fieldTypes} />
  );

  const handleClose = () => {
    onClose?.();
    setError("");
    setTestSqlResult(null);
  };

  return (
    <>
      <AiGenerateSqlDrawer
        open={sqlGenerateModalOpen}
        onClose={() => setSqlGenerateModalOpen(false)}
        onSubmit={(data) => {
          form.setFieldsValue({
            script: data.script,
            sql: data.sql,
            outputFields: data.output?.fields || [],
          });
          setSqlGenerateModalOpen(false);
        }}
        datasources={datasources || []}
      />
      <Drawer
        title={initial?.id ? "编辑数据集" : "新建数据集"}
        open={open}
        onClose={handleClose}
        size={800}
        extra={
          <Space>
            {type === DataSetType.SQL && (
              <Permission value="tenant:sa:dataset:generate-sql">
                <Button
                  onClick={() => setSqlGenerateModalOpen(true)}
                  loading={createMut.isPending || updateMut.isPending}
                >
                  <IconFont type="icon-llm" /> AI一句话生成
                </Button>
              </Permission>
            )}
            <Permission value={["tenant:sa:dataset:create", "tenant:sa:dataset:update"]}>
              <Button
                type="primary"
                icon={<SaveOutlined />}
                onClick={handleOk}
                loading={createMut.isPending || updateMut.isPending}
              >
                保存
              </Button>
            </Permission>
          </Space>
        }
        destroyOnHidden={true}
        styles={{ body: { paddingTop: 0 } }}
      >
        <Form form={form} layout="vertical">
          <Tabs
            defaultActiveKey="basic"
            items={[
              {
                key: "basic",
                label: "基础信息",
                forceRender: true,
                children: (
                  <>
                    <Form.Item
                      name="name"
                      label="名称"
                      rules={[{ required: true }]}
                    >
                      <Input placeholder="请输入数据集名称" />
                    </Form.Item>
                    <Form.Item name="folderId" label="文件夹">
                      <TreeSelect
                        allowClear={true}
                        treeLine={true}
                        treeData={convertFoldersToTree(folders || [])}
                        placeholder="选择文件夹"
                        treeDefaultExpandAll
                      />
                    </Form.Item>
                    <Form.Item
                      name="type"
                      label="类型"
                      rules={[{ required: true }]}
                    >
                      <Radio.Group optionType="button">
                        <Radio value={DataSetType.SQL}>SQL</Radio>
                        <Radio value={DataSetType.API}>API</Radio>
                      </Radio.Group>
                    </Form.Item>
                    <Form.Item name="status" label="状态">
                      <Radio.Group optionType="button">
                        <Radio value={EnableStatus.ENABLE}>启用</Radio>
                        <Radio value={EnableStatus.DISABLE}>禁用</Radio>
                      </Radio.Group>
                    </Form.Item>
                    <Form.Item name="description" label="描述">
                      <Input.TextArea placeholder="请输入描述" />
                    </Form.Item>
                  </>
                ),
              },
              {
                key: "config",
                label: "配置",
                forceRender: true,
                children: renderConfig(),
              },
              {
                key: "input",
                label: "输入",
                forceRender: true,
                children: renderInput(),
              },
              {
                key: "output",
                label: "输出",
                forceRender: true,
                children: (
                  <>
                    <BlockLabelFormItem
                      name="script"
                      label={
                        <div
                          style={{
                            display: "flex",
                            alignItems: "center",
                            justifyContent: "space-between",
                            width: "100%",
                          }}
                        >
                          <Space>
                            数据处理脚本{" "}
                            <HelpIcon title="支持 ES2025 语法，最后一行必须为return语句" />
                          </Space>
                          <a
                            onClick={() => {
                              /* formatting via Monaco action not exposed here */
                              const editor = (refScript.current as any)?.editor;
                              editor
                                ?.getAction("editor.action.formatDocument")
                                ?.run();
                            }}
                          >
                            <IconFont type="icon-formate" /> 格式化
                          </a>
                        </div>
                      }
                    >
                      <div style={{ border: "1px solid #d9d9d9" }}>
                        <div className="function-wrapper">
                          function normalize(data) {"{"}
                        </div>
                        <Editor
                          height="200px"
                          onMount={(editor) => {
                            (refScript.current as any) = { editor };
                          }}
                          defaultLanguage="javascript"
                          value={form.getFieldValue("script")}
                          onChange={(v) =>
                            form.setFieldsValue({ script: v || "" })
                          }
                          options={{
                            minimap: { enabled: false },
                            scrollBeyondLastLine: false,
                            tabSize: 2,
                          }}
                        />
                        <div className="function-wrapper">{"}"}</div>
                      </div>
                    </BlockLabelFormItem>
                    <div style={{ marginBottom: 8 }}>输出字段配置:</div>
                    {renderOutput()}
                  </>
                ),
              },
            ]}
          />
        </Form>
      </Drawer>
    </>
  );
};
