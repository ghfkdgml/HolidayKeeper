package com.ksh.holidayKeeper.service;

import com.ksh.holidayKeeper.api.NagerApiClient;
import com.ksh.holidayKeeper.common.ApiException;
import com.ksh.holidayKeeper.dto.HolidayDtos;
import com.ksh.holidayKeeper.entity.Holiday;
import com.ksh.holidayKeeper.repository.HolidayRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class HolidayServiceTest {

    @InjectMocks
    private HolidayService holidayService;

    @Mock
    private HolidayRepository holidayRepository;

    @Mock
    private NagerApiClient nagerApiClient;

    @Test
    @DisplayName("오프셋 기반 검색 성공")
    void searchByOffset_Success() {
        // given
        String[] countries = {"KR"};
        int from = 2024, to = 2024, page = 0, size = 10;
        Holiday holiday = Holiday.builder().id(1L).countryCode("KR").year(2024).name("New Year").date(LocalDate.of(2024, 1, 1)).build();
        Page<Holiday> holidayPage = new PageImpl<>(List.of(holiday));

        given(holidayRepository.findHolidayByPage(eq(countries), eq(from), eq(to), any(Pageable.class))).willReturn(holidayPage);

        // when
        HolidayDtos.HolidayOffsetList result = holidayService.searchByOffset(countries, from, to, page, size, Sort.Direction.ASC, new String[]{"date"});

        // then
        assertThat(result.items()).hasSize(1);
        assertThat(result.total()).isEqualTo(1);
        assertThat(result.items().get(0).name()).isEqualTo("New Year");
        verify(holidayRepository).findHolidayByPage(eq(countries), eq(from), eq(to), any(Pageable.class));
    }

    @Test
    @DisplayName("오프셋 기반 검색 시 허용되지 않은 정렬 기준으로 실패")
    void searchByOffset_FailWithInvalidSortBy() {
        // given
        String[] countries = {"KR"};
        int from = 2024, to = 2024, page = 0, size = 10;
        String[] sortBy = {"invalidField"};

        // when & then
        assertThatThrownBy(() -> holidayService.searchByOffset(countries, from, to, page, size, Sort.Direction.ASC, sortBy))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("허용되지 않는 정렬 기준입니다");
    }

    @Test
    @DisplayName("커서 기반 검색 성공")
    void searchByCursor_Success() {
        // given
        String[] countries = {"KR"};
        int from = 2024, to = 2024, size = 10;
        long cursorId = 0;
        Holiday holiday1 = Holiday.builder().id(1L).countryCode("KR").year(2024).name("New Year").date(LocalDate.of(2024, 1, 1)).build();
        Holiday holiday2 = Holiday.builder().id(2L).countryCode("KR").year(2024).name("Lunar New Year").date(LocalDate.of(2024, 2, 10)).build();
        List<Holiday> holidayList = List.of(holiday1, holiday2);

        given(holidayRepository.findHolidayByCursor(eq(countries), eq(from), eq(to), eq(cursorId), any(Pageable.class))).willReturn(holidayList);

        // when
        HolidayDtos.HolidayCursorList result = holidayService.searchByCursor(countries, from, to, cursorId, size);

        // then
        assertThat(result.items()).hasSize(2);
        assertThat(result.nextCursor()).isEqualTo(2L); // 마지막 아이템의 ID
        verify(holidayRepository).findHolidayByCursor(eq(countries), eq(from), eq(to), eq(cursorId), any(Pageable.class));
    }

    @Test
    @DisplayName("커서 기반 검색 결과가 없을 때 nextCursor는 -1")
    void searchByCursor_EmptyResult() {
        // given
        String[] countries = {"KR"};
        int from = 2024, to = 2024, size = 10;
        long cursorId = 100;
        given(holidayRepository.findHolidayByCursor(eq(countries), eq(from), eq(to), eq(cursorId), any(Pageable.class))).willReturn(List.of());

        // when
        HolidayDtos.HolidayCursorList result = holidayService.searchByCursor(countries, from, to, cursorId, size);

        // then
        assertThat(result.items()).isEmpty();
        assertThat(result.nextCursor()).isEqualTo(-1L);
    }

    @Test
    @DisplayName("특정 연도/국가 공휴일 Upsert 성공")
    void upsertYearAndCountry_Success() {
        // given
        int year = 2024;
        String countryCode = "KR";
        List<Map<String, Object>> holidaysFromApi = List.of(
                Map.of("date", "2024-01-01", "localName", "신정", "name", "New Year's Day", "type", "Public"),
                Map.of("date", "2024-03-01", "localName", "삼일절", "name", "Independence Movement Day", "type", "Public")
        );

        given(nagerApiClient.fetchHolidays(year, countryCode)).willReturn(holidaysFromApi);

        // when
        holidayService.upsertYearAndCountry(year, countryCode);

        // then
        verify(holidayRepository).deleteByCountryCodeAndYear(countryCode, year);
        verify(nagerApiClient).fetchHolidays(year, countryCode);
        verify(holidayRepository, times(2)).save(any(Holiday.class));
    }
}