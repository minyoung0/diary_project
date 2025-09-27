package com.example.coupleDiary.domain;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity(name="date_weather")
@NoArgsConstructor
@AllArgsConstructor
@Table(name="tb_weatherlog")
public class DateWeather {
    @Id
    @Column(name="date")
    private LocalDate date;
    private String weather;
    @Column(name="temperature")
    private double temperature;
}
