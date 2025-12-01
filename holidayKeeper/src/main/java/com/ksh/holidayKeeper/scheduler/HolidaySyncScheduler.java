package com.ksh.holidayKeeper.scheduler;

import com.ksh.holidayKeeper.api.NagerApiClient;
import com.ksh.holidayKeeper.service.HolidayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class HolidaySyncScheduler {

    private final HolidayService holidayService;
    private final NagerApiClient nagerApiClient;

    /**
     * 매년 1월 2일 01:00 (KST)에 전년도, 금년도 공휴일 데이터를 동기화합니다.
     * cron: 초 분 시 일 월 요일
     */
    @Scheduled(cron = "0 0 1 2 1 *", zone = "Asia/Seoul")
    public void syncHolidayData() {
        log.info("정기 공휴일 데이터 동기화 작업을 시작합니다.");

        int currentYear = LocalDate.now().getYear();
        int[] targetYears = {currentYear - 1, currentYear}; // #. 전년도, 금년도

        List<Map<String, Object>> countries = nagerApiClient.fetchCountries();

        for (int year : targetYears) {
            countries.forEach(country -> holidayService.upsertYearAndCountry(year, (String) country.get("countryCode")));
        }

        log.info("정기 공휴일 데이터 동기화 작업을 완료했습니다.");
    }
}