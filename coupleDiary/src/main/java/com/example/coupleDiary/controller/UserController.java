package com.example.coupleDiary.controller;

import com.example.coupleDiary.domain.Auth;
import com.example.coupleDiary.domain.MemberEntity;
import com.example.coupleDiary.repository.CoupleRepository;
import com.example.coupleDiary.repository.MemberRepository;
import com.example.coupleDiary.security.TokenProvider;
import com.example.coupleDiary.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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
    private final MemberRepository memberRepository;
    private final CoupleRepository coupleRepository;
    private final PasswordEncoder passwordEncoder;

    //회원가입
    @PostMapping("/signup")
    public String signup(@ModelAttribute Auth.SignUp request, RedirectAttributes ra,
                         @RequestPart(value="profileImg", required=false) MultipartFile profileImg) {
        try {
            String savePath = "/profileImg/img.png";

            if (profileImg != null && !profileImg.isEmpty()) {
                String uploadDir = "c:/uploads/profile/";
                File dir = new File(uploadDir);
                if (!dir.exists()) dir.mkdirs();

                String fileName = UUID.randomUUID() + "_" + profileImg.getOriginalFilename();
                Path filePath = Paths.get(uploadDir, fileName);
                Files.copy(profileImg.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                savePath = "/profileImg/" + fileName;
            }

            request.setProfileImgPath(savePath);
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

    //로그인
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
                .secure(false)        // localhost(HTTP) 환경에서는 false, 배포(HTTPS)에서는 true
                .sameSite("Lax")      // CSRF 방지 기본 설정
                .path("/")            // 꼭 루트 경로
                .maxAge(Duration.ofHours(2))
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token",refreshToken)
                .httpOnly(true)
                .secure(false)
                .sameSite("Strict")
                .path("/")
                .maxAge(Duration.ofDays(7))
                .build();
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("userId", member.getUserId());
        responseData.put("nickname", member.getNickname());
        responseData.put("nickname", member.getNickname());
        responseData.put("profileImg",
                member.getProfileImg() != null ? member.getProfileImg() : "/profileImg/img.png");
        responseData.put("coupleId", member.getCoupleId());

        Integer coupleId = member.getCoupleId();
        if (coupleId != null && coupleId > 0) {
            var partner = memberRepository.findPartnerByCoupleId(coupleId, member.getUserId());
            if (partner != null) {
                responseData.put("coupleId", coupleId);
                responseData.put("partnerNickname", partner.getNickname());
                responseData.put("partnerProfileImg",
                        partner.getProfileImg() != null ? partner.getProfileImg() : "/profileImg/img.png");
            }

            var couple = coupleRepository.findById(coupleId).orElse(null);
            if (couple != null && couple.getAnniversary() != null) {
                long days = ChronoUnit.DAYS.between(couple.getAnniversary().toLocalDate(), LocalDate.now());
                responseData.put("dayCount", days);
            }
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .build();
    }


    //로그아웃
    @GetMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            String userId = auth.getName();
            redisTemplate.delete("refresh:" + userId);
        }
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

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, delAccess.toString())
                .header(HttpHeaders.SET_COOKIE, delRefresh.toString())
                .body("logout success");
    }


    //권한체크
   @GetMapping("/CheckAuth")
   public ResponseEntity<?> me() {
       Authentication auth = SecurityContextHolder.getContext().getAuthentication();
       System.out.println("[CheckAuth] auth=" + auth);

       // anonymous 방어
       if (auth == null || !auth.isAuthenticated()
               || "anonymousUser".equals(String.valueOf(auth.getPrincipal()))) {
           return ResponseEntity.status(401).build();
       }

       MemberEntity member = (MemberEntity) auth.getPrincipal();

       Map<String, Object> response = new HashMap<>();
       response.put("userId", member.getUserId());
       response.put("nickname", member.getNickname());
       response.put("profileImg", member.getProfileImg() != null ? member.getProfileImg() : "/profileImg/img.png");
       response.put("coupleId", member.getCoupleId());

       if (member.getCoupleId()!=null && member.getCoupleId() >0 && member.getCoupleId() > 0) {
           var partner = memberRepository.findPartnerByCoupleId(member.getCoupleId(), member.getUserId());
           var couple = coupleRepository.findById(member.getCoupleId()).orElse(null);
           long days = 0;
           if (couple != null && couple.getAnniversary() != null) {
               days = ChronoUnit.DAYS.between(couple.getAnniversary().toLocalDate(), LocalDate.now());
           }

           response.put("partnerNickname", partner.getNickname());
           response.put("partnerProfileImg", partner.getProfileImg() != null ? partner.getProfileImg() : "/profileImg/img.png");
           response.put("dayCount", days);
           response.put("anniversary",couple.getAnniversary());
       }
       return ResponseEntity.ok(response);
   }

   //회원정보 수정
   @PostMapping("userUpdate")
    public ResponseEntity<?> updateUser(@ModelAttribute MemberEntity form, @RequestPart(value="profileImg",required = false)MultipartFile profileImg){
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            MemberEntity loginUser = (MemberEntity) auth.getPrincipal();

            MemberEntity user = memberRepository.findById(loginUser.getId())
                    .orElseThrow(() -> new RuntimeException("사용자 없음"));
            if (form.getNickname() != null && !form.getNickname().isBlank())
                user.setNickname(form.getNickname());

            if (form.getPassword() != null && !form.getPassword().isBlank())
                user.setPassword(passwordEncoder.encode(form.getPassword()));

            if (profileImg != null && !profileImg.isEmpty()) {
                String uploadDir = "c:/uploads/profile/";
                File dir = new File(uploadDir);
                if (!dir.exists()) dir.mkdirs();

                String fileName = UUID.randomUUID() + "_" + profileImg.getOriginalFilename();
                Path filePath = Paths.get(uploadDir, fileName);
                Files.copy(profileImg.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                user.setProfileImg("/profileImg/" + fileName);
            }
            memberRepository.save(user);
            return ResponseEntity.ok("회원정보 수정 완료");
        }catch(Exception e){
            return ResponseEntity.badRequest().body("오류발생");
        }


   }
}
