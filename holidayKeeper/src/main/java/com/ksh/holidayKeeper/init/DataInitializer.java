package com.ksh.holidayKeeper.init;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.ksh.holidayKeeper.service.HolidayService;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final HolidayService holidayService;

    @Override
    public void run(String... args) throws Exception {
        log.info("초기 공휴일 데이터 적재를 시작합니다...");
        holidayService.loadInitialData();
        log.info("초기 공휴일 데이터 적재를 완료했습니다.");
    }
}