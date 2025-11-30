package com.ksh.holidayKeeper.dto;

import java.time.Instant;
import java.util.List;

public class HolidayDtos {
    
    public record HolidayItem(Long id, String countryCode, int year, String localName, String name, Instant date, String type) {}
    public record HolidayOffsetList(List<HolidayItem> items, long total, int page, int size) {}
    public record HolidayCursorList(List<HolidayItem> items, long nextCursor) {}
}
