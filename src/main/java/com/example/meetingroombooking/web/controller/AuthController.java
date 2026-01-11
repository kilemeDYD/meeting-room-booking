package com.example.meetingroombooking.web.controller;

import com.example.meetingroombooking.service.AuthService;
import com.example.meetingroombooking.web.dto.LoginForm;
import com.example.meetingroombooking.web.exception.BusinessException;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String loginPage(@ModelAttribute("form") LoginForm form) {
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("form") LoginForm form,
                        BindingResult bindingResult,
                        HttpSession session,
                        RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            return "auth/login";
        }

        try {
            var user = authService.login(form.getUsername().trim(), form.getPassword());
            session.setAttribute("userId", user.getId());
            session.setAttribute("role", user.getRole().name());
            session.setAttribute("username", user.getUsername());
            ra.addFlashAttribute("success", "登录成功");
            return "redirect:/rooms";
        } catch (BusinessException e) {
            bindingResult.reject("login_failed", e.getMessage());
            return "auth/login";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes ra) {
        session.invalidate();
        ra.addFlashAttribute("success", "已退出");
        return "redirect:/login";
    }
}
