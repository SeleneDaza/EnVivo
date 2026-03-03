package com.edu.uptc.EnVivo.controller;

import com.edu.uptc.EnVivo.dto.RegisterDTO;
import com.edu.uptc.EnVivo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public String registrar(@ModelAttribute RegisterDTO dto) {
        boolean exito = userService.registrar(dto);

        if (exito) {
            // Registro exitoso: redirige al login con mensaje de éxito
            return "redirect:/?registered=true";
        } else {
            // Error: usuario ya existe o contraseñas no coinciden
            return "redirect:/?registerError=true";
        }
    }
}

