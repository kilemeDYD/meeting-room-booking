package com.example.meetingroombooking.web;

import com.example.meetingroombooking.repo.MeetingRoomRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RoomController {

    private final MeetingRoomRepository roomRepo;

    public RoomController(MeetingRoomRepository roomRepo) {
        this.roomRepo = roomRepo;
    }

    @GetMapping("/rooms")
    public String list(Model model) {
        model.addAttribute("rooms", roomRepo.findByStatusOrderByIdAsc("ACTIVE"));
        return "rooms";
    }
}
