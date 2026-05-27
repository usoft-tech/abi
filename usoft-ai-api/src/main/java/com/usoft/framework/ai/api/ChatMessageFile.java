package com.usoft.framework.ai.api;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 聊天消息文件
 */
@Data
@Accessors(chain = true)
public class ChatMessageFile {

    /**
     * 文件ID
     */
    private String id;
    /**
     * 文件名
     */
    private String name;
    /**
     * 文件扩展名
     */
    private String extension;
}
