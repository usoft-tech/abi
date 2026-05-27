import { EditOutlined } from "@ant-design/icons";
import { Button, Tooltip } from "antd";

const EditIcon: React.FC<{
  onEdit: () => void;
  label?: string;
  style?: React.CSSProperties;
  disabled?: boolean;
  className?: string;
}> = ({ onEdit, label, style, disabled, className }) => {
  return (
    <Button type="link" onClick={onEdit} style={style} className={className} disabled={disabled}>
      {label?.length ? (
        <EditOutlined />
      ) : (
        <Tooltip title="编辑">
          <EditOutlined />
        </Tooltip>
      )}
      {label}
    </Button>
  );
};

export default EditIcon;
