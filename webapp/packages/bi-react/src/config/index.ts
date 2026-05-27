
const config = {
  name: "智数报表",
  basePath: import.meta.env.PUBLIC_BASE_PATH || "/webapp",
  apiBasePath: import.meta.env.PUBLIC_API_URL || "/api",
  sm2: {
    publicKey: import.meta.env.PUBLIC_SM2_PUBLIC_KEY,
  },
  register: import.meta.env.PUBLIC_REGISTER_ENABLED === "true",
  forgotPassword: import.meta.env.PUBLIC_FORGOT_PASSWORD_ENABLED === "true",
  sso: {
    third: import.meta.env.PUBLIC_SSO_THIRD_ENABLED === "true",
    cas: import.meta.env.PUBLIC_SSO_CAS_ENABLED === "true",
    wechatOpen: import.meta.env.PUBLIC_SSO_WECHAT_OPEN_ENABLED === "true",
    dingtalk: import.meta.env.PUBLIC_SSO_DINGTALK_ENABLED === "true",
    github: import.meta.env.PUBLIC_SSO_GITHUB_ENABLED === "true",
  },
};

export default config;