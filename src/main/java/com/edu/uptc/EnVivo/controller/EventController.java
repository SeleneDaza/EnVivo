package com.edu.uptc.EnVivo.controller;

import com.edu.uptc.EnVivo.dto.CreateCategoryDTO;
import com.edu.uptc.EnVivo.dto.CreateEventDTO;
import com.edu.uptc.EnVivo.dto.EventDetailDTO;
import com.edu.uptc.EnVivo.dto.EventReporteDTO;
import com.edu.uptc.EnVivo.service.CategoryService;
import com.edu.uptc.EnVivo.service.CloudinaryService;
import com.edu.uptc.EnVivo.service.EventService;
import com.edu.uptc.EnVivo.service.TicketService;
import com.edu.uptc.EnVivo.service.TicketTypeService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseBody;
import java.security.Principal;
import java.util.Map;

import java.util.Set;
import java.util.Collections;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class EventController {

    private static final Logger logger = LoggerFactory.getLogger(EventController.class);
    private final EventService eventoService;
    private final CategoryService categoryService;
    private final TicketService ticketService;
    private final TicketTypeService ticketTypeService;

    @GetMapping("/login")
    public String loginPage() {
        return "index";
    }

    @GetMapping("/")
    public String index(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            Principal principal,
            Model model) {

        int pageSize = 10;
        Pageable pageable = PageRequest.of(page, pageSize);

        // Solo eventos vigentes (fecha >= hoy) para la página pública
        Page<Event> eventPage = eventoService.buscarOPaginarVigentes(keyword, pageable);

        Set<Long> misFavoritos = (principal != null)
            ? eventoService.obtenerFavoritosUsuario(principal.getName())
            : Collections.emptySet();

        model.addAttribute("misFavoritos", misFavoritos);

        model.addAttribute("eventos", eventPage);
        model.addAttribute("keyword", keyword);
        cargarDatosComunes(model);

        return "main";
    }

    @PostMapping("/evento/crear")
    public String createEvent(@ModelAttribute CreateEventDTO dto) {
        try {
            eventoService.createEvent(dto, null); 
        } catch(IOException e) {
            logger.error("Error creating event", e);
        }
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
        cargarDatosComunes(model);

        return "admin";
    }

    @PostMapping("/admin/guardar")
    public String guardarEventoAdmin(@ModelAttribute("evento") CreateEventDTO dto,
                                     @RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            eventoService.createEvent(dto, file);
            return "redirect:/admin?exito";
        } catch (IllegalArgumentException e) {
            logger.warn("Validation error saving event: {}", e.getMessage());
            return "redirect:/admin?error=datos_invalidos";
        } catch (IOException e) {
            logger.error("Error saving event", e);
            return "redirect:/admin?error_imagen";
        } catch (Exception e) {
            logger.error("Unexpected error saving event", e);
            return "redirect:/admin?error=error_general";
        }
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

        CreateEventDTO dto = eventoService.obtenerEventoParaEdicion(id);
        model.addAttribute("evento", dto);
     
        cargarDatosComunes(model);

        return "admin";
    }

    @PostMapping("/admin/editar/{id}")
    public String guardarEdicionAdmin(@PathVariable Long id,
                                      @ModelAttribute("evento") CreateEventDTO dto,
                                      @RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            eventoService.actualizarEvento(id, dto, file);
            return "redirect:/admin?exito";
        } catch (IllegalArgumentException e) {
            logger.warn("Validation error updating event {}: {}", id, e.getMessage());
            return "redirect:/admin?error=datos_invalidos";
        } catch (IOException e) {
            logger.error("Error updating event", e);
            return "redirect:/admin?error_imagen";
        } catch (Exception e) {
            logger.error("Unexpected error updating event {}", id, e);
            return "redirect:/admin?error=error_general";
        }
    }

    @PostMapping("/evento/{id}/interest")
    @ResponseBody
    public ResponseEntity<?> toggleInterest(@PathVariable("id") Long eventId, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Debes iniciar sesión para marcar interés."));
        }
        
        try {
            // 2. Llamamos al servicio pasando el ID del evento y el email del usuario logueado (principal.getName())
            boolean newState = eventoService.toggleInterest(eventId, principal.getName());
            
            // 3. Cumpliendo el RF09: Preparamos un mensaje de éxito para el cliente
            String mensaje = newState ? "¡Añadido a tus intereses!" : "Eliminado de tus intereses";
            
            // Devolvemos el nuevo estado y el mensaje en formato JSON
            return ResponseEntity.ok(Map.of("interested", newState, "message", mensaje));
            
        } catch (Exception e) {
            logger.error("Error toggling interest", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error al procesar tu solicitud."));
        }
    }

    // --- ENDPOINT PARA Ver panel de favoritos
    @GetMapping("/favorites")
    public String verFavoritos(
            @RequestParam(name = "page", defaultValue = "0") int page,
            Principal principal, 
            Model model) {
            
        if (principal == null) {
            return "redirect:/";
        }
        
        int pageSize = 10;
        Pageable pageable = PageRequest.of(page, pageSize);
        
        Page<Event> misFavoritos = eventoService.obtenerEventosFavoritosPaginados(principal.getName(), pageable);
        
        model.addAttribute("eventos", misFavoritos);
        
        return "favorites";
    }

    @GetMapping("/reports")
    public String verReporte(Model model) {
        List<EventReporteDTO> top10 = eventoService.getTop10EventosPorInteres();
        model.addAttribute("top10", top10);
        return "reports";
    }

    private void cargarDatosComunes(Model model) {
        model.addAttribute("categorias", categoryService.getCategories());
        model.addAttribute("tiposEntrada", ticketTypeService.getTicketTypes());
        model.addAttribute("nuevaCategoria", new CreateCategoryDTO());
    }

    // --- ENDPOINTS REST PARA GESTIONAR TICKETS ---

    /**
     * Obtiene todos los tickets de un evento como JSON
     */
    @GetMapping("/api/eventos/{eventId}/tickets")
    @ResponseBody
    public ResponseEntity<?> getEventTickets(@PathVariable Long eventId) {
        try {
            eventoService.obtenerPorId(eventId);
            List<com.edu.uptc.EnVivo.dto.TicketDTO> tickets = ticketService.getTicketsAsDTO(eventId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "tickets", tickets,
                    "count", tickets.size()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Evento no encontrado."));
        } catch (Exception e) {
            logger.error("Error fetching event tickets", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error al obtener tickets."));
        }
    }

    @GetMapping("/api/eventos/{eventId}/detalle")
    @ResponseBody
    public ResponseEntity<?> getEventDetail(@PathVariable Long eventId) {
        try {
            EventDetailDTO detalle = eventoService.obtenerDetalleEvento(eventId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "event", detalle,
                    "countTickets", detalle.getTickets() != null ? detalle.getTickets().size() : 0
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Evento no encontrado."));
        } catch (Exception e) {
            logger.error("Error fetching event detail for id {}", eventId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error al obtener detalle del evento."));
        }
    }

    /**
     * Obtiene todos los tipos de entrada disponibles
     */
    @GetMapping("/api/ticket-types")
    @ResponseBody
    public ResponseEntity<?> getTicketTypes() {
        try {
            var types = ticketTypeService.getTicketTypes();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "ticketTypes", types
            ));
        } catch (Exception e) {
            logger.error("Error fetching ticket types", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error al obtener tipos de entrada."));
        }
    }
}