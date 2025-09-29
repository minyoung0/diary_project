package com.example.coupleDiary.controller;

import com.example.coupleDiary.domain.MemberEntity;
import com.example.coupleDiary.service.CoupleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/couple")
@RequiredArgsConstructor
public class CoupleController {

    private final CoupleService coupleService;

    // 1) 초대코드 생성
    @PostMapping("/invite")
    public ResponseEntity<String> generateInviteCode(Authentication auth) {
        MemberEntity user = (MemberEntity) auth.getPrincipal();
        String code = coupleService.generateInviteCode(user.getUserId());
        return ResponseEntity.ok(code);
    }

    // 2) 초대코드 수락
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
}
