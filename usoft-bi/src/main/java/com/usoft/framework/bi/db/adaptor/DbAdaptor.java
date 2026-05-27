package com.usoft.framework.bi.db.adaptor;

import java.util.List;

import com.usoft.framework.bi.api.DataSourceCreateRequest;
import com.usoft.framework.bi.api.db.schema.DatabaseSchema;

public interface DbAdaptor {

    boolean testConnection(DataSourceCreateRequest req);

    List<DatabaseSchema> getDatabaseSchemas();
}
