import React from "react";
import { Form, Select } from "antd";
import type { PropEditorProps } from "bi-sdk-react";

export type ContainerModel = {
  htmlTag?: string;
  classNames?: string[];
};

export const ContainerProps: React.FC<PropEditorProps<ContainerModel>> = ({
  model,
  onChange,
}) => {
  const trigger = (key: keyof ContainerModel, value: any) =>
    onChange && onChange({ ...model, [key]: value });
  return (
    <Form layout="vertical">
      <Form.Item label="HTML标签">
        <Select
          size="small"
          value={model.htmlTag}
          onChange={(value) => trigger("htmlTag", value)}
        >
          <Select.Option value="div">块容器</Select.Option>
          <Select.Option value="span">行内容器</Select.Option>
          <Select.Option value="a">链接</Select.Option>
          <Select.Option value="section">章节容器</Select.Option>
          <Select.Option value="h1">一级标题</Select.Option>
          <Select.Option value="h2">二级标题</Select.Option>
          <Select.Option value="h3">三级标题</Select.Option>
          <Select.Option value="h4">四级标题</Select.Option>
          <Select.Option value="h5">五级标题</Select.Option>
          <Select.Option value="h6">六级标题</Select.Option>
          <Select.Option value="p">段落</Select.Option>
          <Select.Option value="pre">预格式化文本</Select.Option>
          <Select.Option value="main">主要内容</Select.Option>
          <Select.Option value="header">页头</Select.Option>
          <Select.Option value="footer">页脚</Select.Option>
        </Select>
      </Form.Item>
      <Form.Item label="类名">
        <Select
          size="small"
          value={model.classNames}
          mode="tags"
          onChange={(value) => trigger("classNames", value)}
        />
      </Form.Item>
    </Form>
  );
};
