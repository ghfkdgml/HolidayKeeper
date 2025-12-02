package com.ksh.holidayKeeper.controller;

import com.ksh.holidayKeeper.config.TestMockConfig;
import com.ksh.holidayKeeper.dto.HolidayDtos;
import com.ksh.holidayKeeper.service.HolidayService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HolidayController.class)
@Import(TestMockConfig.class)
class HolidayControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private HolidayService holidayService;

    @Test
    @DisplayName("GET /api/holidays - 오프셋 페이징 검색 성공")
    void search_OffsetPaging_Success() throws Exception {
        // given
        String[] countries = {"KR"};
        int from = 2024, to = 2024, page = 0, size = 10;        
        Sort.Direction direction = Sort.Direction.ASC;

        HolidayDtos.HolidayItem item = new HolidayDtos.HolidayItem(1L, "KR", 2024, "신정", "New Year", Instant.now(), "Public");
        HolidayDtos.HolidayOffsetList responseDto = new HolidayDtos.HolidayOffsetList(List.of(item), 1, page, size);

        given(holidayService.searchByOffset(eq(countries), eq(from), eq(to), isNull(), eq(page), eq(size), eq(direction), isNull()))
                .willReturn(responseDto);

        // when & then
        mockMvc.perform(get("/api/holidays")
                        .param("countries", "KR")
                        .param("from", "2024")
                        .param("to", "2024")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items[0].countryCode").value("KR"))
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    @DisplayName("GET /api/holidays/search-cursor - 커서 페이징 검색 성공")
    void search_CursorPaging_Success() throws Exception {
        // given
        String[] countries = {"KR"};
        int from = 2024, to = 2024, size = 10;
        long cursor = 0;

        HolidayDtos.HolidayItem item = new HolidayDtos.HolidayItem(1L, "KR", 2024, "신정", "New Year", Instant.now(), "Public");
        HolidayDtos.HolidayCursorList responseDto = new HolidayDtos.HolidayCursorList(List.of(item), 1L);

        given(holidayService.searchByCursor(eq(countries), eq(from), eq(to), isNull(), eq(cursor), eq(size))).willReturn(responseDto);

        // when & then
        mockMvc.perform(get("/api/holidays/search-cursor")
                        .param("countries", "KR")
                        .param("from", "2024")
                        .param("to", "2024")
                        .param("cursor", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items[0].countryCode").value("KR"))
                .andExpect(jsonPath("$.data.nextCursor").value(1L));
    }

    @Test
    @DisplayName("POST /api/holidays/refresh - 공휴일 데이터 갱신 성공")
    void refresh_Success() throws Exception {
        // given
        int year = 2024;
        String country = "KR";

        // when & then
        mockMvc.perform(post("/api/holidays/refresh")
                        .param("year", String.valueOf(year))
                        .param("country", country))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(holidayService).upsertYearAndCountry(year, country);
    }

    @Test
    @DisplayName("DELETE /api/holidays - 공휴일 데이터 삭제 성공")
    void delete_Success() throws Exception {
        // given
        int year = 2024;
        String country = "KR";

        // when & then
        mockMvc.perform(delete("/api/holidays")
                        .param("year", String.valueOf(year))
                        .param("country", country))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(holidayService).delete(country, year);
    }
}
