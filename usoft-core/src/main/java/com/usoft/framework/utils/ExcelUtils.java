package com.usoft.framework.utils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;

import com.zaxxer.hikari.HikariDataSource;

import cn.idev.excel.FastExcel;
import cn.idev.excel.context.AnalysisContext;
import cn.idev.excel.event.AnalysisEventListener;

public class ExcelUtils {

    /**
     * H2 导入结果上下文
     */
    public static class CachedExcelDatabase implements AutoCloseable {

        private final DataSource dataSource;

        private final List<ExcelSchema> excelSchemas;

        public CachedExcelDatabase(DataSource dataSource, List<ExcelSchema> excelSchemas) {
            this.dataSource = dataSource;
            this.excelSchemas = excelSchemas;
        }

        public static CachedExcelDatabase load(String dbPath) throws SQLException {
            HikariDataSource dataSource = new HikariDataSource();
            dataSource.setDriverClassName("org.h2.Driver");
            // 使用文件模式存储 H2 数据库
            dataSource.setJdbcUrl("jdbc:h2:file:" + dbPath);
            dataSource.setUsername("sa");
            dataSource.setPassword("");

            List<ExcelSchema> excelSchemas = new ArrayList<>();
            loadExistingSchemas(dataSource, excelSchemas);

            return new CachedExcelDatabase(dataSource, excelSchemas);
        }

        public DataSource getDataSource() {
            return dataSource;
        }

        public List<ExcelSchema> getExcelSchemas() {
            return excelSchemas;
        }

        @Override
        public void close() {
            if (dataSource instanceof HikariDataSource) {
                ((HikariDataSource) dataSource).close();
            }
        }
    }

    /**
     * 使用 FastExcel 读取 Excel 所有 sheet 内容
     *
     * @param fileUrl 文件的可访问 URL
     * @return Map<Sheet名称, List<行数据>>
     */
    public static Map<String, List<Object>> readExcel(String fileUrl) {
        Path temp = null;
        try {
            temp = Files.createTempFile("upload-", ".xlsx");
            copyFile(fileUrl, temp);

            String contentType = Files.probeContentType(temp);
            if (!StringUtils.equalsIgnoreCase(contentType,
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
                throw new RuntimeException("不支持的文件 MIME 类型: " + contentType);
            }

            Map<String, List<Object>> result = new LinkedHashMap<>();

            FastExcel.read(temp.toFile(), new AnalysisEventListener<Map<Integer, Object>>() {
                @Override
                public void invoke(Map<Integer, Object> data, AnalysisContext context) {
                    String sheetName = context.readSheetHolder().getSheetName();
                    result.computeIfAbsent(sheetName, k -> new ArrayList<>()).add(data);
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {
                }
            }).doReadAll();

            return result;
        } catch (Exception e) {
            throw new RuntimeException("读取 Excel 文件失败", e);
        } finally {
            if (temp != null) {
                try {
                    Files.deleteIfExists(temp);
                } catch (IOException ignored) {
                }
            }
        }
    }

    /**
     * 通过 FastExcel 读取多个 Excel 文件并导入 H2 数据库（持久化存储）
     *
     * 使用规范：
     * 1、数据存储在 dbStoreageDir 指定目录下的 h2_excel_db 文件中
     * 2、表名规范：t{fileIndex}_s{sheetIndex}，fileIndex 全局递增（基于 TABLE_SCHEMA
     * 记录），sheetIndex 从 1 开始
     * 3、字段名规范：c1...cn，对应 Excel 表头的列顺序
     * 4、会自动创建 TABLE_SCHEMA 和 COLUMN_SCHEMA 表记录映射关系
     *
     * @param fileUrls      Excel 文件可访问 URL 列表
     * @param dbStoreageDir 数据库文件存储目录
     * @return H2 导入结果上下文，包含连接与映射关系
     */
    public static CachedExcelDatabase importExcelToH2(List<String> fileUrls, String dbFile) {
        if (fileUrls == null || fileUrls.isEmpty()) {
            throw new IllegalArgumentException("fileUrls 不能为空");
        }
        if (StringUtils.isBlank(dbFile)) {
            throw new IllegalArgumentException("dbStoreageDir 不能为空");
        }

        try {
            Path db = Path.of(dbFile);
            if (!Files.exists(db)) {
                Files.createDirectories(db.getParent());
            }
            String dbPath = db.toAbsolutePath().toString();

            HikariDataSource dataSource = new HikariDataSource();
            dataSource.setDriverClassName("org.h2.Driver");
            // 使用文件模式存储 H2 数据库
            dataSource.setJdbcUrl("jdbc:h2:file:" + dbPath);
            dataSource.setUsername("sa");
            dataSource.setPassword("");

            // 初始化 Schema 表
            String createTableSchemaSql = """
                    CREATE TABLE IF NOT EXISTS TABLE_SCHEMA (
                        FILE_NAME VARCHAR(255),
                        SHEET_NAME VARCHAR(255),
                        TABLE_NAME VARCHAR(255),
                        FILE_INDEX INT,
                        CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    )
                    """;
            MybatisUtils.insert(dataSource, createTableSchemaSql, null);

            String createColumnSchemaSql = """
                    CREATE TABLE IF NOT EXISTS COLUMN_SCHEMA (
                        TABLE_NAME VARCHAR(255),
                        HEADER_TEXT VARCHAR(255),
                        COLUMN_NAME VARCHAR(50),
                        COLUMN_INDEX INT
                    )
                    """;
            MybatisUtils.insert(dataSource, createColumnSchemaSql, null);

            // 获取当前最大 fileIndex
            String maxIndexSql = "SELECT MAX(FILE_INDEX) AS MAX_INDEX FROM TABLE_SCHEMA";
            List<Map<String, Object>> result = MybatisUtils.queryForList(dataSource, maxIndexSql, null);
            int startFileIndex = 0;
            if (result != null && !result.isEmpty() && result.get(0) != null) {
                Map<String, Object> row = result.get(0);
                Object maxIndexObj = row.get("MAX_INDEX");
                if (maxIndexObj != null) {
                    startFileIndex = Integer.parseInt(maxIndexObj.toString());
                }
            }

            List<ExcelSchema> excelSchemas = new ArrayList<>();

            loadExistingSchemas(dataSource, excelSchemas);

            int fileIndex = startFileIndex;
            for (String fileUrl : fileUrls) {
                fileIndex++;
                String normalized = StringUtils.substringBefore(fileUrl, "?");
                String fileName = normalized.substring(normalized.lastIndexOf('/') + 1);
                ExcelSchema excelSchema = new ExcelSchema();
                excelSchema.setFileName(fileName);
                excelSchemas.add(excelSchema);

                Path temp = Files.createTempFile("upload-h2-", ".xlsx");
                try {
                    copyFile(fileUrl, temp);
                    String contentType = Files.probeContentType(temp);
                    if (!StringUtils.equalsIgnoreCase(contentType,
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
                        throw new RuntimeException("不支持的文件 MIME 类型: " + contentType);
                    }

                    importExcelToH2(dataSource, temp, fileIndex, fileName, excelSchema);
                } finally {
                    Files.deleteIfExists(temp);
                }
            }

            return new CachedExcelDatabase(dataSource, excelSchemas);
        } catch (Exception e) {
            throw new RuntimeException("导入 Excel 到 H2 失败", e);
        }
    }

    /**
     * 从已存在的表结构中加载历史 ExcelSchema 映射
     *
     * @param dataSource   数据源
     * @param excelSchemas ExcelSchema 列表
     */
    private static void loadExistingSchemas(DataSource dataSource, List<ExcelSchema> excelSchemas) {
        String tableSchemaSql = "SELECT FILE_NAME, SHEET_NAME, TABLE_NAME, FILE_INDEX FROM TABLE_SCHEMA "
                + "ORDER BY FILE_INDEX, TABLE_NAME";
        List<Map<String, Object>> tableRows = MybatisUtils.queryForList(dataSource, tableSchemaSql, null);
        if (tableRows == null || tableRows.isEmpty()) {
            return;
        }
        Map<String, ExcelSchema> excelSchemaMap = new LinkedHashMap<>();
        Map<String, SheetSchema> sheetSchemaByTable = new HashMap<>();
        for (Map<String, Object> row : tableRows) {
            if (row == null) {
                continue;
            }
            Object fileNameObj = row.get("FILE_NAME");
            Object sheetNameObj = row.get("SHEET_NAME");
            Object tableNameObj = row.get("TABLE_NAME");
            Object fileIndexObj = row.get("FILE_INDEX");
            String fileName = fileNameObj == null ? "" : fileNameObj.toString();
            String sheetName = sheetNameObj == null ? "" : sheetNameObj.toString();
            String tableName = tableNameObj == null ? "" : tableNameObj.toString();
            String fileKey = fileName + "_" + (fileIndexObj == null ? "0" : fileIndexObj.toString());
            ExcelSchema excelSchema = excelSchemaMap.get(fileKey);
            if (excelSchema == null) {
                excelSchema = new ExcelSchema();
                excelSchema.setFileName(fileName);
                excelSchemaMap.put(fileKey, excelSchema);
                excelSchemas.add(excelSchema);
            }
            SheetSchema sheetSchema = new SheetSchema();
            sheetSchema.setSheetName(sheetName);
            sheetSchema.setTableName(tableName);
            excelSchema.getSheetSchemas().add(sheetSchema);
            sheetSchemaByTable.put(tableName, sheetSchema);
        }

        String columnSchemaSql = "SELECT TABLE_NAME, HEADER_TEXT, COLUMN_NAME, COLUMN_INDEX FROM COLUMN_SCHEMA "
                + "ORDER BY TABLE_NAME, COLUMN_INDEX";
        List<Map<String, Object>> columnRows = MybatisUtils.queryForList(dataSource, columnSchemaSql, null);
        if (columnRows == null || columnRows.isEmpty()) {
            return;
        }
        for (Map<String, Object> row : columnRows) {
            if (row == null) {
                continue;
            }
            Object tableNameObj = row.get("TABLE_NAME");
            Object headerTextObj = row.get("HEADER_TEXT");
            Object columnNameObj = row.get("COLUMN_NAME");
            String tableName = tableNameObj == null ? "" : tableNameObj.toString();
            SheetSchema sheetSchema = sheetSchemaByTable.get(tableName);
            if (sheetSchema == null) {
                continue;
            }
            String headerText = headerTextObj == null ? "" : headerTextObj.toString();
            String columnName = columnNameObj == null ? "" : columnNameObj.toString();
            sheetSchema.getColumnMapping().put(headerText, columnName);
        }
    }

    private static void importExcelToH2(DataSource dataSource, Path temp, int fileIndex, String fileName,
            ExcelSchema excelSchema)
            throws SQLException {
        Map<String, Integer> sheetIndexMap = new LinkedHashMap<>();
        Map<String, Integer> sheetColumnCountMap = new HashMap<>();
        Map<String, String> insertSqlMapping = new HashMap<>();
        FastExcel.read(temp.toFile(), new AnalysisEventListener<Map<Integer, Object>>() {
            @Override
            public void invoke(Map<Integer, Object> data, AnalysisContext context) {
                String sheetName = context.readSheetHolder().getSheetName();
                int sheetIndex = sheetIndexMap.computeIfAbsent(sheetName, k -> sheetIndexMap.size() + 1);
                String tableName = "t" + fileIndex + "_s" + sheetIndex;

                if (!sheetColumnCountMap.containsKey(sheetName)) {
                    if (data == null || data.isEmpty()) {
                        sheetColumnCountMap.put(sheetName, 0);
                        return;
                    }
                    TreeSet<Integer> sortedKeys = new TreeSet<>(data.keySet());
                    int maxIndex = sortedKeys.isEmpty() ? -1 : sortedKeys.last();
                    int columnCount = maxIndex + 1;
                    sheetColumnCountMap.put(sheetName, columnCount);

                    SheetSchema sheetSchema = new SheetSchema();
                    sheetSchema.setSheetName(sheetName);
                    sheetSchema.setTableName(tableName);
                    Map<String, String> columnMap = new LinkedHashMap<>();

                    // 插入 TABLE_SCHEMA
                    Map<String, Object> tableSchemaParams = new HashMap<>();
                    tableSchemaParams.put("fileName", fileName);
                    tableSchemaParams.put("sheetName", sheetName);
                    tableSchemaParams.put("tableName", tableName);
                    tableSchemaParams.put("fileIndex", fileIndex);
                    String insertTableSchemaSql = "INSERT INTO TABLE_SCHEMA (FILE_NAME, SHEET_NAME, TABLE_NAME, FILE_INDEX) VALUES (#{fileName}, #{sheetName}, #{tableName}, #{fileIndex})";
                    MybatisUtils.insert(dataSource, insertTableSchemaSql, tableSchemaParams);

                    for (int i = 0; i < columnCount; i++) {
                        String columnName = "c" + i;
                        Object headerValue = data.get(i);
                        String headerText = headerValue == null ? "" : headerValue.toString();
                        columnMap.put(headerText, columnName);

                        // 插入 COLUMN_SCHEMA
                        Map<String, Object> colSchemaParams = new HashMap<>();
                        colSchemaParams.put("tableName", tableName);
                        colSchemaParams.put("headerText", headerText);
                        colSchemaParams.put("columnName", columnName);
                        colSchemaParams.put("columnIndex", i);
                        String insertColSchemaSql = "INSERT INTO COLUMN_SCHEMA (TABLE_NAME, HEADER_TEXT, COLUMN_NAME, COLUMN_INDEX) VALUES (#{tableName}, #{headerText}, #{columnName}, #{columnIndex})";
                        MybatisUtils.insert(dataSource, insertColSchemaSql, colSchemaParams);
                    }
                    sheetSchema.setColumnMapping(columnMap);
                    excelSchema.getSheetSchemas().add(sheetSchema);

                    StringBuilder createSql = new StringBuilder();
                    createSql.append("CREATE TABLE ").append(tableName).append(" (");
                    for (int i = 0; i < columnCount; i++) {
                        if (i > 0) {
                            createSql.append(",");
                        }
                        createSql.append("c").append(i).append(" VARCHAR(4000)");
                    }
                    createSql.append(")");
                    MybatisUtils.insert(dataSource, createSql.toString(), null);

                    StringBuilder insertSql = new StringBuilder();
                    insertSql.append("INSERT INTO ").append(tableName).append(" (");
                    for (int i = 0; i < columnCount; i++) {
                        if (i > 0) {
                            insertSql.append(",");
                        }
                        insertSql.append("c").append(i);
                    }
                    insertSql.append(") VALUES (");
                    for (int i = 0; i < columnCount; i++) {
                        if (i > 0) {
                            insertSql.append(",");
                        }
                        insertSql.append("#{d%d}".formatted(i));
                    }
                    insertSql.append(")");
                    insertSqlMapping.put(tableName, insertSql.toString());
                    return;
                }

                int columnCount = sheetColumnCountMap.getOrDefault(sheetName, 0);
                if (columnCount <= 0) {
                    return;
                }
                Map<String, Object> params = new HashMap<>();
                for (int i = 0; i < columnCount; i++) {
                    Object value = data == null ? null : data.get(i);
                    params.put("d%d".formatted(i), value);
                }
                MybatisUtils.insert(dataSource, insertSqlMapping.get(tableName), params);
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
            }
        }).headRowNumber(0).doReadAll();

    }

    public static class ExcelSchema {
        /**
         * 文件名
         */
        private String fileName;
        /**
         * 工作表映射
         */
        private List<SheetSchema> sheetSchemas = new ArrayList<>();

        /**
         * 获取文件名
         *
         * @return 文件名
         */
        public String getFileName() {
            return fileName;
        }

        /**
         * 设置文件名
         *
         * @param fileName 文件名
         */
        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        /**
         * 获取工作表结构列表
         *
         * @return 工作表结构列表
         */
        public List<SheetSchema> getSheetSchemas() {
            return sheetSchemas;
        }

        /**
         * 设置工作表结构列表
         *
         * @param sheetSchemas 工作表结构列表
         */
        public void setSheetSchemas(List<SheetSchema> sheetSchemas) {
            this.sheetSchemas = sheetSchemas;
        }
    }

    public static class SheetSchema {
        /**
         * 工作表名
         */
        private String sheetName;
        /**
         * 数据库表名
         */
        private String tableName;
        /**
         * 数据库列名映射: sheet表头名称 -> 数据库列名
         */
        private Map<String, String> columnMapping = new HashMap<>();

        /**
         * 获取工作表名
         *
         * @return 工作表名
         */
        public String getSheetName() {
            return sheetName;
        }

        /**
         * 设置工作表名
         *
         * @param sheetName 工作表名
         */
        public void setSheetName(String sheetName) {
            this.sheetName = sheetName;
        }

        /**
         * 获取数据库表名
         *
         * @return 数据库表名
         */
        public String getTableName() {
            return tableName;
        }

        /**
         * 设置数据库表名
         *
         * @param tableName 数据库表名
         */
        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        /**
         * 获取列名映射
         *
         * @return 列名映射
         */
        public Map<String, String> getColumnMapping() {
            return columnMapping;
        }

        /**
         * 设置列名映射
         *
         * @param columnMapping 列名映射
         */
        public void setColumnMapping(Map<String, String> columnMapping) {
            this.columnMapping = columnMapping;
        }
    }

    private static void copyFile(String fileUrl, Path target) throws IOException {
        if (fileUrl == null || fileUrl.isBlank()) {
            return;
        }
        if (!StringUtils.startsWithAny(fileUrl.toLowerCase(), "http://", "https://", "file://", "jar://", "classpath://", "ftp://", "ftps://", "sftp://", "nfs://", "smb://")) {
            Files.copy(Paths.get(fileUrl), target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return;
        }
        URL url = URI.create(fileUrl).toURL();
        URLConnection conn = url.openConnection();
        try (InputStream in = new BufferedInputStream(conn.getInputStream());
                OutputStream out = Files.newOutputStream(target)) {
            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
        }
    }

}
