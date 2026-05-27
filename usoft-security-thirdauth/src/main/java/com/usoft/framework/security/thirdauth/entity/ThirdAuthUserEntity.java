package com.usoft.framework.security.thirdauth.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

/**
 * Entity for third-party authentication binding.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "sys_third_auth_user")
public class ThirdAuthUserEntity implements Serializable {

    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * System user ID.
     */
    private String userId;

    /**
     * Platform source (e.g., wechat, github).
     */
    private String source;

    /**
     * Unique ID from the platform (OpenID).
     */
    private String uuid;

    /**
     * Union ID (if available).
     */
    private String unionId;

    /**
     * Username from platform.
     */
    private String username;

    /**
     * Nickname from platform.
     */
    private String nickname;

    /**
     * Avatar URL from platform.
     */
    private String avatar;

    /**
     * Raw user info (JSON string).
     */
    private String rawData;

    /**
     * Create time.
     */
    private Instant createTime;

    /**
     * Update time.
     */
    private Instant updateTime;
}
