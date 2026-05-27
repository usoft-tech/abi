import DeleteIcon from "@/components/icon/DeleteIcon";
import EditIcon from "@/components/icon/EditIcon";
import IconFont from "@/components/icon/IconFont";
import StrongTable from "@/components/table/StrongTable";
import { AppMenuType, AppResponse } from "@/services/app";
import {
  KeyOutlined,
  MinusSquareOutlined,
  PlusOutlined,
  PlusSquareOutlined,
} from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  Button,
  Form,
  Input,
  Modal,
  Radio,
  Select,
  Space,
  Switch,
  Tooltip,
  TreeSelect,
  InputNumber,
  message,
} from "antd";
import type { ColumnsType } from "antd/es/table";
import React, { useEffect, useMemo, useState } from "react";
import {
  AppCreateRequest,
  AppUpdateRequest,
  PageResp,
  createApp,
  deleteApp,
  listApps,
  listPages,
  updateApp,
} from "./services";
import IconPicker from "@/components/icon/IconPicker";
import { AuthorizeDrawer } from "./modules/AuthorizeDrawer";
import { AuthorizationBizType } from "@/services/authorization";
import { Permission } from "@/permission";

type AppFormValues = {
  menuType: AppMenuType;
  title: string;
  icon?: string;
  pageId?: string;
  appKey?: string;
  redirectUrl?: string;
  parentId?: string;
  sort?: number;
  hasWatermark?: boolean;
};
type AppFormModalChildProps = {
  title: string;
  value: string;
  key: string;
  children?: AppFormModalChildProps[];
};
type AppFormModalProps = {
  open: boolean;
  initial?: AppResponse | null;
  parentId?: string;
  onCancel: () => void;
  onSubmit: (values: AppFormValues) => void;
  loading?: boolean;
  pages: PageResp[];
  parentOptions: AppFormModalChildProps[];
};

const menuTypeOptions = [
  { label: "分组", value: AppMenuType.GROUP },
  { label: "页面", value: AppMenuType.PAGE },
];

/**
 * 构建上级应用选择树
 */
const buildParentOptions = (apps: AppResponse[]): AppFormModalChildProps[] => {
  const loop = (nodes: AppResponse[]): AppFormModalChildProps[] =>
    nodes
      .map((item) => {
        if (!item.handle) {
          return null;
        }
        if (item.handle.type !== AppMenuType.GROUP) {
          return null;
        }
        const children = item.children ? loop(item.children) : [];
        return {
          title: item.handle.title!,
          value: item.id!,
          key: item.id!,
          children,
        };
      })
      .filter((item) => item !== null) as AppFormModalChildProps[];

  return loop(apps);
};

/**
 * 应用应用编辑弹窗
 */
const AppFormModal: React.FC<AppFormModalProps> = ({
  open,
  initial,
  parentId,
  onCancel,
  onSubmit,
  loading,
  pages,
  parentOptions,
}) => {
  const [form] = Form.useForm<AppFormValues>();
  const isEdit = !!initial?.id;
  const [pageType, setPageType] = useState<"embed" | "third">("embed");

  useEffect(() => {
    if (!open) {
      form.resetFields();
      return;
    }
    if (initial && initial.handle) {
      form.setFieldsValue({
        menuType: (initial.handle.type as AppMenuType) || AppMenuType.PAGE,
        title: initial.handle.title,
        icon: initial.handle.icon || undefined,
        pageId: initial.handle.pageId || undefined,
        appKey: initial.handle.appKey || undefined,
        redirectUrl: initial.handle.redirectUrl || undefined,
        sort: initial.handle.sort || undefined,
        parentId: initial.parentId || undefined,
        hasWatermark: initial.handle.hasWatermark ?? false,
      });
    } else {
      form.setFieldsValue({
        menuType: AppMenuType.PAGE,
        parentId,
        hasWatermark: false,
      });
    }
  }, [open, initial, parentId, form]);

  /**
   * 提交表单
   */
  const handleOk = async () => {
    try {
      const values = await form.validateFields();
      onSubmit(values);
    } catch {
      // ignore
    }
  };

  const pageOptions = useMemo(
    () =>
      pages.map((p) => ({
        label: p.name,
        value: p.id,
      })),
    [pages],
  );

  const icon = Form.useWatch("icon", form);

  return (
    <Modal
      title={isEdit ? "编辑应用" : "新建应用"}
      open={open}
      onCancel={onCancel}
      onOk={handleOk}
      confirmLoading={loading}
      destroyOnHidden
    >
      <Form<AppFormValues> form={form} layout="vertical">
        <Form.Item
          name="menuType"
          label="类型"
          rules={[{ required: true, message: "请选择类型" }]}
        >
          <Radio.Group options={menuTypeOptions} optionType="button" />
        </Form.Item>
        <Form.Item
          name="title"
          label="名称"
          rules={[{ required: true, message: "请输入名称" }]}
        >
          <Input placeholder="请输入名称" />
        </Form.Item>
        <Form.Item name="icon" label="图标">
          <IconPicker
            value={icon}
            trigger={["click"]}
            onChange={(value) => form.setFieldValue("icon", value)}
          />
        </Form.Item>
        <Form.Item name="parentId" label="上级应用">
          <TreeSelect
            allowClear
            treeDefaultExpandAll
            placeholder="不选择则为顶级应用"
            treeData={parentOptions}
          />
        </Form.Item>
        <Form.Item
          noStyle
          shouldUpdate={(prev, curr) => prev.menuType !== curr.menuType}
        >
          {({ getFieldValue }) => {
            const menuType = getFieldValue("menuType") as AppMenuType;
            if (menuType !== AppMenuType.PAGE) {
              return null;
            }
            return (
              <>
                <Form.Item label="页面类型">
                  <Radio.Group
                    optionType="button"
                    options={[
                      { label: "内部页面", value: "embed" },
                      { label: "第三方页面", value: "third" },
                    ]}
                    value={pageType}
                    onChange={(e) => {
                      const value = e.target.value as "embed" | "third";
                      setPageType(value);
                      if (value === "embed") {
                        form.setFieldValue("redirectUrl", undefined);
                      }
                    }}
                  />
                </Form.Item>
                {pageType === "embed" && (
                  <Form.Item
                    name="pageId"
                    label="绑定页面"
                    rules={[{ required: true, message: "请选择页面" }]}
                  >
                    <Select
                      showSearch={{
                        optionFilterProp: "label",
                      }}
                      options={pageOptions}
                      placeholder="请选择页面"
                    />
                  </Form.Item>
                )}
                {/* <Form.Item
                  name="appKey"
                  label="App Key"
                  rules={[{ required: true, message: "请输入App Key" }]}
                >
                  <Input placeholder="同一租户下需唯一，例如: report" />
                </Form.Item> */}
                {pageType === "third" && (
                  <Form.Item name="redirectUrl" label="跳转地址">
                    <Input placeholder="可选，外部跳转URL" />
                  </Form.Item>
                )}
              </>
            );
          }}
        </Form.Item>
        <Form.Item name="sort" label="排序">
          <InputNumber min={0} style={{ width: "100%" }} />
        </Form.Item>
        <Form.Item name="hasWatermark" label="显示水印" valuePropName="checked">
          <Switch />
        </Form.Item>
      </Form>
    </Modal>
  );
};

/**
 * 构建表格列
 */
const buildColumns = (
  onCreateChild: (record: AppResponse) => void,
  onEdit: (record: AppResponse) => void,
  onDelete: (record: AppResponse) => void,
  onAuth: (record: AppResponse) => void,
): ColumnsType<AppResponse> => [
  {
    title: "名称",
    dataIndex: ["handle", "title"],
    key: "title",
    render: (title: string, record: AppResponse) => (
      <>
        <>
          {record.handle.icon && (
            <IconFont
              type={record.handle.icon!}
              style={{
                fontSize: 14,
                marginLeft: 8,
                color: "var(--ant-color-text-label)",
              }}
            />
          )}
        </>
        <span style={{ marginLeft: 8 }}>{title || "-"}</span>
      </>
    ),
  },
  // {
  //   title: "图标",
  //   dataIndex: ["handle", "icon"],
  //   key: "icon",
  //   width: 80,
  //   align: "center",
  //   render: (icon?: string) => (
  //     <>{icon && <IconFont type={icon!} style={{ fontSize: 16, color: "var(--ant-color-text-label)" }} />}</>
  //   ),
  // },
  {
    title: "类型",
    dataIndex: ["handle", "type"],
    key: "type",
    width: 80,
    render: (type: AppMenuType) =>
      type === AppMenuType.GROUP ? "分组" : "页面",
  },
  {
    title: "操作",
    key: "action",
    width: 100,
    render: (_: any, record: AppResponse) => (
      <Space size="middle">
        <Permission value="tenant:sa:app:create">
          <Tooltip title="新建子应用">
            <Button
              type="link"
              size="small"
              onClick={() => onCreateChild(record)}
              disabled={record.handle?.type !== AppMenuType.GROUP}
            >
              <PlusSquareOutlined />
            </Button>
          </Tooltip>
        </Permission>
        <Permission value="tenant:sa:app:update">
          <EditIcon onEdit={() => onEdit(record)} />
        </Permission>
        <Permission value="tenant:sa:auth:save">
          <Tooltip title="授权">
            <Button
              type="link"
              onClick={(e) => {
                e.preventDefault();
                onAuth(record);
              }}
            >
              <KeyOutlined />
            </Button>
          </Tooltip>
        </Permission>
        <Permission value="tenant:sa:app:delete">
          <DeleteIcon name="应用" onDelete={() => onDelete(record)} />
        </Permission>
      </Space>
    ),
  },
];

/**
 * 应用管理页面
 */
const App: React.FC = () => {
  const queryClient = useQueryClient();
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<AppResponse | null>(null);
  const [parentId, setParentId] = useState<string | undefined>();
  const [authModalOpen, setAuthModalOpen] = useState(false);

  const { data: appsData, isLoading: appsLoading } = useQuery({
    retry: false,
    queryKey: ["bi-apps"],
    queryFn: async () => {
      const res = await listApps();
      return res.data || [];
    },
  });

  const { data: pagesData, isLoading: pagesLoading } = useQuery({
    retry: false,
    queryKey: ["bi-pages-all"],
    queryFn: async () => {
      const res = await listPages({ page: 1, size: 1000 });
      return res.data?.items || [];
    },
  });

  const apps = appsData || [];
  const pages = pagesData || [];

  const parentOptions = useMemo(() => buildParentOptions(apps), [apps]);

  const createMut = useMutation({
    mutationFn: (payload: AppCreateRequest) => createApp(payload),
    onSuccess: () => {
      message.success("创建成功");
      setModalOpen(false);
      setEditing(null);
      setParentId(undefined);
      queryClient.invalidateQueries({ queryKey: ["bi-apps"] });
    },
    onError: (err: any) => {
      message.error(err?.message || "创建失败");
    },
  });

  const updateMut = useMutation({
    mutationFn: (params: { id: string; payload: AppUpdateRequest }) =>
      updateApp(params.id, params.payload),
    onSuccess: () => {
      message.success("更新成功");
      setModalOpen(false);
      setEditing(null);
      setParentId(undefined);
      queryClient.invalidateQueries({ queryKey: ["bi-apps"] });
    },
    onError: (err: any) => {
      message.error(err?.message || "更新失败");
    },
  });

  const deleteMut = useMutation({
    mutationFn: deleteApp,
    onSuccess: () => {
      message.success("删除成功");
      queryClient.invalidateQueries({ queryKey: ["bi-apps"] });
    },
    onError: (err: any) => {
      message.error(err?.message || "删除失败");
    },
  });

  /**
   * 新建根应用
   */
  const handleCreateRoot = () => {
    setEditing(null);
    setParentId(undefined);
    setModalOpen(true);
  };

  /**
   * 新建子应用
   */
  const handleCreateChild = (record: AppResponse) => {
    setEditing(null);
    setParentId(record.id);
    setModalOpen(true);
  };

  /**
   * 编辑应用
   */
  const handleEdit = (record: AppResponse) => {
    setEditing(record);
    setParentId(undefined);
    setModalOpen(true);
  };

  /**
   * 删除应用
   */
  const handleDelete = (record: AppResponse) => {
    deleteMut.mutate(record.id!);
  };

  /**
   * 提交弹窗表单
   */
  const handleSubmit = (values: AppFormValues) => {
    if (editing?.id) {
      const payload: AppUpdateRequest = {
        menuType: values.menuType,
        title: values.title,
        icon: values.icon,
        pageId:
          values.menuType === AppMenuType.PAGE ? values.pageId : undefined,
        appKey:
          values.menuType === AppMenuType.PAGE ? values.appKey : undefined,
        redirectUrl: values.redirectUrl,
        sort: values.sort,
        hasWatermark: values.hasWatermark,
      };
      updateMut.mutate({ id: editing.id, payload });
    } else {
      const payload: AppCreateRequest = {
        menuType: values.menuType,
        title: values.title,
        icon: values.icon,
        pageId:
          values.menuType === AppMenuType.PAGE ? values.pageId : undefined,
        appKey:
          values.menuType === AppMenuType.PAGE ? values.appKey : undefined,
        redirectUrl: values.redirectUrl,
        parentId: values.parentId || parentId,
        sort: values.sort,
        hasWatermark: values.hasWatermark,
      };
      createMut.mutate(payload);
    }
  };

  /**
   * 授权应用
   */
  const handleAuth = (record: AppResponse) => {
    setEditing(record);
    setAuthModalOpen(true);
  };

  const columns = useMemo(
    () => buildColumns(handleCreateChild, handleEdit, handleDelete, handleAuth),
    [apps],
  );

  const loading = appsLoading || pagesLoading;

  return (
    <>
      <StrongTable<AppResponse>
        title="应用管理"
        titleExtra={
          <Permission value="tenant:sa:app:create">
            <Button type="primary" onClick={handleCreateRoot}>
              <PlusOutlined /> 新建
            </Button>
          </Permission>
        }
        showIndex={false}
        dataSource={apps}
        loading={loading}
        selection={false}
        pagination={false}
        rowKey="id"
        columns={columns}
        expandable={{
          indentSize: 30,
          expandRowByClick: true,
          expandIcon: ({ expanded, record }) =>
            record.handle?.type === AppMenuType.GROUP ? (
              expanded ? (
                <MinusSquareOutlined
                  style={{ color: "var(--ant-color-text-tertiary)" }}
                />
              ) : (
                <PlusSquareOutlined
                  style={{ color: "var(--ant-color-text-tertiary)" }}
                />
              )
            ) : null,
        }}
      />

      <AppFormModal
        open={modalOpen}
        initial={editing}
        parentId={parentId}
        onCancel={() => {
          setModalOpen(false);
          setEditing(null);
          setParentId(undefined);
        }}
        onSubmit={handleSubmit}
        loading={createMut.isPending || updateMut.isPending}
        pages={pages}
        parentOptions={parentOptions}
      />

      {editing && (
        <AuthorizeDrawer
          open={authModalOpen}
          bizType={AuthorizationBizType.APP}
          bizId={editing.id!}
          onClose={() => setAuthModalOpen(false)}
          publicScopeEnabled={false}
        />
      )}
    </>
  );
};

export default App;
