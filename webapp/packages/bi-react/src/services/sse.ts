import {
  getTenantId,
  getToken,
  TENANT_ID_HEADER,
  TOKEN_HEADER,
  TOKEN_PREFIX,
} from "@/utils/token";
import { fetchEventSource } from "@microsoft/fetch-event-source";

export type SseEvent<T> = {
  id?: string;
  event?: string;
  status?: string;
  message?: string;
  data?: T;
  finished?: boolean;
  timestamp?: number;
};

export type SseOptions = {
  url: string;
  data?: any;
  onOpen?: () => void;
  onMessage?: (evt: SseEvent<any>) => void;
  onError?: (err: Error) => void;
  onClose?: (evt?: SseEvent<any>) => void;
};

export type SseConnection = {
  stop: () => void;
};

/**
 * 启动 SSE 连接
 * @param {SseOptions} options - SSE 选项
 * @return {SseConnection} - SSE 连接对象
 */
export const startSSE: (options: SseOptions) => SseConnection = ({
  url,
  data,
  onOpen,
  onMessage,
  onError,
  onClose,
}) => {
  const controller = new AbortController();

  const token = getToken();
  const tenantId = getTenantId();

  const headers: Record<string, string> = {
    "Content-Type": "application/json",
    "Cache-Control": "no-cache",
    Connection: "keep-alive",
    Accept: "text/event-stream",
  };

  if (token) {
    headers[TOKEN_HEADER] = TOKEN_PREFIX + token;
  }
  if (tenantId) {
    headers[TENANT_ID_HEADER] = tenantId;
  }
  fetchEventSource(url, {
    method: "POST",
    headers,
    openWhenHidden: true,
    credentials: "include",
    body: data ? JSON.stringify(data) : undefined,
    signal: controller.signal,
    // 建立连接成功
    onopen: async (response) => {
      if (response.ok) {
        // 正常连接
        onOpen?.();
        return;
      }
      // 非 2xx
      throw new Error(`SSE connection error: ${response.status}`);
    },
    // 接收消息事件
    onmessage: (msg) => {
      // msg: {id, event, data}
      const { event, data } = msg;
      let payload: SseEvent<any> = {};
      try {
        payload = data ? JSON.parse(data) : {};
      } catch (e) {
        // 非 JSON 数据，作为 message 文本
        payload = { message: data };
      }
      const evt = {
        id: payload.id,
        event,
        status: payload.status,
        message: payload.message,
        data: payload.data,
        finished: payload.finished,
        timestamp: payload.timestamp,
      };
      if (event === "heartbeat") {
        // 心跳事件，忽略
        return;
      }
      if (event === "complete") {
        // 完成事件
        onClose?.(evt);
        return;
      }
      onMessage?.(evt);
    },
    onerror: (err) => {
      // 触发错误事件，交给上层处理
      onError?.(err);
    },
    // 连接关闭或完成
    onclose: () => {
      onClose?.();
    },
  });

  return {
    stop: () => controller.abort(),
  };
};
