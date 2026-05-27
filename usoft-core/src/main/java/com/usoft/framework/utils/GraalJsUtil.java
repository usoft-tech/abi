package com.usoft.framework.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.lang3.StringUtils;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;
import org.springframework.util.ResourceUtils;

import com.alibaba.fastjson2.JSON;

/**
 * GraalJsUtil
 */
public class GraalJsUtil {

    static {
        System.setProperty("polyglot.engine.WarnInterpreterOnly", "false");
    }

    // script/question-corrector
    private static final String CLASSPATH_CONFIG_QUESTION_CORRECTOR_JS = "classpath:script/question-corrector.js";
    private static final String CLASSPATH_CONFIG_QUESTION_CORRECTOR_CACHE = "classpath:script/question-corrector.cache";

    private static String cacheScriptContent = null;

    /**
     * 通过JavaScript脚本对输入字符串进行修正或处理。 如果脚本内容为空，则直接返回原始输入字符串。
     * 该方法使用GraalVM执行JavaScript，要求脚本中包含一个名为'corrector'的函数用于字符串处理。
     *
     * @param input 需要处理的字符串
     * @return 处理后的字符串
     */
    public static String correctorString(String input) {
        String scriptContent = getScriptContent();

        if (StringUtils.isBlank(scriptContent)) {
            return input;
        }

        // 创建GraalVM上下文并加载脚本
        try (Context context = getContext()) {
            context.eval("js", scriptContent);

            // 假设脚本中存在一个名为'corrector'的函数，该函数接收字符串作为参数
            Value transformFunction = context.getBindings("js").getMember("corrector");
            if (transformFunction == null || !transformFunction.canExecute()) {
                return input;
            }

            // 执行函数并传入输入字符串
            Value result = transformFunction.execute(input);
            return result.asString();
        }
    }

    /**
     * 获取脚本内容，优先从缓存中读取。 如果没有缓存，则尝试从指定路径读取脚本文件，并将其内容缓存以供后续使用。
     *
     * @return 脚本内容，如果脚本文件不存在则返回null
     */
    private static String getScriptContent() {
        boolean isCache = StringUtils.isNotBlank(cacheScriptContent);
        try {
            ResourceUtils.getFile(CLASSPATH_CONFIG_QUESTION_CORRECTOR_CACHE);
        } catch (FileNotFoundException e) {
            isCache = false;
        }
        if (isCache) {
            return cacheScriptContent;
        }
        // 加载JavaScript文件
        File file;
        try {
            file = ResourceUtils.getFile(CLASSPATH_CONFIG_QUESTION_CORRECTOR_JS);
        } catch (FileNotFoundException e) {
            return null;
        }

        try {
            cacheScriptContent = String.join("\n", Files.readAllLines(file.toPath(), StandardCharsets.UTF_8));
            Files.createFile(
                    Path.of(file.getParent() + File.separator + "question-corrector.cache"));
            return cacheScriptContent;
        } catch (FileAlreadyExistsException e) {
            return cacheScriptContent;
        } catch (IOException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T execute(String script, String functionName, Class<T> clazz,
            Object... arguments) {

        if (StringUtils.isBlank(script)) {
            return null;
        }

        // 创建GraalVM上下文并加载脚本
        try (Context context = getContext()) {
            context.eval("js", script);

            // 假设脚本中存在一个名为'corrector'的函数，该函数接收字符串作为参数
            Value transformFunction = context.getBindings("js").getMember(functionName);
            if (transformFunction == null || !transformFunction.canExecute()) {
                return null;
            }

            // 执行函数并传入输入字符串
            Value jsonParse = context.eval("js", "JSON.parse");
            Object[] args = new Object[arguments.length];
            for (int i = 0; i < arguments.length; i++) {
                args[i] = arguments[i] == null ? null : jsonParse.execute(JSON.toJSONString(arguments[i]));
            }
            Value result = transformFunction.execute(args);
            if (result.isHostObject()) {
                return (T) result.asHostObject();
            }
            Value jsonStringify = context.eval("js", "JSON.stringify");
            Value stringified = jsonStringify.execute(result);
            if (stringified.isNull()) {
                return null;
            }
            String json = stringified.asString();
            return JSON.parseObject(json, clazz);
        }
    }

    public static Object execute(String script, String functionName,
            Object... arguments) {

        if (StringUtils.isBlank(script)) {
            return null;
        }

        // 创建GraalVM上下文并加载脚本
        try (Context context = getContext()) {
            context.eval("js", script);

            // 假设脚本中存在一个名为'corrector'的函数，该函数接收字符串作为参数
            Value transformFunction = context.getBindings("js").getMember(functionName);
            if (transformFunction == null || !transformFunction.canExecute()) {
                return null;
            }

            // 执行函数并传入输入字符串
            Value jsonParse = context.eval("js", "JSON.parse");
            Object[] args = new Object[arguments.length];
            for (int i = 0; i < arguments.length; i++) {
                args[i] = arguments[i] == null ? null : jsonParse.execute(JSON.toJSONString(arguments[i]));
            }
            Value result = transformFunction.execute(args);
            if (result.isHostObject()) {
                return result.asHostObject();
            }
            Value jsonStringify = context.eval("js", "JSON.stringify");
            Value stringified = jsonStringify.execute(result);
            if (stringified.isNull()) {
                return null;
            }
            String json = stringified.asString();
            return JSON.parse(json);
        }
    }

    private static Context getContext() {
        return Context.newBuilder("js")
                .allowHostAccess(HostAccess.ALL)
                .allowHostClassLookup(s -> true)
                .build();
    }

}
