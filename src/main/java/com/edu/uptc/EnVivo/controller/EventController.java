package com.edu.uptc.EnVivo.controller;

import com.edu.uptc.EnVivo.dto.CreateEventDTO;
import com.edu.uptc.EnVivo.service.EventService;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;

@Controller
@RequiredArgsConstructor
public class EventController {

    private final EventService eventoService;

    // EventController.java
    @GetMapping("/")
    public String index(@RequestParam(defaultValue = "0") int page, Model model) {
        // Definimos el tamaño máximo de 10 por página
        int pageSize = 10; 
        Page<Event> eventPage = eventoService.findAll(PageRequest.of(page, pageSize));
        
        model.addAttribute("eventos", eventPage.getContent()); // Lista de eventos
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", eventPage.getTotalPages());
        
        return "index"; 
    }

    @PostMapping("/evento/crear")
    public String createEvent(@ModelAttribute CreateEventDTO dto) {
        eventoService.createEvent(dto);
        return "redirect:/";
    }
}
