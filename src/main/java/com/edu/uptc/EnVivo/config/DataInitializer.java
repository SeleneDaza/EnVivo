package com.edu.uptc.EnVivo.config;

import com.edu.uptc.EnVivo.entity.Category;
import com.edu.uptc.EnVivo.entity.Event;
import com.edu.uptc.EnVivo.entity.Role;
import com.edu.uptc.EnVivo.entity.User;
import com.edu.uptc.EnVivo.repository.CategoryRepository;
import com.edu.uptc.EnVivo.repository.EventRepository;
import com.edu.uptc.EnVivo.repository.RoleRepository;
import com.edu.uptc.EnVivo.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(RoleRepository roleRepository,
                                      UserRepository userRepository,
                                      CategoryRepository categoryRepository,
                                      EventRepository eventRepository) {
        return args -> {
            System.out.println("⏳ Inicializando datos de prueba...");

            // 1. POBLAR ROLES (Si no existen)
            Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseGet(() -> {
                Role r = new Role();
                r.setName("ROLE_ADMIN");
                return roleRepository.save(r);
            });

            Role clientRole = roleRepository.findByName("ROLE_CLIENT").orElseGet(() -> {
                Role r = new Role();
                r.setName("ROLE_CLIENT");
                return roleRepository.save(r);
            });

            // 2. POBLAR USUARIO ADMINISTRADOR
            // Nota: La contraseña está en texto plano por ahora.
            // Cuando agreguemos Spring Security, aquí usaremos passwordEncoder.encode("admin123")
            if (!userRepository.existsByEmail("admin@envivo.com")) {
                User adminUser = new User();
                adminUser.setEmail("admin@envivo.com");
                adminUser.setPassword("admin123");
                adminUser.getRoles().add(adminRole); // Le asignamos el rol
                userRepository.save(adminUser);
            }

            // 3. POBLAR CATEGORÍAS
            Category catMusica = categoryRepository.findByName("Música").orElseGet(() -> {
                Category c = new Category();
                c.setName("Música");
                return categoryRepository.save(c);
            });

            Category catTeatro = categoryRepository.findByName("Teatro").orElseGet(() -> {
                Category c = new Category();
                c.setName("Teatro");
                return categoryRepository.save(c);
            });

            // 4. POBLAR UN EVENTO DE PRUEBA
            if (eventRepository.count() == 0) {
                Event evento = new Event();
                evento.setName("Gran Concierto de Apertura");
                evento.setDescription("Evento inaugural generado automáticamente por el sistema.");
                evento.setDate(LocalDate.now().plusDays(15)); // Fecha: 15 días en el futuro
                evento.setPrice(50000);
                evento.setCategory(catMusica);
                // evento.setImage("/uploads/default.jpg"); // Opcional si tienes una imagen por defecto
                eventRepository.save(evento);
            }

            System.out.println("✅ Datos de prueba cargados correctamente en la base de datos.");
        };
    }
}