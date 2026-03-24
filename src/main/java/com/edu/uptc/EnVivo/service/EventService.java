package com.edu.uptc.EnVivo.service;

import com.edu.uptc.EnVivo.dto.CreateEventDTO;
import com.edu.uptc.EnVivo.dto.EventReporteDTO;
import com.edu.uptc.EnVivo.entity.Category;
import com.edu.uptc.EnVivo.entity.Event;
import com.edu.uptc.EnVivo.repository.CategoryRepository;
import com.edu.uptc.EnVivo.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import com.edu.uptc.EnVivo.entity.User;
import com.edu.uptc.EnVivo.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    public Event createEvent(CreateEventDTO dto, MultipartFile file) throws IOException {
        Event event = new Event();
        event.setName(dto.getName());
        event.setDescription(dto.getDescription());
        event.setDate(dto.getDate());
        event.setPrice(dto.getPrice());
        
        if (file != null && !file.isEmpty()) {
            String imageUrl = cloudinaryService.uploadImage(file);
            event.setImage(imageUrl);
        } else {
            event.setImage(dto.getImage()); 
        }

        if (dto.getCategory() != null && !dto.getCategory().isBlank()) {
            Category category = categoryRepository
                    .findByName(dto.getCategory())
                    .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
            event.setCategory(category);
        }
        return eventRepository.save(event);
    }

    public List<Event> findAll() {
        return eventRepository.findAll();
    }

    public Page<Event> findAll(Pageable pageable) {
        return eventRepository.findAll(pageable);
    }

    public Page<Event> buscarOPaginar(String keyword, Pageable pageable) {
        Sort porFechaAsc = Sort.by(Sort.Direction.ASC, "date");
        Pageable ordenado = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), porFechaAsc);

        if (keyword != null && !keyword.trim().isEmpty()) {
            return eventRepository.findByNameContainingIgnoreCase(keyword, ordenado);
        }
        return eventRepository.findAll(ordenado);
    }

    public Page<Event> buscarOPaginarVigentes(String keyword, Pageable pageable) {
        Sort porFechaAsc = Sort.by(Sort.Direction.ASC, "date");
        Pageable ordenado = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), porFechaAsc);
        LocalDate hoy = LocalDate.now();

        if (keyword != null && !keyword.trim().isEmpty()) {
            return eventRepository.findVigentesByNameContaining(keyword, hoy, ordenado);
        }
        return eventRepository.findVigentes(hoy, ordenado);
    }

    public void eliminarEvento(Long id) {
        eventRepository.deleteById(id);
    }

    public Event obtenerPorId(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));
    }

    public Event actualizarEvento(Long id, CreateEventDTO dto, MultipartFile file) throws IOException {
        Event event = obtenerPorId(id);

        event.setName(dto.getName());
        event.setDescription(dto.getDescription());
        event.setDate(dto.getDate());
        event.setPrice(dto.getPrice());

        if (file != null && !file.isEmpty()) {
            String imageUrl = cloudinaryService.uploadImage(file);
            event.setImage(imageUrl);
        } else if (dto.getImage() != null && !dto.getImage().isEmpty()) {
            event.setImage(dto.getImage());
        }

        if (dto.getCategory() != null && !dto.getCategory().isEmpty()) {
            Category categoryEntity = categoryRepository.findByName(dto.getCategory())
                    .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada: " + dto.getCategory()));

            event.setCategory(categoryEntity);
        } else {
            event.setCategory(null);
        }

        return eventRepository.save(event);
    }

    @Transactional
    public boolean toggleInterest(Long eventId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Event event = obtenerPorId(eventId);
        if (event.getInterestCount() == null) {
            event.setInterestCount(0);
        }

        boolean isInterested = user.getFavoriteEvents().contains(event);

        if (isInterested) {
            user.removeFavoriteEvent(event);
            event.setInterestCount(event.getInterestCount() - 1);
        } else {
            user.addFavoriteEvent(event);
            event.setInterestCount(event.getInterestCount() + 1);
        }

        userRepository.saveAndFlush(user);
        eventRepository.saveAndFlush(event); 
        
        return !isInterested; 
    }

    public Set<Long> obtenerFavoritosUsuario(String userEmail) {
        if (userEmail == null) return Collections.emptySet();
        return userRepository.findByEmail(userEmail)
                .map(user -> user.getFavoriteEvents().stream()
                        .map(Event::getEvent_id)
                        .collect(Collectors.toSet()))
                .orElse(Collections.emptySet());
    }

    // --- LÓGICA PARA Top 10 eventos más atractivos (reporte admin) ---
    public List<EventReporteDTO> getTop10EventosPorInteres() {
        Pageable top10 = PageRequest.of(0, 10);
        return eventRepository.findTop10ByFavoritesCount(top10).stream()
                .map(e -> new EventReporteDTO(e, e.getFavoritedByUsers().size()))
                .collect(Collectors.toList());
    }

    // --- LÓGICA PARA Lista de favoritos ordenada ---
    public List<Event> obtenerEventosFavoritosOrdenados(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
                
        return user.getFavoriteEvents().stream()
                .sorted((e1, e2) -> {
                    // Protegemos por si algún evento no tiene fecha
                    if (e1.getDate() == null) return 1;
                    if (e2.getDate() == null) return -1;
                    return e1.getDate().compareTo(e2.getDate()); // Ordena de más próximo a más lejano
                })
                .collect(java.util.stream.Collectors.toList());
    }

    // --- LÓGICA PARA Lista de favoritos paginada ---
    public Page<Event> obtenerEventosFavoritosPaginados(String userEmail, Pageable pageable) {
        // Le inyectamos el ordenamiento por fecha al Pageable
        Pageable ordenado = PageRequest.of(
                pageable.getPageNumber(), 
                pageable.getPageSize(), 
                Sort.by(Sort.Direction.ASC, "date")
        );
        return eventRepository.findFavoritosByUsuarioEmailPaginated(userEmail, ordenado);
    }

    public CreateEventDTO obtenerEventoParaEdicion(Long id) {
        Event evento = obtenerPorId(id);
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
        return dto;
    }
}
