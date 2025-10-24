package com.example.coupleDiary.controller;

import com.example.coupleDiary.domain.Couple;
import com.example.coupleDiary.domain.MemberEntity;
import com.example.coupleDiary.repository.CoupleRepository;
import com.example.coupleDiary.service.CoupleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/couple")
@RequiredArgsConstructor
public class CoupleController {

    private final CoupleService coupleService;
    private final CoupleRepository coupleRepository;

    // 초대코드
    @PostMapping("/invite")
    public ResponseEntity<String> generateInviteCode(Authentication auth) {
        MemberEntity user = (MemberEntity) auth.getPrincipal();
        String code = coupleService.generateInviteCode(user.getUserId());
        return ResponseEntity.ok(code);
    }

    // 커플 연결
    @PostMapping("/accept")
    public ResponseEntity<?> acceptInvite(@RequestParam String code, Authentication auth) {
        MemberEntity user = (MemberEntity) auth.getPrincipal();
        try {
            coupleService.acceptInviteCode(code, user.getUserId());
            return ResponseEntity.ok("커플 연결 성공!");
        } catch (RuntimeException e) {
            System.out.println(e);
            return ResponseEntity.badRequest().body("커플 연결 실패: " + e.getMessage());
        }
    }

    //커플 정보 (기념일) 수정
    @PostMapping("/updateAnniversary")
    public ResponseEntity<?> updateAnniversary(@RequestBody Map<String,String> body){
        String dateStr = body.get("anniversary");
        LocalDateTime anniversary = LocalDate.parse(dateStr).atStartOfDay();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        MemberEntity member = (MemberEntity) auth.getPrincipal();

        Couple couple = coupleRepository
                .findById(member.getCoupleId())
                .orElseThrow(() -> new RuntimeException("커플정보 없음"));

        couple.setAnniversary(anniversary);
        coupleRepository.save(couple);

        return ResponseEntity.ok("기념일 수정 완료");
    }
}
