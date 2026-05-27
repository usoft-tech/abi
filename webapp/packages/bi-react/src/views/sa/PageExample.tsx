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
  createPageExample,
  deletePageExample,
  listPageExamples,
  updatePageExample,
  type PageExampleCreateRequest,
  type PageExampleQueryRequest,
  type PageExampleResponse,
  type PageExampleUpdateRequest,
} from "./services";
import CropUpload from "@/components/upload/CropUpload";

/**
 * 查询工具条
 */
const QueryBar: React.FC<{
  query: PageExampleQueryRequest;
  onChange: (q: PageExampleQueryRequest) => void;
}> = ({ query, onChange }) => {
  const [form] = Form.useForm<PageExampleQueryRequest>();
  return (
    <Form<PageExampleQueryRequest>
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
 * 页面片段管理
 */
const PageExample: React.FC = () => {
  const [query, setQuery] = useState<PageExampleQueryRequest>({
    page: 1,
    size: 12,
  });
  const [visible, setVisible] = useState(false);
  const [editing, setEditing] = useState<PageExampleResponse | null>(null);
  const [form] = Form.useForm();
  const cardListRef = useRef<any>(null);

  const refresh = () => cardListRef.current?.refresh();

  const createMutation = useMutation({
    mutationFn: createPageExample,
    onSuccess: () => {
      message.success("创建成功");
      setVisible(false);
      refresh();
    },
  });

  const updateMutation = useMutation({
    mutationFn: updatePageExample,
    onSuccess: () => {
      message.success("更新成功");
      setVisible(false);
      refresh();
    },
  });

  const deleteMutation = useMutation({
    mutationFn: deletePageExample,
    onSuccess: () => {
      message.success("删除成功");
      refresh();
    },
  });

  const handleAdd = () => {
    setEditing(null);
    form.resetFields();
    form.setFieldsValue({ item: "{}" });
    setVisible(true);
  };

  const handleEdit = (record: PageExampleResponse) => {
    setEditing(record);
    form.setFieldsValue({
      ...record,
      item: JSON.stringify(record.item, null, 2),
    });
    setVisible(true);
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      const payload = {
        ...values,
        item: values.item ? JSON.parse(values.item) : undefined,
      };

      if (editing) {
        updateMutation.mutate({
          ...payload,
          id: editing.id,
        } as PageExampleUpdateRequest);
      } else {
        createMutation.mutate(payload as PageExampleCreateRequest);
      }
    } catch (e) {
      if (e instanceof SyntaxError) {
        message.error("JSON 格式错误");
      }
    }
  };

  return (
    <>
      <RemoteCardList<PageExampleResponse>
        ref={cardListRef}
        title="页面片段管理"
        fetchKey={["pageExamples", query]}
        fetchData={() => listPageExamples(query) as any}
        rowKey="id"
        titleKey="name"
        queryBar={<QueryBar query={query} onChange={setQuery} />}
        titleExtra={
          <Permission value="sa:page-example:create">
            <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
              新增片段
            </Button>
          </Permission>
        }
        descriptionKey="description"
        coverKey="cover"
        actionsRender={(record) => [
          <Permission value="sa:page-example:update">
            <EditIcon
              onEdit={() => handleEdit(record)}
              label="编辑"
              style={{ fontSize: 12 }}
            />
          </Permission>,
          <Permission value="sa:page-example:delete">
            <DeleteIcon
              name="片段"
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
        title={editing ? "编辑页面片段" : "新增页面片段"}
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
            <Input placeholder="请输入片段名称" />
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
            <Input.TextArea placeholder="请输入片段描述" rows={3} />
          </Form.Item>
          <Form.Item
            name="item"
            label="片段配置 (JSON)"
            rules={[{ required: true, message: "请输入片段配置" }]}
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
                onChange={(value) => form.setFieldsValue({ item: value })}
                value={form.getFieldValue("item")}
              />
            </div>
          </Form.Item>
        </Form>
      </Drawer>
    </>
  );
};

export default PageExample;
