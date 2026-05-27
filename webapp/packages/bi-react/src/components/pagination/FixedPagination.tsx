import { Pagination, PaginationProps } from "antd";

type Props =
  | {
      current?: number;
      pageSize?: number;
      total?: number;
      size: PaginationProps["size"];
      onChange?: (current: number, pageSize: number) => void;
    }
  | false;

const FixedPagination: React.FC<Props> = (rest) => {
  if (!rest) {
    return null;
  }

  const { current, pageSize, total, size, onChange } = rest;

  const config = {
    current,
    pageSize,
    total: total || 0,
    size,
    showSizeChanger: true,
    showQuickJumper: true,
    pageSizeOptions: [10, 20, 50, 100],
    showTotal: (total, range) => {
      return total
        ? `当前 ${range[0]}-${range[1]} 条 / 总共 ${total} 条`
        : `总共 ${total} 条`;
    },
    onChange,
    onShowSizeChange(current, size) {
      onChange?.(current, size);
    },
  } as PaginationProps;

  return (
    <Pagination
      {...config}
      locale={{
        items_per_page: "条/页",
        jump_to: "跳转至",
        jump_to_confirm: "确认",
        page: "页",
        page_size: "每页条数",
        next_page: "下一页",
        prev_page: "上一页",
      }}
    />
  );
};

export default FixedPagination;
