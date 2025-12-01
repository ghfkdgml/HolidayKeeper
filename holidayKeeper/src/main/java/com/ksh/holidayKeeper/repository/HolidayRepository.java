package com.ksh.holidayKeeper.repository;


import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ksh.holidayKeeper.entity.Holiday;

import java.util.List;

public interface HolidayRepository extends JpaRepository<Holiday, Long> {

    List<Holiday> findByCountryCodeAndHolidayYear(String countryCode, int year);

    void deleteByCountryCodeAndHolidayYear(String countryCode, int year);

    @Query("""
        select i from Holiday i
        where i.countryCode in :countryCodes
            and i.holidayYear >= :from
            and i.holidayYear <= :to            
        """)
    Page<Holiday> findHolidayByPage(@Param("countryCodes") String[] countryCodes,
                            @Param("from") int from,
                            @Param("to") int to,
                            Pageable pageable);

   
    @Query("""
        select i from Holiday i
        where i.countryCode in :countryCodes
            and i.holidayYear >= :from
            and i.holidayYear <= :to
            and i.id > :cursorId
        """)
    List<Holiday> findHolidayByCursor(@Param("countryCodes") String[] countryCodes,
                                      @Param("from") int from,
                                      @Param("to") int to,
                                      @Param("cursorId") long cursorId,
                                      Pageable pageable);
}