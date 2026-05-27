import { InfoCircleOutlined } from "@ant-design/icons";
import { Tooltip } from "antd";

const HelpIcon: React.FC<{ title: React.ReactNode }> = ({ title }) => {
  return (
    <Tooltip title={title}>
      <InfoCircleOutlined
        style={{ color: "var(--ant-color-text-secondary)" }}
      />
    </Tooltip>
  );
};

export default HelpIcon;
