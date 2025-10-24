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


    //날씨 불러오기
    @GetMapping("/getWeather")
    public ResponseEntity<?> getWeather(@RequestParam("date") LocalDate date) {
        try {
            System.out.println("[getWeather]" + date);
            DateWeather dw = diaryService.getDateWeather(date);
            return ResponseEntity.ok(dw);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("날씨불러오기 실패");
        }

    }

    //일기 저장
    @PostMapping("/save")
    public ResponseEntity<Diary> saveDiary(@RequestBody Diary diary, Principal principal) {
        try {
            String username = principal.getName();
            System.out.println("글쓴사람: " + username);
            LocalDate today = LocalDate.now();
            diary.setCreatedAt(today);
            Diary saved = diaryService.saveDiary(diary);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            System.out.println(e);
            throw new RuntimeException();
        }

    }

    //모든 다이어리 불러오기
    @GetMapping("/events")
    public ResponseEntity<?> getAllDiary() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            MemberEntity user = (MemberEntity) auth.getPrincipal();

            Integer coupleId = user.getCoupleId();
            List<String> userIds;
            if (coupleId >= 0 && coupleId != null) {
                userIds = memberRepository.findUserIdsByCoupleId(coupleId);
            } else {
                userIds = List.of(user.getUserId());
            }
            List<Map<String, Object>> allDiaries = diaryRepository.findDiariesWithNickname(userIds);
            List<Map<String, Object>> diaryList = new ArrayList<>();

            for (Map<String, Object> row : allDiaries) {
                Map<String, Object> diary = new HashMap<>();
                String nickname = (String) row.get("nickname");
                diary.put("title", nickname + " : " + row.get("content"));
                diary.put("content", row.get("content"));
                diary.put("writer", row.get("user_id"));
                diary.put("id", row.get("diary_id"));
                diary.put("mood", row.get("mood"));
                diary.put("weather", row.get("weather"));
                diary.put("date", row.get("created_at"));
                String createdAt = row.get("created_at").toString();
                String dateOnly = createdAt.contains("T") ? createdAt.split("T")[0] : createdAt;
                diary.put("start", dateOnly);
                diary.put("allDay", true);
                diary.put("backgroundColor",
                        row.get("user_id").equals(user.getUserId()) ? "#ffb896" : "#b8d9ff");
                diaryList.add(diary);
            }
            return ResponseEntity.ok(diaryList);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Collections.emptyList());
        }
    }

    //특정 날짜 일기 불러오기
    @GetMapping("/getDiary/{diaryId}")
    public ResponseEntity<?> getDiaryById(@PathVariable("diaryId") int diaryId) {
        try {
            Diary diary = diaryRepository.findByDiaryId(diaryId);
            return ResponseEntity.ok(diary);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("일기불러오기 실패");
        }
    }

    //일기 내용 수정
    @PostMapping("/update")
    public ResponseEntity<?> updateDiary(@RequestBody Diary req) {
        var diary = diaryRepository.findById(req.getDiaryId()).orElse(null);
        if (diary == null) {
            return ResponseEntity.badRequest().body("존재하지 않거나 삭제된 일기입니다");
        }

        diary.setContent(req.getContent());
        diaryRepository.save(diary);

        return ResponseEntity.ok("일기 수정 완료!");
    }

    //일기 삭제 (is_deleted =1 로 수정)
    @PostMapping("/delete/{diaryId}")
    public ResponseEntity<?> deleteDiary(@PathVariable int diaryId){
        var diary = diaryRepository.findById(diaryId).orElse(null);
        if (diary == null) {
            return ResponseEntity.badRequest().body("일기를 찾을 수 없습니다.");
        }

        diary.setIsDeleted(1);
        diaryRepository.save(diary);
        return ResponseEntity.ok("삭제 완료");
    }
}
