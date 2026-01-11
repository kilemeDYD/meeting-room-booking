package com.example.meetingroombooking.web.controller;

import com.example.meetingroombooking.repo.RoomRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/rooms")
public class RoomController {

    private final RoomRepository roomRepository;

    public RoomController(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @GetMapping
    public String list(Model model, HttpSession session) {
        model.addAttribute("rooms", roomRepository.findAll());
        model.addAttribute("username", session.getAttribute("username"));
        return "room/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        var room = roomRepository.findById(id).orElseThrow();
        model.addAttribute("room", room);
        return "room/detail";
    }
}
