package com.ksh.holidayKeeper.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.ksh.holidayKeeper.api.NagerApiClient;
import com.ksh.holidayKeeper.config.ApiMockConfig;
import com.ksh.holidayKeeper.entity.Holiday;
import com.ksh.holidayKeeper.repository.HolidayRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@Import(ApiMockConfig.class)
@ActiveProfiles("test")
class HolidayIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private HolidayRepository holidayRepository;
    
    @Autowired
    private NagerApiClient nagerApiClient;

    @Test
    @DisplayName("통합 테스트: GET /api/holidays - 오프셋 페이징 검색")
    void search_OffsetPaging_Integration() throws Exception {
        // given
        holidayRepository.save(Holiday.builder().countryCode("KR").holidayYear(2024).name("New Year").date(LocalDate.of(2024, 1, 1)).types("Public").build());
        holidayRepository.save(Holiday.builder().countryCode("KR").holidayYear(2024).name("Independence Movement Day").date(LocalDate.of(2024, 3, 1)).types("Public").build());

        // when & then
        mockMvc.perform(get("/api/holidays")
                        .param("countries", "KR")
                        .param("from", "2024")
                        .param("to", "2024")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.items[0].name").value("New Year"));
    }

    @Test
    @DisplayName("통합 테스트: GET /api/holidays/search-cursor - 커서 페이징 검색")
    void search_CursorPaging_Integration() throws Exception {
        // given
        holidayRepository.save(Holiday.builder().countryCode("KR").holidayYear(2024).name("New Year").date(LocalDate.of(2024, 1, 1)).types("Public").build());
        holidayRepository.save(Holiday.builder().countryCode("KR").holidayYear(2024).name("Independence Movement Day").date(LocalDate.of(2024, 3, 1)).types("Public").build());
        holidayRepository.save(Holiday.builder().countryCode("US").holidayYear(2024).name("New Year").date(LocalDate.of(2024, 1, 1)).types("Public").build());

        // when & then
        mockMvc.perform(get("/api/holidays/search-cursor")
                        .param("countries", "KR", "US")
                        .param("from", "2024")
                        .param("to", "2024")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items.length()").value(3))
                .andExpect(jsonPath("$.data.nextCursor").isNumber());
    }

    @Test
    @DisplayName("통합 테스트: POST /api/holidays/refresh - 공휴일 데이터 갱신")
    void refresh_Integration() throws Exception {
        // given
        int year = 2025;
        String countryCode = "KR";

        // Mock API가 반환할 새로운 공휴일 데이터 정의
        List<Map<String, Object>> holidaysFromApi = List.of(
                Map.of("date", "2025-01-01", "localName", "새해", "name", "New Year's Day", "type", "Public"),
                Map.of("date", "2025-03-01", "localName", "삼일절", "name", "Independence Movement Day", "type", "Public")
        );
        given(nagerApiClient.fetchHolidays(year, countryCode)).willReturn(holidaysFromApi);

        // when
        mockMvc.perform(post("/api/holidays/refresh")
                        .param("year", String.valueOf(year))
                        .param("country", countryCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // then
        // 1. 서비스가 Mock API 클라이언트를 호출했는지 검증
        verify(nagerApiClient).fetchHolidays(year, countryCode);

        // 2. 실제 DB에 데이터가 저장되었는지 검증
        List<Holiday> savedHolidays = holidayRepository.findByCountryCodeAndHolidayYear(countryCode, year);
        assertThat(savedHolidays).hasSize(2);
        assertThat(savedHolidays.get(0).getName()).isEqualTo("New Year's Day");
    }

    @Test
    @DisplayName("통합 테스트: DELETE /api/holidays - 공휴일 데이터 삭제")
    void delete_Integration() throws Exception {
        // given
        int year = 2024;
        String countryCode = "KR";
        holidayRepository.save(Holiday.builder().countryCode("KR").holidayYear(2024).name("New Year").date(LocalDate.of(2024, 1, 1)).types("Public").build());
        holidayRepository.save(Holiday.builder().countryCode("KR").holidayYear(2024).name("Independence Movement Day").date(LocalDate.of(2024, 3, 1)).types("Public").build());

        // when
        mockMvc.perform(delete("/api/holidays")
                        .param("year", String.valueOf(year))
                        .param("country", countryCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // then
        // 실제 DB에서 데이터가 삭제되었는지 검증
        List<Holiday> deletedHolidays = holidayRepository.findByCountryCodeAndHolidayYear(countryCode, year);
        assertThat(deletedHolidays).isEmpty();
    }
}
