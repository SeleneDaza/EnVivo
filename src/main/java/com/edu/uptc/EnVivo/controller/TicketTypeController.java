package com.edu.uptc.EnVivo.controller;

import com.edu.uptc.EnVivo.dto.CreateTicketTypeDTO;
import com.edu.uptc.EnVivo.service.TicketTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/ticket-types")
public class TicketTypeController {

    private final TicketTypeService ticketTypeService;

    private void cargarListaTipos(Model model) {
        model.addAttribute("tiposEntrada", ticketTypeService.getTicketTypes());
    }

    @GetMapping
    public String listTicketTypes(Model model) {
        cargarListaTipos(model);
        model.addAttribute("tipoEntrada", new CreateTicketTypeDTO());
        return "ticket-types";
    }

    @GetMapping("/edit/{id}")
    public String editTicketType(@PathVariable Long id, Model model) {
        cargarListaTipos(model);
        model.addAttribute("tipoEntrada", ticketTypeService.getTicketTypeDTO(id));
        return "ticket-types";
    }

    @PostMapping("/create")
    public String saveTicketType(@ModelAttribute("tipoEntrada") CreateTicketTypeDTO dto) {
        try {
            ticketTypeService.saveTicketType(dto);
            return "redirect:/ticket-types?exito";
        } catch (IllegalArgumentException e) {
            return "redirect:/ticket-types?error_validacion";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteTicketType(@PathVariable Long id) {
        try {
            ticketTypeService.deleteTicketType(id);
            return "redirect:/ticket-types?exito";
        } catch (Exception e) {
            return "redirect:/ticket-types?error_general";
        }
    }
}

