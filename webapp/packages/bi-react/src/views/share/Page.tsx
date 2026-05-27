import { useQuery } from "@tanstack/react-query";
import { Skeleton } from "antd";
import { PageCanvas, PageProvider, plugins } from "bi-sdk-react";
import { useParams, useSearchParams } from "react-router-dom";
import { fetch } from "../bi";
import Error404 from "../errors/404";
import { ContainerPlugin, IconPlugin } from "../tenant/designer-plugins";
import { getSharePage } from "../tenant/services";
import { setToken } from "@/utils/token";

const Page = () => {
  const params = useParams();
  const [ searchParams ]= useSearchParams();
  const { shareKey } = params;

  const token = searchParams.get("token");
  if (token) {
    setToken(token);
  }

  const { data: page, isLoading } = useQuery({
    retry: false,
    queryKey: ["sharePage", shareKey],
    queryFn: () => getSharePage(shareKey!).then((res) => res.data),
    enabled: !!shareKey,
  });

  return (
    <>
      {!isLoading && !!page ? (
        <PageProvider
          pageId={page.id!}
          designable={false}
          plugins={[...plugins, IconPlugin, ContainerPlugin]}
          schema={page.schema!}
          fetch={fetch}
        >
          <PageCanvas device="desktop" />
        </PageProvider>
      ) : isLoading ? (
        <Skeleton active />
      ) : (
        <Error404 />
      )}
    </>
  );
};

export default Page;
