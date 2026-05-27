import QueryButton from "@/components/button/QueryButton";
import DeleteIcon from "@/components/icon/DeleteIcon";
import EditIcon from "@/components/icon/EditIcon";
import RemoteCardList from "@/components/list/RemoteCardList";
import { Permission } from "@/permission";
import { PlusOutlined } from "@ant-design/icons";
import { Editor } from "@monaco-editor/react";
import { useMutation } from "@tanstack/react-query";
import { Button, Drawer, Form, Input, message, Space } from "antd";
import React, { useRef, useState } from "react";
import {
  createPageTemplate,
  deletePageTemplate,
  listPageTemplates,
  updatePageTemplate,
  type PageTemplateCreateRequest,
  type PageTemplateQueryRequest,
  type PageTemplateResponse,
  type PageTemplateUpdateRequest,
} from "./services";
import CropUpload from "@/components/upload/CropUpload";

/**
 * 查询工具条
 */
const QueryBar: React.FC<{
  query: PageTemplateQueryRequest;
  onChange: (q: PageTemplateQueryRequest) => void;
}> = ({ query, onChange }) => {
  const [form] = Form.useForm<PageTemplateQueryRequest>();
  return (
    <Form<PageTemplateQueryRequest>
      form={form}
      layout="inline"
      initialValues={query}
      onFinish={(values) => onChange({ ...query, ...values, page: 1 })}
      style={{ marginBottom: 16 }}
    >
      <Form.Item name="keyword" label="关键字">
        <Input placeholder="名称或描述" allowClear />
      </Form.Item>
      <Form.Item>
        <QueryButton />
      </Form.Item>
    </Form>
  );
};

/**
 * 页面模板管理
 */
const PageTemplate: React.FC = () => {
  const [query, setQuery] = useState<PageTemplateQueryRequest>({
    page: 1,
    size: 12,
  });
  const [visible, setVisible] = useState(false);
  const [editing, setEditing] = useState<PageTemplateResponse | null>(null);
  const [form] = Form.useForm();
  const cardListRef = useRef<any>(null);

  const refresh = () => cardListRef.current?.refresh();

  const createMutation = useMutation({
    mutationFn: createPageTemplate,
    onSuccess: () => {
      message.success("创建成功");
      setVisible(false);
      refresh();
    },
  });

  const updateMutation = useMutation({
    mutationFn: updatePageTemplate,
    onSuccess: () => {
      message.success("更新成功");
      setVisible(false);
      refresh();
    },
  });

  const deleteMutation = useMutation({
    mutationFn: deletePageTemplate,
    onSuccess: () => {
      message.success("删除成功");
      refresh();
    },
  });

  const handleAdd = () => {
    setEditing(null);
    form.resetFields();
    form.setFieldsValue({ schema: "{}" });
    setVisible(true);
  };

  const handleEdit = (record: PageTemplateResponse) => {
    setEditing(record);
    form.setFieldsValue({
      ...record,
      schema: JSON.stringify(record.schema, null, 2),
    });
    setVisible(true);
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      const payload = {
        ...values,
        schema: values.schema ? JSON.parse(values.schema) : undefined,
      };

      if (editing) {
        updateMutation.mutate({
          ...payload,
          id: editing.id,
        } as PageTemplateUpdateRequest);
      } else {
        createMutation.mutate(payload as PageTemplateCreateRequest);
      }
    } catch (e) {
      if (e instanceof SyntaxError) {
        message.error("JSON 格式错误");
      }
    }
  };

  return (
    <>
      <RemoteCardList<PageTemplateResponse>
        ref={cardListRef}
        title="页面模板管理"
        fetchKey={["pageTemplates", query]}
        fetchData={() => listPageTemplates(query) as any}
        rowKey="id"
        titleKey="name"
        queryBar={<QueryBar query={query} onChange={setQuery} />}
        titleExtra={
          <Permission value="sa:page-template:create">
            <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
              新增模板
            </Button>
          </Permission>
        }
        descriptionKey="description"
        coverKey="cover"
        actionsRender={(record) => [
          <Permission value="sa:page-template:update">
            <EditIcon
              onEdit={() => handleEdit(record)}
              label="编辑"
              style={{ fontSize: 12 }}
            />
          </Permission>,
          <Permission value="sa:page-template:delete">
            <DeleteIcon
              name="模板"
              onDelete={() => deleteMutation.mutate(record.id)}
              label="删除"
              style={{ fontSize: 12 }}
            />
          </Permission>,
        ]}
        grid={{ gutter: 16, xs: 1, sm: 2, md: 3, lg: 4, xl: 4, xxl: 6 }}
        pagination={{
          current: query.page,
          pageSize: query.size,
          onChange: (page, size) => setQuery({ ...query, page, size }),
        }}
      />

      <Drawer
        title={editing ? "编辑页面模板" : "新增页面模板"}
        open={visible}
        onClose={() => setVisible(false)}
        size={800}
        extra={
          <Space>
            <Button onClick={() => setVisible(false)}>取消</Button>
            <Button
              type="primary"
              onClick={handleSubmit}
              loading={createMutation.isPending || updateMutation.isPending}
            >
              确定
            </Button>
          </Space>
        }
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="name"
            label="名称"
            rules={[{ required: true, message: "请输入名称" }]}
          >
            <Input placeholder="请输入模板名称" />
          </Form.Item>
          <Form.Item name="cover" label="封面">
            <CropUpload
              bizType="cover"
              maxCount={1}
              aspectSlider={false}
              aspect={1.78}
              fileList={editing?.cover ? [editing.cover] : []}
              onChange={(fileList) => {
                form.setFieldValue(
                  "cover",
                  fileList?.length ? fileList[0].url : null,
                );
              }}
            />
          </Form.Item>
          <Form.Item name="description" label="描述">
            <Input.TextArea placeholder="请输入模板描述" rows={3} />
          </Form.Item>
          <Form.Item
            name="schema"
            label="模板配置 (JSON)"
            rules={[{ required: true, message: "请输入模板配置" }]}
          >
            <div style={{ border: "1px solid #d9d9d9", borderRadius: 2 }}>
              <Editor
                height="400px"
                defaultLanguage="json"
                options={{
                  minimap: { enabled: false },
                  fontSize: 14,
                  scrollBeyondLastLine: false,
                }}
                onChange={(value) => form.setFieldsValue({ schema: value })}
                value={form.getFieldValue("schema")}
              />
            </div>
          </Form.Item>
        </Form>
      </Drawer>
    </>
  );
};

export default PageTemplate;
