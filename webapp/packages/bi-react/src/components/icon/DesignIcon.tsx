import { BlockOutlined } from "@ant-design/icons";
import { Button, Tooltip } from "antd";

const EditIcon: React.FC<{
  onEdit: () => void;
  label?: string;
  style?: React.CSSProperties;
  className?: string;
}> = ({ onEdit, label, style, className }) => {
  return (
    <Button type="link" onClick={onEdit} style={style} className={className}>
      {label?.length ? (
        <BlockOutlined />
      ) : (
        <Tooltip title="设计">
          <BlockOutlined />
        </Tooltip>
      )}
      {label}
    </Button>
  );
};

export default EditIcon;
