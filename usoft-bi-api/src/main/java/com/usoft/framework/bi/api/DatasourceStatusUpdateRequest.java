package com.usoft.framework.bi.api;

import com.usoft.framework.common.enums.EnableStatus;

import lombok.Data;

@Data
public class DatasourceStatusUpdateRequest {
    private EnableStatus status;
}
