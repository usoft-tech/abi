import {
  Flex,
  PaginationProps,
  Table,
  type TableProps
} from "antd";
import { type TableRowSelection } from "antd/es/table/interface";
import { useMemo, useState } from "react";
import styled from "styled-components";
import FixedPagination from "../pagination/FixedPagination";

export interface StrongTableProps<RecordType = Record<PropertyKey, any>>
  extends Omit<
    TableProps<RecordType>,
    "title" | "pagination" | "locale" | "rowSelection"
  > {
  showIndex?: boolean;
  title?: React.ReactNode | null;
  queryBar?: React.ReactNode | null;
  titleExtra?: React.ReactNode | null;
  footerExtra?: React.ReactNode | null;
  selection?:
    | false
    | Pick<
        TableRowSelection<RecordType>,
        "type" | "selectedRowKeys" | "onChange"
      >;
  pagination:
    | {
        topEnabled?: boolean;
        current?: number;
        pageSize?: number;
        total?: number;
        onChange?: (page: number, pageSize: number) => void;
      }
    | false;
}

const classPrefix = "strong-table";

const Wrapper = styled.div`
  .${classPrefix}-query-bar {
    margin: 12px 0 24px 0;
  }
  .${classPrefix}-title-wrapper {
    margin-bottom: 12px;

    .${classPrefix}-title {
      font-size: 16px;
      font-weight: 500;
    }
    .${classPrefix}-title-extra {
      font-size: 14px;
      font-weight: 400;
    }
  }

  .${classPrefix}-footer {
    margin-top: 12px;
    .${classPrefix}-footer-extra {
      font-size: 14px;
      font-weight: 400;
    }
  }
`;

const StrongTable = <
  RecordType extends Record<PropertyKey, any> = Record<PropertyKey, any>
>({
  showIndex = true,
  queryBar,
  title,
  pagination = {},
  titleExtra,
  footerExtra,
  selection = false,
  ...rest
}: StrongTableProps<RecordType>) => {
  const [current, setCurrent] = useState(
    pagination ? pagination.current || 1 : 1
  );
  const [pageSize, setPageSize] = useState(
    pagination ? pagination.pageSize || 10 : 10
  );

  const tablePagination = useMemo(() => {
    if (pagination === false) {
      return null;
    }

    return (
      <FixedPagination
        size={rest.size as PaginationProps["size"]}
        current={current}
        pageSize={pageSize}
        total={pagination.total || rest.dataSource?.length || 0}
        onChange={(page, size) => {
          setCurrent(page);
          setPageSize(size);
          pagination?.onChange?.(page, size);
        }}
      />
    );
  }, [pagination, rest.columns]);

  const titleExtraNode = useMemo(() => {
    if (titleExtra) {
      return titleExtra;
    }
    if (pagination && pagination?.topEnabled) {
      return tablePagination;
    }
    return null;
  }, [titleExtra, tablePagination, pagination]);

  const tableColumns: TableProps<RecordType>["columns"] = useMemo(
    () =>
      [
        showIndex
          ? {
              title: "序号",
              key: "$index",
              width: 60,
              align: "center",
              render(_: string, __: RecordType, index: number) {
                return current * pageSize - (pageSize - 1) + index;
              },
            }
          : null,
        ...(rest.columns || []),
      ].filter(Boolean) as TableProps<RecordType>["columns"],
    [rest.columns]
  );

  return (
    <Wrapper>
      {queryBar && <div className={`${classPrefix}-query-bar`}>{queryBar}</div>}
      {(title || titleExtraNode) && (
        <Flex
          justify="space-between"
          align="center"
          className={`${classPrefix}-title-wrapper`}
        >
          <div className={`${classPrefix}-title`}>{title}</div>
          <div className={`${classPrefix}-title-extra`}>{titleExtraNode}</div>
        </Flex>
      )}
      <Table<RecordType>
        {...rest}
        columns={tableColumns}
        pagination={false}
        locale={{ emptyText: "暂无数据" }}
        rowSelection={
          selection
            ? {
                ...selection,
              }
            : undefined
        }
        bordered={typeof rest.bordered === "boolean" ? rest.bordered : true}
        size={rest.size || "small"}
      />
      {(footerExtra || tablePagination) && (
        <Flex
          justify="space-between"
          align="center"
          className={`${classPrefix}-footer`}
          style={{ flexDirection: "row-reverse" }}
        >
          {footerExtra && (
            <div className={`${classPrefix}-footer-extra`}>{footerExtra}</div>
          )}
          {tablePagination}
        </Flex>
      )}
    </Wrapper>
  );
};

export default StrongTable;
