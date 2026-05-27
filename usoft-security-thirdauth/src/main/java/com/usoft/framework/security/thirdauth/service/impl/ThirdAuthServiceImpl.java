package com.usoft.framework.security.thirdauth.service.impl;

import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import com.usoft.framework.common.exception.BizException;
import com.usoft.framework.security.thirdauth.model.ThirdAuthCallback;
import com.usoft.framework.security.thirdauth.model.ThirdAuthUser;
import com.usoft.framework.security.thirdauth.provider.ThirdAuthProvider;
import com.usoft.framework.security.thirdauth.service.ThirdAuthService;

import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of ThirdAuthService using custom providers.
 */
@Slf4j
@Service
public class ThirdAuthServiceImpl implements ThirdAuthService {

    private final Map<String, ThirdAuthProvider> providerMap;

    public ThirdAuthServiceImpl(ObjectProvider<ThirdAuthProvider> providers) {
        this.providerMap = providers.stream()
                .collect(Collectors.toMap(ThirdAuthProvider::getPlatform, Function.identity()));
    }

    @Override
    public String getAuthorizeUrl(String platform) {
        ThirdAuthProvider provider = providerMap.get(platform);
        if (provider == null) {
            throw new BizException("Unsupported platform: " + platform);
        }
        String state = UUID.randomUUID().toString();
        // Ideally we should cache state to validate it in callback, but for now we just generate it.
        return provider.getAuthorizeUrl(state);
    }

    @Override
    public ThirdAuthUser login(String platform, ThirdAuthCallback callback) {
        ThirdAuthProvider provider = providerMap.get(platform);
        if (provider == null) {
            throw new BizException("Unsupported platform: " + platform);
        }
        return provider.login(callback);
    }
}
