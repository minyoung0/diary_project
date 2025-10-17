package com.example.coupleDiary.domain;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name="tb_weatherlog")
public class DateWeather {
    @Id
    @Column(name="date", nullable=false)
    private LocalDate date;

    private String weather;
    private double temperature;
}
