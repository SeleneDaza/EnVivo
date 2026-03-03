package com.edu.uptc.EnVivo.repository;

import com.edu.uptc.EnVivo.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    Page<Event> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @Query("SELECT e FROM Event e LEFT JOIN e.favoritedByUsers u GROUP BY e ORDER BY COUNT(u) DESC")
    List<Event> findTop10ByFavoritesCount(Pageable pageable);

}
