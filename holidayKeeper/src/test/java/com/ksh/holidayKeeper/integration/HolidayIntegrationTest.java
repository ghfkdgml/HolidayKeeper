package com.ksh.holidayKeeper.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.ksh.holidayKeeper.api.NagerApiClient;
import com.ksh.holidayKeeper.entity.Holiday;
import com.ksh.holidayKeeper.repository.HolidayRepository;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class HolidayIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private HolidayRepository holidayRepository;

    @MockBean
    private NagerApiClient nagerApiClient;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 미리 저장
        holidayRepository.save(Holiday.builder()
                .countryCode("KR").year(2024).name("New Year").date(LocalDate.of(2024, 1, 1)).type("Public").build());
        holidayRepository.save(Holiday.builder()
                .countryCode("KR").year(2024).name("Independence Movement Day").date(LocalDate.of(2024, 3, 1)).type("Public").build());
        holidayRepository.save(Holiday.builder()
                .countryCode("US").year(2024).name("New Year").date(LocalDate.of(2024, 1, 1)).type("Public").build());

        // Mock API Client
        given(nagerApiClient.fetchCountries()).willReturn(List.of());
        given(nagerApiClient.fetchHolidays(anyInt(), anyString())).willReturn(List.of());
    }

    @Test
    @DisplayName("통합 테스트: GET /api/holidays - 오프셋 페이징 검색")
    void search_OffsetPaging_Integration() throws Exception {
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
}
