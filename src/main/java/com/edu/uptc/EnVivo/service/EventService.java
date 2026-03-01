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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

        // --- 1. LÓGICA PARA PROCESAR Y GUARDAR LA IMAGEN ---
        MultipartFile archivo = dto.getImagenArchivo(); // Asegúrate de tener esto en tu DTO

        if (archivo != null && !archivo.isEmpty()) {
            try {
                // Generamos un nombre único para que no se borren fotos con el mismo nombre
                String nombreArchivo = java.util.UUID.randomUUID().toString() + "_" + archivo.getOriginalFilename();

                // Creamos la carpeta 'uploads' si no existe
                // Creamos la carpeta 'uploads' si no existe
                java.nio.file.Path rutaCarpeta = java.nio.file.Paths.get("uploads");


                if (!java.nio.file.Files.exists(rutaCarpeta)) {
                    java.nio.file.Files.createDirectories(rutaCarpeta);
                    System.out.println("✅ Carpeta recién creada en la ruta de arriba.");
                }

                // Guardamos el archivo físico en el disco duro
                java.nio.file.Path rutaArchivo = rutaCarpeta.resolve(nombreArchivo);
                java.nio.file.Files.write(rutaArchivo, archivo.getBytes());

                // Le decimos al evento cuál es la ruta web de su nueva foto
                event.setImage("/uploads/" + nombreArchivo);

            } catch (java.io.IOException e) {
                e.printStackTrace();
                throw new RuntimeException("Error al guardar la imagen del evento", e);
            }
        } else {
            // Si no subieron ninguna imagen, guardamos lo que viniera en texto o lo dejamos null
            event.setImage(dto.getImage());
        }

        // --- 2. LÓGICA DE CATEGORÍAS (Tu código original intacto) ---
        if (dto.getCategory() != null && !dto.getCategory().isBlank()) {
            Category category = categoryRepository
                    .findByName(dto.getCategory())
                    .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
            event.setCategory(category);
        }

        // --- 3. GUARDAR EN BASE DE DATOS ---
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
        event.setImage(dto.getImage());
        //Aqui se actualizan las categorias cuando sea necesario

        MultipartFile archivo = dto.getImagenArchivo();

        // 👉 SOLO SI SUBEN UNA NUEVA IMAGEN
        if (archivo != null && !archivo.isEmpty()) {
            try {
                String nombreArchivo = UUID.randomUUID() + "_" + archivo.getOriginalFilename();

                Path carpeta = Paths.get("uploads");
                Files.createDirectories(carpeta);

                Path rutaArchivo = carpeta.resolve(nombreArchivo);
                Files.write(rutaArchivo, archivo.getBytes());

                event.setImage("/uploads/" + nombreArchivo);

            } catch (IOException e) {
                throw new RuntimeException("Error al guardar la imagen", e);
            }
        }
        // ❗ Si no suben nada → se mantiene la imagen anterior

        return eventRepository.save(event);

    }
}
