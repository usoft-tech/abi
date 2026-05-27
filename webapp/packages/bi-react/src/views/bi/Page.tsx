import IconFont from "@/components/icon/IconFont";
import MarkdownEditor from "@/components/markdown/MarkdownEditor";
import RemoteTable from "@/components/table/RemoteTable";
import { Permission } from "@/permission";
import { PageReportResponse, listPageReports } from "@/services/report";
import { SseConnection, SseEvent } from "@/services/sse";
import { LoadingOutlined, UnorderedListOutlined } from "@ant-design/icons";
import { useQuery } from "@tanstack/react-query";
import {
  Drawer,
  FloatButton,
  Modal,
  Skeleton,
  Typography,
  Watermark,
} from "antd";
import { PageCanvas, PageProvider, PageSchema, plugins } from "bi-sdk-react";
import { SchemaItemType } from "bi-sdk-react/dist/types/components/typing";
import dayjs from "dayjs";
import { useEffect, useRef, useState } from "react";
import MdEditor from "react-markdown-editor-lite";
import { useParams } from "react-router-dom";
import styled from "styled-components";
import { fetch } from "../bi";
import Error404 from "../errors/404";
import { ContainerPlugin, IconPlugin } from "../tenant/designer-plugins";
import { getAppSchema, pageInterpretation } from "../tenant/services";
import { RootState } from "@/store";
import { useSelector } from "react-redux";

const StyledMarkdownEditor = styled(MarkdownEditor)`
  height: 100%;
  border: none;

  .sec-html {
    border: none;

    .section-container.html-wrap {
      padding: 0 20px 0 0;
    }
  }
`;

type InterpretationItem = {
  id: string;
  type: string;
  name: string;
  description: string;
  data?: string | null;
  script?: string | null;
  template?: string | null;
  children?: InterpretationItem[] | Record<string, InterpretationItem[]>;
};

type PageInterpretation = {
  id: string;
  reportId?: string;
  info: PageSchema["info"];
  items: InterpretationItem[];
};

const toInterpretationItem = (
  item: SchemaItemType,
  getItemData: (id: string) => any,
): InterpretationItem => {
  const { id, type, name, description, props } = item;
  let children;
  if (item.children) {
    if (Array.isArray(item.children)) {
      children = item.children
        .filter((child) => child.type !== "b-icon")
        .map((child) => toInterpretationItem(child, getItemData));
    } else if (typeof item.children === "object") {
      children = Object.fromEntries(
        Object.entries(item.children).map(([key, children]) => [
          key,
          children
            .filter((child) => child.type !== "b-icon")
            .map((child) => toInterpretationItem(child, getItemData)),
        ]),
      );
    }
  }
  const data = getItemData(id!);
  return {
    id: id!,
    type,
    name: name!,
    description: description!,
    data: JSON.stringify(data) || "",
    script: props?.script || "",
    template: props?.template || "",
    children,
  };
};

const Page = () => {
  const { user, tenant } = useSelector((state: RootState) => state.account);
  const [itemDatas, setItemDatas] = useState<Record<string, any>>({});
  const [sseClient, setSseClient] = useState<SseConnection | null>(null);
  const [sseRunning, setSseRunning] = useState(false);
  const [interpretOpen, setInterpretOpen] = useState(false);
  const [interpretion, setInterpretion] = useState("");
  const [schema, setSchema] = useState<PageSchema | null>(null);
  const refMarkdownEditor = useRef<MdEditor>(null);

  // 历史报告相关状态
  const [historyOpen, setHistoryOpen] = useState(false);
  const [detailOpen, setDetailOpen] = useState(false);
  const [currentReport, setCurrentReport] = useState<PageReportResponse | null>(
    null,
  );
  const [reportPage, setReportPage] = useState(1);
  const [reportPageSize, setReportPageSize] = useState(10);
  const reportTableRef = useRef<{ refresh: () => void }>(null);

  const params = useParams();
  const { appId } = params;

  const { data: page, isLoading } = useQuery({
    retry: false,
    queryKey: ["app", appId],
    queryFn: () => getAppSchema(appId!).then((res) => res.data),
    enabled: !!appId,
  });

  const startSse = () => {
    setSseRunning(true);
    setInterpretOpen(true);
  };

  const stopSse = () => {
    setSseRunning(false);
  };

  const handleSseError = (err: Error) => {
    console.error("SSE 错误:", err);
    stopSse();
    sseClient?.stop();
  };

  const handleSseMessage = (evt: SseEvent<any>) => {
    // console.log("SSE 消息:", evt);
    setInterpretion((prev) => prev + evt.message);
    const el = refMarkdownEditor.current?.getHtmlElement();
    if (el) {
      el.scrollTo({
        top: el.scrollHeight,
        behavior: "smooth",
      });
    }
  };

  const setItemData = (id: string, data: any) => {
    setItemDatas((prev) => ({ ...prev, [id]: data }));
  };

  useEffect(() => {
    if (page?.schema) {
      setSchema(page.schema);
    }
  }, [page?.schema]);

  useEffect(() => {
    return () => {
      if (sseClient) {
        sseClient.stop();
      }
    };
  }, [sseClient]);

  return (
    <div style={{margin: -20}}>
      {!isLoading && !!schema ? (
        <>
          <PageProvider
            pageId={appId!}
            designable={false}
            plugins={[...plugins, IconPlugin, ContainerPlugin]}
            schema={schema!}
            fetch={fetch}
            setItemData={setItemData}
          >
            {page?.hasWatermark ? (
              <Watermark content={[user?.displayName!, tenant?.name!]}>
                <PageCanvas device="desktop" />
              </Watermark>
            ) : (
              <PageCanvas device="desktop" />
            )}
          </PageProvider>
          <Drawer
            title={
              <>
                {sseRunning ? (
                  <>
                    <Typography.Text strong>
                      <LoadingOutlined spin />
                      数据解读中...
                    </Typography.Text>
                  </>
                ) : (
                  <Typography.Text strong>数据解读</Typography.Text>
                )}

                <Typography.Text type="secondary">
                  （以下内容均由AI自动生成，仅用于参考）
                </Typography.Text>
              </>
            }
            open={interpretOpen}
            onClose={() => setInterpretOpen(false)}
            size={800}
            styles={{
              body: { padding: 0 },
            }}
          >
            {page?.hasWatermark ? (
              <Watermark content={[user?.displayName!, tenant?.name!]}>
                <StyledMarkdownEditor
                  ref={refMarkdownEditor}
                  value={interpretion}
                  height="100%"
                  menu={false}
                  md={false}
                  html={true}
                />
              </Watermark>
            ) : (
              <StyledMarkdownEditor
                ref={refMarkdownEditor}
                value={interpretion}
                height="100%"
                menu={false}
                md={false}
                html={true}
              />
            )}
          </Drawer>

          {page?.id && (
            <Drawer
              title="历史报告"
              open={historyOpen}
              onClose={() => setHistoryOpen(false)}
              size={600}
            >
              <RemoteTable<PageReportResponse>
                ref={reportTableRef}
                fetchKey={["page-reports", page.id, reportPage, reportPageSize]}
                fetchData={() =>
                  listPageReports(page.id, {
                    page: reportPage,
                    size: reportPageSize,
                  })
                }
                pagination={{
                  current: reportPage,
                  pageSize: reportPageSize,
                  onChange: (page, size) => {
                    setReportPage(page);
                    setReportPageSize(size);
                  },
                }}
                columns={[
                  {
                    title: "标题",
                    dataIndex: "title",
                    key: "title",
                    render: (text, record) => (
                      <a
                        onClick={() => {
                          setCurrentReport(record);
                          setDetailOpen(true);
                        }}
                      >
                        {text}
                      </a>
                    ),
                  },
                  {
                    title: "创建时间",
                    dataIndex: "createdAt",
                    key: "createdAt",
                    width: 180,
                    render: (text) => dayjs(text).format("YYYY-MM-DD HH:mm:ss"),
                  },
                ]}
                selection={false}
                rowKey="id"
              />
            </Drawer>
          )}

          <Drawer
            title={currentReport?.title || "报告详情"}
            open={detailOpen}
            onClose={() => setDetailOpen(false)}
            size={800}
            styles={{
              body: { padding: 0 },
            }}
          >
            {page?.hasWatermark ? (
              <Watermark content={[user?.displayName!, tenant?.name!]}>
                <StyledMarkdownEditor
                  value={currentReport?.content || ""}
                  height="100%"
                  menu={false}
                  md={false}
                  html={true}
                  readOnly
                />
              </Watermark>
            ) : (
              <StyledMarkdownEditor
                value={currentReport?.content || ""}
                height="100%"
                menu={false}
                md={false}
                html={true}
                readOnly
              />
            )}
          </Drawer>
          {page?.id && (
            <Permission value="tenant:sa:page:assistant-interpret">
              <FloatButton.Group
                type="primary"
                trigger="hover"
                icon={<IconFont type="icon-robot" />}
              >
                <FloatButton
                  type="primary"
                  icon={<IconFont type="icon-robot" />}
                  tooltip="数据解读"
                  content="解读"
                  onClick={() => {
                    Modal.confirm({
                      title: "确认由AI解读页面数据？",
                      okText: "解读",
                      okType: "primary",
                      cancelText: "取消",
                      onOk: () => {
                        const data: PageInterpretation = {
                          id: page.id,
                          info: schema.info!,
                          items: schema.items.map((item) =>
                            toInterpretationItem(item, (id) => itemDatas[id]),
                          ),
                        };
                        const sseClient = pageInterpretation({
                          id: page.id,
                          data,
                          onOpen: startSse,
                          onMessage: handleSseMessage,
                          onError: handleSseError,
                          onClose: stopSse,
                        });
                        setSseClient(sseClient);
                      },
                      onCancel: () => {
                        sseClient?.stop();
                        setSseClient(null);
                        setInterpretion("");
                      },
                    });
                  }}
                />
                <FloatButton
                  type="primary"
                  icon={<UnorderedListOutlined />}
                  tooltip="历史报告"
                  content="列表"
                  onClick={() => setHistoryOpen(true)}
                />
              </FloatButton.Group>
            </Permission>
          )}
        </>
      ) : isLoading ? (
        <Skeleton active />
      ) : (
        <Error404 />
      )}
    </div>
  );
};

export default Page;
