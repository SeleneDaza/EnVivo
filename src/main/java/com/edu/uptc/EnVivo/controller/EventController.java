package com.edu.uptc.EnVivo.controller;

import com.edu.uptc.EnVivo.dto.CreateCategoryDTO;
import com.edu.uptc.EnVivo.dto.CreateEventDTO;
import com.edu.uptc.EnVivo.service.CategoryService;
import com.edu.uptc.EnVivo.service.EventService;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
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
    private final CategoryService categoryService;

    @GetMapping("/")
    public String loginPage() {
        return "index"; 
    }

    @GetMapping("/main")
    public String index(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            Model model) {

        int pageSize = 10;
        Pageable pageable = PageRequest.of(page, pageSize);

        Page<Event> eventPage = eventoService.buscarOPaginar(keyword, pageable);

        model.addAttribute("eventos", eventPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("categorias", categoryService.getCategories());
        model.addAttribute("nuevaCategoria", new CreateCategoryDTO());

        return "main"; 
    }

    @PostMapping("/evento/crear")
    public String createEvent(@ModelAttribute CreateEventDTO dto) {
        eventoService.createEvent(dto);
        return "redirect:/main"; 
    }

    @GetMapping("/admin")
    public String adminIndex(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            Model model) {

        int pageSize = 50;
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<Event> eventPage = eventoService.buscarOPaginar(keyword, pageable);

        model.addAttribute("eventos", eventPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("evento", new CreateEventDTO());
        model.addAttribute("categorias", categoryService.getCategories());
        model.addAttribute("nuevaCategoria", new CreateCategoryDTO());

        return "admin";
    }

    @PostMapping("/admin/guardar")
    public String guardarEventoAdmin(@ModelAttribute("evento") CreateEventDTO dto) {
        eventoService.createEvent(dto);
        return "redirect:/admin";
    }

    @GetMapping("/admin/eliminar/{id}")
    public String eliminarEventoAdmin(@PathVariable Long id) {
        eventoService.eliminarEvento(id);
        return "redirect:/admin";
    }

    @GetMapping("/admin/editar/{id}")
    public String editarEventoAdmin(@PathVariable Long id,
                                    @RequestParam(name = "keyword", required = false) String keyword,
                                    @RequestParam(name = "page", defaultValue = "0") int page,
                                    Model model) {
        int pageSize = 50;
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<Event> eventPage = eventoService.buscarOPaginar(keyword, pageable);
        model.addAttribute("eventos", eventPage);
        model.addAttribute("keyword", keyword);

        Event evento = eventoService.obtenerPorId(id);
        CreateEventDTO dto = new CreateEventDTO();
        dto.setEvent_id(evento.getEvent_id());
        dto.setName(evento.getName());
        dto.setDescription(evento.getDescription());
        dto.setDate(evento.getDate());
        dto.setPrice(evento.getPrice());
        dto.setImage(evento.getImage());

        if (evento.getCategory() != null) {
            dto.setCategory(evento.getCategory().getName());
        }

        model.addAttribute("evento", dto);
        model.addAttribute("categorias", categoryService.getCategories());
        model.addAttribute("nuevaCategoria", new CreateCategoryDTO());

        return "admin";
    }

    @PostMapping("/admin/editar/{id}")
    public String guardarEdicionAdmin(@PathVariable Long id, @ModelAttribute("evento") CreateEventDTO dto) {
        eventoService.actualizarEvento(id, dto);
        return "redirect:/admin";
    }
}