package com.example.coupleDiary.controller;

import com.example.coupleDiary.model.Auth;
import com.example.coupleDiary.security.TokenProvider;
import com.example.coupleDiary.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Duration;
import java.util.Map;

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
//        var member = this.memberService.authenticate(request);
//        String token = tokenProvider.generateToken(member.getUserId());
//
//        ResponseCookie cookie = ResponseCookie.from("access_token", token)
//                .httpOnly(true).secure(false)      // HTTPS에서 true 권장
//                .sameSite("Lax").path("/")
//                .maxAge(Duration.ofHours(2))
//                .build();
//
////        var result=ResponseEntity.status(HttpStatus.FOUND)            // 302
////                .header(HttpHeaders.SET_COOKIE, cookie.toString())
////                .header(HttpHeaders.LOCATION, "/")                    // index로
////                .build();
//        var result= ResponseEntity.status(HttpStatus.SEE_OTHER)   // 303
//                .header(HttpHeaders.SET_COOKIE, cookie.toString())
//                .header(HttpHeaders.LOCATION, "/")
//                .build();
//
//        System.out.println("[UserController-signin result: "+result);
//
//        return result;
        var member = memberService.authenticate(request);
        String token = tokenProvider.generateToken(member.getUserId());
        ResponseCookie cookie = ResponseCookie.from("access_token", token)
                .httpOnly(true).secure(false).sameSite("Lax").path("/").maxAge(Duration.ofHours(2)).build();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(){
        ResponseCookie del= ResponseCookie.from("access_token","")
                .httpOnly(true).secure(true).sameSite("Lax").path("/").maxAge(0).build();
        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE,del.toString()).build();
    }
}
