import IconFont from "@/components/icon/IconFont";
import FixedPagination from "@/components/pagination/FixedPagination";
import { Permission } from "@/permission";
import {
  Button,
  ButtonProps,
  Dropdown,
  DropdownProps,
  Form,
  Input,
  Space,
  Tabs,
} from "antd";
import React, { useEffect, useMemo, useState } from "react";
import styled from "styled-components";
import {
  listIcons,
  SysIconQueryRequest,
  SysIconResponse,
} from "../../views/tenant/services";

const SvgCard = styled.div`
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  gap: 4px;
  position: relative;

  .anticon {
    font-size: 30px;
    color: var(--ant-color-text-secondary);
  }

  .name {
    font-size: 12px;
    text-align: center;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  a {
    position: absolute;
    top: 0;
    right: 0;
    width: 100%;
    height: 100%;
    display: none;
    align-items: center;
    justify-content: center;
    background-color: rgba(0, 0, 0, 0.4);
    border-radius: 4px;
    cursor: pointer;

    .anticon {
      color: #ffffff;
      font-size: 30px;
    }
  }

  &:hover a {
    display: flex;
  }

  .description {
    font-size: 12px;
    color: #666;
    text-align: left;
    height: 32px;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
    overflow: hidden;
  }
`;

const embedIcons = [
  "question",
  "paper-clip",
  "at",
  "audio",
  "chat-plus",
  "ai-compute",
  "llm",
  "textarea",
  "AI",
  "input-number",
  "json",
  "setting",
  "dataset",
  "report-page",
  "report-pane",
  "tenant",
  "group",
  "key",
  "app-store",
  "user-setting",
  "delete-square",
  "square-inner",
  "tabs-square",
  "tabs",
  "sun",
  "moon",
  "magic-fill",
  "excel",
  "zoom-reset",
  "form-switch",
  "container",
  "space",
  "cascade",
  "thunderbolt",
  "search",
  "image",
  "text",
  "style",
  "formate",
  "drag",
  "assistant",
  "info",
  "form-input-number",
  "form-checkbox",
  "button",
  "html",
  "form-select",
  "form-input",
  "table",
  "unordered-list",
  "grid-columns",
  "grid-row",
  "echarts",
  "form-capsule",
  "page-header",
  "form-date-picker",
  "card",
  "js",
  "datasource",
  "puzzle",
  "outline",
  "code",
  "layers",
  "message-plus",
  "sider-l",
  "sider-r",
  "sub-script",
  "super-script",
  "variable",
];

/**
 * 租户图标管理页面
 */
const IconPicker: React.FC<{
  value?: string;
  placement?: DropdownProps["placement"];
  trigger?: DropdownProps["trigger"];
  size?: ButtonProps["size"];
  onChange?: (value?: string) => void;
}> = ({ value, placement, trigger, size, onChange }) => {
  const [queryForm] = Form.useForm<SysIconQueryRequest>();
  const [items, setItems] = useState<SysIconResponse[]>([]);
  const [current, setCurrent] = useState(1);
  const [pageSize, setPageSize] = useState(40);
  const [total, setTotal] = useState(0);
  const [icon, setIcon] = useState(value);

  const onSearch = async (page?: number, size?: number) => {
    const values = queryForm.getFieldsValue();
    const list = await listIcons({
      ...values,
      page: page || current,
      size: size || pageSize,
    });
    setItems(list.data.items || []);
    setTotal(list.data.total || 0);
  };

  const handleSelect = async (id: string) => {
    const next = `icon-${id}`;
    setIcon(next);
    onChange?.(next);
  };

  const iconCards = useMemo(() => {
    return items.map((item: SysIconResponse, index) => (
      <SvgCard key={index}>
        <div>
          <IconFont type={`icon-${item.id}`} />
        </div>
        <div className="name">{item.name}</div>
        <Permission value="tenant:sa:icon:delete">
          <a onClick={() => handleSelect(item.id)} style={{ color: "#ffffff" }}>
            选择
          </a>
        </Permission>
      </SvgCard>
    ));
  }, [items]);

  useEffect(() => {
    onSearch();
  }, []);

  useEffect(() => {
    setIcon(value);
  }, [value]);

  return (
    <Dropdown
      placement={placement}
      trigger={trigger}
      popupRender={() => (
        <div
          style={{
            width: 540,
            padding: 20,
            borderRadius: 10,
            backgroundColor: "white",
            boxShadow: "var(--ant-box-shadow-drawer-up)",
            maxHeight: 500,
            overflowY: "auto",
          }}
        >
          <Tabs>
            <Tabs.TabPane tab="内建图标" key="embed">
              <div
                style={{
                  display: "flex",
                  flexWrap: "wrap",
                  justifyContent: "flex-start",
                  gap: 20,
                }}
              >
                {embedIcons.map((item, index) => (
                  <SvgCard key={index}>
                    <div>
                      <IconFont type={`icon-${item}`} />
                    </div>
                    <div className="name">{item}</div>
                    <Permission value="tenant:sa:icon:delete">
                      <a
                        onClick={() => handleSelect(item)}
                        style={{ color: "#ffffff" }}
                      >
                        选择
                      </a>
                    </Permission>
                  </SvgCard>
                ))}
              </div>
            </Tabs.TabPane>
            <Tabs.TabPane tab="自定义图标" key="custom">
              <Form
                form={queryForm as any}
                layout="inline"
                onFinish={() => onSearch()}
                style={{ marginTop: 12, marginBottom: 24 }}
              >
                <Form.Item name="name" label="名称">
                  <Input size="small" allowClear placeholder="按名称筛选" />
                </Form.Item>
                {/* <Form.Item name="keyword" label="关键字">
              <Input size="small" allowClear placeholder="按名称或描述筛选" />
            </Form.Item> */}
                <Form.Item>
                  <Space>
                    <Button size="small" htmlType="submit">
                      查询
                    </Button>
                  </Space>
                </Form.Item>
              </Form>

              <div
                style={{
                  display: "flex",
                  flexWrap: "wrap",
                  justifyContent: "flex-start",
                  gap: 20,
                }}
              >
                {iconCards}
              </div>
              <div
                style={{
                  marginTop: 30,
                  display: "flex",
                  justifyContent: "center",
                }}
              >
                <FixedPagination
                  size="small"
                  current={current}
                  pageSize={pageSize}
                  total={total}
                  onChange={(page, size) => {
                    setCurrent(page);
                    setPageSize(size);
                    onSearch(page, size);
                  }}
                />
              </div>
            </Tabs.TabPane>
          </Tabs>
        </div>
      )}
    >
      <Button size={size}>
        <IconFont
          type={icon || "icon-delete-square"}
          style={{ color: "var(--ant-color-primary-active)" }}
        />{" "}
        选择图标
      </Button>
    </Dropdown>
  );
};

export default IconPicker;
