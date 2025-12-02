package com.ksh.holidayKeeper.repository;


import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ksh.holidayKeeper.entity.Holiday;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HolidayRepository extends JpaRepository<Holiday, Long> {

    Optional<Holiday> findByCountryCodeAndDate(String countryCode, LocalDate date);
    List<Holiday> findByCountryCodeAndHolidayYear(String countryCode, int year);

    void deleteByCountryCodeAndHolidayYear(String countryCode, int year);

    @Query("""
        select i from Holiday i
        where i.countryCode in :countryCodes
            and (:from is null or i.holidayYear >= :from)
            and (:to is null or i.holidayYear <= :to)
            and (:type is null or i.types like %:type%)
        """)
    Page<Holiday> findHolidayByPage(@Param("countryCodes") String[] countryCodes,
                            @Param("from") Integer from,
                            @Param("to") Integer to,
                            @Param("type") String type,
                            Pageable pageable);

   
    @Query("""
        select i from Holiday i
        where i.countryCode in :countryCodes
            and (:from is null or i.holidayYear >= :from)
            and (:to is null or i.holidayYear <= :to)
            and (:type is null or i.types like %:type%)
            and i.id > :cursorId
        """)
    List<Holiday> findHolidayByCursor(@Param("countryCodes") String[] countryCodes,
                                      @Param("from") Integer from,
                                      @Param("to") Integer to,
                                      @Param("type") String type,
                                      @Param("cursorId") long cursorId,
                                      Pageable pageable);
}