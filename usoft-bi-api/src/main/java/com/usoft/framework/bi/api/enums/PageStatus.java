package com.usoft.framework.bi.api.enums;

import lombok.Getter;

@Getter
public enum PageStatus {
  PUBLISHED("已发布"),
  DRAFT("草稿"),
  LOCKED("已锁定");

  private final String label;

  PageStatus(String label) {
    this.label = label;
  }
}
