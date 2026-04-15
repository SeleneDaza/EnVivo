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

    boolean existsByCategory_CategoryId(Long categoryId);

    Page<Event> findByNameContainingIgnoreCase(String name, Pageable pageable);

    long countByDateGreaterThanEqual(LocalDate date);

    long countByDateLessThan(LocalDate date);

    @Query("SELECT e FROM Event e LEFT JOIN e.favoritedByUsers u GROUP BY e ORDER BY COUNT(u) DESC")
    List<Event> findTop10ByFavoritesCount(Pageable pageable);

    @Query(value = "SELECT e FROM Event e JOIN e.favoritedByUsers u WHERE u.userName = :login OR u.email = :login",
            countQuery = "SELECT count(e) FROM Event e JOIN e.favoritedByUsers u WHERE u.userName = :login OR u.email = :login")
    Page<Event> findFavoritosByUsuarioLoginPaginated(@Param("login") String login, Pageable pageable);

}
