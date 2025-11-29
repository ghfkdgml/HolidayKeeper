package com.ksh.holidayKeeper.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.ksh.holidayKeeper.entity.Holiday;

import java.util.List;

public interface HolidayRepository extends JpaRepository<Holiday, Long> {

    List<Holiday> findByCountryCodeAndYear(String countryCode, int year);

    void deleteByCountryCodeAndYear(String countryCode, int year);
}