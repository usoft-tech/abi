import QueryButton from "@/components/button/QueryButton";
import DeleteIcon from "@/components/icon/DeleteIcon";
import DesignIcon from "@/components/icon/DesignIcon";
import EditIcon from "@/components/icon/EditIcon";
import RemoteCardList from "@/components/list/RemoteCardList";
import RemoteTable from "@/components/table/RemoteTable";
import CropUpload from "@/components/upload/CropUpload";
import { AuthorizationBizType } from "@/services/authorization";
import {
  AppstoreOutlined,
  CloseOutlined,
  DatabaseOutlined,
  EditOutlined,
  FolderAddOutlined,
  FolderOpenOutlined,
  FolderOutlined,
  PlusOutlined,
  ShareAltOutlined,
  UnorderedListOutlined,
} from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  Button,
  Divider,
  Drawer,
  Dropdown,
  Flex,
  Form,
  Image,
  Input,
  MenuProps,
  message,
  Modal,
  Radio,
  Select,
  Space,
  Tooltip,
  Tree,
  Typography,
} from "antd";
import { ColumnsType } from "antd/es/table";
import { PageDesigner, plugins, type PageSchema } from "bi-sdk-react";
import "bi-sdk-react/dist/es/css/bi-sdk.css";
import {
  DataSetType,
  SchemaItemType,
} from "bi-sdk-react/dist/types/components/typing";
import React, {
  forwardRef,
  useEffect,
  useImperativeHandle,
  useRef,
  useState,
} from "react";

import Empty from "@/assets/images/empty.png";
import styled from "styled-components";
import { fetch } from "../bi";
import Dataset from "./Dataset";
import { ContainerPlugin, IconPlugin } from "./designer-plugins";
import { DatasetFolderForm } from "./form/DatasetFolderForm";
import { DatasetDrawer } from "./form/DatasetForm";
import { ShareDrawer } from "./modules/ShareDrawer";
import {
  assistantChatToDataset,
  createPage,
  DatasetFolderEntity,
  DatasetResponse,
  deleteDataset,
  deleteDatasetFolder,
  deletePage,
  getPage,
  listChatBiAgents,
  listDatasetFolders,
  listDatasets,
  listPages,
  PageCreateRequest,
  PageQueryRequest,
  PageResp,
  PageUpdateRequest,
  updatePage,
  updatePageSchema,
} from "./services";
import { Permission, usePermission } from "@/permission";
import { DictSelect } from "@/components/dict/Dict";
import config from "@/config";

const statusEnums = [
  { value: "PUBLISHED", label: "已发布" },
  { value: "DRAFT", label: "草稿" },
  { value: "LOCKED", label: "已锁定" },
];

const statusMap: Record<string, string> = statusEnums.reduce(
  (acc, cur) => {
    acc[cur.value] = cur.label;
    return acc;
  },
  {} as Record<string, string>,
);

const emptySchema: PageSchema = {
  info: {
    name: "",
    description: "",
  },
  datasources: [],
  scripts: [],
  variables: [],
  items: [],
};

const DesignDrawer = styled(Drawer)`
  width: 80%;

  .ant-drawer-content-wrapper {
    width: 100%;

    .ant-drawer-section {
      width: 100%;

      .ant-drawer-header {
        padding: 8px 24px;
      }
      .ant-drawer-body {
        padding: 0;
        height: "calc(100vh - 41px)";
        overflow: "hidden";
      }
    }
  }
`;

const StyledPageDesigner = styled(PageDesigner)`
  .page-canvas {
    min-height: 500px;
  }
`;

/**
 * 查询条件工具条
 */
const QueryBar: React.FC<{
  query: PageQueryRequest;
  onChange: (q: PageQueryRequest) => void;
}> = ({ query, onChange }) => {
  const [form] = Form.useForm<PageQueryRequest>();
  return (
    <Form<PageQueryRequest>
      form={form}
      layout="inline"
      initialValues={{
        name: query.name,
        status: query.status,
      }}
      onFinish={(values: PageQueryRequest) =>
        onChange({ ...query, ...values, page: 1 })
      }
      style={{ marginBottom: 12 }}
    >
      <Form.Item name="name" label="名称">
        <Input placeholder="请输入页面名称" allowClear />
      </Form.Item>
      <Form.Item name="status" label="状态">
        <Select
          placeholder="请选择状态"
          allowClear
          style={{ width: 120 }}
          options={statusEnums}
        />
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
const EditModal: React.FC<{
  open: boolean;
  initial?: PageResp | null;
  onCancel: () => void;
  onSubmit: (values: PageCreateRequest | PageUpdateRequest) => void;
  loading?: boolean;
}> = ({ open, initial, onCancel, onSubmit, loading }) => {
  const [form] = Form.useForm();
  const isEdit = !!initial?.id;
  useEffect(() => {
    if (!open) {
      form.resetFields();
    } else {
      form.setFieldsValue({
        name: initial?.name,
        status: initial?.status || "PUBLISHED",
        cover: initial?.cover,
        industry: initial?.industry,
        description: initial?.description,
      });
    }
  }, [open, form]);
  return (
    <Modal
      title={isEdit ? "编辑页面" : "新建页面"}
      open={open}
      onCancel={onCancel}
      onOk={() => {
        form.validateFields().then(onSubmit);
      }}
      confirmLoading={loading}
      destroyOnHidden
    >
      <Form form={form} layout="vertical">
        <Form.Item
          name="name"
          label="名称"
          rules={[{ required: true, message: "请输入页面名称" }]}
        >
          <Input placeholder="请输入页面名称" />
        </Form.Item>
        <Form.Item name="industry" label="行业">
          <DictSelect type="industry" />
        </Form.Item>
        <Form.Item name="description" label="描述">
          <Input.TextArea placeholder="请输入描述" rows={3} />
        </Form.Item>
        <Form.Item name="status" label="状态" initialValue="PUBLISHED">
          <Radio.Group optionType="button" options={statusEnums} />
        </Form.Item>
        <Form.Item name="cover" label="封面">
          <CropUpload
            bizType="cover"
            maxCount={1}
            aspectSlider={false}
            aspect={1.78}
            fileList={initial?.cover ? [initial.cover] : []}
            onChange={(fileList) => {
              form.setFieldValue(
                "cover",
                fileList?.length ? fileList[0].url : null,
              );
            }}
          />
        </Form.Item>
      </Form>
    </Modal>
  );
};

const datasetSelector = (
  editing: PageResp,
  onSelect: (item: DataSetType) => void,
) => {
  return (
    <Dataset
      tempDbs={
        editing?.excels?.length
          ? [{ id: "page-excel:" + editing?.id || "", name: "Excel文件数据源" }]
          : []
      }
      onSelect={({ id, name, config }) =>
        onSelect({ id, name, output: config?.output || { fields: [] } })
      }
    />
  );
};

const DatasetGenerate: React.FC<{
  item: SchemaItemType;
  tempDbs?: {
    id: string;
    name: string;
  }[];
  onAdd: (item: DataSetType) => void;
  onClose: () => void;
}> = ({ item, tempDbs, onAdd, onClose }) => {
  const [open, setOpen] = useState(true);
  const [editingDrawerOpen, setEditingDrawerOpen] = useState(false);
  const [editingDataset, setEditingDataset] =
    useState<Partial<DatasetResponse> | null>(null);

  const [form] = Form.useForm<{
    dbId?: string;
    title?: string;
    description?: string;
  }>();

  const { data: agentList } = useQuery({
    retry: false,
    queryKey: ["listChatBiAgents"],
    queryFn: () =>
      listChatBiAgents().then((res) =>
        (res?.data || []).filter((item) => item.type !== "ChatBI"),
      ),
  });

  const handleOpen = () => {
    setEditingDrawerOpen(true);
  };

  const handleClose = () => {
    setEditingDrawerOpen(false);
    onClose?.();
  };

  const chatMutation = useMutation({
    mutationFn: assistantChatToDataset,
    onSuccess: (res) => {
      message.success("生成成功");
      setEditingDataset(res?.data || null);
      setOpen(false);
      handleOpen();
    },
  });

  const startGenerate = () => {
    form.validateFields().then(({ dbId, title, description }) => {
      chatMutation.mutate({
        dbId: dbId!,
        title: title!,
        description: description!,
        schemaItem: item,
      });
    });
  };

  return (
    <>
      <Modal
        title="AI 生成数据集"
        open={open}
        okText={chatMutation.isPending ? "生成中..." : "生成"}
        cancelText="取消"
        loading={chatMutation.isPending}
        onCancel={() => {
          setOpen(false);
          onClose?.();
        }}
        onOk={startGenerate}
        okButtonProps={{
          disabled: chatMutation.isPending,
          loading: chatMutation.isPending,
        }}
        cancelButtonProps={{
          disabled: chatMutation.isPending,
          loading: chatMutation.isPending,
        }}
      >
        <Form
          form={form}
          layout="vertical"
          initialValues={{ title: item.name, description: item.description }}
        >
          <Form.Item
            name="dbId"
            label="数据库"
            rules={[{ required: true, message: "请选择数据库" }]}
          >
            <Select
              placeholder="请选择数据库"
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
                ...(agentList?.map((item) => ({
                  label: item.name,
                  value: item.id,
                  options:
                    item.children?.map((child) => ({
                      label: child.name,
                      value: child.id,
                    })) || [],
                })) || []),
              ]}
            />
          </Form.Item>
          <Form.Item
            name="title"
            label="数据主题"
            rules={[{ required: true, message: "请输入数据主题" }]}
          >
            <Input placeholder="请输入数据主题" />
          </Form.Item>
          <Form.Item
            name="description"
            label="数据描述"
            rules={[{ required: true, message: "请输入数据描述" }]}
          >
            <Input.TextArea placeholder="请输入数据描述" rows={3} />
          </Form.Item>
        </Form>
      </Modal>

      <DatasetDrawer
        tempDbs={tempDbs}
        open={editingDrawerOpen}
        initial={editingDataset}
        onClose={handleClose}
        onSuccess={(dataset) => {
          handleClose();
          const { id, name, config } = dataset;
          onAdd({ id, name, output: config?.output || { fields: [] } });
        }}
      />
    </>
  );
};

type DatasetAddType = (
  item: SchemaItemType,
  tempDbs: {
    id: string;
    name: string;
  }[],
  onAdd: (item: DataSetType) => void,
  onClose: () => void,
) => React.ReactNode;

const datasetAdd: DatasetAddType = (item, tempDbs, onAdd, onClose) => {
  return (
    <DatasetGenerate
      item={item}
      tempDbs={tempDbs}
      onAdd={onAdd}
      onClose={onClose}
    />
  );
};

/**
 * 将数据集文件夹转换为树结构
 */
const convertFoldersToTree = (folders: DatasetFolderEntity[]): any[] => {
  const map = new Map<string, any>();
  const roots: any[] = [];

  folders.forEach((folder) => {
    map.set(folder.id, {
      key: `folder-${folder.id}`,
      value: folder.id,
      title: folder.name,
      icon: ({ expanded }: any) =>
        expanded ? <FolderOpenOutlined /> : <FolderOutlined />,
      children: [],
      nodeType: "folder",
      folder,
    });
  });

  folders.forEach((folder) => {
    const node = map.get(folder.id);
    if (!node) {
      return;
    }
    if (folder.parentId && map.has(folder.parentId)) {
      map.get(folder.parentId).children.push(node);
    } else {
      roots.push(node);
    }
  });

  return roots;
};

/**
 * 数据集维护面板
 */
const DatasetPanel = forwardRef<{ handleAdd: () => void }, any>((_, ref) => {
  const queryClient = useQueryClient();
  const [selectedKey, setSelectedKey] = useState<string | undefined>();
  const [datasetModalVisible, setDatasetModalVisible] = useState(false);
  const [folderModalVisible, setFolderModalVisible] = useState(false);
  const [editingDataset, setEditingDataset] =
    useState<Partial<DatasetResponse> | null>(null);
  const [currentFolder, setCurrentFolder] =
    useState<Partial<DatasetFolderEntity> | null>(null);
  const [folderForm] = Form.useForm();

  const { hasPermission } = usePermission();

  const { data: foldersData } = useQuery({
    retry: false,
    queryKey: ["dataset-folders"],
    queryFn: async () => {
      const res = await listDatasetFolders();
      return res.data || [];
    },
  });

  const { data: datasetsData } = useQuery({
    retry: false,
    queryKey: ["datasets-all"],
    queryFn: async () => {
      const res = await listDatasets({
        page: 1,
        size: 1000,
      });
      return res.data?.items || [];
    },
  });

  const deleteFolderMutation = useMutation({
    mutationFn: deleteDatasetFolder,
    onSuccess: () => {
      message.success("删除成功");
      queryClient.invalidateQueries({ queryKey: ["dataset-folders"] });
      queryClient.invalidateQueries({ queryKey: ["datasets-all"] });
      if (
        selectedKey &&
        currentFolder?.id &&
        selectedKey === `folder-${currentFolder.id}`
      ) {
        setSelectedKey(undefined);
      }
    },
  });

  const deleteDatasetMutation = useMutation({
    mutationFn: deleteDataset,
    onSuccess: () => {
      message.success("删除成功");
      queryClient.invalidateQueries({ queryKey: ["datasets-all"] });
    },
  });

  const treeData = React.useMemo(() => {
    const folderNodes = convertFoldersToTree(foldersData || []);
    const folderMap = new Map<string, any>();
    folderNodes.forEach((node) => {
      folderMap.set(node.folder.id, node);
      if (node.children && node.children.length > 0) {
        const stack = [...node.children];
        while (stack.length > 0) {
          const current = stack.pop();
          if (current.folder) {
            folderMap.set(current.folder.id, current);
          }
          if (current.children && current.children.length > 0) {
            stack.push(...current.children);
          }
        }
      }
    });

    const datasets = datasetsData || [];
    datasets.forEach((dataset) => {
      const datasetNode = {
        key: `dataset-${dataset.id}`,
        title: dataset.name,
        icon: <DatabaseOutlined />,
        isLeaf: true,
        nodeType: "dataset",
        dataset,
      };
      if (dataset.folderId && folderMap.has(dataset.folderId)) {
        const parentNode = folderMap.get(dataset.folderId);
        if (!parentNode.children) {
          parentNode.children = [];
        }
        parentNode.children.push(datasetNode);
      } else {
        folderNodes.push(datasetNode);
      }
    });

    return [
      {
        key: "root",
        title: "全部数据集",
        icon: <FolderOpenOutlined />,
        nodeType: "root",
        children: folderNodes,
      },
    ];
  }, [foldersData, datasetsData]);

  const handleAddFolder = (parentId?: string) => {
    setCurrentFolder({ parentId });
    folderForm.resetFields();
    setFolderModalVisible(true);
  };

  const handleEditFolder = (folder: DatasetFolderEntity) => {
    setCurrentFolder(folder);
    folderForm.setFieldsValue({ name: folder.name, sort: folder.sort });
    setFolderModalVisible(true);
  };

  const handleDeleteFolder = (folder: DatasetFolderEntity) => {
    Modal.confirm({
      title: "确认删除",
      content: "删除文件夹将同时删除其下所有子文件夹和数据集，确认继续？",
      onOk: () => deleteFolderMutation.mutate(folder.id),
    });
  };

  const handleAddDataset = (folderId?: string) => {
    setEditingDataset(null);
    setDatasetModalVisible(true);
    if (folderId) {
      setCurrentFolder({ id: folderId } as any);
    } else {
      setCurrentFolder(null);
    }
  };

  const handleEditDataset = (dataset: DatasetResponse) => {
    setEditingDataset(dataset);
    setDatasetModalVisible(true);
  };

  useImperativeHandle(ref, () => ({
    handleAdd: () => {
      handleAddDataset();
    },
  }));

  const handleNodeClick = (node: any) => {
    setSelectedKey(node.key as string);
  };

  const renderContextMenuItems = (node: any): MenuProps["items"] => {
    if (node.key === "root") {
      return [
        hasPermission("tenant:sa:dataset-folder:create")
          ? {
              icon: <FolderAddOutlined />,
              label: "新建文件夹",
              key: "add-folder",
              onClick: () => handleAddFolder(undefined),
            }
          : null,
        hasPermission("tenant:sa:dataset:create")
          ? {
              icon: <PlusOutlined />,
              label: "新建数据集",
              key: "add-dataset",
              onClick: () => handleAddDataset(undefined),
            }
          : null,
      ].filter(Boolean) as MenuProps["items"];
    }
    if (node.nodeType === "folder") {
      const folder = node.folder as DatasetFolderEntity;
      return [
        hasPermission("tenant:sa:dataset-folder:create")
          ? {
              icon: <FolderAddOutlined />,
              label: "新建子文件夹",
              key: "add-folder",
              onClick: () => handleAddFolder(folder.id),
            }
          : null,
        hasPermission("tenant:sa:dataset:create")
          ? {
              icon: <PlusOutlined />,
              label: "新建数据集",
              key: "add-dataset",
              onClick: () => handleAddDataset(folder.id),
            }
          : null,
        hasPermission("tenant:sa:dataset-folder:update")
          ? {
              icon: <EditOutlined />,
              label: "修改文件夹",
              key: "rename",
              onClick: () => handleEditFolder(folder),
            }
          : null,
        hasPermission("tenant:sa:dataset-folder:delete")
          ? {
              icon: <CloseOutlined />,
              label: "删除文件夹",
              key: "delete",
              onClick: () => handleDeleteFolder(folder),
            }
          : null,
      ].filter(Boolean) as MenuProps["items"];
    }
    if (node.nodeType === "dataset") {
      const dataset = node.dataset as DatasetResponse;
      return [
        hasPermission("tenant:sa:dataset:update")
          ? {
              icon: <EditOutlined />,
              label: "编辑数据集",
              key: "edit",
              onClick: () => handleEditDataset(dataset),
            }
          : null,
        hasPermission("tenant:sa:dataset:delete")
          ? {
              icon: <CloseOutlined />,
              label: "删除数据集",
              key: "delete",
              onClick: () => deleteDatasetMutation.mutate(dataset.id),
            }
          : null,
      ].filter(Boolean) as MenuProps["items"];
    }
    return [];
  };

  return (
    <>
      <Tree
        showIcon
        selectable
        blockNode
        showLine
        treeData={treeData}
        selectedKeys={selectedKey ? [selectedKey] : ["root"]}
        defaultExpandedKeys={["root"]}
        titleRender={(node) => (
          <Dropdown
            menu={{ items: renderContextMenuItems(node) }}
            trigger={["contextMenu"]}
          >
            <div
              style={{
                display: "inline-flex",
                justifyContent: "space-between",
                alignItems: "center",
              }}
              onClick={() => handleNodeClick(node)}
            >
              {node.title}
            </div>
          </Dropdown>
        )}
      />

      <DatasetFolderForm
        open={folderModalVisible}
        onClose={() => setFolderModalVisible(false)}
        currentFolder={currentFolder}
      />

      <DatasetDrawer
        open={datasetModalVisible}
        initial={editingDataset}
        folderId={currentFolder?.id as string | undefined}
        onClose={() => {
          setDatasetModalVisible(false);
          setEditingDataset(null);
        }}
        onSuccess={() => {
          setDatasetModalVisible(false);
          setEditingDataset(null);
          queryClient.invalidateQueries({ queryKey: ["datasets-all"] });
        }}
      />
    </>
  );
});

const Page: React.FC = () => {
  const tableRef = useRef<any>(null);
  const designDrawerRef = useRef<any>(null);
  const [designVisible, setDesignVisible] = useState(false);
  const [shareVisible, setShareVisible] = useState(false);

  const [queryParams, setQueryParams] = useState<PageQueryRequest>({
    page: 1,
    size: 10,
  });

  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<PageResp | null>(null);
  const [viewMode, setViewMode] = useState<"table" | "card">("card");

  const refreshTable = () => {
    tableRef.current?.refresh();
  };

  const handleCreate = () => {
    setEditing(null);
    setModalOpen(true);
  };

  const handleEdit = (record: PageResp) => {
    setEditing(record);
    setModalOpen(true);
  };

  const handleDesign = async (record: PageResp) => {
    setEditing(record);
    setDesignVisible(true);
    try {
      const { code, data, message: msg } = await getPage(record.id);
      if (code !== "OK" || !data) {
        message.error(msg || "获取页面失败");
        setDesignVisible(false);
        return;
      }
      setEditing(data);
      if (data?.schema) {
        designDrawerRef.current?.handleOpen(data.schema);
      } else {
        const schema = JSON.parse(JSON.stringify(emptySchema));
        schema.info.name = data.name!;
        schema.info.description = data.description!;
        designDrawerRef.current?.handleOpen(schema);
      }
    } catch (err) {
      message.error("获取页面失败");
      setDesignVisible(false);
    }
  };

  const handleShare = (record: PageResp) => {
    setEditing(record);
    setShareVisible(true);
  };

  const createMut = useMutation({
    mutationFn: createPage,
    onSuccess: () => {
      message.success("创建成功");
      setModalOpen(false);
      setEditing(null);
      refreshTable();
    },
    onError: (err: any) => message.error(err?.message || "创建失败"),
  });

  const updateMut = useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: PageUpdateRequest }) =>
      updatePage(id, payload),
    onSuccess: () => {
      message.success("更新成功");
      setModalOpen(false);
      setEditing(null);
      refreshTable();
    },
    onError: (err: any) => message.error(err?.message || "更新失败"),
  });

  const updateSchemaMut = useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: PageSchema }) =>
      updatePageSchema(id, payload),
    onSuccess: () => {
      message.success("更新成功");
      // setDesignVisible(false);
      // setEditing(null);
      refreshTable();
    },
    onError: (err: any) => message.error(err?.message || "更新失败"),
  });

  const deleteMut = useMutation({
    mutationFn: deletePage,
    onSuccess: () => {
      message.success("删除成功");
      refreshTable();
    },
    onError: (err: any) => message.error(err?.message || "删除失败"),
  });

  const { data: agentList } = useQuery({
    retry: false,
    queryKey: ["listChatBiAgents", "ChatBI"],
    queryFn: () => listChatBiAgents().then((res) => res?.data || []),
  });

  const baseUrl = location.protocol + "//" + location.host + config.basePath + "/share/page";

  const columns = [
    {
      title: "封面",
      dataIndex: "cover",
      key: "cover",
      width: 140,
      align: "center",
      render: (url: string) => (
        <Image
          src={url}
          fallback={Empty}
          alt="封面"
          style={{ width: 120, height: 90, objectFit: "cover" }}
        />
      ),
    },
    {
      title: "页面",
      dataIndex: "name",
      key: "name",
      render: (text: string, record: PageResp) => (
        <Flex vertical gap={10} align="flex-start">
          <Typography.Text ellipsis={{ tooltip: text }}>{text}</Typography.Text>
          <Typography.Text type="secondary">
            {record.description}
          </Typography.Text>
        </Flex>
      ),
    },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      width: 80,
      align: "center",
      render: (text: string) => {
        return statusMap[text] || text;
      },
    },
    {
      title: "操作",
      key: "action",
      width: 120,
      align: "center",
      render: (_: any, record: PageResp) => (
        <Space>
          <Permission value="tenant:sa:page:update">
            <EditIcon onEdit={() => handleEdit(record)} />
          </Permission>
          <Permission value="tenant:sa:page:update-schema">
            <DesignIcon onEdit={() => handleDesign(record)} />
          </Permission>
          <Permission value="tenant:sa:page:share">
            <Tooltip title="分享">
              <Button type="link" onClick={() => handleShare(record)}>
                <ShareAltOutlined />
              </Button>
            </Tooltip>
          </Permission>
          <Permission value="tenant:sa:page:delete">
            <DeleteIcon
              name="页面"
              onDelete={() => deleteMut.mutate(record.id)}
            />
          </Permission>
        </Space>
      ),
    },
  ];

  const queryBar = <QueryBar query={queryParams} onChange={setQueryParams} />;
  const toolBar = (
    <Space>
      <Radio.Group
        value={viewMode}
        onChange={(e) => setViewMode(e.target.value as "table" | "card")}
        options={[
          { value: "table", label: <UnorderedListOutlined /> },
          { value: "card", label: <AppstoreOutlined /> },
        ]}
        optionType="button"
        buttonStyle="solid"
      />
      <Permission value="tenant:sa:page:create">
        <Button type="primary" onClick={handleCreate}>
          <PlusOutlined /> 新建页面
        </Button>
      </Permission>
    </Space>
  );

  const pagination = {
    current: queryParams.page,
    pageSize: queryParams.size,
    onChange: (page: number, size: number) =>
      setQueryParams({ ...queryParams, page, size }),
  };

  return (
    <>
      {viewMode === "table" ? (
        <RemoteTable<PageResp>
          ref={tableRef}
          queryBar={queryBar}
          title="页面管理"
          titleExtra={toolBar}
          fetchKey={["pages", queryParams]}
          fetchData={() => listPages(queryParams)}
          pagination={pagination}
          selection={false}
          rowKey="id"
          columns={columns as ColumnsType<PageResp>}
        />
      ) : (
        <RemoteCardList<PageResp>
          ref={tableRef}
          queryBar={queryBar}
          size="small"
          title="页面管理"
          titleExtra={toolBar}
          titleKey="name"
          descriptionKey="description"
          coverKey="cover"
          actionsRender={(record) => [
            <Permission value="tenant:sa:page:update">
              <EditIcon
                onEdit={() => handleEdit(record)}
                label="编辑"
                style={{ fontSize: 12 }}
              />
            </Permission>,
            <Permission value="tenant:sa:page:update-schema">
              <DesignIcon
                onEdit={() => handleDesign(record)}
                label="设计"
                style={{ fontSize: 12 }}
              />
            </Permission>,
            <Permission value="tenant:sa:page:share">
              <Button
                type="link"
                onClick={() => handleShare(record)}
                style={{ fontSize: 12 }}
              >
                <ShareAltOutlined /> 分享
              </Button>
            </Permission>,
            <Permission value="tenant:sa:page:delete">
              <DeleteIcon
                name="页面"
                onDelete={() => deleteMut.mutate(record.id)}
                label="删除"
                style={{ fontSize: 12 }}
              />
            </Permission>,
          ]}
          fetchKey={["pages", queryParams]}
          fetchData={() => listPages(queryParams)}
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
      )}

      <EditModal
        open={modalOpen}
        initial={editing}
        onCancel={() => {
          setModalOpen(false);
          setEditing(null);
        }}
        loading={createMut.isPending || updateMut.isPending}
        onSubmit={(values) => {
          if (editing?.id) {
            updateMut.mutate({
              id: editing.id,
              payload: values as PageUpdateRequest,
            });
          } else {
            createMut.mutate(values as PageCreateRequest);
          }
        }}
      />

      {designVisible && (
        <DesignDrawer
          open={designVisible}
          onClose={() => setDesignVisible(false)}
          styles={{
            wrapper: { width: "100%", transform: "none !important" },
            section: { width: "100%" },
            header: { display: "none" },
            body: {
              padding: 0,
              height: "calc(100vh - 41px)",
              overflow: "hidden",
            },
          }}
        >
          <StyledPageDesigner
            pageId={editing?.id!}
            ref={designDrawerRef}
            agentList={[
              ...(agentList?.flatMap((item) => item.children || []) || []),
            ]}
            plugins={[...plugins, IconPlugin, ContainerPlugin]}
            scriptEnable={false}
            datasourceEnable={false}
            headerExtra={
              <>
                <Divider orientation="vertical" />
                <a className="toolbar" onClick={() => setDesignVisible(false)}>
                  <CloseOutlined />
                </a>
              </>
            }
            datasetPanel={DatasetPanel}
            datasetSelector={(onSelect) => datasetSelector(editing!, onSelect)}
            datasetAdd={(item, onAdd, onClose) =>
              datasetAdd(
                item,
                editing?.excels?.length
                  ? [
                      {
                        id: "page-excel:" + editing?.id || "",
                        name: "Excel文件数据源",
                      },
                    ]
                  : [],
                onAdd,
                onClose,
              )
            }
            fetch={{
              ...fetch,
              ai: {
                ...fetch.ai!,
                chat: async (bizType, bizId, conversationId, request) => {
                  return fetch?.ai
                    ?.chat(bizType, bizId, conversationId, request)
                    ?.then((res) => {
                      const excels = editing?.excels || [];
                      excels.push(...(request.files || []));
                      setEditing({ ...editing, excels } as PageResp);
                      return res;
                    })!;
                },
              },
            }}
            onSave={(schema) => {
              updateSchemaMut.mutate({
                id: editing?.id!,
                payload: schema,
              });
            }}
          />
        </DesignDrawer>
      )}

      <ShareDrawer
        baseUrl={baseUrl}
        bizType={AuthorizationBizType.PAGE}
        bizId={editing?.id!}
        open={shareVisible}
        onClose={() => setShareVisible(false)}
      />
    </>
  );
};

export default Page;
