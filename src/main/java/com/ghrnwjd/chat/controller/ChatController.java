package com.ghrnwjd.chat.controller;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ChatController {

    @GetMapping("/chat")
    public String chatRoom(@RequestParam("roomId") String roomId, Model model) {
        model.addAttribute("roomId", roomId);
        return "chat";
    }

    @GetMapping("/rooms")
    public String roomList(Model model) {
        // 방 목록 조회 로직 추가
        return "room-list";
    }
}
