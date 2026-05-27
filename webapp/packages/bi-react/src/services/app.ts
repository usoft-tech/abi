import { type RouteObject } from "react-router-dom";
import request from "./request";

/****************************** 应用管理 start ******************************/

export enum AppMenuType {
  GROUP = "GROUP",
  PAGE = "PAGE",
}

export type AppResponseHandle = {
  type: string;
  title: string;
  icon: string;
  pageId: string;
  appKey: string;
  redirectUrl: string;
  sort: number;
  isLeaf: boolean;
  hasWatermark?: boolean;
  permissions?: string[];
};

export type AppResponse = Pick<RouteObject, "id" | "path"> & {
  parentId?: string | null;
  children?: AppResponse[] | null;
  handle: AppResponseHandle;
};

/**
 * 获取应用菜单列表
 */
export const listApps = () => {
  return request.get<AppResponse[]>("/bi/apps");
};

/****************************** 应用管理 end ******************************/







type SysFileResponse = {
  id: string;
  name: string;
  size: number;
  url: string;
  contentType: string;
}

export const uploadFile = (type: string, file: File) => {
  const formData = new FormData();
  formData.append("file", file);
  formData.append("type", type);
  return request.post<SysFileResponse>("/files/upload", formData);
};