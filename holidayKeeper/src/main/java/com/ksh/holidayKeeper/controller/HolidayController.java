package com.ksh.holidayKeeper.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ksh.holidayKeeper.common.ApiResponse;
import com.ksh.holidayKeeper.dto.HolidayDtos.*;
import com.ksh.holidayKeeper.service.HolidayService;

@RestController
@RequestMapping("/api/holidays")
@RequiredArgsConstructor
public class HolidayController {

    private final HolidayService holidayService;

    @GetMapping
    public ResponseEntity<ApiResponse<HolidayOffsetList>> search(
            @RequestParam String[] countries,
            @RequestParam Integer from,
            @RequestParam Integer to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "ASC") Sort.Direction direction,
            @RequestParam(required = false) String[] sortBy,
            @RequestParam(required = false) String type
    ) {
        HolidayOffsetList list = holidayService.searchByOffset(countries, from, to, type, page, size, direction, sortBy);

        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/search-cursor")
    public ResponseEntity<ApiResponse<HolidayCursorList>> searchCursor(
            @RequestParam String[] countries,
            @RequestParam Integer from,
            @RequestParam Integer to,
            @RequestParam(defaultValue = "0") long cursor,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String type
    ) {
        HolidayCursorList list = holidayService.searchByCursor(countries, from, to, type, cursor, size);

        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Void>> refresh(
            @RequestParam int year,
            @RequestParam String country
    ) {
        holidayService.upsertYearAndCountry(year, country);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> delete(
            @RequestParam int year,
            @RequestParam String country
    ) {
        holidayService.delete(country, year);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
