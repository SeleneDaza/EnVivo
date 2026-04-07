package com.edu.uptc.EnVivo.controller;

import com.edu.uptc.EnVivo.dto.RegisterDTO;
import com.edu.uptc.EnVivo.service.UserService;
import lombok.RequiredArgsConstructor;
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
    public String viewProfile(Model model) {
        // Seguimos usando el mismo servicio para obtener los datos
        model.addAttribute("usuarios", userService.getClientUsers());
        
        // Esto buscará el archivo src/main/resources/templates/profile.html
        return "profile";
    }
}
