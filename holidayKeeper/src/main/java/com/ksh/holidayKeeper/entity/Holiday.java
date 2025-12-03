package com.ksh.holidayKeeper.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "holidays",
    indexes = {
        @Index(name = "idx_holiday_search_and_sort", columnList = "countryCode, holidayYear, types, date")
    },
    uniqueConstraints = {    
        @UniqueConstraint(
            name = "UK_holiday_country_date",
            columnNames = {"countryCode", "date"}
        )})
public class Holiday {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String countryCode;
    private int holidayYear;

    private String localName;
    private String name;

    private LocalDate date;
    private String types;
}
