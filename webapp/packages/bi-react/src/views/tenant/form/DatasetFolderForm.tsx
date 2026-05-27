import { useMutation, useQueryClient } from "@tanstack/react-query";
import { Form, Input, message, Modal } from "antd";
import { useEffect } from "react";
import {
  DatasetFolderEntity,
  DatasetFolderUpdateRequest,
  createDatasetFolder,
  updateDatasetFolder,
} from "../services";

type Props = {
  open: boolean;
  currentFolder?: Partial<DatasetFolderEntity> | null;
  onClose: () => void;
};

export const DatasetFolderForm: React.FC<Props> = ({
  open,
  currentFolder,
  onClose,
}) => {
  const [folderForm] = Form.useForm();
  const queryClient = useQueryClient();

  const createFolderMutation = useMutation({
    mutationFn: createDatasetFolder,
    onSuccess: () => {
      message.success("创建成功");
      onClose();
      queryClient.invalidateQueries({ queryKey: ["dataset-folders"] });
    },
  });

  const updateFolderMutation = useMutation({
    mutationFn: (data: DatasetFolderUpdateRequest & { id: string }) =>
      updateDatasetFolder(data.id, data),
    onSuccess: () => {
      message.success("更新成功");
      onClose();
      queryClient.invalidateQueries({ queryKey: ["dataset-folders"] });
    },
  });

  const handleFolderSubmit = async () => {
    try {
      const values = await folderForm.validateFields();
      if (currentFolder?.id) {
        updateFolderMutation.mutate({ id: currentFolder.id, ...values });
      } else {
        createFolderMutation.mutate({
          ...values,
          parentId: currentFolder?.parentId,
        });
      }
    } catch (e) {
      // validation failed
    }
  };

  useEffect(() => {
    if (currentFolder) {
      folderForm.setFieldsValue(currentFolder);
    } else {
      folderForm.resetFields();
    }
  }, [currentFolder, folderForm]);

  return (
    <Modal
      title={currentFolder?.id ? "编辑文件夹" : "新建文件夹"}
      open={open}
      onOk={handleFolderSubmit}
      onCancel={onClose}
      confirmLoading={
        createFolderMutation.isPending || updateFolderMutation.isPending
      }
      destroyOnHidden
    >
      <Form form={folderForm} layout="vertical">
        <Form.Item
          name="name"
          label="名称"
          rules={[{ required: true, message: "请输入名称" }]}
        >
          <Input placeholder="请输入文件夹名称" />
        </Form.Item>
        <Form.Item name="sort" label="排序">
          <Input type="number" placeholder="请输入排序值" />
        </Form.Item>
      </Form>
    </Modal>
  );
};
