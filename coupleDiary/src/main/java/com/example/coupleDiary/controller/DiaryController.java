package com.example.coupleDiary.controller;

import com.example.coupleDiary.domain.DateWeather;
import com.example.coupleDiary.domain.Diary;
import com.example.coupleDiary.domain.MemberEntity;
import com.example.coupleDiary.repository.DateWeatherRepository;
import com.example.coupleDiary.repository.DiaryRepository;
import com.example.coupleDiary.repository.MemberRepository;
import com.example.coupleDiary.service.DiaryService;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/diary")
public class DiaryController {

    private final DiaryService diaryService;
    private final DateWeatherRepository dateWeatherRepository;
    private final DiaryRepository diaryRepository;
    private final MemberRepository memberRepository;


    public DiaryController(DiaryService diaryService, DateWeatherRepository dateWeatherRepository, DiaryRepository diaryRepository, MemberRepository memberRepository) {
        this.diaryService = diaryService;
        this.dateWeatherRepository = dateWeatherRepository;
        this.diaryRepository = diaryRepository;
        this.memberRepository = memberRepository;
    }


    @GetMapping("/getWeather")
    public ResponseEntity<?> getWeather(@RequestParam("date")LocalDate date){
        try {
            System.out.println("[getWeather]"+date);
            DateWeather dw= diaryService.getDateWeather(date);
            return ResponseEntity.ok(dw);
        }catch(Exception e){
            System.out.println(e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("날씨불러오기 실패");
        }

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
    public ResponseEntity<?> getAllDiary(){
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            MemberEntity user = (MemberEntity) auth.getPrincipal();

            int coupleId =  user.getCoupleId();
            List<String> userIds;
            if(coupleId >=0 ){
                userIds = memberRepository.findUserIdsByCoupleId(coupleId);
            }else{
                userIds = List.of(user.getUserId());
            }
            List<Diary> allDiaries = diaryRepository.findByUserIdIn(userIds);
            List<Map<String, Object>> diaryList = new ArrayList<>();
            for(Diary d : allDiaries){
                Map<String,Object> diary = new HashMap<>();
                diary.put("title", d.getUserId());
                diary.put("content","📙일기 : " + d.getContent());
                diary.put("writer",d.getUserId());
                diary.put("id",d.getDiaryId());
                diary.put("mood",d.getMood());
                diary.put("weather",d.getWeather());
                diary.put("date",d.getCreatedAt());
                diary.put("start", d.getCreatedAt().toString());
                diary.put("backgroundColor",
                        d.getUserId().equals(user.getUserId()) ? "#ffb896" : "#b8d9ff");

                diaryList.add(diary);
            }
            return ResponseEntity.ok(diaryList);
        }catch(Exception e){
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Collections.emptyList());
        }

    }
    @GetMapping("/getDiary/{diaryId}")
    public ResponseEntity<?> getDiaryById(@PathVariable("diaryId") int diaryId){
        try {
            Diary diary = diaryRepository.findByDiaryId(diaryId);
            return ResponseEntity.ok(diary);
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.badRequest().body("일기불러오기 실패");
        }
    }
}
