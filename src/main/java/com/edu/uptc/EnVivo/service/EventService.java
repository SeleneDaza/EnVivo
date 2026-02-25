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

    // Agrega este método para listar en el index
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
}
