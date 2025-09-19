package com.example.coupleDiary.domain;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity(name="date_weather")
@NoArgsConstructor
@AllArgsConstructor
public class DateWeather {
    @Id
    private LocalDate date;
    private String weather;
    private double temperature;
}
