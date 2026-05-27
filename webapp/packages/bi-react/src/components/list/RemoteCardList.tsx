import { useEffect, useImperativeHandle, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import CardList, { CardListProps } from "./CardList";
import React from "react";
import { ApiResponse, PageResponse } from "@/services/api";

type RemoteCardListHandle = { refresh: () => void };

interface RemoteCardListProps<
  RecordType extends Record<PropertyKey, any> = Record<PropertyKey, any>
> extends Omit<
    CardListProps<RecordType>,
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

function RemoteCardListInner<
  RecordType extends Record<PropertyKey, any> = Record<PropertyKey, any>
>(
  { fetchKey = [], fetchData, ...rest }: RemoteCardListProps<RecordType>,
  ref: React.ForwardedRef<RemoteCardListHandle>
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
    <CardList<RecordType>
      {...rest}
      pagination={{ ...rest.pagination, total }}
      dataSource={dataSource}
      loading={isLoading}
    />
  );
}

const RemoteCardList = React.forwardRef(RemoteCardListInner) as <
  RecordType extends Record<PropertyKey, any> = Record<PropertyKey, any>
>(
  props: RemoteCardListProps<RecordType> & React.RefAttributes<RemoteCardListHandle>
) => React.ReactElement;

export default RemoteCardList;
