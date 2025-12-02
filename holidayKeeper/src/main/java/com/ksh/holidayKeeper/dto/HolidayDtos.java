package com.ksh.holidayKeeper.dto;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.util.List;

public class HolidayDtos {

    public record HolidayItem(Long id, String countryCode, int year, String localName, String name, Instant date, String types) {}
    public record HolidayOffsetList(List<HolidayItem> items, long total, int page, int size) {}
    public record HolidayCursorList(List<HolidayItem> items, long nextCursor) {}

    @Getter
    @Setter
    public static class HolidaySearchRequest {
        private String[] countries;
        private Integer from;
        private Integer to;
        private int page = 0;
        private int size = 10;
        private Sort.Direction direction = Sort.Direction.ASC;
        @Parameter(description = "정렬할 필드 이름. (예: date, name)")
        private String[] sortBy;
        private String type;
    }

    @Getter
    @Setter
    public static class HolidayCursorSearchRequest extends HolidaySearchRequest {
        private long cursor = 0;
    }
}
