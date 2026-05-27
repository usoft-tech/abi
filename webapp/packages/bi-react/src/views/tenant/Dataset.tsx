import PlusButton from "@/components/button/PlusButton";
import QueryButton from "@/components/button/QueryButton";
import DeleteIcon from "@/components/icon/DeleteIcon";
import EditIcon from "@/components/icon/EditIcon";
import RemoteTable from "@/components/table/RemoteTable";
import {
  DeleteOutlined,
  EditOutlined,
  FolderOpenOutlined,
  FolderOutlined,
  PlusOutlined,
  SearchOutlined,
} from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  Button,
  Dropdown,
  Form,
  Input,
  Layout,
  MenuProps,
  message,
  Modal,
  Space,
  Tag,
  Tree,
} from "antd";
import { ColumnsType } from "antd/es/table";
import type { TreeProps } from "antd/es/tree";
import React, { useEffect, useMemo, useRef, useState } from "react";
import { DatasetFolderForm } from "./form/DatasetFolderForm";
import { DatasetDrawer } from "./form/DatasetForm";
import {
  DatasetFolderEntity,
  DatasetFolderUpdateRequest,
  DatasetResponse,
  deleteDataset,
  deleteDatasetFolder,
  listDatasetFolders,
  listDatasets,
  updateDatasetFolder,
} from "./services";
import { Permission, usePermission } from "@/permission";

const { Sider, Content } = Layout;

// --- Folder Tree Helper ---

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

const Dataset: React.FC<{
  tempDbs?: {
    id: string;
    name: string;
  }[];
  onSelect?: (dataset: DatasetResponse) => void;
}> = ({ tempDbs, onSelect }) => {
  const queryClient = useQueryClient();
  const [selectedFolderId, setSelectedFolderId] = useState<string | undefined>(
    undefined,
  );
  const [searchParams, setSearchParams] = useState({
    page: 1,
    size: 10,
    name: "",
  });
  const [datasetModalVisible, setDatasetModalVisible] = useState(false);
  const [editingDataset, setEditingDataset] =
    useState<Partial<DatasetResponse> | null>(null);
  const tableRef = useRef<{ refresh: () => void }>(null);

  // --- Tree Expansion ---
  const [expandedKeys, setExpandedKeys] = useState<React.Key[]>([]);
  const [autoExpandParent, setAutoExpandParent] = useState(true);

  const { hasPermission } = usePermission();

  // --- Folder State & Queries ---

  const { data: foldersData } = useQuery({
    retry: false,
    queryKey: ["dataset-folders"],
    queryFn: async () => {
      const res = await listDatasetFolders();
      return res.data || [];
    },
  });

  useEffect(() => {
    if (foldersData) {
      setExpandedKeys((prev) => {
        // If it's the first load (prev is empty), expand all.
        // Or if we want to enforce expand all always:
        const allKeys = foldersData.map((f) => f.id).concat(["root"]);
        // We can just merge or reset. User said "Default expand all".
        // Let's expand all on every load to ensure new folders are shown,
        // or strictly follow "default" which implies initial.
        // Given "drag and drop data not change" might be confusion about where it went,
        // expanding all helps visibility.
        return allKeys;
      });
    }
  }, [foldersData]);

  const onExpand = (newExpandedKeys: React.Key[]) => {
    setExpandedKeys(newExpandedKeys);
    setAutoExpandParent(false);
  };

  const treeData = useMemo(() => {
    const nodes = convertFoldersToTree(foldersData || []);
    return [
      {
        key: "root",
        title: "全部数据集",
        icon: <FolderOpenOutlined />,
        children: nodes,
      },
    ];
  }, [foldersData]);

  // --- Folder Mutations ---

  const [folderModalVisible, setFolderModalVisible] = useState(false);
  const [currentFolder, setCurrentFolder] =
    useState<Partial<DatasetFolderEntity> | null>(null);
  const [folderForm] = Form.useForm();

  const updateFolderMutation = useMutation({
    mutationFn: (data: DatasetFolderUpdateRequest & { id: string }) =>
      updateDatasetFolder(data.id, data),
    onSuccess: () => {
      message.success("更新成功");
      setFolderModalVisible(false);
      queryClient.invalidateQueries({ queryKey: ["dataset-folders"] });
    },
  });

  const onDrop: TreeProps["onDrop"] = (info) => {
    const dropKey = info.node.key as string;
    const dragKey = info.dragNode.key as string;

    if (dragKey === "root") return;

    const dropPos = info.node.pos.split("-");
    const dropPosition =
      info.dropPosition - Number(dropPos[dropPos.length - 1]);

    let parentId: string | undefined;

    if (dropKey === "root") {
      if (dropPosition === 0) {
        parentId = "";
      } else {
        return;
      }
    } else {
      const dropFolder = foldersData?.find((f) => f.id === dropKey);
      if (!dropFolder) return;

      if (dropPosition === 0) {
        parentId = dropFolder.id;
      } else {
        parentId = dropFolder.parentId || "";
      }
    }

    updateFolderMutation.mutate({ id: dragKey, parentId });
  };

  const deleteFolderMutation = useMutation({
    mutationFn: deleteDatasetFolder,
    onSuccess: () => {
      message.success("删除成功");
      queryClient.invalidateQueries({ queryKey: ["dataset-folders"] });
      // If deleted folder was selected, reset selection
      if (selectedFolderId === currentFolder?.id) {
        setSelectedFolderId(undefined);
      }
    },
  });

  const handleAddSubFolder = (parentId?: string) => {
    setCurrentFolder({ parentId });
    folderForm.resetFields();
    setFolderModalVisible(true);
  };

  const handleEditFolder = (folder: DatasetFolderEntity) => {
    setCurrentFolder(folder);
    folderForm.setFieldsValue({ name: folder.name, sort: folder.sort });
    setFolderModalVisible(true);
  };

  const handleDeleteFolder = (id: string) => {
    deleteFolderMutation.mutate(id);
  };

  // --- Tree Context Menu ---

  const renderFolderMenu = (node: any): MenuProps["items"] => {
    if (node.key === "root") {
      return [
        hasPermission("tenant:sa:dataset-folder:create") ? {
          key: "add",
          label: "新建文件夹",
          icon: <PlusOutlined />,
          onClick: () => handleAddSubFolder(undefined),
        } : null,
      ].filter(Boolean) as MenuProps["items"];
    }

    // Find the folder object
    const folder = foldersData?.find((f) => f.id === node.key);
    if (!folder) return [];

    return [
      hasPermission("tenant:sa:dataset-folder:create") ? {
        key: "add",
        label: "新建子文件夹",
        icon: <PlusOutlined />,
        onClick: () => handleAddSubFolder(folder.id),
      } : null,
      hasPermission("tenant:sa:dataset-folder:update") ? {
        key: "edit",
        label: "重命名",
        icon: <EditOutlined />,
        onClick: () => handleEditFolder(folder),
      } : null,
      hasPermission("tenant:sa:dataset-folder:delete") ? {
        key: "delete",
        label: "删除",
        icon: <DeleteOutlined />,
        danger: true,
        onClick: () => {
          Modal.confirm({
            title: "确认删除",
            content: "删除文件夹将同时删除其下所有子文件夹和数据集，确认继续？",
            onOk: () => handleDeleteFolder(folder.id),
          });
        },
      } : null,
    ].filter(Boolean) as MenuProps["items"];
  };

  // --- Dataset State & Queries ---

  const deleteDatasetMutation = useMutation({
    mutationFn: deleteDataset,
    onSuccess: () => {
      message.success("删除成功");
      tableRef.current?.refresh();
    },
  });

  const columns = [
    onSelect
      ? {
          title: "选择",
          dataIndex: "select",
          key: "select",
          width: 60,
          align: "center",
          render: (_: string, record: DatasetResponse) => (
            <Button
              type="link"
              onClick={() => onSelect(record)}
              disabled={record.status !== "ENABLE"}
            >
              选择
            </Button>
          ),
        }
      : null,
    {
      title: "名称",
      dataIndex: "name",
      key: "name",
    },
    {
      title: "类型",
      dataIndex: "type",
      key: "type",
      render: (type: string) => <Tag>{type}</Tag>,
    },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      render: (status: string) => (
        <Tag color={status === "ENABLE" ? "success" : "default"}>
          {status === "ENABLE" ? "启用" : "禁用"}
        </Tag>
      ),
    },
    {
      title: "操作",
      key: "action",
      width: 80,
      render: (_: any, record: DatasetResponse) => (
        <Space size="middle">
          <Permission value="tenant:sa:dataset:update">
            <EditIcon
              onEdit={() => {
                setEditingDataset(record);
                setDatasetModalVisible(true);
              }}
            />
          </Permission>
          <Permission value="tenant:sa:dataset:delete">
            <DeleteIcon
              name="数据集"
              onDelete={() => deleteDatasetMutation.mutate(record.id)}
            />
          </Permission>
        </Space>
      ),
    },
  ].filter(Object);

  return (
    <Layout
      style={{ height: "calc(100% + 40px)", background: "#fff", margin: -20 }}
    >
      <Sider
        width={280}
        theme="light"
        style={{ borderRight: "1px solid #f0f0f0" }}
      >
        <div
          style={{
            padding: "16px",
            borderBottom: "1px solid #f0f0f0",
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
          }}
        >
          <span style={{ fontWeight: "bold" }}>文件夹</span>
          <Permission value="tenant:sa:dataset-folder:create">
            <Button
              type="text"
              icon={<PlusOutlined />}
              onClick={() => handleAddSubFolder(undefined)}
            />
          </Permission>
        </div>
        <div
          style={{ overflow: "auto", padding: 10, height: "calc(100% - 55px)" }}
        >
          <Tree
            draggable={{ icon: false }}
            onDrop={onDrop}
            blockNode
            expandedKeys={expandedKeys}
            autoExpandParent={autoExpandParent}
            onExpand={onExpand}
            treeData={treeData}
            selectedKeys={selectedFolderId ? [selectedFolderId] : ["root"]}
            onSelect={(keys) => {
              const key = keys[0] as string;
              setSelectedFolderId(key === "root" ? undefined : key);
              setSearchParams((prev) => ({ ...prev, page: 1 }));
            }}
            showLine={true}
            titleRender={(node) => (
              <Dropdown
                menu={{ items: renderFolderMenu(node) }}
                trigger={["contextMenu"]}
              >
                <div
                  style={{
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center",
                  }}
                >
                  <span>{node.title as React.ReactNode}</span>
                </div>
              </Dropdown>
            )}
          />
        </div>
      </Sider>
      <Content
        style={{ padding: "24px", display: "flex", flexDirection: "column" }}
      >
        <div style={{ flex: 1, overflow: "hidden" }}>
          <RemoteTable<DatasetResponse>
            ref={tableRef}
            title={
              <Space>
                <Input
                  placeholder="搜索数据集名称"
                  prefix={<SearchOutlined />}
                  value={searchParams.name}
                  onChange={(e) =>
                    setSearchParams((prev) => ({
                      ...prev,
                      name: e.target.value,
                    }))
                  }
                  onPressEnter={() => tableRef.current?.refresh()}
                  style={{ width: 200 }}
                />
                <QueryButton onQuery={() => tableRef.current?.refresh()} />
              </Space>
            }
            titleExtra={
              <Permission value="tenant:sa:dataset:create">
                <PlusButton
                  name="数据集"
                  onClick={() => {
                    setEditingDataset(null);
                    setDatasetModalVisible(true);
                  }}
                />
              </Permission>
            }
            fetchKey={["datasets", selectedFolderId, searchParams]}
            fetchData={() =>
              listDatasets({
                folderId: selectedFolderId,
                name: searchParams.name,
                page: searchParams.page,
                size: searchParams.size,
              })
            }
            columns={columns as ColumnsType<DatasetResponse>}
            rowKey="id"
            pagination={{
              current: searchParams.page,
              pageSize: searchParams.size,
              onChange: (page, size) =>
                setSearchParams((prev) => ({ ...prev, page, size })),
            }}
            selection={false}
          />
        </div>
      </Content>

      <DatasetFolderForm
        open={folderModalVisible}
        onClose={() => setFolderModalVisible(false)}
        currentFolder={currentFolder}
      />

      <DatasetDrawer
        open={datasetModalVisible}
        initial={editingDataset}
        folderId={selectedFolderId}
        tempDbs={tempDbs}
        onClose={() => setDatasetModalVisible(false)}
        onSuccess={() => {
          setDatasetModalVisible(false);
          tableRef.current?.refresh();
        }}
      />
    </Layout>
  );
};

export default Dataset;
