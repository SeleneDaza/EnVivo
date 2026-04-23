package com.edu.uptc.EnVivo.controller;

import com.edu.uptc.EnVivo.dto.CreateCategoryDTO;
import com.edu.uptc.EnVivo.dto.CreateEventDTO;
import com.edu.uptc.EnVivo.dto.EventDetailDTO;
import com.edu.uptc.EnVivo.dto.EventReporteDTO;
import com.edu.uptc.EnVivo.service.CategoryService;
import com.edu.uptc.EnVivo.service.EventService;
import com.edu.uptc.EnVivo.service.TicketService;
import com.edu.uptc.EnVivo.service.TicketTypeService;
import com.edu.uptc.EnVivo.service.UserService;
import com.edu.uptc.EnVivo.service.PurchaseService;
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

import org.springframework.security.core.Authentication;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseBody;
import java.security.Principal;
import java.util.Map;

import java.util.Set;
import java.util.Collections;

import java.util.List;
import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
public class EventController {

    public static final int PAGESIZE = 10;
    private static final Logger logger = LoggerFactory.getLogger(EventController.class);
    
    private static final String ATTR_TODAY = "today";
    private static final String ATTR_EVENT = "event";
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_SUCCESS = "success";
    private static final String KEY_INTERESTED = "interested";
    private static final String KEY_COUNT_TICKETS = "countTickets";
    private static final String KEY_TICKETS = "tickets";
    private static final String KEY_TICKET_TYPES = "ticketTypes";
    private static final String KEY_STRING = "keyword";

    private final EventService eventService;
    private final CategoryService categoryService;
    private final TicketService ticketService;
    private final TicketTypeService ticketTypeService;
    private final UserService userService;
    private final PurchaseService purchaseService;

    @GetMapping("/login")
    public String loginPage() {
        return "index";
    }

    @GetMapping("/")
    public String index(
            @RequestParam(name = KEY_STRING, required = false) String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            Authentication authentication,
            Model model) {

        if (authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ADMIN"))) {
            return "redirect:/admin";
        }

        Pageable pageable = PageRequest.of(page, PAGESIZE);

        Page<Event> eventPage = eventService.buscarOPaginar(keyword, pageable);

        Set<Long> misFavoritos = (authentication != null)
            ? eventService.obtenerFavoritosUsuario(authentication.getName())
            : Collections.emptySet();

        model.addAttribute("misFavoritos", misFavoritos);
        model.addAttribute(ATTR_TODAY, LocalDate.now());

        model.addAttribute("eventos", eventPage);
        model.addAttribute(KEY_STRING, keyword);
        cargarDatosComunes(model);

        return "main";
    }

    @PostMapping("/evento/crear")
    public String createEvent(@ModelAttribute CreateEventDTO dto) {
        try {
            eventService.createEvent(dto, null);
        } catch(IOException e) {
            logger.error("Error creating event", e);
        }
        return "redirect:/main";
    }

    @GetMapping("/admin")
    public String adminIndex(
            @RequestParam(name = KEY_STRING, required = false) String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            Model model) {

        int pageSize = 50;
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<Event> eventPage = eventService.buscarOPaginar(keyword, pageable);

        model.addAttribute("totalGanancias", purchaseService.getTotalGanancias());
        model.addAttribute("eventosActivos", eventService.getEventosActivosCount());
        model.addAttribute("eventosPasados", eventService.getEventosPasadosCount());
        model.addAttribute("usuariosRegistrados", userService.getUsuariosRegistradosCount());
        

        model.addAttribute("eventos", eventPage);
        model.addAttribute(KEY_STRING, keyword);
        model.addAttribute(ATTR_TODAY, LocalDate.now());
        model.addAttribute(ATTR_EVENT, new CreateEventDTO());
        cargarDatosComunes(model);

        return "admin";
    }

    @PostMapping("/admin/guardar")
    public String saveEventAdmin(@ModelAttribute(ATTR_EVENT) CreateEventDTO dto,
                                     @RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            eventService.createEvent(dto, file);
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
    public String deleteEventAdmin(@PathVariable Long id) {
        eventService.eliminarEvento(id);
        return "redirect:/admin";
    }

    @GetMapping("/admin/editar/{id}")
    public String editEventAdmin(@PathVariable Long id,
                                    @RequestParam(name = KEY_STRING, required = false) String keyword,
                                    @RequestParam(name = "page", defaultValue = "0") int page,
                                    Model model) {
        int pageSize = 50;
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<Event> eventPage = eventService.buscarOPaginar(keyword, pageable);
        
        model.addAttribute("totalGanancias", purchaseService.getTotalGanancias());
        model.addAttribute("eventosActivos", eventService.getEventosActivosCount());
        model.addAttribute("eventosPasados", eventService.getEventosPasadosCount());
        model.addAttribute("usuariosRegistrados", userService.getUsuariosRegistradosCount());
        model.addAttribute(ATTR_TODAY, LocalDate.now()); 

        model.addAttribute("eventos", eventPage);
        model.addAttribute(KEY_STRING, keyword);

        CreateEventDTO dto = eventService.obtenerEventoParaEdicion(id);
        model.addAttribute(ATTR_EVENT, dto);
     
        cargarDatosComunes(model);

        return "admin";
    }

    @PostMapping("/admin/editar/{id}")
    public String saveEditionAdmin(@PathVariable Long id,
                                      @ModelAttribute(ATTR_EVENT) CreateEventDTO dto,
                                      @RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            eventService.actualizarEvento(id, dto, file);
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
    public ResponseEntity<Map<String, Object>> toggleInterest(@PathVariable("id") Long eventId, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.<String, Object>of(KEY_MESSAGE, "Debes iniciar sesión para marcar interés."));
        }
        
        try {
            // 2. Llamamos al servicio pasando el ID del evento y el email del usuario logueado (principal.getName())
            boolean newState = eventService.toggleInterest(eventId, principal.getName());
            
            // 3. Cumpliendo el RF09: Preparamos un mensaje de éxito para el cliente
            String mensaje = newState ? "¡Añadido a tus intereses!" : "Eliminado de tus intereses";
            
            // Devolvemos el nuevo estado y el mensaje en formato JSON
            return ResponseEntity.ok(Map.<String, Object>of(KEY_INTERESTED, newState, KEY_MESSAGE, mensaje));

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.<String, Object>of(KEY_MESSAGE, e.getMessage()));

        } catch (Exception e) {
            logger.error("Error toggling interest", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.<String, Object>of(KEY_MESSAGE, "Error al procesar tu solicitud."));
        }
    }

    // --- ENDPOINT PARA Ver panel de favoritos
    @GetMapping("/favorites")
    public String getFavorites(
            @RequestParam(name = "page", defaultValue = "0") int page,
            Principal principal, 
            Model model) {
            
        if (principal == null) {
            return "redirect:/";
        }

        Pageable pageable = PageRequest.of(page, PAGESIZE);
        
        Page<Event> misFavoritos = eventService.obtenerEventosFavoritosPaginados(principal.getName(), pageable);
        
        model.addAttribute("eventos", misFavoritos);
        model.addAttribute(ATTR_TODAY, LocalDate.now());
        
        return "favorites";
    }

    @GetMapping("/reports")
    public String getReport(Model model) {
        List<EventReporteDTO> top10 = eventService.getTop10EventosPorInteres();
        model.addAttribute("top10", top10);
        return "reports";
    }

    @GetMapping("/buy-ticket/{eventId}")
    public String viewBuyTicket(@PathVariable Long eventId, Principal principal, Model model) {
        try {
            EventDetailDTO detailDTO = eventService.obtenerDetalleEvento(eventId);
            if (detailDTO.isHistorical()) {
                return "redirect:/?error=evento_historico";
            }
            model.addAttribute(ATTR_EVENT, detailDTO);

            if (principal != null) {
                userService.findByUserName(principal.getName())
                        .or(() -> userService.findByEmail(principal.getName()))
                        .ifPresent(user -> {
                    model.addAttribute("buyerFullName", user.getFullName());
                    model.addAttribute("buyerDocument", user.getDocument());
                    model.addAttribute("buyerEmail", user.getEmail());
                    model.addAttribute("buyerPhone", user.getPhone());
                });
            }

            return "buy-ticket";
        } catch (IllegalArgumentException e) {
            return "redirect:/?error=evento_no_encontrado";
        }
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
    public ResponseEntity<Map<String, Object>> getEventTickets(@PathVariable Long eventId) {
        try {
            eventService.obtenerPorId(eventId);
            List<com.edu.uptc.EnVivo.dto.TicketDTO> tickets = ticketService.getTicketsAsDTO(eventId);
            return ResponseEntity.ok(Map.<String, Object>of(
                    KEY_SUCCESS, true,
                    KEY_TICKETS, tickets,
                    "count", tickets.size()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.<String, Object>of(KEY_SUCCESS, false, KEY_MESSAGE, "Evento no encontrado."));
        } catch (Exception e) {
            logger.error("Error fetching event tickets", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.<String, Object>of(KEY_SUCCESS, false, KEY_MESSAGE, "Error al obtener tickets."));
        }
    }

    @GetMapping("/api/eventos/{eventId}/detalle")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getEventDetail(@PathVariable Long eventId) {
        try {
            EventDetailDTO detalle = eventService.obtenerDetalleEvento(eventId);
            return ResponseEntity.ok(Map.<String, Object>of(
                    KEY_SUCCESS, true,
                    "event", detalle,
                    KEY_COUNT_TICKETS, detalle.getTickets() != null ? detalle.getTickets().size() : 0
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.<String, Object>of(KEY_SUCCESS, false, KEY_MESSAGE, "Evento no encontrado."));
        } catch (Exception e) {
            logger.error("Error fetching event detail for id {}", eventId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.<String, Object>of(KEY_SUCCESS, false, KEY_MESSAGE, "Error al obtener detalle del evento."));
        }
    }

    /**
     * Obtiene todos los tipos de entrada disponibles
     */
    @GetMapping("/api/ticket-types")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTicketTypes() {
        try {
            var types = ticketTypeService.getTicketTypes();
            return ResponseEntity.ok(Map.<String, Object>of(
                    KEY_SUCCESS, true,
                    KEY_TICKET_TYPES, types
            ));
        } catch (Exception e) {
            logger.error("Error fetching ticket types", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.<String, Object>of(KEY_SUCCESS, false, KEY_MESSAGE, "Error al obtener tipos de entrada."));
        }
    }
}