package com.edu.uptc.EnVivo.repository;

import com.edu.uptc.EnVivo.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    @Query("SELECT t FROM Ticket t WHERE t.event.event_id = :eventId")
    List<Ticket> findByEventId(@Param("eventId") Long eventId);
}


