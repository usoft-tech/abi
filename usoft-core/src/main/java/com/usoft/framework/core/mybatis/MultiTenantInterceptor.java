package com.usoft.framework.core.mybatis;

import com.usoft.framework.core.tenant.TenantContext;
import com.usoft.framework.core.tenant.TenantEntityScanner;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.Collection;
import java.util.Properties;

/**
 * MyBatis拦截器，在SQL执行前为查询语句追加租户条件
 */
@Intercepts({
        @Signature(type = StatementHandler.class, method = "prepare", args = { Connection.class, Integer.class })
})
public class MultiTenantInterceptor implements Interceptor {

    private static final Logger logger = LoggerFactory.getLogger(MultiTenantInterceptor.class);

    /**
     * 拦截并追加租户条件
     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        if (TenantContext.hasTenant()) {
            StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
            MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
            String originalSql = (String) metaObject.getValue("delegate.boundSql.sql");
            String tenantId = TenantContext.getTenantId();
            try {
                String tenantSql = appendTenantCondition(originalSql, tenantId);
                metaObject.setValue("delegate.boundSql.sql", tenantSql);
            } catch (Exception e) {
                logger.error("Failed to append tenant condition to SQL: " + originalSql, e);
            }
        }
        return invocation.proceed();
    }

    /**
     * 为SQL追加租户条件
     */
    private String appendTenantCondition(String sql, String tenantId) throws Exception {
        Statement statement = CCJSqlParserUtil.parse(sql);
        if (statement instanceof Select select) {
            processSelect(select, tenantId);
            return statement.toString();
        }
        return sql;
    }

    private void processSelect(Select select, String tenantId) {
        if (select instanceof PlainSelect) {
            processPlainSelect((PlainSelect) select, tenantId);
        } else if (select instanceof SetOperationList) {
            SetOperationList operationList = (SetOperationList) select;
            if (operationList.getSelects() != null) {
                for (Select body : operationList.getSelects()) {
                    processSelect(body, tenantId);
                }
            }
        } else if (select instanceof ParenthesedSelect) {
            processSelect(((ParenthesedSelect) select).getSelect(), tenantId);
        }
    }

    private void processPlainSelect(PlainSelect plainSelect, String tenantId) {
        FromItem fromItem = plainSelect.getFromItem();
        processFromItem(fromItem, tenantId);

        if (fromItem instanceof Table) {
            Table table = (Table) fromItem;
            if (isTenantTable(table)) {
                plainSelect.setWhere(buildTenantCondition(plainSelect.getWhere(), table, tenantId));
            }
        }

        if (plainSelect.getJoins() != null) {
            for (Join join : plainSelect.getJoins()) {
                processFromItem(join.getRightItem(), tenantId);
                if (join.getRightItem() instanceof Table) {
                    Table table = (Table) join.getRightItem();
                    if (isTenantTable(table)) {
                        Collection<Expression> onExpressions = join.getOnExpressions();
                        if (onExpressions != null && !onExpressions.isEmpty()) {
                            join.setOnExpressions(onExpressions.stream()
                                    .map(on -> buildTenantCondition(on, table, tenantId)).toList());
                        }
                    }
                }
            }
        }
    }

    private void processFromItem(FromItem fromItem, String tenantId) {
        if (fromItem instanceof ParenthesedSelect) {
            processSelect(((ParenthesedSelect) fromItem).getSelect(), tenantId);
        } else if (fromItem instanceof ParenthesedFromItem) {
            processFromItem(((ParenthesedFromItem) fromItem).getFromItem(), tenantId);
        }
    }

    private boolean isTenantTable(Table table) {
        String tableName = table.getName();
        if (tableName.startsWith("`") && tableName.endsWith("`")) {
            tableName = tableName.substring(1, tableName.length() - 1);
        } else if (tableName.startsWith("\"") && tableName.endsWith("\"")) {
            tableName = tableName.substring(1, tableName.length() - 1);
        }
        return TenantEntityScanner.isTenantTable(tableName);
    }

    private Expression buildTenantCondition(Expression currentExpression, Table table, String tenantId) {
        EqualsTo equalsTo = new EqualsTo();
        equalsTo.setLeftExpression(new Column(table, "tenant_id"));
        equalsTo.setRightExpression(new StringValue(tenantId));
        if (currentExpression == null) {
            return equalsTo;
        } else {
            return new AndExpression(currentExpression, equalsTo);
        }
    }

    /**
     * 生成代理
     */
    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    /**
     * 设置属性
     */
    @Override
    public void setProperties(Properties properties) {
    }
}
