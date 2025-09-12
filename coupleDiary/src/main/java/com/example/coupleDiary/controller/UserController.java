package com.example.coupleDiary.controller;

import com.example.coupleDiary.model.Auth;
import com.example.coupleDiary.service.MemberService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserController {

    private final MemberService memberService;

    public UserController(MemberService memberService) {
        this.memberService = memberService;
    }

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
}
