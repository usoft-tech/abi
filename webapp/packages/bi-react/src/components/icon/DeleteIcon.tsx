import { DeleteOutlined } from "@ant-design/icons";
import { Button, Popconfirm, Tooltip } from "antd";

const DeleteIcon: React.FC<{
  onDelete: () => void;
  name?: string;
  label?: string;
  loading?: boolean;
  style?: React.CSSProperties;
  className?: string;
  disabled?: boolean;
}> = ({ onDelete, name = "数据", label, loading, style, className, disabled }) => {
  return (
    <Popconfirm
      title={`确定删除该${name || "数据"}吗?`}
      onConfirm={onDelete}
      okText="确定"
      cancelText="取消"
    >
      <Button type="link" danger style={style} className={className} loading={loading} disabled={disabled || loading}>
        {label?.length ? (
          <DeleteOutlined />
        ) : (
          <Tooltip title="删除">
            <DeleteOutlined />
          </Tooltip>
        )}
        {label}
      </Button>
    </Popconfirm>
  );
};

export default DeleteIcon;
