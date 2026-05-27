package com.usoft.framework.bi.db.adaptor;

import org.apache.commons.lang3.StringUtils;

import com.usoft.framework.bi.db.adaptor.impl.ChatBiAdaptor;
import com.usoft.framework.bi.db.adaptor.impl.MySqlAdaptor;
import com.usoft.framework.bi.db.adaptor.impl.OracleAdaptor;
import com.usoft.framework.bi.db.adaptor.impl.PostgreSQLAdaptor;
import com.usoft.framework.bi.db.adaptor.impl.SQLServerAdaptor;

public class DbAdaptorFactory {

    public static DbAdaptor getDbAdaptor(String type, String id) {
        if (StringUtils.isBlank(type)) {
            return null;
        }
        switch (type.toLowerCase()) {
            case "mysql":
                return new MySqlAdaptor(id);
            case "pgsql":
            case "postgresql":
                return new PostgreSQLAdaptor(id);
            case "sqlserver":
                return new SQLServerAdaptor(id);
            case "oracle":
                return new OracleAdaptor(id);
            case "chatbi":
                return new ChatBiAdaptor(id);
            default:
                break;
        }
        return null;
    }
}
