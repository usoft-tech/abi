import QueryButton from "@/components/button/QueryButton";
import DeleteIcon from "@/components/icon/DeleteIcon";
import EditIcon from "@/components/icon/EditIcon";
import RemoteCardList from "@/components/list/RemoteCardList";
import MarkdownEditor from "@/components/markdown/MarkdownEditor";
import { PlusOutlined, UploadOutlined } from "@ant-design/icons";
import { useMutation } from "@tanstack/react-query";
import {
  Button,
  Drawer,
  Flex,
  Form,
  Input,
  message,
  Modal,
  Space,
  Upload,
  UploadProps,
} from "antd";
import "bi-sdk-react/dist/es/css/bi-sdk.css";
import React, { useEffect, useRef, useState } from "react";
import {
  AgentSkill,
  createSkill,
  deleteSkill,
  getSkill,
  importSkill,
  listSkills,
  SkillListItemResponse,
  updateSkill,
} from "./services";
import { Permission } from "@/permission";

/**
 * 查询条件工具条
 */
const QueryBar: React.FC<{
  query: SkillListItemResponse;
  onChange: (q: SkillListItemResponse) => void;
}> = ({ query, onChange }) => {
  const [form] = Form.useForm<SkillListItemResponse>();
  return (
    <Form<SkillListItemResponse>
      form={form}
      layout="inline"
      initialValues={{
        name: query.name,
      }}
      onFinish={(values: SkillListItemResponse) =>
        onChange({ ...query, ...values })
      }
      style={{ marginBottom: 12 }}
    >
      <Form.Item name="name" label="名称">
        <Input placeholder="请输入技能名称" allowClear />
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
type AgentSkillFormValues = Omit<AgentSkill, "resources"> & {
  resourceList: { key: string; value: string }[];
};

/**
 * 资源编辑弹窗
 */
const ResourceEditModal: React.FC<{
  open: boolean;
  initialValues?: { key: string; value: string };
  onCancel: () => void;
  onOk: (values: { key: string; value: string }) => void;
}> = ({ open, initialValues, onCancel, onOk }) => {
  const [form] = Form.useForm();

  useEffect(() => {
    if (open) {
      form.resetFields();
      if (initialValues) {
        form.setFieldsValue(initialValues);
      }
    }
  }, [open, initialValues, form]);

  const handleOk = async () => {
    try {
      const values = await form.validateFields();
      onOk(values);
    } catch (e) {
      console.error(e);
    }
  };

  return (
    <Modal
      title={initialValues ? "编辑资源" : "添加资源"}
      open={open}
      onCancel={onCancel}
      onOk={handleOk}
      width={800}
      keyboard={false}
      destroyOnHidden
    >
      <Form form={form} layout="vertical">
        <Form.Item
          name="key"
          label="资源路径"
          rules={[{ required: true, message: "请输入资源路径" }]}
        >
          <Input placeholder="资源路径" />
        </Form.Item>
        <Form.Item
          name="value"
          label="资源内容"
          rules={[{ required: true, message: "请输入资源内容" }]}
        >
          <MarkdownEditor height="400px" />
        </Form.Item>
      </Form>
    </Modal>
  );
};

const EditModal: React.FC<{
  open: boolean;
  initial?: AgentSkill | null;
  onCancel: () => void;
  onSubmit: (values: AgentSkill) => void;
  loading?: boolean;
}> = ({ open, initial, onCancel, onSubmit, loading }) => {
  const [form] = Form.useForm<AgentSkillFormValues>();
  const [resourceModalOpen, setResourceModalOpen] = useState(false);
  const [editingResource, setEditingResource] = useState<{
    index: number;
    values: { key: string; value: string };
  } | null>(null);

  useEffect(() => {
    if (!open) {
      form.resetFields();
    } else {
      const resourceList = initial?.resources
        ? Object.entries(initial.resources).map(([key, value]) => ({
            key,
            value,
          }))
        : [];
      form.setFieldsValue({ ...initial, resourceList } as any);
    }
  }, [open, form, initial]);

  const handleSave = async () => {
    try {
      const values = await form.validateFields();
      const resources: Record<string, string> = {};
      values.resourceList?.forEach((item) => {
        if (item.key) resources[item.key] = item.value;
      });
      const { resourceList, ...rest } = values;
      onSubmit({ ...rest, resources } as AgentSkill);
    } catch (e) {
      console.error(e);
    }
  };

  const handleAddResource = () => {
    setEditingResource(null);
    setResourceModalOpen(true);
  };

  const handleEditResource = (
    index: number,
    values: { key: string; value: string },
  ) => {
    setEditingResource({ index, values });
    setResourceModalOpen(true);
  };

  const handleResourceOk = (values: { key: string; value: string }) => {
    const list = form.getFieldValue("resourceList") || [];
    if (editingResource) {
      const newList = [...list];
      newList[editingResource.index] = values;
      form.setFieldValue("resourceList", newList);
    } else {
      form.setFieldValue("resourceList", [...list, values]);
    }
    setResourceModalOpen(false);
  };

  return (
    <Drawer
      title={
        <Flex justify="space-between" align="center">
          {initial?.name ? "编辑技能" : "新建技能"}
          <Button type="primary" onClick={handleSave} loading={loading}>
            保存
          </Button>
        </Flex>
      }
      size={800}
      placement="right"
      open={open}
      keyboard={false}
      onClose={onCancel}
      destroyOnHidden
    >
      <Form<AgentSkillFormValues> form={form} layout="vertical">
        <Form.Item
          name="name"
          label="名称"
          rules={[{ required: true, message: "请输入技能名称" }]}
        >
          <Input placeholder="请输入技能名称" />
        </Form.Item>
        <Form.Item
          name="description"
          label="描述"
          rules={[{ required: true, message: "请输入描述" }]}
        >
          <Input.TextArea placeholder="请输入描述" rows={3} />
        </Form.Item>
        <Form.Item
          name="skillContent"
          label="技能内容"
          rules={[{ required: true, message: "请输入技能内容" }]}
        >
          <MarkdownEditor
            onChange={(value) => form.setFieldValue("skillContent", value)}
          />
        </Form.Item>

        <Form.List name="resourceList">
          {(fields, { remove }) => (
            <>
              <Flex
                justify="space-between"
                align="center"
                style={{ marginBottom: 8 }}
              >
                <span style={{ fontWeight: 500 }}>资源配置</span>
              </Flex>
              {fields.map((field, index) => (
                <React.Fragment key={field.key}>
                  <Form.Item
                    name={[field.name, "key"]}
                    hidden
                    rules={[{ required: true, message: "请输入Key" }]}
                  >
                    <Input />
                  </Form.Item>
                  <Form.Item
                    name={[field.name, "value"]}
                    hidden
                    rules={[{ required: true, message: "请输入Value" }]}
                  >
                    <Input />
                  </Form.Item>
                  <Form.Item
                    shouldUpdate={(prev, curr) =>
                      prev.resourceList?.[index] !== curr.resourceList?.[index]
                    }
                    noStyle
                  >
                    {({ getFieldValue }) => {
                      const resource = getFieldValue([
                        "resourceList",
                        field.name,
                      ]);
                      return (
                        <div
                          style={{
                            marginBottom: 16,
                            padding: 12,
                            border: "1px solid #f0f0f0",
                            borderRadius: 4,
                            background: "#fafafa",
                          }}
                        >
                          <Flex
                            justify="space-between"
                            align="center"
                            style={{ marginBottom: 8 }}
                          >
                            <span style={{ fontWeight: 600 }}>
                              {resource?.key}
                            </span>
                            <Space>
                              <EditIcon
                                label="编辑"
                                onEdit={() =>
                                  handleEditResource(index, resource)
                                }
                              />
                              <DeleteIcon
                                label="删除"
                                name="资源"
                                onDelete={() => remove(field.name)}
                              />
                            </Space>
                          </Flex>
                          <div
                            style={{
                              color: "#666",
                              fontSize: 12,
                              maxHeight: 60,
                              overflow: "hidden",
                              textOverflow: "ellipsis",
                              display: "-webkit-box",
                              WebkitLineClamp: 3,
                              WebkitBoxOrient: "vertical",
                            }}
                          >
                            {resource?.value}
                          </div>
                        </div>
                      );
                    }}
                  </Form.Item>
                </React.Fragment>
              ))}
              <Button
                type="dashed"
                onClick={handleAddResource}
                block
                icon={<PlusOutlined />}
              >
                添加资源
              </Button>
            </>
          )}
        </Form.List>
      </Form>
      <ResourceEditModal
        open={resourceModalOpen}
        initialValues={editingResource?.values}
        onCancel={() => setResourceModalOpen(false)}
        onOk={handleResourceOk}
      />
    </Drawer>
  );
};

const Skill: React.FC = () => {
  const tableRef = useRef<any>(null);

  const [queryParams, setQueryParams] = useState<SkillListItemResponse>({
    name: "",
    description: "",
  });
  const [pageParams, setPageParams] = useState<{ page: number; size: number }>({
    page: 1,
    size: 10,
  });

  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<AgentSkill | null>(null);

  const refreshTable = () => {
    tableRef.current?.refresh();
  };

  const handleCreate = () => {
    setEditing(null);
    setModalOpen(true);
  };

  const handleEdit = async (record: SkillListItemResponse) => {
    const skill = await getSkill(record.name);
    setEditing(skill.data || {});
    setModalOpen(true);
  };

  const createMut = useMutation({
    mutationFn: createSkill,
    onSuccess: () => {
      message.success("创建成功");
      setModalOpen(false);
      setEditing(null);
      refreshTable();
    },
    onError: (err: any) => message.error(err?.message || "创建失败"),
  });

  const updateMut = useMutation({
    mutationFn: updateSkill,
    onSuccess: () => {
      message.success("更新成功");
      setModalOpen(false);
      setEditing(null);
      refreshTable();
    },
    onError: (err: any) => message.error(err?.message || "更新失败"),
  });

  const deleteMut = useMutation({
    mutationFn: deleteSkill,
    onSuccess: () => {
      message.success("删除成功");
      refreshTable();
    },
    onError: (err: any) => message.error(err?.message || "删除失败"),
  });

  const queryBar = <QueryBar query={queryParams} onChange={setQueryParams} />;

  const pagination = {
    current: pageParams.page,
    pageSize: pageParams.size,
    onChange: (page: number, size: number) =>
      setPageParams({ ...pageParams, page, size }),
  };

  const importProps: UploadProps = {
    name: "file",
    showUploadList: false,
    accept: ".zip",
    customRequest: async (options) => {
      const { file, onSuccess, onError } = options;
      try {
        await importSkill(file as File);
        message.success("导入成功");
        onSuccess?.("ok");
        refreshTable();
      } catch (err: any) {
        message.error(err?.message || "导入失败");
        onError?.(err);
      }
    },
  };

  return (
    <>
      <RemoteCardList<SkillListItemResponse>
        ref={tableRef}
        queryBar={queryBar}
        size="small"
        title="技能管理"
        titleExtra={
          <Permission value="ai:skills:create">
            <Space>
              <Upload {...importProps}>
                <Button icon={<UploadOutlined />}>导入技能</Button>
              </Upload>
              <Button type="primary" onClick={handleCreate}>
                <PlusOutlined /> 新建技能
              </Button>
            </Space>
          </Permission>
        }
        titleKey="name"
        descriptionKey="description"
        actionsRender={(record) => [
          <Permission value="ai:skills:update">
            <EditIcon
              onEdit={() => handleEdit(record)}
              label="编辑"
              style={{ fontSize: 12 }}
            />
          </Permission>,
          <Permission value="ai:skills:delete">
            <DeleteIcon
              name="技能"
              onDelete={() => deleteMut.mutate(record.name)}
              label="删除"
              style={{ fontSize: 12 }}
            />
          </Permission>,
        ]}
        fetchKey={["pages", queryParams]}
        fetchData={() =>
          listSkills().then((res) => {
            const items = (res.data || [])
              .filter(
                (item) =>
                  !queryParams.name?.length ||
                  item.name.includes(queryParams.name) ||
                  item.description.includes(queryParams.name),
              )
              .slice(
                (pageParams.page - 1) * pageParams.size,
                pageParams.page * pageParams.size,
              );
            return {
              code: res.code,
              message: res.message,
              success: true,
              timestamp: res.timestamp,
              data: {
                page: 1,
                size: pageParams.size,
                items,
                total: items.length,
              },
            };
          })
        }
        pagination={pagination}
        selection={false}
        grid={{
          gutter: [16, 16],
          xs: 24,
          sm: 12,
          md: 8,
          lg: 6,
          xl: 6,
          xxl: 4,
        }}
        rowKey="id"
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
          if (editing) {
            updateMut.mutate(values as AgentSkill);
          } else {
            createMut.mutate(values as AgentSkill);
          }
        }}
      />
    </>
  );
};

export default Skill;
