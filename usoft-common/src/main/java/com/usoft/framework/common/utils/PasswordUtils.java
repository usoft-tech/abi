package com.usoft.framework.common.utils;

import com.usoft.framework.common.exception.BizException;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 密码工具类
 */
public class PasswordUtils {

    private PasswordUtils() {}
        /**
     * 校验密码是否符合规则
     * 1、长度不能小于8
     * 2、字符种类不能小于3
     * 3、强度不能低于中（7）
     * 4、不能包含用户名（顺序、倒序都判定）
     *
     * @param password 密码
     * @param username 用户名
     * @throws BizException 校验失败抛出异常
     */
    public static void validatePassword(String password, String username) {
        if (password == null || password.length() < 8) {
            throw new BizException("密码长度不能小于8位");
        }

        PasswordStrength strength = getPasswordStrength(password);

        if (strength.getCharTypes() < 3) {
            throw new BizException("密码必须包含至少3种字符类型（大写字母、小写字母、数字、特殊字符）");
        }

        if (strength.getScore() < 7) {
            throw new BizException("密码强度太低，请增加密码复杂度");
        }

        if (username != null && !username.isEmpty()) {
            String lowerPassword = password.toLowerCase();
            String lowerUsername = username.toLowerCase();

            if (lowerPassword.contains(lowerUsername)) {
                throw new BizException("密码不能包含用户名");
            }

            String reversedUsername = new StringBuilder(lowerUsername).reverse().toString();
            if (lowerPassword.contains(reversedUsername)) {
                throw new BizException("密码不能包含用户名的倒序");
            }
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PasswordStrength {
        /**
         * 密码总长度
         */
        private int len;
        /**
         * 字符种类数量
         */
        private int charTypes;
        /**
         * 最大连续字符长度 (如 "123" 为 3)
         */
        private int maxConsecutive;
        /**
         * 密码强度得分 (0-12)
         */
        private int score;
    }

    /**
     * 计算密码强度
     * 弱：0-6
     * 中：7-9
     * 强：10-12
     *
     * 连续数字或字符判定为长度1（顺序或倒序都判定）
     *
     * @param value 密码
     * @return 强度详情
     */
    public static PasswordStrength getPasswordStrength(String value) {
        if (value == null || value.isEmpty()) {
            return new PasswordStrength(0, 0, 0, 0);
        }

        int len = value.length();
        int charTypes = 0;
        int maxConsecutive = 1;
        
        // 计算有效长度和最大连续长度
        int effectiveLen = 0;
        int currentConsecutive = 1;
        char prev = '\0';

        for (int i = 0; i < len; i++) {
            char curr = value.charAt(i);
            if (i > 0) {
                // 判定是否连续（ASCII差值为1）
                if (Math.abs(curr - prev) == 1) {
                    currentConsecutive++;
                } else {
                    if (currentConsecutive > maxConsecutive) {
                        maxConsecutive = currentConsecutive;
                    }
                    currentConsecutive = 1;
                    effectiveLen++;
                }
            } else {
                effectiveLen++;
            }
            prev = curr;
        }
        if (currentConsecutive > maxConsecutive) {
            maxConsecutive = currentConsecutive;
        }

        // 字符类型统计
        boolean hasLower = false;
        boolean hasUpper = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (char c : value.toCharArray()) {
            if (c >= 'a' && c <= 'z') {
                hasLower = true;
            } else if (c >= 'A' && c <= 'Z') {
                hasUpper = true;
            } else if (c >= '0' && c <= '9') {
                hasDigit = true;
            } else {
                hasSpecial = true;
            }
        }

        if (hasLower) charTypes++;
        if (hasUpper) charTypes++;
        if (hasDigit) charTypes++;
        if (hasSpecial) charTypes++;

        // 计算得分
        int score = 0;

        // 1. 有效长度得分 (Max 4)
        // 注意：这里使用 effectiveLen 计算长度分
        if (effectiveLen >= 6) score += 1;
        if (effectiveLen >= 8) score += 1;
        if (effectiveLen >= 10) score += 1;
        if (effectiveLen >= 12) score += 1;

        // 2. 字符类型得分 (Max 8)
        // 保持原有逻辑：小写+1，大写+2，数字+2，特殊+3
        if (hasLower) score += 1;
        if (hasUpper) score += 2;
        if (hasDigit) score += 2;
        if (hasSpecial) score += 3;

        return new PasswordStrength(len, charTypes, maxConsecutive, Math.min(score, 12));
    }
}
