package com.usoft.framework.common.api;

import jakarta.validation.constraints.Min;

/**
 * 提供基本分页请求结构
 */
public class PageRequest {
    @Min(1)
    private int page = 1;
    @Min(1)
    private int size = 10;

    /**
     * 获取页码
     */
    public int getPage() {
        return page;
    }

    /**
     * 设置页码
     */
    public void setPage(int page) {
        this.page = page;
    }

    /**
     * 获取页大小
     */
    public int getSize() {
        return size;
    }

    /**
     * 设置页大小
     */
    public void setSize(int size) {
        this.size = size;
    }
}

