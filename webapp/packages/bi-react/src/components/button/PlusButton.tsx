import { PlusOutlined } from "@ant-design/icons";
import { Button, ButtonProps } from "antd";
import { PropsWithChildren } from "react";

const PlusButton: React.FC<
  PropsWithChildren<Omit<ButtonProps, "icon" | "type"> & { name?: string }>
> = ({ name = "", children, ...rest }) => {
  return (
    <Button type="primary" icon={<PlusOutlined />} {...rest}>
      {children || <>新增{name}</>}
    </Button>
  );
};

export default PlusButton;
