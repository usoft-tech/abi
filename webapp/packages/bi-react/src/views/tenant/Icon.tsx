import Header from "@/components/header/Header";
import FixedPagination from "@/components/pagination/FixedPagination";
import { DeleteOutlined } from "@ant-design/icons";
import type { UploadProps } from "antd";
import {
  Button,
  Form,
  Input,
  message,
  Modal,
  Radio,
  Space,
  Upload,
} from "antd";
import React, { useEffect, useMemo, useRef, useState } from "react";
import styled from "styled-components";
import {
  deleteIcon,
  listIcons,
  SysIconQueryRequest,
  SysIconResponse,
  uploadIcon,
} from "./services";
import { useDispatch } from "react-redux";
import { updateIcon } from "@/store/handleSlice";
import IconFont from "@/components/icon/IconFont";
import { Permission } from "@/permission";

/**
 * 读取文件文本内容
 */
const readFileText = (file: File): Promise<string> => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve(String(reader.result || ""));
    reader.onerror = (e) => reject(e);
    reader.readAsText(file, "utf-8");
  });
};

/**
 * 去除SVG中的填充色
 */
const stripFillColors = (svg: string): string => {
  let s = svg.replace(/\sfill\s*=\s*"[^"]*"/gi, `fill="currentColor"`);
  s = s.replace(/fill\s*:[^;"']+;?/gi, "");
  return s;
};

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
    font-size: 60px;
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

const Svg: React.FC<{ svg: string; keepFill?: boolean }> = ({
  svg,
  keepFill = false,
}) => {
  const computedSvg = useMemo(
    () => (keepFill ? svg : stripFillColors(svg)),
    [svg, keepFill]
  );
  return (
    <>
      <div
        style={{
          width: 40,
          height: 40,
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          margin: "auto auto 10px auto",
          color: "var(--ant-color-text-secondary)",
        }}
        dangerouslySetInnerHTML={{ __html: computedSvg }}
      />
      <div>{keepFill ? "保留填充色" : "去除填充色"}</div>
    </>
  );
};

const EditModal: React.FC<{
  open: boolean;
  onOk: () => void;
  onClose: () => void;
}> = ({ open = false, onOk, onClose }) => {
  const [form] = Form.useForm();

  const [uploading, setUploading] = useState(false);
  const [previewMode, setPreviewMode] = useState<"keep" | "strip">("keep");
  const fileRef = useRef<File | null>(null);
  const [originalSvg, setOriginalSvg] = useState<string>("");

  const uploadProps: UploadProps = {
    accept: ".svg",
    maxCount: 1,
    showUploadList: false,
    beforeUpload: async (file) => {
      try {
        const text = await readFileText(file as File);
        setOriginalSvg(text);
        fileRef.current = file as File;
      } catch (e) {
        message.error("读取SVG失败");
      }
      return false;
    },
    onRemove: () => {
      fileRef.current = null;
      setOriginalSvg("");
    },
  };

  const onSubmit = async () => {
    const values = await form.validateFields();
    const file = fileRef.current;
    if (!file) {
      message.warning("请先选择SVG文件");
      return;
    }
    setUploading(true);
    try {
      const keepFill = previewMode === "keep";
      const res = await uploadIcon(
        file,
        keepFill,
        values.name,
        values.description
      );
      if (res.code === "OK" || res.code === 200) {
        message.success("上传成功");
        form.resetFields();
        fileRef.current = null;
        await onOk();
        onClose();
      } else {
        message.error(res.message || "上传失败");
      }
    } finally {
      setUploading(false);
    }
  };

  useEffect(() => {
    if (form) {
      if (!open) {
        form.resetFields();
      } else {
        form.setFieldsValue({
          name: "",
          description: "",
        });
        setOriginalSvg("");
        setPreviewMode("keep");
      }
    }
  }, [open, form]);

  return (
    <Modal
      title="上传图标"
      okText="提交"
      cancelText="取消"
      open={open}
      loading={uploading}
      onCancel={onClose}
      onOk={onSubmit}
    >
      <Form form={form} layout="vertical" style={{ maxWidth: 520 }}>
        <Form.Item name="name" label="图标名称">
          <Input placeholder="可不填，默认取文件名" />
        </Form.Item>
        <Form.Item name="description" label="图标描述">
          <Input.TextArea rows={3} placeholder="可选" />
        </Form.Item>
        <Form.Item label="选择SVG文件">
          <Upload.Dragger {...uploadProps}>
            <p>点击或拖拽上传SVG文件</p>
          </Upload.Dragger>
        </Form.Item>
        {originalSvg && (
          <Form.Item label="填充色选择">
            <Radio.Group
              value={previewMode}
              onChange={(e) => setPreviewMode(e.target.value)}
            >
              <Radio.Button
                value="keep"
                style={{ height: "auto", textAlign: "center" }}
              >
                <Svg svg={originalSvg} keepFill={true} />
              </Radio.Button>
              <Radio.Button
                value="strip"
                style={{ height: "auto", textAlign: "center" }}
              >
                <Svg svg={originalSvg} keepFill={false} />
              </Radio.Button>
            </Radio.Group>
          </Form.Item>
        )}
      </Form>
    </Modal>
  );
};

/**
 * 租户图标管理页面
 */
const Icon: React.FC = () => {
  const dispatch = useDispatch();
  const [queryForm] = Form.useForm<SysIconQueryRequest>();
  const [items, setItems] = useState<SysIconResponse[]>([]);
  const [uploadModalOpen, setUploadModalOpen] = useState(false);
  const [current, setCurrent] = useState(1);
  const [pageSize, setPageSize] = useState(40);
  const [total, setTotal] = useState(0);

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

  const handleDelete = async (id: string) => {
    Modal.confirm({
      title: "确认删除吗？",
      okText: "确认",
      cancelText: "取消",
      onOk: async () => {
        await deleteIcon(id);
        message.success("删除成功");
        dispatch(updateIcon());
        onSearch();
      },
    });
  };

  const handleAdded = () => {
    onSearch();
    dispatch(updateIcon());
  };

  const onOpenUploadModal = () => {
    setUploadModalOpen(true);
  };

  const iconCards = useMemo(() => {
    return items.map((item: SysIconResponse, index) => (
      <SvgCard key={index}>
        <div>
          <IconFont type={`icon-${item.id}`} />
        </div>
        <div className="name">{item.name}</div>
        <Permission value="tenant:sa:icon:delete">
          <a onClick={() => handleDelete(item.id)}>
            <DeleteOutlined />
          </a>
        </Permission>
      </SvgCard>
    ));
  }, [items]);

  useEffect(() => {
    onSearch();
  }, []);

  return (
    <>
      <Form
        form={queryForm as any}
        layout="inline"
        onFinish={() => onSearch()}
        style={{ marginTop: 12, marginBottom: 24 }}
      >
        <Form.Item name="name" label="名称">
          <Input size="small" allowClear placeholder="按名称筛选" />
        </Form.Item>
        <Form.Item name="keyword" label="关键字">
          <Input size="small" allowClear placeholder="按名称或描述筛选" />
        </Form.Item>
        <Form.Item>
          <Space>
            <Button size="small" htmlType="submit">
              查询
            </Button>
          </Space>
        </Form.Item>
      </Form>

      <Header
        title="图标管理"
        extra={
          <Permission value="tenant:sa:icon:upload">
            <Button type="primary" onClick={onOpenUploadModal}>
              上传图标
            </Button>
          </Permission>
        }
      />

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
      <div style={{ marginTop: 30, display: "flex", justifyContent: "center" }}>
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

      <EditModal
        open={uploadModalOpen}
        onClose={() => setUploadModalOpen(false)}
        onOk={handleAdded}
      />
    </>
  );
};

export default Icon;
