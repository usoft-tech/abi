import { PageResponse } from "./api";
import request from "./request";

export interface PageReportResponse {
  id: string;
  tenantId: string;
  pageId: string;
  title: string;
  content: string;
  createdAt: string;
  createdBy: string;
  updatedAt: string;
  updatedBy: string;
}

export interface PageReportQueryRequest {
  page?: number;
  size?: number;
}

/**
 * 根据页面ID查询报告列表
 */
export const listPageReports = (
  pageId: string,
  params?: PageReportQueryRequest
) => {
  return request.get<PageResponse<PageReportResponse>>(
    `/bi/page-reports/page/${pageId}`,
    params
  );
};

/**
 * 获取报告详情
 */
export const getPageReport = (id: string) => {
  return request.get<PageReportResponse>(`/bi/page-reports/${id}`);
};
