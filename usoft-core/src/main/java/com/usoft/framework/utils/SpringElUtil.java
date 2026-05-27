package com.usoft.framework.utils;

import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * spring el工具类
 *
 * @author lilu
 */
@SuppressWarnings("unused")
public class SpringElUtil {
    private static final ExpressionParser EXPRESSION_PARSER = new SpelExpressionParser();
    private static final ParameterNameDiscoverer PARAMETER_NAME_DISCOVERER = new DefaultParameterNameDiscoverer();

    private SpringElUtil() {
    }

    /**
     * 评估给定的表达式并返回结果。
     * <p>
     * 该方法用于动态评估表达式，允许根据给定的变量集解析和计算表达式。
     * 主要用于需要动态计算表达式的场景，例如脚本语言或动态配置处理。
     *
     * @param expressionString 要评估的表达式字符串，包含变量和操作符。
     * @param variables        包含变量名及其对应值的映射。
     * @return 表达式评估的结果，返回值类型取决于具体的表达式。
     *         <p>
     *         注意：此方法重载提供了一个默认实现，允许在不指定类名的情况下调用。
     */
    public static Object evaluateExpression(String expressionString, Map<String, Object> variables) {
        return evaluateExpression(expressionString, variables, null);
    }

    /**
     * 根据给定的表达式字符串和变量映射，评估并返回指定类型的表达式结果
     *
     * @param expressionString  表达式字符串，描述了需要评估的逻辑或计算
     * @param variables         包含表达式中所有变量及其值的映射
     * @param desiredResultType 期望的返回值类型，用于转换表达式的计算结果
     * @param <T>               泛型参数，表示期望的返回值类型
     * @return 表达式评估结果，类型为 desiredResultType 指定的类型
     */
    public static <T> T evaluateExpression(String expressionString, Map<String, Object> variables,
            Class<T> desiredResultType) {
        // 如果为空，则不进行解析
        if (StringUtils.isBlank(expressionString)) {
            return null;
        }

        // Spring 的表达式上下文对象
        EvaluationContext context = new StandardEvaluationContext();
        // 确保变量映射不为空，并将所有变量添加到上下文中
        if (ObjectUtils.isNotEmpty(variables)) {
            variables.forEach(context::setVariable);
        }

        // 第二步，逐个参数解析
        // 解析表达式字符串，并根据上下文和期望的返回类型获取其值
        return EXPRESSION_PARSER.parseExpression(expressionString, ParserContext.TEMPLATE_EXPRESSION).getValue(context,
                desiredResultType);
    }

}
