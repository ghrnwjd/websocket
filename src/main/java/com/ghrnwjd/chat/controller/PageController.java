package com.ghrnwjd.chat.controller;


import lombok.Getter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/chata")
    public String chat() {
        return "chat.html";
    }
}
