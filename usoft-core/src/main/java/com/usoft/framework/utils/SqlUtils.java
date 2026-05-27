package com.usoft.framework.utils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/** tools functions about sql query */
@Slf4j
@Component
public class SqlUtils {

    public SqlUtils() {
    }

    public JdbcTemplate jdbcTemplate(DataSource dataSource) throws RuntimeException {

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.setFetchSize(500);
        return jdbcTemplate;
    }

    /**
     * 内部查询方法，执行SQL查询并将结果存储在指定对象中
     * 
     * @param sql                    要执行的SQL语句
     * @param queryResultWithColumns 存储查询结果的对象
     */
    public void queryInternal(DataSource dataSource, String sql, SemanticQueryResp queryResultWithColumns) {
        getResult(sql, queryResultWithColumns, jdbcTemplate(dataSource));
    }

    private SemanticQueryResp getResult(String sql, SemanticQueryResp queryResultWithColumns,
            JdbcTemplate jdbcTemplate) {
        jdbcTemplate.query(sql, rs -> {
            if (null == rs) {
                return queryResultWithColumns;
            }

            ResultSetMetaData metaData = rs.getMetaData();
            List<QueryColumn> queryColumns = new ArrayList<>();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                String key = metaData.getColumnLabel(i);
                queryColumns.add(new QueryColumn(key, metaData.getColumnTypeName(i)));
            }
            queryResultWithColumns.setColumns(queryColumns);

            List<Map<String, Object>> resultList = getAllData(rs, queryColumns);
            queryResultWithColumns.setResultList(resultList);
            return queryResultWithColumns;
        });
        return queryResultWithColumns;
    }

    private List<Map<String, Object>> getAllData(ResultSet rs, List<QueryColumn> queryColumns) {
        List<Map<String, Object>> data = new ArrayList<>();
        try {
            while (rs.next()) {
                data.add(getLineData(rs, queryColumns));
            }
        } catch (Exception e) {
            log.warn("error in getAllData, e:", e);
        }
        return data;
    }

    private Map<String, Object> getLineData(ResultSet rs, List<QueryColumn> queryColumns)
            throws SQLException {
        Map<String, Object> map = new LinkedHashMap<>();
        for (QueryColumn queryColumn : queryColumns) {
            String colName = queryColumn.getBizName();
            Object value = rs.getObject(colName);
            map.put(colName, getValue(value));
        }
        return map;
    }

    private Object getValue(Object value) {
        if (value instanceof LocalDate) {
            LocalDate localDate = (LocalDate) value;
            return localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } else if (value instanceof LocalDateTime) {
            LocalDateTime localDateTime = (LocalDateTime) value;
            return localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } else if (value instanceof Date) {
            Date date = (Date) value;
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String rtv = dateFormat.format(date);
            if (rtv.endsWith(" 00:00:00")) {
                return rtv.substring(0, 10);
            }
            return rtv;
        } else if (value instanceof byte[]) {
            return new String((byte[]) value);
        }
        return value;
    }

    @Data
    public static class SemanticQueryResp {
        private List<QueryColumn> columns;
        private List<Map<String, Object>> resultList;
    }

    @Data
    public static class QueryColumn {
        private String name;
        private String bizName;
        private String type;

        public QueryColumn(String bizName, String type) {
            this.type = type;
            this.bizName = bizName;
            this.name = bizName;
        }
    }
}
