import { RootState } from "@/store";
import { createFromIconfontCN } from "@ant-design/icons";
import { IconFontProps } from "@ant-design/icons/lib/components/IconFont";
import { useMemo } from "react";
import { useSelector } from "react-redux";
import config from "@/config";

const IconFont: React.FC<IconFontProps<string>> = (props) => {
  const { tenant } = useSelector((state: RootState) => state.account);
  const { icon } = useSelector((state: RootState) => state.handle);

  const Icon = useMemo(() => {
    const scriptUrl = ["//at.alicdn.com/t/c/font_5072483_uqvajkxw0rf.js"];
    if (tenant?.id) {
      scriptUrl.push(config.apiBasePath + `/icons/${tenant?.id}.js?t=${icon}`);
    }
    return createFromIconfontCN({
      scriptUrl,
    });
  }, [tenant, icon]);

  return <Icon {...props} />;
};

export default IconFont;
