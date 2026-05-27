import { RuleObject } from "antd/es/form";
import { DataNode } from "antd/es/tree";
import { v4 } from "uuid";

export const uuid = () => v4();

export const isMobile = () => {
  const userAgent = navigator.userAgent;
  const mobileKeywords = [
    "Android",
    "iPhone",
    "iPad",
    "Windows Phone",
    "BlackBerry",
    "Opera Mini",
    "Symbian",
    "Kindle",
    "Mobile",
  ];

  const regex = new RegExp(mobileKeywords.join("|"), "i");
  return regex.test(userAgent);
};

const updateTreeData = (
  list: (DataNode & { data?: any })[],
  key: React.Key,
  children: (DataNode & { data?: any })[],
): (DataNode & { data?: any })[] =>
  list.map((node) => {
    if (node.key === key) {
      return {
        ...node,
        children,
      };
    }
    if (node.children) {
      return {
        ...node,
        children: updateTreeData(
          node.children as (DataNode & { data?: any })[],
          key,
          children,
        ),
      };
    }
    return node;
  });

/**
 * 常用正则规则
 */
export const Pattern = {
  /**
   * 用户名规则：4-20位字母、数字、下划线
   */
  username: /^[a-zA-Z0-9_]{4,20}$/,
  /**
   * 变量命名规则：字母或下划线开头，仅包含字母、数字、下划线
   */
  variableName: /^[a-zA-Z_][a-zA-Z0-9_]*$/,
};

/**
 * 计算密码强度 (0-12)
 * 弱：0-6
 * 中：7-9
 * 强：10-12
 */
export const getPasswordStrength = (
  value: string,
): {
  len: number;
  charTypes: number;
  maxConsecutive: number;
  score: number;
} => {
  if (!value) return { len: 0, charTypes: 0, maxConsecutive: 0, score: 0 };

  const len = value.length;
  let charTypes = 0;
  let maxConsecutive = 1;

  // 计算有效长度和最大连续长度
  let effectiveLen = 0;
  let currentConsecutive = 1;
  let prev = value.charCodeAt(0);
  effectiveLen = 1;

  for (let i = 1; i < len; i++) {
    const curr = value.charCodeAt(i);
    // 判定是否连续（ASCII差值为1）
    if (Math.abs(curr - prev) === 1) {
      currentConsecutive++;
    } else {
      if (currentConsecutive > maxConsecutive) {
        maxConsecutive = currentConsecutive;
      }
      currentConsecutive = 1;
      effectiveLen++;
    }
    prev = curr;
  }
  if (currentConsecutive > maxConsecutive) {
    maxConsecutive = currentConsecutive;
  }

  // 2. 字符类型得分 (Max 8)
  const hasLower = /[a-z]/.test(value);
  const hasUpper = /[A-Z]/.test(value);
  const hasDigit = /\d/.test(value);
  const hasSpecial = /[^a-zA-Z0-9]/.test(value);

  if (hasLower) charTypes++;
  if (hasUpper) charTypes++;
  if (hasDigit) charTypes++;
  if (hasSpecial) charTypes++;

  let score = 0;

  // 1. 有效长度得分 (Max 4)
  if (effectiveLen >= 6) score += 1;
  if (effectiveLen >= 8) score += 1;
  if (effectiveLen >= 10) score += 1;
  if (effectiveLen >= 12) score += 1;

  if (hasLower) score += 1;
  if (hasUpper) score += 2;
  if (hasDigit) score += 2;
  if (hasSpecial) score += 3;

  return {
    len,
    charTypes,
    maxConsecutive,
    score: Math.min(score, 12),
  };
};

/**
 * 校验密码是否符合规则
 * 1、长度不能小于8
 * 2、字符种类不能小于3
 * 3、强度不能低于中（7）
 * 4、不能包含用户名（顺序、倒序都判定）
 *
 * @param password 密码
 * @param username 用户名
 * @returns 错误信息，如果通过则返回 null
 */
export const validatePassword = (
  password: string,
  username?: string,
): string | null => {
  if (!password || password.length < 8) {
    return "密码长度不能小于8位";
  }

  const { charTypes, score } = getPasswordStrength(password);

  if (charTypes < 3) {
    return "密码必须包含至少3种字符类型（大写字母、小写字母、数字、特殊字符）";
  }

  if (score < 7) {
    return "密码强度太低，请增加密码复杂度";
  }

  if (username) {
    const lowerPassword = password.toLowerCase();
    const lowerUsername = username.toLowerCase();

    if (lowerPassword.includes(lowerUsername)) {
      return "密码不能包含用户名";
    }

    const reversedUsername = lowerUsername.split("").reverse().join("");
    if (lowerPassword.includes(reversedUsername)) {
      return "密码不能包含用户名的倒序";
    }
  }

  return null;
};

/**
 * 常用校验规则
 */
export const Validator: Record<string, RuleObject["validator"]> = {
  /**
   * 密码强度校验：必须是强密码（强度 >= 10）
   */
  password: async (_: RuleObject, value: string) => {
    if (!value) return Promise.resolve();
    const message = validatePassword(value);
    if (message) {
      return Promise.reject(new Error(message));
    }
    return Promise.resolve();
  },

  /**
   * 手机号校验
   */
  mobile: async (_: RuleObject, value: string) => {
    if (!value) return Promise.resolve();
    const pattern = /^1[3-9]\d{9}$/;
    if (!pattern.test(value)) {
      return Promise.reject(new Error("请输入正确的手机号"));
    }
    return Promise.resolve();
  },

  /**
   * 邮箱校验
   */
  email: async (_: RuleObject, value: string) => {
    if (!value) return Promise.resolve();
    const pattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    if (!pattern.test(value)) {
      return Promise.reject(new Error("请输入正确的邮箱"));
    }
    return Promise.resolve();
  },

  /**
   * 身份证号校验
   */
  idCard: async (_: RuleObject, value: string) => {
    if (!value) return Promise.resolve();
    const pattern =
      /^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[1-2]\d|3[0-1])\d{3}[\dXx]$/;
    if (!pattern.test(value)) {
      return Promise.reject(new Error("请输入正确的身份证号"));
    }
    // 校验码校验
    const factors = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2];
    const parity = ["1", "0", "X", "9", "8", "7", "6", "5", "4", "3", "2"];
    let sum = 0;
    for (let i = 0; i < 17; i++) {
      sum += parseInt(value[i]) * factors[i];
    }
    if (parity[sum % 11] !== value[17].toUpperCase()) {
      return Promise.reject(new Error("身份证号校验位错误"));
    }
    return Promise.resolve();
  },

  /**
   * 统一社会信用代码校验
   */
  creditCode: async (_: RuleObject, value: string) => {
    if (!value) return Promise.resolve();
    const pattern = /^[0-9A-HJ-NPQRTUWXY]{18}$/;
    if (!pattern.test(value)) {
      return Promise.reject(new Error("请输入正确的统一社会信用代码"));
    }

    // 加权校验
    const code = "0123456789ABCDEFGHJKLMNPQRTUWXY";
    const factors = [
      1, 3, 9, 27, 19, 26, 16, 17, 20, 29, 25, 13, 8, 24, 10, 30, 28,
    ];

    let sum = 0;
    for (let i = 0; i < 17; i++) {
      const index = code.indexOf(value[i]);
      sum += index * factors[i];
    }

    const remainder = sum % 31;
    const checkCodeIndex = (31 - remainder) % 31;
    const checkCode = code[checkCodeIndex];

    if (value[17] !== checkCode) {
      return Promise.reject(new Error("统一社会信用代码校验位错误"));
    }

    return Promise.resolve();
  },
};
