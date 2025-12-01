package com.ksh.holidayKeeper.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Year;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Tag("integration")
@DisplayName("Nager API 실제 연동 테스트")
class NagerApiClientIntegrationTest {

    @Autowired
    private NagerApiClient apiClient;

    @Test
    @DisplayName("fetchCountries: 실제 API를 호출하여 국가 목록을 가져온다")
    void testFetchCountries() {
        // when
        List<Map<String, Object>> countries = apiClient.fetchCountries();

        // then
        assertNotNull(countries);
        assertFalse(countries.isEmpty(), "국가 목록이 비어있으면 안됩니다.");

        // 대한민국(KR)이 목록에 포함되어 있는지 확인
        boolean foundKorea = countries.stream()
                .anyMatch(country -> "KR".equals(country.get("countryCode")));
        assertTrue(foundKorea, "국가 목록에 대한민국(KR)이 포함되어 있어야 합니다.");
    }

    @Test
    @DisplayName("fetchHolidays: 실제 API를 호출하여 특정 국가의 공휴일 목록을 가져온다")
    void testFetchHolidays() {
        // given
        int currentYear = Year.now().getValue();

        // when
        List<Map<String, Object>> holidays = apiClient.fetchHolidays(currentYear, "KR");

        // then
        assertNotNull(holidays);
        assertFalse(holidays.isEmpty(), "대한민국의 " + currentYear + "년 공휴일 목록이 비어있으면 안됩니다.");

        // 공휴일 목록에 '새해'가 포함되어 있는지 확인
        boolean foundNewYear = holidays.stream()
                .anyMatch(holiday -> "새해".equals(holiday.get("localName")));
        assertTrue(foundNewYear, currentYear + "년 공휴일 목록에 '새해'가 포함되어 있어야 합니다.");
    }
}