package com.example.meetingroombooking.web.controller;

import com.example.meetingroombooking.service.ReservationService;
import com.example.meetingroombooking.web.dto.ReservationCreateForm;
import com.example.meetingroombooking.web.exception.BusinessException;
import com.example.meetingroombooking.web.exception.ConflictException;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/new")
    public String createPage(@ModelAttribute("form") ReservationCreateForm form,
                             @RequestParam("roomId") Long roomId) {
        form.setRoomId(roomId);
        return "reservation/create";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") ReservationCreateForm form,
                         BindingResult bindingResult,
                         HttpSession session,
                         RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            return "reservation/create";
        }

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            Long id = reservationService.create(userId, form);
            ra.addFlashAttribute("success", "预约成功，编号：" + id);
            return "redirect:/my/reservations";
        } catch (ConflictException e) {
            bindingResult.reject("conflict", e.getMessage());
            return "reservation/create";
        } catch (BusinessException e) {
            bindingResult.reject("biz", e.getMessage());
            return "reservation/create";
        }
    }
}
