package com.edu.uptc.EnVivo.controller;

import com.edu.uptc.EnVivo.dto.CreateCategoryDTO;
import com.edu.uptc.EnVivo.dto.CreateEventDTO;
import com.edu.uptc.EnVivo.dto.EventReporteDTO;
import com.edu.uptc.EnVivo.service.CategoryService;
import com.edu.uptc.EnVivo.service.CloudinaryService;
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

    private final EventService eventoService;
    private final CategoryService categoryService;
    private final CloudinaryService cloudinaryService;

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
    public String guardarEventoAdmin(@ModelAttribute("evento") CreateEventDTO dto,
                                     @RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            // Si el usuario seleccionó un archivo, lo subimos a Cloudinary
            if (file != null && !file.isEmpty()) {
                String imageUrl = cloudinaryService.uploadImage(file);
                dto.setImage(imageUrl); // Le pasamos la URL de la nube al DTO
            }

            eventoService.createEvent(dto);
            return "redirect:/admin";

        } catch (IOException e) {
            e.printStackTrace();
            return "redirect:/admin?error_imagen";
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
    public String guardarEdicionAdmin(@PathVariable Long id,
                                      @ModelAttribute("evento") CreateEventDTO dto,
                                      @RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            if (file != null && !file.isEmpty()) {
                String imageUrl = cloudinaryService.uploadImage(file);
                dto.setImage(imageUrl); // Actualiza con la nueva URL
            }

            eventoService.actualizarEvento(id, dto);
            return "redirect:/admin";

        } catch (IOException e) {
            e.printStackTrace();
            return "redirect:/admin?error_imagen";
        }
    }

    // ENDPOINT(Botón Me Interesa) ---
    @PostMapping("/evento/{id}/interest")
    @ResponseBody
    public ResponseEntity<?> toggleInterest(@PathVariable("id") Long eventId, Principal principal) {
        // 1. Validamos que el usuario haya iniciado sesión
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Debes iniciar sesión para marcar interés."));
        }
        
        try {
            // 2. Llamamos al servicio pasando el ID del evento y el email del usuario logueado (principal.getName())
            boolean newState = eventoService.toggleInterest(eventId, principal.getName());
            
            // 3. Cumpliendo el RF09: Preparamos un mensaje de éxito para el cliente
            String mensaje = newState ? "¡Añadido a tus intereses! ❤️" : "Eliminado de tus intereses 🤍";
            
            // Devolvemos el nuevo estado y el mensaje en formato JSON
            return ResponseEntity.ok(Map.of("interested", newState, "message", mensaje));
            
        } catch (Exception e) {
            // Manejo de errores en caso de que el evento no exista
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error al procesar tu solicitud: " + e.getMessage()));
        }
    }

    // --- ENDPOINT PARA Ver panel de favoritos
    @GetMapping("/favorites")
    public String verFavoritos(Principal principal, Model model) {
        // Si no hay sesión iniciada, lo devolvemos al inicio
        if (principal == null) {
            return "redirect:/";
        }
        
        // Obtenemos la lista ya ordenada por fecha
        List<Event> misFavoritos = eventoService.obtenerEventosFavoritosOrdenados(principal.getName());
        
        model.addAttribute("eventos", misFavoritos);
        
        // Retornamos el nombre de la nueva vista (favoritos.html)
        return "favorites";
    }

    @GetMapping("/reports")
    public String verReporte(Model model) {
        List<EventReporteDTO> top10 = eventoService.getTop10EventosPorInteres();
        model.addAttribute("top10", top10);
        return "reports";
    }
}