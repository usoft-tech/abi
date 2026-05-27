package com.usoft.framework.utils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.apache.ibatis.type.TypeReference;
import org.mybatis.spring.transaction.SpringManagedTransactionFactory;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * mybatis工具类
 *
 * @author lilu
 */
@Slf4j
@SuppressWarnings("unused")
public class MybatisUtils {

    private MybatisUtils() {
    }

    // 语句ID
    public static final String STATEMENT_ID = "statementId";

    /**
     * 解析sql，获取最终的sql和参数
     *
     * @param selectSql sql
     * @param variables 参数
     * @return 可执行的sql和参数
     */
    public static BoundingSql compile(String selectSql, Map<String, Object> variables) {
        MappedStatement mappedStatement = compileMapper(StatementType.SELECT, selectSql, Map.class.getTypeName());
        assert mappedStatement != null;
        BoundSql boundSql = mappedStatement.getBoundSql(variables);
        // 替换参数值获取最终的sql
        return boxing(mappedStatement.getConfiguration(), boundSql);
    }

    /**
     * 执行插入操作
     * 此方法根据提供的数据源、插入SQL语句和参数执行数据库插入操作
     *
     * @param dataSource 数据源，用于建立数据库连接
     * @param insertSql  插入SQL语句，描述了希望向数据库中插入的数据
     * @param variables  插入变量，一个包含插入所需参数的Map对象
     * @return 返回一个Map对象，包含查询结果
     */
    public static Integer insert(DataSource dataSource, String insertSql,
            Map<String, Object> variables) {
        // 执行插入操作，返回Map类型的结果
        return execute(dataSource, StatementType.INSERT, insertSql, Integer.class,
                (session, mappedStatement) -> session.insert(mappedStatement.getId(), variables));
    }

    /**
     * 根据提供的数据源和SQL查询语句执行查询，并将结果作为Map对象返回
     * 此方法主要用于执行查询语句，并将查询结果映射到一个Map对象中，便于后续处理或分析查询结果
     *
     * @param dataSource 数据源，用于建立数据库连接
     * @param selectSql  查询SQL语句，描述了希望从数据库中获取的数据
     * @param variables  查询变量，一个包含查询所需参数的Map对象
     * @return 返回一个Map对象，包含查询结果
     */
    public static Map<String, Object> queryForMap(DataSource dataSource, String selectSql,
            Map<String, Object> variables) {
        // 执行查询操作，返回Map类型的结果
        return execute(dataSource, StatementType.SELECT, selectSql, Map.class,
                (session, mappedStatement) -> session.selectOne(mappedStatement.getId(), variables));
    }

    /**
     * 查询数据库中的单个对象
     * 该方法使用指定的数据源执行SQL查询，并期望返回单个对象结果
     * 它允许传递SQL查询语句和查询参数，以及指定返回值的类型
     *
     * @param dataSource 数据源，用于执行数据库操作
     * @param selectSql  查询的SQL语句
     * @param variables  查询参数，以键值对形式提供，与SQL语句中的参数占位符对应
     * @param resultType 期望的返回值类型，方法将返回此类型的一个实例
     * @param <T>        泛型参数，表示返回值的类型
     * @return 返回查询结果，该结果是指定返回值类型的一个实例
     */
    public static <T> T queryForObject(DataSource dataSource, String selectSql, Map<String, Object> variables,
            Class<T> resultType) {
        // 调用execute方法执行查询操作，execute方法是一个通用的数据库操作方法
        // 通过lambda表达式指定具体的查询操作：使用selectOne方法执行单条目查询
        return execute(dataSource, StatementType.SELECT, selectSql, resultType,
                (session, mappedStatement) -> session.selectOne(mappedStatement.getId(), variables));
    }

    /**
     * 查询数据库中的单个对象
     * 该方法使用指定的数据源执行SQL查询，并期望返回单个对象结果
     * 它允许传递SQL查询语句和查询参数，以及指定返回对象的类型
     *
     * @param dataSource 数据源，用于建立数据库连接
     * @param selectSql  查询的SQL语句，用于指定查询条件
     * @param variables  查询变量，用于替换SQL语句中的参数占位符
     * @param resultType 结果类型，指定期望返回的对象类型
     * @param <T>        泛型参数，表示返回对象的具体类型
     * @return 根据SQL查询结果转换后的单个对象
     */
    public static <T> T queryForObject(DataSource dataSource, String selectSql, Map<String, Object> variables,
            TypeReference<T> resultType) {
        // 执行查询操作，使用提供的数据源、SQL语句、查询变量和结果类型
        // 通过内部函数execute来处理实际的数据库操作逻辑
        // 在这个上下文中，lambda表达式定义了如何使用session对象执行查询操作
        // 它调用session.selectOne方法，传递映射语句的ID和查询变量作为参数
        return execute(dataSource, StatementType.SELECT, selectSql, resultType,
                (session, mappedStatement) -> session.selectOne(mappedStatement.getId(), variables));
    }

    /**
     * 使用给定的数据源执行SQL查询，并将结果映射到一个包含多个键值对的列表中
     * 主要用于执行查询操作，并将每行查询结果映射为一个Map对象，所有行组成一个List集合
     *
     * @param dataSource 数据源，用于建立数据库连接
     * @param selectSql  查询SQL语句，用于指定查询操作
     * @param variables  SQL变量，包含SQL语句中的参数
     * @return 查询结果列表，每个元素都是一个键值对映射，代表一行数据
     */
    public static List<Map<String, Object>> queryForList(DataSource dataSource, String selectSql,
            Map<String, Object> variables) {
        // 调用execute函数执行查询操作，传入数据源、SQL语句、结果映射类型以及具体的操作逻辑
        // 在这个场景中，操作逻辑是执行一个列表查询，传入变量参数，并将结果映射为Map对象列表
        return execute(dataSource, StatementType.SELECT, selectSql, Map.class,
                (session, mappedStatement) -> session.selectList(mappedStatement.getId(), variables));
    }

    /**
     * 根据提供的数据源、SQL查询语句、变量映射和结果类型，执行查询并返回结果列表
     * 此方法使用泛型来处理不同类型的结果，提高了代码的可重用性和灵活性
     *
     * @param dataSource 数据源，用于建立数据库连接
     * @param selectSql  查询的SQL语句，可以包含动态参数占位符
     * @param variables  包含SQL语句中动态参数的映射，键为参数名，值为参数值
     * @param resultType 结果类型，指定查询结果应映射到的类类型
     * @param <T>        泛型参数，表示查询结果的类型
     * @return 返回一个包含查询结果的列表，列表中的每个元素都是指定结果类型的实例
     */
    public static <T> List<T> queryForList(DataSource dataSource, String selectSql, Map<String, Object> variables,
            Class<T> resultType) {
        // 调用execute方法来执行查询，其中session.selectList方法用于执行SQL并获取列表结果
        return execute(dataSource, StatementType.SELECT, selectSql, resultType,
                (session, mappedStatement) -> session.selectList(mappedStatement.getId(), variables));
    }

    /**
     * 根据提供的数据源和SQL语句查询数据库，并将结果映射为指定类型的列表
     *
     * @param dataSource 数据源，用于建立数据库连接
     * @param selectSql  查询的SQL语句
     * @param variables  SQL语句中的变量，以键值对形式提供
     * @param resultType 结果集的类型引用，用于指定返回列表中元素的类型
     * @param <T>        泛型参数，表示结果列表中元素的类型
     * @return 查询结果列表，列表中的元素类型为T
     */
    public static <T> List<T> queryForList(DataSource dataSource, String selectSql, Map<String, Object> variables,
            TypeReference<T> resultType) {
        // 执行查询操作，并将结果映射为列表
        return execute(dataSource, StatementType.SELECT, selectSql, resultType,
                (session, mappedStatement) -> session.selectList(mappedStatement.getId(), variables));
    }

    /**
     * 执行SQL查询并返回结果
     * 该方法使用泛型来处理不同类型的结果，并通过传递的Execute接口实现具体的执行逻辑
     *
     * @param dataSource 数据源，用于建立数据库连接
     * @param sql        查询的SQL语句
     * @param resultType 结果的类类型，用于将查询结果映射到指定的类
     * @param execute    Execute接口的实现，定义了如何执行查询和处理结果
     * @return T 查询结果，类型由调用者指定
     */
    private static <T> T execute(DataSource dataSource, StatementType statementType, String sql, Class<?> resultType,
            Execute<T> execute) {
        return execute(dataSource, statementType, sql, resultType.getTypeName(), execute);
    }

    /**
     * 执行数据库查询操作的通用方法
     *
     * @param dataSource 数据源，用于建立数据库连接
     * @param sql        查询SQL语句，用于指定查询条件
     * @param resultType 期望的查询结果类型，用于类型转换
     * @param execute    执行查询的操作接口，定义了如何处理查询结果
     * @param <T>        查询结果的实际类型参数
     * @param <R>        方法返回的类型参数
     * @return 执行查询后的结果，类型为R
     */
    private static <T, R> R execute(DataSource dataSource, StatementType statementType, String sql,
            TypeReference<T> resultType,
            Execute<R> execute) {
        // 调用重载方法，传入数据源、SQL语句、结果类型的全限定名和执行操作接口
        return execute(dataSource, statementType, sql, resultType.getRawType().getTypeName(), execute);
    }

    /**
     * 执行SQL查询并返回结果
     * 此方法通过MyBatis框架执行给定的SQL查询，并使用回调接口处理查询结果
     *
     * @param dataSource     数据源，用于执行SQL查询
     * @param sql            查询SQL语句
     * @param resultTypeName 期望的结果类型名称，用于映射查询结果
     * @param execute        一个执行接口实例，定义了如何处理查询结果
     * @param <T>            查询结果映射的类型参数
     * @param <R>            返回值类型参数
     * @return 执行查询后的结果
     */
    private static <T, R> R execute(DataSource dataSource, StatementType statementType, String sql,
            String resultTypeName,
            Execute<R> execute) {
        // 编译映射器，准备SQL执行语句
        MappedStatement mappedStatement = compileMapper(statementType, sql, resultTypeName);
        // 获取配置对象，用于设置环境和创建SqlSessionFactory
        Configuration configuration = Objects.requireNonNull(mappedStatement).getConfiguration();
        // 设置配置的环境，包括ID、事务管理和数据源
        configuration.setEnvironment(new Environment("default", new SpringManagedTransactionFactory(), dataSource));
        // 构建SqlSessionFactory，用于生产SqlSession
        SqlSessionFactory sessionFactory = new SqlSessionFactoryBuilder().build(configuration);
        // 打开SqlSession，执行SQL并处理结果
        try (SqlSession session = sessionFactory.openSession()) {
            // 调用回调接口的execute方法，传入当前会话和映射语句，获取并返回结果
            return execute.execute(session, mappedStatement);
        }
    }

    /**
     * 执行操作的功能性接口
     * 用于定义在SqlSession和MappedStatement上执行的数据库操作
     * 由于数据库操作的多样性，这里使用函数式接口来允许灵活的执行不同的操作
     *
     * @param <T> 执行结果的类型
     */
    @FunctionalInterface
    private interface Execute<T> {
        /**
         * 执行数据库操作的方法
         *
         * @param session         当前的SqlSession实例，用于执行SQL语句
         * @param mappedStatement 映射的SQL语句和相关配置的封装
         * @return 执行结果，类型由调用方指定
         */
        T execute(SqlSession session, MappedStatement mappedStatement);
    }

    /**
     * 编译Mapper
     * <p>
     * 该方法用于动态地将给定的SQL查询语句和结果类型编译成一个MappedStatement对象
     * 这在MyBatis框架中是至关重要的，因为它允许我们在运行时生成和解析Mapper配置，
     * 而不需要在静态XML文件中预先定义这些配置
     *
     * @param sql            SQL查询语句，用于从数据库中选择数据
     * @param resultTypeName 结果类型名，指定查询结果应映射到的Java类型
     * @return 返回一个MappedStatement对象，它封装了映射信息和查询配置如果解析失败，返回null
     */
    private static MappedStatement compileMapper(StatementType statementType, String sql, String resultTypeName) {
        // 使用更健壮的转义逻辑，确保 MyBatis 标签被保留，而 SQL 中的 <, >, & 被转义
        String source = sql;
        // 1. 转义 & (必须最先处理，防止二次转义)
        // 匹配 & 但排除掉已经是 XML 实体的部分
        source = source.replaceAll("&(?!(amp|lt|gt|quot|apos);)", "&amp;");
        // 2. 转义 <
        // 仅当 < 后面不是 mybatis 标签名或 / 时才转义
        // 支持的标签: if, where, set, foreach, choose, when, otherwise, bind, trim, sql, include, script
        source = source.replaceAll("<(?!(/?(if|where|set|foreach|choose|when|otherwise|bind|trim|sql|include|script))(\\s|>|/))", "&lt;");
        // 3. 转义 >
        // 仅当 > 前面不是 / 或 " 或 ' 或字母或数字时才转义 (保留标签结尾 > 和属性结尾 >)
        source = source.replaceAll("(?<![a-zA-Z0-9/\"'])>", "&gt;");

        String computedMapper = statementType.getTemplate().replace("{{sql}}", source).replace("{{resultType}}",
                resultTypeName);
        try (InputStream stream = new ByteArrayInputStream(computedMapper.getBytes(StandardCharsets.UTF_8))) {
            // 解析配置
            Configuration configuration = new Configuration();
            XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(stream, configuration, STATEMENT_ID,
                    new HashMap<>());
            xmlMapperBuilder.parse();
            // 获取解析后的MappedStatement对象
            return xmlMapperBuilder.getConfiguration().getMappedStatement(STATEMENT_ID);
        } catch (Exception e) {
            // 记录详细异常信息
            log.error("编译 MyBatis Mapper 失败: \n{}", computedMapper, e);
            return null;
        }
    }

    /**
     * 根据提供的Configuration和BoundSql对象，构建并返回一个BoundingSql对象
     * 该方法主要用于将SQL语句和对应的参数进行合并，以便于后续处理
     *
     * @param configuration MyBatis的配置对象，包含全局配置信息
     * @param boundSql      MyBatis的BoundSql对象，包含具体的SQL语句和参数映射
     * @return BoundingSql对象，包含格式化后的SQL语句和参数列表
     */
    private static BoundingSql boxing(Configuration configuration, BoundSql boundSql) {
        // 获取SQL语句
        String sql = boundSql.getSql();
        // 填充占位符， 把传参填进去，使用#｛｝、${} 一样的方式
        Object parameterObject = boundSql.getParameterObject();
        // 参数映射列表，有入的（in） 有出的（O），后面只遍历传入的参数，并且把传入的参数赋值成我们代码里的值
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();

        List<Object> params = new ArrayList<>();
        MetaObject metaObject = parameterObject == null ? null : configuration.newMetaObject(parameterObject);
        Stream.ofNullable(parameterMappings)
                .flatMap(Collection::stream)
                .filter(parameterMapping -> parameterMapping.getMode() != ParameterMode.OUT)
                .forEach(parameterMapping -> {
                    Object value;
                    String propertyName = parameterMapping.getProperty();
                    // 获取参数名称
                    if (boundSql.hasAdditionalParameter(propertyName)) {
                        // 获取参数值
                        value = boundSql.getAdditionalParameter(propertyName);
                    } else if (parameterObject == null) {
                        value = null;
                    } else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                        // 如果是单个值则直接赋值
                        value = parameterObject;
                    } else {
                        value = metaObject == null ? null : metaObject.getValue(propertyName);
                    }
                    params.add(value);
                });
        return BoundingSql.builder().sql(sql).params(params).build();
    }

    /**
     * 可执行的sql和参数
     */
    @Data
    @Builder
    public static class BoundingSql {
        /**
         * sql
         */
        private String sql;
        /**
         * 参数
         */
        private List<Object> params;
    }

    @Getter
    private enum StatementType {
        SELECT("""
                <?xml version="1.0" encoding="UTF-8"?>
                <!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "https://mybatis.org/dtd/mybatis-3-mapper.dtd">
                <mapper namespace="customMapperUtils">
                   <select id="statementId" parameterType="java.util.Map" resultType="{{resultType}}">
                       {{sql}}
                   </select>
                </mapper>
                """),
        INSERT("""
                <?xml version="1.0" encoding="UTF-8"?>
                <!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "https://mybatis.org/dtd/mybatis-3-mapper.dtd">
                <mapper namespace="customMapperUtils">
                   <insert id="statementId">
                       {{sql}}
                   </insert>
                </mapper>
                """);

        private final String template;

        StatementType(String template) {
            this.template = template;
        }
    }
}
