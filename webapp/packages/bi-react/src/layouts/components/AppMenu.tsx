import IconFont from "@/components/icon/IconFont";
import { Flex, Typography } from "antd";
import React, { useEffect, useState } from "react";
import {
  RouteObject,
  useLocation,
  useMatches,
  useNavigate,
  useParams,
} from "react-router-dom";
import styled from "styled-components";
import { routerConfig } from "@/router";
import { uuid } from "@/utils";
import { usePermission } from "@/permission";
import { AppResponse, listApps } from "@/services/app";
import { setCurrentApp } from "@/store/appSlice";
import { useDispatch, useSelector } from "react-redux";
import { useQuery } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";
import { RootState } from "@/store";

const AppItem = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  cursor: pointer;
  line-height: initial;
  font-size: 12px;
  color: var(--ant-color-text-secondary);
  width: 55px;
  height: 55px;
  border-radius: 8px;

  &:hover {
    color: var(--ant-blue-6);
    box-shadow: inset 0 0 7px 5px #b3b3b326;
  }

  &.active {
    color: var(--ant-blue-6);
    font-weight: bold;
  }

  .anticon {
    font-size: 20px;
  }

  .ant-typography {
    color: inherit;
    font-size: 12px;
  }
`;

type AppType = RouteObject & {
  id: string | number;
  name: string;
  icon?: string;
  path: string;
  children?: AppType[] | null;
};

const AppMenu: React.FC<{ theme?: string; mode?: string }> = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { hasPermission } = usePermission();
  const dispatch = useDispatch();
  const matches = useMatches();
  const params = useParams();
  const [remoteApps, setRemoteApps] = useState<AppResponse[]>([]);
  const [apps, setApps] = useState<AppType[]>([]);
  const [fixedRoutes, setFixedRoutes] = useState<AppType[]>([]);

  const permissions = useSelector(
    (state: RootState) => state.account.permissions,
  );
  const roles = useSelector((state: RootState) => state.account.roles);

  const { t } = useTranslation();

  const rootRoute = matches[0];

  const convert = (list: AppResponse[]) => {
    const items = list.map((item) => ({
      id: item.id || uuid(),
      name: (item.handle?.title as string) || undefined,
      icon: item.handle?.icon,
      path: "/app/" + item.id,
      children: item.children ? convert(item.children) : null,
    })) as AppType[];
    return items;
  };

  const { data: appList } = useQuery<AppResponse[]>({
    staleTime: 1000 * 60,
    queryKey: ["appList"],
    queryFn: () => listApps().then((res) => res.data || []),
  });

  const handleClick = (item: AppType) => {
    const { redirectUrl } = item.handle || {};
    if (redirectUrl) {
      window.open(redirectUrl, "_blank");
      return;
    }
    navigate(item.path!);
  };

  useEffect(() => {
    if (rootRoute.id === "app" && params.groupId) {
      dispatch(
        setCurrentApp(
          remoteApps.find((item) => item.id === params.groupId) || null,
        ),
      );
    }
  }, [remoteApps, rootRoute.id, params.groupId]);

  useEffect(() => {
    if (appList) {
      setRemoteApps(appList);
      setApps(convert(appList));
    }
  }, [appList]);

  useEffect(() => {
    setFixedRoutes([
      ...routerConfig
        .filter(
          (item) =>
            !item.handle?.hidden &&
            hasPermission(item.handle?.permissions || []),
        )
        .map((item) => ({
          id: uuid(),
          name: item.handle?.title as string,
          icon: item.handle?.icon as string,
          path: item.path || "/",
        })),
    ]);
  }, [routerConfig, permissions, roles]);

  return (
    <Flex gap={30}>
      {[...apps, ...fixedRoutes].map((item) => (
        <AppItem
          key={item.id}
          className={location.pathname.startsWith(item.path!) ? "active" : ""}
          onClick={() => handleClick(item)}
        >
          <IconFont type={item.icon!} />
          <Typography.Text
            ellipsis={{ tooltip: t(item.handle?.title || item.name!) }}
          >
            {t(item.handle?.title || item.name!)}
          </Typography.Text>
        </AppItem>
      ))}
    </Flex>
  );
};

export default AppMenu;
