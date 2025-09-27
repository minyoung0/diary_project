package com.example.coupleDiary.controller;

import com.example.coupleDiary.domain.DateWeather;
import com.example.coupleDiary.domain.Diary;
import com.example.coupleDiary.repository.DateWeatherRepository;
import com.example.coupleDiary.repository.DiaryRepository;
import com.example.coupleDiary.service.DiaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/diary")
public class DiaryController {

    private final DiaryService diaryService;
    private final DateWeatherRepository dateWeatherRepository;
    private final DiaryRepository diaryRepository;


    public DiaryController(DiaryService diaryService, DateWeatherRepository dateWeatherRepository, DiaryRepository diaryRepository) {
        this.diaryService = diaryService;
        this.dateWeatherRepository = dateWeatherRepository;
        this.diaryRepository = diaryRepository;
    }


    @GetMapping("/getWeather")
    public ResponseEntity<?> getWeather(@RequestParam("date")LocalDate date){
        System.out.println(date);
        DateWeather dw= diaryService.getDateWeather(date);
        return ResponseEntity.ok(dw);
    }

    @PostMapping("/save")
    public ResponseEntity<Diary> saveDiary(@RequestBody Diary diary, Principal principal) {
        try{
            String username= principal.getName();
            System.out.println("글쓴사람: "+username);
            LocalDate today = LocalDate.now();
            diary.setCreatedAt(today);
            Diary saved = diaryService.saveDiary(diary);
            return ResponseEntity.ok(saved);
        }catch (Exception e){
            System.out.println(e);
            throw new RuntimeException();
        }

    }

    @GetMapping("/events")
    public List<Map<String,Object>> getDiary(){
        List<Diary> diaryList = diaryRepository.findAll();
        List<Map<String,Object>> allDiaries = new ArrayList<>();

        for(Diary d : diaryList){
            Map<String,Object> diary = new HashMap<>();
            diary.put("title", "📙일기 : " + d.getUserId());
            diary.put("content",d.getContent());
            diary.put("writer",d.getUserId());
            diary.put("id",d.getDiaryId());
            diary.put("mood",d.getMood());
            diary.put("weather",d.getWeather());
            diary.put("date",d.getCreatedAt());
            allDiaries.add(diary);
        }
        return allDiaries;
    }
}
