import BasicLayout from "@/layouts/BasicLayout";
import { BlankLayout } from "@/layouts/BlankLayout";
import { AppMenuType, AppResponse, listApps } from "@/services/app";
import { config } from "@/store/appSlice";
import { getApp } from "@/views/tenant/services";
import { Empty, Flex, Skeleton } from "antd";
import { useEffect, useState } from "react";
import { useSelector } from "react-redux";
import {
  createBrowserRouter,
  Navigate,
  Params,
  type RouteObject,
} from "react-router-dom";
import setting from "../config";

const AppIndex = () => {
  const { currentApp } = useSelector(config);
  const [firstPage, setFirstPage] = useState<AppResponse | null>(null);
  const [loading, setLoading] = useState(true);
  useEffect(() => {
    const fn = (children: AppResponse[]): AppResponse | null => {
      if (children?.length) {
        for (const item of children) {
          if (item.handle?.type === AppMenuType.PAGE) {
            return item;
          }
          if (item.children?.length) {
            const res = fn(item.children);
            if (res) {
              return res;
            }
          }
        }
      }
      return null;
    };
    if (currentApp) {
      const res = fn(currentApp.children || []);
      if (res) {
        setLoading(false);
        setFirstPage(res);
      }
    }
  }, [currentApp]);
  return (
    <>
      {loading ? (
        ""
      ) : (
        <Navigate to={`/app/${currentApp?.id}/${firstPage?.id}`} />
      )}
    </>
  );
};

const AppHome = () => {
  const [empty, setEmpty] = useState(false);
  const fetchApps = async () => {
    const { data: appList } = await listApps();
    if (!appList?.length) {
      setEmpty(true);
    } else {
      const first = appList[0];
      location.replace(
        location.pathname.endsWith("/") ? `./${first.id}` : `./app/${first.id}`,
      );
    }
  };
  useEffect(() => {
    fetchApps();
  }, []);

  return (
    <>
      {empty ? (
        <Flex style={{ height: "100%" }} align="center" justify="center">
          <Empty
            description="当前租户未发布任何页面"
            image={Empty.PRESENTED_IMAGE_DEFAULT}
          />
        </Flex>
      ) : (
        <Skeleton active />
      )}
    </>
  );
};

export const routerConfig: RouteObject[] = [
  {
    path: "/",
    handle: { title: "menu.home", hidden: true },
    element: <BlankLayout />, //<AdLayout />,
    children: [
      {
        index: true,
        element: <Navigate to="/tenant" />,
        // path: "/",
        // handle: { title: "menu.home" },
        // async lazy() {
        //   const { Home } = await import("@/views/Home");
        //   return {
        //     Component: Home,
        //   };
        // },
      },
    ],
  },
  {
    path: "/login",
    handle: { title: "menu.login", hidden: true },
    async lazy() {
      const { Login } = await import("@/views/Login");
      return {
        Component: Login,
      };
    },
  },
  {
    path: "/third-auth/callback",
    handle: { title: "menu.login", hidden: true },
    async lazy() {
      const { ThirdAuthCallback } = await import("@/views/ThirdAuthCallback");
      return {
        Component: ThirdAuthCallback,
      };
    },
  },
  {
    path: "/app",
    element: <BasicLayout />,
    handle: { hidden: true },
    children: [
      {
        index: true,
        element: <AppHome />,
      },
    ],
  },
  {
    id: "app",
    path: "/app/:groupId",
    element: <BasicLayout />,
    handle: { hidden: true },
    children: [
      {
        index: true,
        element: <AppIndex />,
      },
      {
        id: "app-page",
        path: ":appId",
        handle: {
          title: "menu.app",
        },
        loader: async ({ params }: { params: Params }) => {
          const { data: app } = await getApp(params.appId!);
          return {
            title: `${app.handle?.title}`,
          };
        },
        async lazy() {
          const { default: Page } = await import("@/views/bi/Page");
          return {
            Component: Page,
          };
        },
      },
    ],
  },
  {
    id: "tenant",
    path: "/tenant",
    element: <BasicLayout />,
    handle: {
      title: "menu.tenant",
      permissions: ["tenant:sa"],
      icon: "icon-tenant",
    },
    children: [
      {
        index: true,
        element: <Navigate to="/tenant/page" />,
      },
      {
        handle: { type: "group", title: "menu.permission" },
        children: [
          {
            path: "setting",
            handle: {
              title: "menu.tenantSetting",
              permissions: ["tenant:sa:setting"],
              icon: "icon-setting",
            },
            async lazy() {
              const { default: Setting } =
                await import("@/views/tenant/Setting");
              return {
                Component: Setting,
              };
            },
          },
          {
            path: "user",
            handle: {
              title: "menu.user",
              permissions: ["tenant:sa:user"],
              icon: "icon-user-setting",
            },
            async lazy() {
              const { User } = await import("@/views/tenant/User");
              return {
                Component: User,
              };
            },
          },
          {
            path: "sk",
            handle: {
              title: "menu.secret",
              permissions: ["tenant:sa:sk"],
              icon: "icon-setting",
            },
            async lazy() {
              const { default: Sk } = await import("@/views/tenant/Sk");
              return {
                Component: Sk,
              };
            },
          },
        ],
      },
      // {
      //   handle: { type: "divider" },
      // },
      {
        handle: { type: "group", title: "menu.data" },
        children: [
          {
            path: "datasource",
            handle: {
              title: "menu.datasource",
              permissions: ["tenant:sa:datasource"],
              icon: "icon-datasource",
            },
            async lazy() {
              const { default: Datasource } =
                await import("@/views/tenant/Datasource");
              return {
                Component: Datasource,
              };
            },
          },
          {
            path: "dataset",
            handle: {
              title: "menu.dataset",
              permissions: ["tenant:sa:dataset"],
              icon: "icon-dataset",
            },
            async lazy() {
              const { default: Dataset } =
                await import("@/views/tenant/Dataset");
              return {
                Component: Dataset,
              };
            },
          },
        ],
      },
      // {
      //   handle: { type: "divider" },
      // },
      {
        handle: { type: "group", title: "menu.appManage" },
        children: [
          {
            path: "app",
            handle: {
              title: "menu.appManage",
              permissions: ["tenant:sa:app"],
              icon: "icon-app-store",
            },
            async lazy() {
              const { default: App } = await import("@/views/tenant/App");
              return {
                Component: App,
              };
            },
          },
          {
            path: "icon",
            handle: {
              title: "menu.iconManage",
              permissions: ["tenant:sa:icon"],
              icon: "icon-delete-square",
            },
            async lazy() {
              const { default: Icon } = await import("@/views/tenant/Icon");
              return {
                Component: Icon,
              };
            },
          },
          {
            path: "page",
            handle: {
              title: "menu.page",
              permissions: ["tenant:sa:page"],
              icon: "icon-report-pane",
            },
            async lazy() {
              const { default: Page } = await import("@/views/tenant/Page");
              return {
                Component: Page,
              };
            },
          },
        ],
      },
    ],
  },
  {
    id: "sa",
    path: "/sa",
    element: <BasicLayout />,
    handle: { title: "menu.system", permissions: ["sa"], icon: "icon-setting" },
    children: [
      {
        index: true,
        element: <Navigate to="setting" />,
      },
      {
        path: "setting",
        handle: {
          title: "menu.setting",
          permissions: ["sa:setting"],
          icon: "icon-setting",
        },
        async lazy() {
          const { default: Setting } = await import("@/views/sa/Setting");
          return {
            Component: Setting,
          };
        },
      },
      {
        path: "dict",
        handle: {
          title: "menu.dict",
          permissions: ["sa:dict"],
          icon: "icon-dict",
        },
        async lazy() {
          const { default: Dict } = await import("@/views/sa/Dict");
          return {
            Component: Dict,
          };
        },
      },
      {
        handle: { type: "divider" },
      },
      {
        path: "tenant",
        handle: {
          title: "menu.tenant",
          permissions: ["sa:tenant"],
          icon: "icon-tenant",
        },
        async lazy() {
          const { default: Tenant } = await import("@/views/sa/Tenant");
          return {
            Component: Tenant,
          };
        },
      },
      {
        path: "permission",
        handle: {
          title: "menu.permission",
          permissions: ["sa:permission"],
          icon: "icon-key",
        },
        async lazy() {
          const { default: Permission } = await import("@/views/sa/Permission");
          return {
            Component: Permission,
          };
        },
      },
      {
        path: "role",
        handle: {
          title: "menu.role",
          permissions: ["sa:role"],
          icon: "icon-group",
        },
        async lazy() {
          const { default: Role } = await import("@/views/sa/Role");
          return {
            Component: Role,
          };
        },
      },
      {
        path: "user",
        handle: {
          title: "menu.user",
          permissions: ["sa:user"],
          icon: "icon-user-setting",
        },
        async lazy() {
          const { default: User } = await import("@/views/sa/User");
          return {
            Component: User,
          };
        },
      },
      {
        handle: { type: "divider" },
      },
      // {
      //   path: "ai/model",
      //   handle: {
      //     title: "menu.model",
      //     permissions: ["ai:models:list"],
      //     icon: "icon-llm",
      //   },
      //   async lazy() {
      //     const { default: Model } = await import("@/views/sa/Model");
      //     return {
      //       Component: Model,
      //     };
      //   },
      // },
      {
        path: "ai/skill",
        handle: {
          title: "menu.skill",
          permissions: ["ai:skills"],
          icon: "icon-skill",
        },
        async lazy() {
          const { default: Skill } = await import("@/views/sa/Skill");
          return {
            Component: Skill,
          };
        },
      },
      {
        handle: { type: "divider" },
      },
      {
        path: "bi/page-template",
        handle: {
          title: "menu.page-template",
          permissions: ["sa:page-template"],
          icon: "icon-template",
        },
        async lazy() {
          const { default: PageTemplate } = await import("@/views/sa/PageTemplate");
          return {
            Component: PageTemplate,
          };
        },
      },
      {
        path: "bi/page-example",
        handle: {
          title: "menu.page-example",
          permissions: ["sa:page-example"],
          icon: "icon-puzzle",
        },
        async lazy() {
          const { default: PageExample } = await import("@/views/sa/PageExample");
          return {
            Component: PageExample,
          };
        },
      },
    ],
  },
  {
    path: "/account",
    element: <BasicLayout />,
    handle: { hidden: true, sider: false },
    children: [
      {
        path: "center",
        handle: { title: "account.center" },
        async lazy() {
          const { default: UserProfile } =
            await import("@/views/account/UserProfile");
          return { Component: UserProfile };
        },
      },
    ],
  },
  {
    path: "/share/page/:shareKey",
    handle: { hidden: true },
    async lazy() {
      const { default: SharePage } = await import("@/views/share/Page");
      return { Component: SharePage };
    },
  },
  {
    path: "/403",
    handle: { title: "errors.403.title", hidden: true },
    async lazy() {
      const { default: Error403 } = await import("@/views/errors/403");
      return { Component: Error403 };
    },
  },
  {
    path: "/500",
    handle: { title: "errors.500.title", hidden: true },
    async lazy() {
      const { default: Error500 } = await import("@/views/errors/500");
      return { Component: Error500 };
    },
  },
  {
    path: "*",
    handle: { title: "errors.404.title", hidden: true },
    async lazy() {
      const { default: Error404 } = await import("@/views/errors/404");
      return { Component: Error404 };
    },
  },
];

const router = createBrowserRouter(routerConfig, {
  basename: setting.basePath,
});

export default router;
