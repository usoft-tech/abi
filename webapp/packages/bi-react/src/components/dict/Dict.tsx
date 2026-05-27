import { DictDataResponse, listDictDatasByType } from "@/views/sa/services";
import { useQuery } from "@tanstack/react-query";
import { Radio, Select, Spin, Tag, TagProps } from "antd";
import { CSSProperties, useEffect, useMemo } from "react";

type DictProps = {
  htmlType: "select" | "radio" | "radio-button";
  type: string;
  value?: string;
  onChange?: (value?: string | null) => void;
  className?: string;
  style?: CSSProperties;
};

const DictRender: React.FC<
  Omit<DictProps, "type"> & { dictDatas: DictDataResponse[] }
> = ({ htmlType, value, onChange, dictDatas, className, style }) => {
  if (!dictDatas) {
    return null;
  }
  useEffect(() => {
    if (!value) {
      const defaultValue = dictDatas.find((item) => item.isDefault === "Y");
      value = defaultValue?.dictValue || undefined;
      onChange?.(value);
    }
  }, [dictDatas, value, onChange]);

  switch (htmlType) {
    case "select":
      return (
        <Select
          className={className}
          style={style}
          options={dictDatas.map((item) => ({
            key: item.dictValue,
            value: item.dictValue,
            label: item.dictLabel,
            className: [item.cssClass, item.listClass]
              .filter(Boolean)
              .join(" "),
          }))}
          value={value}
          onChange={onChange}
        />
      );
    case "radio":
    case "radio-button":
      return (
        <Radio.Group
          className={className}
          style={style}
          optionType={htmlType === "radio-button" ? "button" : undefined}
          options={dictDatas.map((item) => ({
            key: item.dictValue,
            value: item.dictValue,
            label: item.dictLabel,
            className: [item.cssClass, item.listClass]
              .filter(Boolean)
              .join(" "),
          }))}
          value={value}
          onChange={(e) => onChange?.(e.target.value)}
        />
      );
    default:
      return null;
  }
};

const Dict: React.FC<DictProps> = ({
  htmlType,
  type,
  value,
  onChange,
  className,
  style,
}) => {
  const { data: dictDatas, isLoading } = useQuery<DictDataResponse[]>({
    queryKey: ["dictDatas", type],
    queryFn: () => listDictDatasByType(type).then((res) => res.data),
    // 数据在缓存中保持 1 分钟为新鲜，不会重复请求
    staleTime: 1000 * 60,
    // 请求失败自动重试 2 次
    retry: 2,
  });

  if (!dictDatas) {
    return null;
  }

  return (
    <Spin spinning={isLoading}>
      <DictRender
        htmlType={htmlType}
        value={value}
        onChange={onChange}
        dictDatas={dictDatas}
        className={className}
        style={style}
      />
    </Spin>
  );
};

/**
 * 字典选择框
 */
export const DictSelect: React.FC<Omit<DictProps, "htmlType">> = ({
  ...rest
}) => <Dict htmlType="select" {...rest} />;

/**
 * 字典单选框
 */
export const DictRadio: React.FC<Omit<DictProps, "htmlType">> = ({
  ...rest
}) => <Dict htmlType="radio" {...rest} />;

/**
 * 字典单选按钮
 */
export const DictRadioButton: React.FC<Omit<DictProps, "htmlType">> = ({
  ...rest
}) => <Dict htmlType="radio-button" {...rest} />;

/**
 * 字典标签
 */
export const DictTag: React.FC<
  {
    type: string;
    value?: string;
  } & TagProps
> = ({ type, value, ...rest }) => {
  const { data: dictDatas, isLoading } = useQuery<DictDataResponse[]>({
    queryKey: ["dictDatas", type],
    queryFn: () => listDictDatasByType(type).then((res) => res.data),
  });
  if (!dictDatas) {
    return null;
  }
  const label = useMemo(() => {
    const item = dictDatas.find((item) => item.dictValue === value);
    return item?.dictLabel || value || "";
  }, [dictDatas, value]);
  return (
    <Spin spinning={isLoading}>
      <Tag {...rest}>{label}</Tag>
    </Spin>
  );
};
