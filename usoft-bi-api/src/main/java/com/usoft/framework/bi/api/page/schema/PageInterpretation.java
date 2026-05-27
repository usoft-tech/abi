package com.usoft.framework.bi.api.page.schema;

import java.util.List;

import lombok.Data;

@Data
public class PageInterpretation {

    private String id;

    private String reportId;

    private PageInfo info;

    private List<InterpretationItem> items;
}
