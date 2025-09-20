package com.example.coupleDiary.controller;

import com.example.coupleDiary.domain.Auth;
import com.example.coupleDiary.security.TokenProvider;
import com.example.coupleDiary.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Duration;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final MemberService memberService;

    private final TokenProvider tokenProvider;

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
        /*var member = memberService.authenticate(request);
        String token = tokenProvider.generateToken(member.getUserId());
        ResponseCookie cookie = ResponseCookie.from("access_token", token)
                .httpOnly(true).secure(false).sameSite("Lax").path("/").maxAge(Duration.ofHours(2)).build();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).build();*/
        var member = memberService.authenticate(request);
        String token = tokenProvider.generateToken(member.getUserId());

        ResponseCookie cookie = ResponseCookie.from("access_token", token)
                .httpOnly(true)       // JavaScript에서 접근 못하게 (보안)
                .secure(false)        // ★ localhost(HTTP) 환경에서는 false, 배포(HTTPS)에서는 true
                .sameSite("Lax")      // CSRF 방지 기본 설정
                .path("/")            // ★ 꼭 루트 경로
                .maxAge(Duration.ofHours(2))
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(){
        ResponseCookie del= ResponseCookie.from("access_token","")
                .httpOnly(true).secure(true).sameSite("Lax").path("/").maxAge(0).build();
        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE,del.toString()).build();
    }

   /* @GetMapping("/CheckAuth")
    public ResponseEntity<?> me(@AuthenticationPrincipal UserDetails user){
        if(user==null){
            System.out.println("user is NULL");
        }
        return (user==null)?ResponseEntity.status(401).build()
                :ResponseEntity.ok().build();
    }*/
   @GetMapping("/CheckAuth")
   public ResponseEntity<?> me(Authentication auth) {
       Authentication auth2 = SecurityContextHolder.getContext().getAuthentication();
       System.out.println("[CheckAuth] auth=" + auth2);

       // anonymous 방어
       if (auth2 == null || !auth2.isAuthenticated()
               || "anonymousUser".equals(String.valueOf(auth2.getPrincipal()))) {
           return ResponseEntity.status(401).build();
       }

       System.out.println("[CheckAuth] principal=" + auth2.getPrincipal());
       return ResponseEntity.ok().build();
   }

}
