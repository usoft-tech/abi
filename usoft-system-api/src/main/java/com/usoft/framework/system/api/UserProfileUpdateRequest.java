package com.usoft.framework.system.api;

import lombok.Data;

/**
 * 用户个人中心更新请求体
 */
@Data
public class UserProfileUpdateRequest {
    /**
     * 显示名称
     */
    private String displayName;
    
    /**
     * 头像URL
     */
    private String avatar;
    
    /**
     * 旧密码（修改密码时必填）
     */
    private String oldPassword;
    
    /**
     * 新密码
     */
    private String newPassword;
}
