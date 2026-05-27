import IconFont from "@/components/icon/IconFont";
import { usePermission } from "@/permission";
import { routerConfig } from "@/router";
import { AppResponse, listApps } from "@/services/app";
import { config } from "@/store/appSlice";
import { isMobile } from "@/utils";
import { useQuery } from "@tanstack/react-query";
import { Menu } from "antd";
import { MenuItemType } from "antd/es/menu/interface";
import React, { useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { useSelector } from "react-redux";
import { useLocation, useMatches, useNavigate } from "react-router-dom";

interface Props {
  theme?: "dark" | "light";
  mode?: "vertical" | "horizontal" | "inline";
  style?: React.CSSProperties;
}

const RouteMenu: React.FC<Props> = ({
  theme = "dark",
  mode = "inline",
  style,
}) => {
  const { layout, currentApp } = useSelector(config);
  const navigate = useNavigate();
  const location = useLocation();
  const matches = useMatches();
  const { hasPermission } = usePermission();
  const root = matches[0];

  const [fixedMenuItems, setFixedMenuItems] = useState<MenuItemType[]>([]);
  const [appMenuItems, setAppMenuItems] = useState<MenuItemType[]>([]);

  const { t } = useTranslation();

  const isMobileDevice = useMemo(() => isMobile(), [isMobile]);
  const filterRoot = useMemo(
    () => layout === "top-side-menu" && !isMobileDevice,
    [layout, isMobileDevice]
  );

  const convert = (
    list?: AppResponse[] | null,
    parentPath = "",
    hasPermission?: (permission: string[]) => boolean
  ) => {
    if (!list) {
      return null;
    }
    const menus = list
      .filter(
        (item) =>
          item.handle &&
          (!hasPermission || hasPermission(item.handle?.permissions || []))
      )
      .map(({ path, handle, children }) => {
        const { type, title, icon, redirectUrl } = handle || {};
        if (type === "divider") {
          return {
            type: "divider",
          };
        }
        const fullPath = !!redirectUrl?.length
          ? redirectUrl
          : path?.startsWith("/")
          ? path
          : (parentPath + "/" + (path || "")).replace(/\/\//g, "/");
        if (type === "group") {
          return {
            type: "group",
            title: t(title as string) || undefined,
            label: <>{t(title as string)}</>,
            children: !!redirectUrl?.length
              ? undefined
              : convert(children, fullPath, hasPermission),
          };
        }
        return {
          key: fullPath as string,
          title: t(title as string) || undefined,
          label: t(title as string) || undefined,
          icon: <IconFont type={icon} />,
          target: !!redirectUrl?.length || undefined,
          children: !!redirectUrl?.length
            ? undefined
            : convert(children, fullPath, hasPermission),
        } as MenuItemType;
      }) as MenuItemType[];
    return menus;
  };

  const convertApp = (
    list?: AppResponse[] | null,
    parentPath = "",
    hasPermission?: (permission: string[]) => boolean
  ) => {
    if (!list) {
      return null;
    }
    const menus = list
      .filter(
        (item) =>
          item.handle &&
          (!hasPermission || hasPermission(item.handle?.permissions || []))
      )
      .map(({ id, handle, children }) => {
        const { type, title, icon, redirectUrl } = handle || {};
        if (type === "divider") {
          return {
            type: "divider",
          };
        }
        let fullPath;
        if (!!redirectUrl?.length) {
          fullPath = redirectUrl;
        } else if (filterRoot) {
          fullPath = (parentPath + "/" + (id || "")).replace(/\/\//g, "/");
        } else {
          fullPath =
            parentPath.length === 0 ? `/app/${id}` : `${parentPath}/${id}`;
        }

        if (type === "group") {
          return {
            type: "group",
            title: t(title as string) || undefined,
            label: <>{t(title as string)}</>,
            children: !!redirectUrl?.length
              ? undefined
              : convertApp(children, fullPath, hasPermission),
          };
        }
        return {
          key: fullPath as string,
          title: t(title as string) || undefined,
          label: t(title as string) || undefined,
          icon: <IconFont type={icon} />,
          target: !!redirectUrl?.length || undefined,
          children: !!redirectUrl?.length
            ? undefined
            : convertApp(children, fullPath, hasPermission),
        } as MenuItemType;
      }) as MenuItemType[];
    return menus;
  };

  const { data: appList } = useQuery<AppResponse[]>({
    staleTime: 1000 * 60,
    queryKey: ["appList"],
    queryFn: () => listApps().then((res) => res.data || []),
    enabled: !filterRoot,
  });

  useEffect(() => {
    if (appList && !filterRoot) {
      setAppMenuItems(convertApp(appList, ``, hasPermission) || []);
    }
  }, [appList, filterRoot]);

  useEffect(() => {
    if (filterRoot && currentApp && root.id === "app") {
      setAppMenuItems(
        convertApp(
          (currentApp?.children || []) as AppResponse[],
          `/app/${currentApp.id!}`,
          hasPermission
        ) || []
      );
    } else if (filterRoot) {
      setAppMenuItems([]);
    }
  }, [filterRoot, currentApp, root.id, hasPermission]);

  useEffect(() => {
    if (!filterRoot) {
      // 从后端加载应用
      const menus = convert(
        routerConfig.filter(
          (item) =>
            !item.handle?.hidden &&
            hasPermission(item.handle?.permissions || [])
        ) as AppResponse[],
        "",
        hasPermission
      );
      setFixedMenuItems(menus || []);
    } else if (root.id === "app") {
      setFixedMenuItems([]);
    } else {
      const current = routerConfig.filter((item) => item.id === root.id)[0];
      const menus =
        convert(
          (current?.children || []) as AppResponse[],
          current?.path || "",
          hasPermission
        ) || [];
      setFixedMenuItems(menus);
    }
  }, [root.id, filterRoot, hasPermission, t]);

  const onClick = (e: any) => {
    const { target } = e?.item?.props || {};
    if (target === "_blank") {
      window.open(e.key);
      return;
    }
    navigate({ pathname: e.key });
  };

  return (
    <Menu
      theme={theme}
      mode={mode}
      style={style}
      items={[...appMenuItems, ...fixedMenuItems]}
      selectedKeys={[location.pathname]}
      onClick={onClick}
    />
  );
};

export default RouteMenu;
