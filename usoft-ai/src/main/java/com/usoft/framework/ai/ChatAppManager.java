package com.usoft.framework.ai;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChatAppManager {

    private final static Map<String, ChatApp> apps = new ConcurrentHashMap<>();

    public static void register(String key, ChatApp app) {
        apps.put(key, app);
    }

    public static ChatApp get(String key) {
        return apps.get(key);
    }
}
