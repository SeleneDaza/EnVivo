package com.edu.uptc.EnVivo.service;

import com.edu.uptc.EnVivo.dto.CreateEventDTO;
import com.edu.uptc.EnVivo.entity.Category;
import com.edu.uptc.EnVivo.entity.Event;
import com.edu.uptc.EnVivo.repository.CategoryRepository;
import com.edu.uptc.EnVivo.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import com.edu.uptc.EnVivo.entity.User;
import com.edu.uptc.EnVivo.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.Collections;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public Event createEvent(CreateEventDTO dto) {
        Event event = new Event();
        event.setName(dto.getName());
        event.setDescription(dto.getDescription());
        event.setDate(dto.getDate());
        event.setPrice(dto.getPrice());
        // ¡IMPORTANTE! No olvides la imagen
        event.setImage(dto.getImage()); 

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
        if (keyword != null && !keyword.trim().isEmpty()) {
            return eventRepository.findByNameContainingIgnoreCase(keyword, pageable);
        }
        return eventRepository.findAll(pageable);
    }

    public void eliminarEvento(Long id) {
        eventRepository.deleteById(id);
    }

    public Event obtenerPorId(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));
    }

    public Event actualizarEvento(Long id, CreateEventDTO dto) {
        Event event = obtenerPorId(id);

        event.setName(dto.getName());
        event.setDescription(dto.getDescription());
        event.setDate(dto.getDate());
        event.setPrice(dto.getPrice());

        // PROTECCIÓN DE IMAGEN: Solo la cambiamos si el DTO trae un link nuevo
        if (dto.getImage() != null && !dto.getImage().isEmpty()) {
            event.setImage(dto.getImage());
        }

        // ACTUALIZAR CATEGORÍA
        if (dto.getCategory() != null && !dto.getCategory().isEmpty()) {
            // Buscamos la categoría en la BD por su nombre
            Category categoryEntity = categoryRepository.findByName(dto.getCategory())
                    .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada: " + dto.getCategory()));

            event.setCategory(categoryEntity);
        } else {
            // Si el admin eligió "-- Sin categoría --" en el select, la dejamos en null
            event.setCategory(null);
        }

        return eventRepository.save(event);
    }

    // --- LÓGICA (Botón Me Interesa) ---
    @Transactional
    public boolean toggleInterest(Long eventId, String userEmail) {
        // 1. Obtenemos al usuario logueado y al evento
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Event event = obtenerPorId(eventId);

        // Prevención de nulos en la base de datos para el contador (por si hay eventos viejos)
        if (event.getInterestCount() == null) {
            event.setInterestCount(0);
        }

        // 2. Verificamos si el usuario YA tiene este evento en sus favoritos
        boolean isInterested = user.getFavoriteEvents().contains(event);

        if (isInterested) {
            // Si ya le interesaba, lo quitamos (Desmarcar) y restamos al contador
            user.removeFavoriteEvent(event);
            event.setInterestCount(event.getInterestCount() - 1);
        } else {
            // Si no le interesaba, lo agregamos (Marcar) y sumamos al contador
            user.addFavoriteEvent(event);
            event.setInterestCount(event.getInterestCount() + 1);
        }

        // 3. Guardamos los cambios en base de datos
        userRepository.saveAndFlush(user);
        eventRepository.saveAndFlush(event); // Guardamos el evento para actualizar el contador

        // Retornamos true si se agregó el interés, false si se quitó
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
}
