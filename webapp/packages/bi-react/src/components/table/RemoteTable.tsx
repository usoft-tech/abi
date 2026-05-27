import { useEffect, useImperativeHandle, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import StrongTable, { StrongTableProps } from "./StrongTable";
import React from "react";
import { ApiResponse, PageResponse } from "@/services/api";

type RemoteTableHandle = { refresh: () => void };

interface RemoteTableProps<
  RecordType extends Record<PropertyKey, any> = Record<PropertyKey, any>
> extends Omit<
    StrongTableProps<RecordType>,
    "dataSource" | "pagination" | "loading"
  > {
  fetchKey: any[];
  fetchData: () => Promise<ApiResponse<PageResponse<RecordType>>>;
  pagination:
    | {
        topEnabled?: boolean;
        current?: number;
        pageSize?: number;
        onChange?: (page: number, pageSize: number) => void;
      }
    | false;
}

function RemoteTableInner<
  RecordType extends Record<PropertyKey, any> = Record<PropertyKey, any>
>(
  { fetchKey = [], fetchData, ...rest }: RemoteTableProps<RecordType>,
  ref: React.ForwardedRef<RemoteTableHandle>
) {
  const [dataSource, setDataSource] = useState<RecordType[]>([]);
  const [total, setTotal] = useState<number>(0);

  const { data: apiData, isLoading, refetch } = useQuery<ApiResponse<PageResponse<RecordType>>>({
    retry: false,
    queryKey: fetchKey,
    queryFn: fetchData,
    enabled: !!fetchKey?.length && !!fetchData,
  });

  useImperativeHandle(ref, () => ({
    refresh: refetch,
  }));

  useEffect(() => {
    if (apiData && apiData.code === "OK") {
      const {total, items} = apiData.data || {}
      setTotal(total || 0);
      setDataSource(items || []);
    }
  }, [apiData]);

  return (
    <StrongTable<RecordType>
      {...rest}
      pagination={{ ...rest.pagination, total }}
      dataSource={dataSource}
      loading={isLoading}
    />
  );
}

const RemoteTable = React.forwardRef(RemoteTableInner) as <
  RecordType extends Record<PropertyKey, any> = Record<PropertyKey, any>
>(
  props: RemoteTableProps<RecordType> & React.RefAttributes<RemoteTableHandle>
) => React.ReactElement;

export default RemoteTable;
