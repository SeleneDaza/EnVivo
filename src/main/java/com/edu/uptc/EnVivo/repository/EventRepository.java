package com.edu.uptc.EnVivo.repository;

import com.edu.uptc.EnVivo.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    // --- Página pública: solo eventos vigentes (date >= hoy) ---

    @Query("SELECT e FROM Event e WHERE e.date >= :today")
    Page<Event> findVigentes(@Param("today") LocalDate today, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.date >= :today AND LOWER(e.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Event> findVigentesByNameContaining(@Param("keyword") String keyword,
                                             @Param("today") LocalDate today,
                                             Pageable pageable);

    // --- Panel admin: todos los eventos (sin filtro de fecha) ---

    Page<Event> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // --- Reporte Top 10 ---
    @Query("SELECT e FROM Event e LEFT JOIN e.favoritedByUsers u GROUP BY e ORDER BY COUNT(u) DESC")
    List<Event> findTop10ByFavoritesCount(Pageable pageable);

    // --- Panel Favoritos: paginado y con query de conteo explícito ---
    @Query(value = "SELECT e FROM Event e JOIN e.favoritedByUsers u WHERE u.email = :email",
           countQuery = "SELECT count(e) FROM Event e JOIN e.favoritedByUsers u WHERE u.email = :email")
    Page<Event> findFavoritosByUsuarioEmailPaginated(@Param("email") String email, Pageable pageable);
}
