package com.usoft.framework.core;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * 打印应用启动URL的运行器
 */
@Component
public class PrintUrlRunner implements ApplicationRunner {

    private final ConfigurableApplicationContext context;

    public PrintUrlRunner(ConfigurableApplicationContext context) {
        this.context = context;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        afterRun(context);
    }

    private static void afterRun(ConfigurableApplicationContext context) {
        ServerProperties serverProperties = context.getBean(ServerProperties.class);
        Environment environment = context.getBean(Environment.class);
        String port = environment.getProperty("local.server.port");
        String contextPath = serverProperties.getServlet().getContextPath();
        String ip = getLocalAddress();
        String portStr = "80".equals(port) ? "" : ":" + port;
        contextPath = "/".equals(contextPath) || StringUtils.isEmpty(contextPath) ? "" : contextPath;
        String localUrl = "http://localhost" + portStr + contextPath;
        if (StringUtils.isNotBlank(ip)) {
            String remoteUrl = "http://" + ip + portStr + contextPath;
            System.out.printf("系统启动成功， 访问地址如下：\n\t\t- Local: \t%s\n\t\t- Remote: \t%s%n", localUrl, remoteUrl);
        } else {
            System.out.printf("系统启动成功， 访问地址如下：\n\t\t- Local: \t%s%n", localUrl);
        }

    }

    private static String getLocalAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return null;
        }
    }

}
