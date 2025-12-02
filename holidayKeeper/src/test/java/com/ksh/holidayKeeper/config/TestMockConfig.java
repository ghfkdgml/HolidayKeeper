package com.ksh.holidayKeeper.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import com.ksh.holidayKeeper.service.HolidayService;

@TestConfiguration(proxyBeanMethods = false)
public class TestMockConfig {

    // @Bean
    // public NagerApiClient nagerApiClient() {
    //     return Mockito.mock(NagerApiClient.class);
    // }

    @Bean
    public HolidayService holidayService() {
        return Mockito.mock(HolidayService.class);
    }
}
