package com.usoft.framework.bi.api.enums;

import lombok.Getter;

/**
 * 数据集状态
 */
@Getter
public enum DataSetStatus {
  PUBLISHED("已发布"),
  DRAFT("草稿"),
  LOCKED("已锁定");

  private final String label;

  DataSetStatus(String label) {
    this.label = label;
  }
}
