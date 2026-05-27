import { ColorPicker, Form, Input, Select, Space } from "antd";
import type { PropEditorProps } from "bi-sdk-react";
import React from "react";

export type IconModel = {
  type?: "font-awesome" | "icon-font";
  icon?: string;
  classNames?: string[];
  customStyle?: React.CSSProperties;
};

export const IconProps: React.FC<PropEditorProps<IconModel>> = ({
  model,
  onChange,
}) => {
  const trigger = (key: keyof IconModel, value: any) =>
    onChange && onChange({ ...model, [key]: value });
  const setStyle = (key: string, value: any) => {
    const next = { ...(model.customStyle || {}), [key]: value };
    onChange && onChange({ ...model, customStyle: next } as any);
  };
  return (
    <Form layout="vertical">
      <Form.Item label="图标类型">
        <Select
          size="small"
          value={model.type}
          onChange={(value) => trigger("type", value)}
        >
          <Select.Option value="font-awesome">内置图标</Select.Option>
          <Select.Option value="icon-font">自定义图标</Select.Option>
        </Select>
      </Form.Item>
      <Form.Item label="图标">
        <Input
          size="small"
          value={model.icon}
          onChange={(e) => trigger("icon", e.target.value)}
        />
      </Form.Item>
      <Form.Item label="类名">
        <Select
          size="small"
          value={model.classNames}
          mode="tags"
          onChange={(value) => trigger("classNames", value)}
        />
      </Form.Item>
      <div
        style={{
          display: "flex",
          alignItems: "center",
          justifyContent: "space-between",
        }}
      >
        <Space.Compact>
          <Select
            size="small"
            value={model.customStyle?.fontSize}
            style={{ width: 90 }}
            onChange={(v) => setStyle("fontSize", v)}
          >
            {[
              "12px",
              "14px",
              "16px",
              "20px",
              "22px",
              "24px",
              "32px",
              "48px",
            ].map((s) => (
              <Select.Option key={s} value={s}>
                {s}
              </Select.Option>
            ))}
          </Select>
          <ColorPicker
            size="small"
            value={model.customStyle?.color}
            onChange={(v) => setStyle("color", v.toHexString())}
            allowClear={true}
          />
        </Space.Compact>
      </div>
    </Form>
  );
};
