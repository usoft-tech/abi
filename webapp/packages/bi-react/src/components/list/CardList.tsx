import {
  Card,
  Col,
  ColProps,
  Flex,
  Image,
  PaginationProps,
  Row,
  RowProps,
  Spin,
  Typography,
} from "antd";
import { useMemo, useState } from "react";
import styled from "styled-components";
import FixedPagination from "../pagination/FixedPagination";

import Empty from "@/assets/images/empty.png"

type Key = string | number;
export type RowSelectionType = "checkbox" | "radio";

export interface CardListProps<RecordType = Record<PropertyKey, any>> {
  size?: PaginationProps["size"];
  hoverable?: boolean;
  title?: React.ReactNode | null;
  titleExtra?: React.ReactNode | null;
  queryBar?: React.ReactNode | null;
  dataSource?: RecordType[];
  rowKey?: string;
  loading?: boolean;
  selection?:
    | false
    | {
        type?: RowSelectionType;
        selectedRowKeys?: Key[];
        onChange?: (selectedRowKeys: Key[]) => void;
      };
  pagination:
    | {
        topEnabled?: boolean;
        current?: number;
        pageSize?: number;
        total?: number;
        onChange?: (page: number, pageSize: number) => void;
      }
    | false;
  titleKey?: string;
  descriptionKey?: string;
  coverKey?: string;
  actionsRender?: (record: RecordType) => React.ReactNode[];
  grid?: {
    gutter?: RowProps["gutter"];
  } & ColProps;
}

const classPrefix = "card-list";

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
  .ant-card-actions li {
    margin-top: 2px;
    margin-bottom: 2px;
  }

  .${classPrefix}-footer {
    margin-top: 12px;
    .${classPrefix}-footer-extra {
      font-size: 14px;
      font-weight: 400;
    }
  }
`;

const CardList = <
  RecordType extends Record<PropertyKey, any> = Record<PropertyKey, any>,
>({
  queryBar,
  size,
  hoverable = true,
  title,
  titleExtra,
  dataSource = [],
  rowKey,
  loading = false,
  pagination = {},
  selection = false,
  titleKey,
  descriptionKey,
  coverKey,
  actionsRender,
  grid,
}: CardListProps<RecordType>) => {
  const [current, setCurrent] = useState(
    pagination ? pagination.current || 1 : 1,
  );
  const [pageSize, setPageSize] = useState(
    pagination ? pagination.pageSize || 10 : 10,
  );

  const { gutter = [12, 12], ...col } = grid || {};

  const listPagination = useMemo(() => {
    if (pagination === false) {
      return null;
    }

    return (
      <FixedPagination
        size={size as PaginationProps["size"]}
        current={current}
        pageSize={pageSize}
        total={pagination.total || dataSource?.length || 0}
        onChange={(page, size) => {
          setCurrent(page);
          setPageSize(size);
          pagination?.onChange?.(page, size);
        }}
      />
    );
  }, [pagination]);

  const titleExtraNode = useMemo(() => {
    if (titleExtra) {
      return titleExtra;
    }
    if (pagination && pagination?.topEnabled) {
      return listPagination;
    }
    return null;
  }, [titleExtra, listPagination, pagination]);

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
      <Spin spinning={loading} delay={200} tip="加载中...">
        <Row gutter={gutter}>
          {(dataSource || []).map((record, index) => (
            <Col
              key={rowKey?.length ? record[rowKey] || index : index}
              {...col}
            >
              <Card
                size={size}
                hoverable={hoverable}
                style={{ width: "100%" }}
                cover={
                  coverKey ? (
                    <Image
                      draggable={false}
                      alt="example"
                      src={record[coverKey] || ""}
                      fallback={Empty}
                      style={{ objectFit: "cover" }}
                    />
                  ) : undefined
                }
                actions={
                  actionsRender
                    ? actionsRender?.(record) || undefined
                    : undefined
                }
              >
                <Card.Meta
                  title={titleKey ? record[titleKey] || "" : ""}
                  description={
                    descriptionKey ? (
                      <Typography.Text
                        ellipsis={{ tooltip: record[descriptionKey] || "" }}
                        style={{
                          color: "var(--ant-color-text-description)",
                        }}
                      >
                        {record[descriptionKey] || ""}
                      </Typography.Text>
                    ) : undefined
                  }
                />
              </Card>
            </Col>
          ))}
        </Row>
      </Spin>
      {listPagination && (
        <Flex
          justify="center"
          align="center"
          className={`${classPrefix}-footer`}
        >
          {listPagination}
        </Flex>
      )}
    </Wrapper>
  );
};

export default CardList;
