/**
 * @description 接口返回结果接口
 */
export interface ApiResponse<T = any> {
  code: string | number;
  message: string;
  data: T;
  success: boolean;
  timestamp: string;
}

/**
 * @description 分页结果接口
 */
export interface PageResponse<T> {
  page: number;
  size: number;
  total: number;
  items: T[];
}
