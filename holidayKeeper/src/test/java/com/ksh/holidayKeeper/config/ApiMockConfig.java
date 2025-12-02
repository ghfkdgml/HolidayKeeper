package com.ksh.holidayKeeper.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import com.ksh.holidayKeeper.api.NagerApiClient;

@TestConfiguration(proxyBeanMethods = false)
public class ApiMockConfig {
    @Bean
    public NagerApiClient nagerApiClient() {
        return Mockito.mock(NagerApiClient.class);
    }
}
