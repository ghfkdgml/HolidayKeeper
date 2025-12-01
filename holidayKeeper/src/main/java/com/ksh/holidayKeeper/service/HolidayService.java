package com.ksh.holidayKeeper.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;

import com.ksh.holidayKeeper.api.NagerApiClient;
import com.ksh.holidayKeeper.common.ApiException;
import com.ksh.holidayKeeper.dto.HolidayDtos.HolidayItem;
import com.ksh.holidayKeeper.dto.HolidayDtos.HolidayOffsetList;
import com.ksh.holidayKeeper.dto.HolidayDtos.HolidayCursorList;
import com.ksh.holidayKeeper.entity.Holiday;
import com.ksh.holidayKeeper.repository.HolidayRepository;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HolidayService {

    private final HolidayRepository holidayRepository;
    private final NagerApiClient nagerApiClient;

    //#. 최초 전체 저장
    @Transactional
    public void loadInitialData() {
        List<Map<String, Object>> countries = nagerApiClient.fetchCountries();

        //#. 현재 기준 5년간
        int endYear = LocalDate.now().getYear();
        int startYear = endYear - 5;

        for (int year = startYear; year <= endYear; year++) {
            for (Map<String, Object> c : countries) {
                String countryCode = (String) c.get("countryCode");
                upsertYearAndCountry(year, countryCode);
            }
        }
    }

    //#. 특정 연도/국가 공휴일 Upsert
    @Transactional
    public void upsertYearAndCountry(int year, String countryCode) {
        holidayRepository.deleteByCountryCodeAndHolidayYear(countryCode, year);

        List<Map<String, Object>> holidays = nagerApiClient.fetchHolidays(year, countryCode);

        for (Map<String, Object> h : holidays) {
            holidayRepository.save(
                    Holiday.builder()
                            .countryCode(countryCode)
                            .holidayYear(year)
                            .localName((String) h.get("localName"))
                            .name((String) h.get("name"))
                            .date(LocalDate.parse((String) h.get("date")))
                            .type((String) h.get("type"))
                            .build()
            );
        }
    }

    public HolidayOffsetList searchByOffset(String[] countries, Integer from, Integer to, int page, int size, Sort.Direction direction, String[] sortBy) {
        String[] sortProperties;
        if (sortBy == null || sortBy.length == 0) {
            //#. 기본 정렬
            sortProperties = new String[]{"countryCode", "holidayYear", "date"};
        } else {
            //#. 정렬 가능한 필드 화이트리스트
            List<String> allowedSortFields = List.of("countryCode", "holidayYear", "name", "localName", "date", "type");
            for (String field : sortBy) {
                if (!allowedSortFields.contains(field)) {
                    throw new ApiException(HttpStatus.BAD_REQUEST, "허용되지 않는 정렬 기준입니다: " + field);
                }
            }
            sortProperties = sortBy;
        }

        Sort sort = Sort.by(direction, sortProperties);
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), sort);

        Page<Holiday> data = holidayRepository.findHolidayByPage(countries, from, to, pageable);

        List<HolidayItem> items = data.getContent().stream().map(this::toItem).toList();
        return new HolidayOffsetList(items, data.getTotalElements(), page, size);
    }

    public HolidayCursorList searchByCursor(String[] countries, Integer from, Integer to, long cursorId, int size) {
        // 커서 기반 페이징에서는 id 오름차순 정렬이 필수적입니다.
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        Pageable pageable = PageRequest.of(0, Math.min(size, 100), sort);

        List<Holiday> data = holidayRepository.findHolidayByCursor(countries, from, to, cursorId, pageable);

        List<HolidayItem> items = data.stream().map(this::toItem).toList();

        long nextCursor = -1L;
        if (!items.isEmpty()) {
            // 마지막 아이템의 ID를 다음 커서로 사용
            nextCursor = items.get(items.size() - 1).id();
        }

        return new HolidayCursorList(items, nextCursor);
    }


    @Transactional
    public void delete(String country, Integer year) {
        holidayRepository.deleteByCountryCodeAndHolidayYear(country, year);
    }


    private HolidayItem toItem(Holiday a) {
        return new HolidayItem(a.getId(), a.getCountryCode(), a.getHolidayYear(), a.getLocalName(), a.getName(), a.getDate().atStartOfDay().toInstant(ZoneOffset.UTC), a.getType());
    }    
}
