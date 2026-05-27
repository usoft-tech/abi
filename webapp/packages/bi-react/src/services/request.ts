import {
  clearToken,
  getTenantId,
  getToken,
  TENANT_ID_HEADER,
  TOKEN_HEADER,
  TOKEN_PREFIX,
} from "@/utils/token";
import { message } from "antd";
import axios, {
  AxiosInstance,
  AxiosRequestConfig,
  AxiosResponse,
  InternalAxiosRequestConfig,
} from "axios";
import { ApiResponse } from "./api";
import setting from "@/config";

/**
 * @description: 基础配置
 */
const URL: string = import.meta.env.PUBLIC_API_URL || setting.apiBasePath || "/api";

enum RequestEnums {
  TIMEOUT = 20000,
  OVERDUE = 401, // 登录失效
  FAIL = 500, // 请求失败
  SUCCESS = 200, // 请求成功
}

const config = {
  // 默认地址
  baseURL: URL,
  // 设置超时时间
  timeout: RequestEnums.TIMEOUT as number,
  // 跨域时候允许携带凭证
  withCredentials: true,
};

class RequestHttp {
  service: AxiosInstance;

  public constructor(config: AxiosRequestConfig) {
    this.service = axios.create(config);

    /**
     * @description 请求拦截器
     * 客户端发送请求 -> [请求拦截器] -> 服务器
     * token校验(JWT) : 接受服务器返回的token,存储到localStorage/vuex/pinia/redux中
     */
    this.service.interceptors.request.use(
      (config: InternalAxiosRequestConfig) => {
        const token = getToken();
        const tenantId = getTenantId();

        if (token) {
          config.headers[TOKEN_HEADER] = TOKEN_PREFIX + token;
        }
        if (tenantId) {
          config.headers[TENANT_ID_HEADER] = tenantId;
        }
        return config;
      },
      (error: any) => {
        return Promise.reject(error);
      },
    );

    /**
     * @description 响应拦截器
     *  服务器换返回信息 -> [拦截统一处理] -> 客户端JS获取到信息
     */
    this.service.interceptors.response.use(
      (response: AxiosResponse) => {
        const { data } = response;

        // 简单处理：如果是 blob 或者 arraybuffer 直接返回
        if (
          response.config.responseType === "blob" ||
          response.config.responseType === "arraybuffer"
        ) {
          return data;
        }

        // 假设标准返回包含 code，如果没有 code 则认为不是标准结构，直接返回
        if (data.code === undefined) {
          return data;
        }

        if (
          data.code &&
          data.code !== RequestEnums.SUCCESS &&
          data.code !== "OK"
        ) {
          message.error(data.message || "Error");

          // 401: 未登录或token过期，重定向到登录页
          if (data.code === RequestEnums.OVERDUE || data.code === "401") {
            clearToken();
            // 可以添加重定向逻辑，例如:
            window.location.href = setting.basePath + "/login";
          }
          return Promise.reject(data);
        }

        return data;
      },
      (error: any) => {
        const { response } = error;
        if (response) {
          this.handleCode(response);
        } else {
          if (!window.navigator.onLine) {
            message.error("网络连接失败");
          } else {
            message.error("请求超时或服务器异常");
          }
        }
        return Promise.reject(error);
      },
    );
  }

  /**
   * @description: 状态码处理
   */
  handleCode(response: AxiosResponse): void {
    const { status, data } = response;
    switch (status) {
      case 401:
        message.error("登录失效，请重新登录");
        clearToken();
        window.location.href = setting.basePath + "/login";
        break;
      case 403:
        message.error("您没有权限访问该资源");
        break;
      case 404:
        message.error("请求资源不存在");
        break;
      case 500:
        message.error(data?.message || "服务器内部错误");
        break;
      default:
        message.error(data?.message || "请求失败");
        break;
    }
  }

  // 常用方法封装
  get<T>(url: string, params?: object, config = {}): Promise<ApiResponse<T>> {
    return this.service.get(url, { params, ...config });
  }
  post<T>(
    url: string,
    data?: object,
    config: AxiosRequestConfig<any> | undefined = {},
  ): Promise<ApiResponse<T>> {
    return this.service.post(url, data, config);
  }
  put<T>(
    url: string,
    data?: object,
    config: AxiosRequestConfig<any> | undefined = {},
  ): Promise<ApiResponse<T>> {
    return this.service.put(url, data, config);
  }
  delete<T>(
    url: string,
    params?: any,
    config: AxiosRequestConfig<any> | undefined = {},
  ): Promise<ApiResponse<T>> {
    return this.service.delete(url, { params, ...config });
  }
}

export enum EnableStatus {
  ENABLE = "ENABLE",
  DISABLE = "DISABLE",
}

export default new RequestHttp(config);
