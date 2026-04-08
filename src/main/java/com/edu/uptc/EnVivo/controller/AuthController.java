package com.edu.uptc.EnVivo.controller;

import com.edu.uptc.EnVivo.dto.RegisterDTO;
import com.edu.uptc.EnVivo.dto.UpdateProfileDTO;
import com.edu.uptc.EnVivo.entity.User;
import com.edu.uptc.EnVivo.service.UserService;
import lombok.RequiredArgsConstructor;
import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("usuarios", userService.getClientUsers());
        return "users";
    }

    @PostMapping("/register")
    public String registrar(@ModelAttribute RegisterDTO dto) {
        boolean exito = userService.registerUser(dto);
        return "redirect:/login?" + (exito ? "registered=true" : "registerError=true");
    }

    @GetMapping("/profile")
    public String viewProfile(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        User user = userService.findByUserName(principal.getName())
                .or(() -> userService.findByEmail(principal.getName()))
                .orElse(null);
        model.addAttribute("user", user);
        return "profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute UpdateProfileDTO dto, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        UserService.UpdateProfileResult result = userService.updatePersonalInfo(principal.getName(), dto);
        return switch (result) {
            case SUCCESS -> "redirect:/profile?updated=true";
            case INVALID_FULL_NAME -> "redirect:/profile?error=invalidFullName";
            case INVALID_DOCUMENT -> "redirect:/profile?error=invalidDocument";
            case INVALID_PHONE -> "redirect:/profile?error=invalidPhone";
            case INVALID_EMAIL -> "redirect:/profile?error=invalidEmail";
            case EMAIL_IN_USE -> "redirect:/profile?error=emailInUse";
            case DOCUMENT_IN_USE -> "redirect:/profile?error=documentInUse";
            default -> "redirect:/profile?error=userNotFound";
        };
    }
}
