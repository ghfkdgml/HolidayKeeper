package com.ksh.holidayKeeper.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ksh.holidayKeeper.common.ApiResponse;
import com.ksh.holidayKeeper.dto.HolidayDtos.*;
import com.ksh.holidayKeeper.service.HolidayService;

import org.springdoc.core.annotations.ParameterObject;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/holidays")
@RequiredArgsConstructor
public class HolidayController {

    private final HolidayService holidayService;

    @Operation(summary = "공휴일 목록 검색 (오프셋 페이징)", description = "다양한 조건으로 공휴일 목록을 검색합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<HolidayOffsetList>> search(@ParameterObject HolidaySearchRequest request) {
        HolidayOffsetList list = holidayService.searchByOffset(request.getCountries(), request.getFrom(), request.getTo(), request.getType(), request.getPage(), request.getSize(), request.getDirection(), request.getSortBy());

        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @Operation(summary = "공휴일 목록 검색 (커서 페이징)", description = "커서 ID를 기반으로 다음 페이지의 공휴일 목록을 검색합니다.")
    @GetMapping("/search-cursor")
    public ResponseEntity<ApiResponse<HolidayCursorList>> searchCursor(@ParameterObject HolidayCursorSearchRequest request) {
        HolidayCursorList list = holidayService.searchByCursor(request.getCountries(),
                request.getFrom(),
                request.getTo(),
                request.getType(),
                request.getCursor(),
                request.getSize());

        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @Operation(summary = "특정 연도/국가 공휴일 데이터 갱신", description = "Nager API로부터 최신 공휴일 정보를 가져와 DB에 업데이트(upsert)합니다.")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Void>> refresh(
            @RequestParam int year,
            @RequestParam String country
    ) {
        holidayService.upsertYearAndCountry(year, country);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @Operation(summary = "특정 연도/국가 공휴일 데이터 삭제", description = "DB에서 특정 연도와 국가에 해당하는 모든 공휴일 데이터를 삭제합니다.")
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> delete(
            @RequestParam int year,
            @RequestParam String country
    ) {
        holidayService.delete(country, year);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
