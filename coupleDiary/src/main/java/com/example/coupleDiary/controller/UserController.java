package com.example.coupleDiary.controller;

import com.example.coupleDiary.domain.Auth;
import com.example.coupleDiary.domain.MemberEntity;
import com.example.coupleDiary.security.TokenProvider;
import com.example.coupleDiary.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final MemberService memberService;

    private final TokenProvider tokenProvider;
    private final RedisTemplate redisTemplate;

    @PostMapping("/signup")
    public String signup(@ModelAttribute Auth.SignUp request, RedirectAttributes ra,
                         @RequestPart(value="profileImg", required=false) MultipartFile profileImg) {
        try {
            String savePath = "/profileImg/img.png";

            if (profileImg != null && !profileImg.isEmpty()) {
                String uploadDir = "c:/uploads/profile/";
                File dir = new File(uploadDir);
                if (!dir.exists()) dir.mkdirs(); // ✅ 폴더 자동 생성

                String fileName = UUID.randomUUID() + "_" + profileImg.getOriginalFilename();
                Path filePath = Paths.get(uploadDir, fileName);
                Files.copy(profileImg.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                // ✅ 여기 수정 — 실제로 저장된 파일명으로 웹 접근 경로를 저장해야 함
                savePath = "/profileImg/" + fileName;
            }

            request.setProfileImgPath(savePath); // ✅ 이제 올바른 경로가 들어감
            memberService.register(request);

            ra.addFlashAttribute("msg", "회원가입 완료! 로그인해 주세요.");
            return "redirect:/loginPage";
        } catch (Exception e) {
            ra.addFlashAttribute("errormessage", e.getMessage());
            ra.addFlashAttribute("prev", request);
            e.printStackTrace();
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

    @GetMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();

        // Redis에서 refresh 토큰 제거
        redisTemplate.delete("refresh:" + userId);

        // 쿠키 제거
        ResponseCookie delAccess = ResponseCookie.from("access_token", "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();

        ResponseCookie delRefresh = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, delAccess.toString())
                .header(HttpHeaders.SET_COOKIE, delRefresh.toString())
                .build();
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
