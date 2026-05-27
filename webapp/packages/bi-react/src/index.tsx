import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { ConfigProvider } from "antd";
import zhCN from "antd/locale/zh_CN";
import React from "react";
import ReactDOM from "react-dom/client";
import { Provider } from "react-redux";
import { RouterProvider } from "react-router-dom";
import router from "./router";
import { store } from "./store";

// for date-picker i18n
import "dayjs/locale/zh-cn";

import "./i18n/config";
import "./styles/global.css";
import { loader } from "@monaco-editor/react";
import config from "./config";

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchInterval: 1000 * 60,
      refetchOnWindowFocus: false,
    },
  },
});

loader.config({
  paths: {
    vs: config.basePath + "/static/monaco-editor@0.55.1/min/vs",
  },
});

const rootElement = document.getElementById("root");

if (rootElement) {
  const root = ReactDOM.createRoot(rootElement);
  root.render(
    <React.StrictMode>
      <ConfigProvider locale={zhCN}>
        <QueryClientProvider client={queryClient}>
          <Provider store={store}>
            <RouterProvider router={router} />
          </Provider>
        </QueryClientProvider>
      </ConfigProvider>
    </React.StrictMode>,
  );
}
