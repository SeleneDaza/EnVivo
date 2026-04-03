package com.edu.uptc.EnVivo.repository;

import com.edu.uptc.EnVivo.entity.TicketType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TicketTypeRepository extends JpaRepository<TicketType, Long> {
    Optional<TicketType> findByNameIgnoreCase(String name);
}

