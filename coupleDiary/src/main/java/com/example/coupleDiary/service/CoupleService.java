package com.example.coupleDiary.service;

import com.example.coupleDiary.domain.Couple;
import com.example.coupleDiary.domain.MemberEntity;
import com.example.coupleDiary.repository.CoupleRepository;
import com.example.coupleDiary.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class CoupleService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final CoupleRepository coupleRepository;
    private final MemberRepository memberRepository;


    // 1) 초대코드 생성 & Redis 저장
    public String createInviteCode(String userId) {
        String inviteCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        // TTL 10분짜리 초대 코드 저장
        redisTemplate.opsForValue().set("invite:" + inviteCode, userId, 10, TimeUnit.MINUTES);

        return inviteCode;
    }

    @Transactional
    public String generateInviteCode(String userId){
        // 6자리 랜덤 코드 생성
        String inviteCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        // Redis에 저장 (TTL 10분)
        redisTemplate.opsForValue().set("invite:" + inviteCode, userId, 10, TimeUnit.MINUTES);

        System.out.println("[CoupleService] 초대코드 생성: " + inviteCode + " (user=" + userId + ")");
        return inviteCode;
    }

    @Transactional
    public void acceptInviteCode(String inviteCode, String targetUserId){

            String userId= (String) redisTemplate.opsForValue().get("invite:"+inviteCode);

            if(userId==null){
                throw new RuntimeException("초대코드가 만료되었거나 잘못되었습니다");
            }

            //DB에 커플 저장
            Couple couple = new Couple();
            couple.setUser1Id(userId);
            couple.setUser2Id(targetUserId);
            couple.setCreatedAt(LocalDateTime.now());
            Couple savedCouple=coupleRepository.save(couple);

            memberRepository.updateCoupleId(userId,savedCouple.getCoupleId());
            memberRepository.updateCoupleId(targetUserId, savedCouple.getCoupleId());

            // 초대코드 삭제
            redisTemplate.delete("invite:" + inviteCode);

        System.out.println("[CoupleService] 커플 연결 성공: " + userId + " ❤️ " + targetUserId);


    }
}
