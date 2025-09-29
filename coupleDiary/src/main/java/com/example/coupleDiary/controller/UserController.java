package com.example.coupleDiary.controller;

import com.example.coupleDiary.domain.Auth;
import com.example.coupleDiary.domain.MemberEntity;
import com.example.coupleDiary.security.TokenProvider;
import com.example.coupleDiary.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final MemberService memberService;

    private final TokenProvider tokenProvider;
    private final RedisTemplate redisTemplate;

    @PostMapping("/signup")
    public String signup(@ModelAttribute Auth.SignUp request, RedirectAttributes ra) {
        try {
            memberService.register(request);
            ra.addFlashAttribute("msg", "회원가입 완료! 로그인해 주세요.");
            return "redirect:/loginPage";
        }catch (RuntimeException e){
            ra.addFlashAttribute("errormessage", e.getMessage());
            ra.addFlashAttribute("prev", request);
            System.out.println(e.getMessage());
            return "redirect:/joinPage";
        }

    }

    @PostMapping("/signin")
    public ResponseEntity<Object> signin(@RequestBody Auth.SignIn request){
        var member = memberService.authenticate(request);
        String userId= member.getUserId();

        //Access Token
        String token = tokenProvider.generateToken(userId);

        //Refresh Token
        String refreshToken= tokenProvider.generateRefreshToken(userId);

        //Refresh Token Redis 저장(1일 유효)
        redisTemplate.opsForValue().set(
                "refresh:"+userId,
                refreshToken,
                1, TimeUnit.DAYS
        );

        //assessToken 쿠키저장
        ResponseCookie accessCookie = ResponseCookie.from("access_token", token)
                .httpOnly(true)       // JavaScript에서 접근 못하게 (보안)
                .secure(false)        // ★ localhost(HTTP) 환경에서는 false, 배포(HTTPS)에서는 true
                .sameSite("Lax")      // CSRF 방지 기본 설정
                .path("/")            // ★ 꼭 루트 경로
                .maxAge(Duration.ofHours(2))
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token",refreshToken)
                .httpOnly(true)
                .secure(false)
                .sameSite("Strict")
                .path("/")
                .maxAge(Duration.ofDays(7))
                .build();


        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(){
        ResponseCookie del= ResponseCookie.from("access_token","")
                .httpOnly(true).secure(true).sameSite("Lax").path("/").maxAge(0).build();
        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE,del.toString()).build();
    }

   @GetMapping("/CheckAuth")
   public ResponseEntity<?> me() {
       Authentication auth = SecurityContextHolder.getContext().getAuthentication();
       System.out.println("[CheckAuth] auth=" + auth);

       // anonymous 방어
       if (auth == null || !auth.isAuthenticated()
               || "anonymousUser".equals(String.valueOf(auth.getPrincipal()))) {
           return ResponseEntity.status(401).build();
       }

       MemberEntity user = (MemberEntity) auth.getPrincipal();

       Map<String, Object> result = new HashMap<>();
       result.put("userId", user.getUserId());
       result.put("coupleId", user.getCoupleId());

       return ResponseEntity.ok(result);
   }

}
