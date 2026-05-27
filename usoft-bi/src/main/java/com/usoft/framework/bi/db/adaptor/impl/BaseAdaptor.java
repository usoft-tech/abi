package com.usoft.framework.bi.db.adaptor.impl;

import com.usoft.framework.bi.db.adaptor.DbAdaptor;

public abstract class BaseAdaptor implements DbAdaptor {

    protected final String id;

    public BaseAdaptor(String id) {
        this.id = id;
    }
}
