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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import com.edu.uptc.EnVivo.entity.Event;
import org.springframework.data.domain.Pageable;


@Controller
@RequiredArgsConstructor
public class EventController {

    private final EventService eventoService;

    // EventController.java
    @GetMapping("/")
    public String index(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            Model model) {

        // Definimos el tamaño máximo de 10 por página
        int pageSize = 10;
        Pageable pageable = PageRequest.of(page, pageSize);

        // Llamamos al servicio para que busque o traiga todos según el caso
        Page<Event> eventPage = eventoService.buscarOPaginar(keyword, pageable);

        // ¡OJO! Aquí pasamos el eventPage COMPLETO (no usamos .getContent())
        // Así el HTML de Thymeleaf podrá usar eventos.totalPages, eventos.hasNext(), etc.
        model.addAttribute("eventos", eventPage);

        // Pasamos el keyword para que no se borre de la barra de búsqueda en el navegador
        model.addAttribute("keyword", keyword);

        return "index";
    }

    @PostMapping("/evento/crear")
    public String createEvent(@ModelAttribute CreateEventDTO dto) {
        eventoService.createEvent(dto);
        return "redirect:/";
    }
}
