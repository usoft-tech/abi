package com.usoft.framework.system.config;

import lombok.Data;

/**
 * 上传配置
 */
@Data
public class UploadProperties {
    /**
     * 存储目录路径
     */
    private String storagePath;
    /**
     * 访问路径前缀
     */
    private String visitPathPrefix;
    /**
     * 最大文件大小（字节）
     */
    private long maxFileSize = 1024 * 1024 * 10;
    /**
     * 允许的文件类型
     */
    private String[] allowedFileTypes = {
        "image/*",
        "video/*",
        "audio/*",
        "application/pdf",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    };
    /**
     * 允许的文件扩展名
     */
    private String[] allowedFileExtensions = {
        "jpg", "jpeg", "png", "gif", "svg",
        "mp4", "avi", "mov", "wav",
        "pdf",
        "xlsx", "docx"
    };
}
