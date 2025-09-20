package com.example.coupleDiary.controller;

import com.example.coupleDiary.domain.DateWeather;
import com.example.coupleDiary.repository.DateWeatherRepository;
import com.example.coupleDiary.service.DiaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/diary")
public class DiaryController {

    private final DiaryService diaryService;
    private final DateWeatherRepository dateWeatherRepository;

    public DiaryController(DiaryService diaryService, DateWeatherRepository dateWeatherRepository) {
        this.diaryService = diaryService;
        this.dateWeatherRepository = dateWeatherRepository;
    }


    @GetMapping("/getWeather")
    public ResponseEntity<?> getWeather(@RequestParam("date")LocalDate date){
        DateWeather dw= diaryService.getDateWeather(date);
        return ResponseEntity.ok(dw);
    }
}
