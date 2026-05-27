import { SearchOutlined } from "@ant-design/icons";
import { Button } from "antd/es";

const QueryButton: React.FC<{
  onQuery?: () => void;
  htmlType?: "submit" | "button";
}> = ({ onQuery, htmlType = "submit" }) => {
  return (
    <Button htmlType={htmlType} icon={<SearchOutlined />} onClick={onQuery}>
      查询
    </Button>
  );
};

export default QueryButton;
