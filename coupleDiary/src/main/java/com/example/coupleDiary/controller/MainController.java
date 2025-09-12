package com.example.coupleDiary.controller;

import lombok.Getter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/")
    public String mainPage(){
        return "index";
    }

    @GetMapping("/loginPage")
    public String loginPage(Model model){
        return "user/loginPage";
    }

    @GetMapping("/joinPage")
    public String joinPage(){return "user/joinPage";}


}
