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
import org.springframework.data.domain.Sort;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;

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
        Sort porFechaAsc = Sort.by(Sort.Direction.ASC, "date");
        Pageable ordenado = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), porFechaAsc);

        if (keyword != null && !keyword.trim().isEmpty()) {
            return eventRepository.findByNameContainingIgnoreCase(keyword, ordenado);
        }
        return eventRepository.findAll(ordenado);
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

        // 🛡️ PROTECCIÓN DE IMAGEN: Solo la cambiamos si el DTO trae un link nuevo
        if (dto.getImage() != null && !dto.getImage().isEmpty()) {
            event.setImage(dto.getImage());
        }

        // 🏷️ ACTUALIZAR CATEGORÍA
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
}
