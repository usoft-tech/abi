package com.usoft.framework.core.mybatis;

import java.util.List;
import java.util.function.Function;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.usoft.framework.common.api.PageRequest;
import com.usoft.framework.common.api.PageResponse;

/**
 * 分页助手
 */
public class PageHelper {

    private PageHelper() {
    }

    /**
     * 应用分页参数
     * 
     * @param qw  查询包装器
     * @param req 分页请求
     */
    public static void apply(QueryWrapper qw, PageRequest req) {
        int page = req.getPage();
        int size = req.getSize();
        int start = Math.max(0, (page - 1) * size);
        qw.limit(start, size);
    }

    /**
     * 应用分页参数并映射结果
     * 
     * @param mapper 映射器
     * @param qw     查询包装器
     * @param req    分页请求
     * @return 分页响应
     */
    public static <T, M extends BaseMapper<T>> PageResponse<T> apply(M mapper, QueryWrapper qw, PageRequest req) {
        int total = (int) mapper.selectCountByQuery(qw);
        apply(qw, req);
        List<T> rows = mapper.selectListByQuery(qw);
        return new PageResponse<>(req.getPage(), req.getSize(), total, rows);
    }

    /**
     * 应用分页参数并映射结果
     * 
     * @param mapper   映射器
     * @param qw       查询包装器
     * @param req      分页请求
     * @param mapperFn 映射函数
     * @return 分页响应
     */
    public static <T, M extends BaseMapper<T>, R> PageResponse<R> apply(M mapper, QueryWrapper qw, PageRequest req,
            Function<T, R> mapperFn) {
        int total = (int) mapper.selectCountByQuery(qw);
        apply(qw, req);
        List<T> rows = mapper.selectListByQuery(qw);
        List<R> items = rows.stream().map(mapperFn).toList();
        return new PageResponse<>(req.getPage(), req.getSize(), total, items);
    }

    /**
     * 应用分页参数并映射结果
     * 
     * @param mapper       映射器
     * @param qw           查询包装器
     * @param req          分页请求
     * @param responseType 响应类型
     * @return 分页响应
     */
    public static <T, M extends BaseMapper<T>, R> PageResponse<R> apply(M mapper, QueryWrapper qw, PageRequest req,
            Class<R> responseType) {
        int total = (int) mapper.selectCountByQuery(qw);
        apply(qw, req);
        List<R> items = mapper.selectListByQueryAs(qw, responseType);
        return new PageResponse<>(req.getPage(), req.getSize(), total, items);
    }
}
