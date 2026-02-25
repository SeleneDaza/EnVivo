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

    @GetMapping("/")
    public String index(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            Model model) {

        int pageSize = 10;
        Pageable pageable = PageRequest.of(page, pageSize);

        Page<Event> eventPage = eventoService.buscarOPaginar(keyword, pageable);

        model.addAttribute("eventos", eventPage);

        model.addAttribute("keyword", keyword);

        return "index";
    }

    @PostMapping("/evento/crear")
    public String createEvent(@ModelAttribute CreateEventDTO dto) {
        eventoService.createEvent(dto);
        return "redirect:/";
    }

    // 1. Mostrar la página de Admin (con paginación y búsqueda, igual que el index)
    @GetMapping("/admin")
    public String adminIndex(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            Model model) {

        int pageSize = 50;
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<Event> eventPage = eventoService.buscarOPaginar(keyword, pageable);

        // Pasamos la lista de eventos y la palabra clave
        model.addAttribute("eventos", eventPage);
        model.addAttribute("keyword", keyword);

        // ¡SÚPER IMPORTANTE! Pasamos un objeto vacío para que el formulario HTML
        // sepa dónde guardar los datos (th:object="${evento}")
        model.addAttribute("evento", new CreateEventDTO());

        return "admin"; // Asumiendo que tu archivo se llama admin.html
    }

    // 2. Recibir los datos del formulario y guardarlos
    @PostMapping("/admin/guardar")
    public String guardarEventoAdmin(@ModelAttribute("evento") CreateEventDTO dto) {
        eventoService.createEvent(dto);
        // Después de guardar, recargamos la página de admin
        return "redirect:/admin";
    }
}
