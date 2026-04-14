package com.edu.uptc.EnVivo.entity;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tickets")
@NoArgsConstructor // Requerido por JPA y tus servicios
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne
    @JoinColumn(name = "ticket_type_id", nullable = false)
    private TicketType ticketType;

    @Column(nullable = false)
    private Integer price;

    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity;

    // --- GETTERS (Firmas exactas de Lombok) ---

    public Long getId() { return id; }
    public Event getEvent() { return event; }
    public TicketType getTicketType() { return ticketType; }
    public Integer getPrice() { return price; }
    public Integer getAvailableQuantity() { return availableQuantity; }

    // --- SETTERS DEFENSIVOS ---

    public void setId(Long id) {
        if (this.id != null && !this.id.equals(id)) {
            throw new IllegalStateException("No se puede modificar el ID de un ticket existente.");
        }
        this.id = id;
    }

    public void setEvent(Event event) {
        if (event == null) {
            throw new IllegalArgumentException("El ticket debe estar asociado a un evento obligatoriamente.");
        }
        this.event = event;
    }

    public void setTicketType(TicketType ticketType) {
        if (ticketType == null) {
            throw new IllegalArgumentException("El ticket debe tener un tipo de ticket asociado.");
        }
        this.ticketType = ticketType;
    }

    public void setPrice(Integer price) {
        // Un evento puede ser gratuito (precio = 0), pero no negativo ni nulo
        if (price == null || price < 0) {
            throw new IllegalArgumentException("El precio del ticket no puede ser nulo ni negativo.");
        }
        this.price = price;
    }

    public void setAvailableQuantity(Integer availableQuantity) {
        // La cantidad disponible no puede bajar de cero bajo ninguna circunstancia
        if (availableQuantity == null || availableQuantity < 0) {
            throw new IllegalArgumentException("La cantidad disponible de tickets no puede ser nula ni negativa.");
        }
        this.availableQuantity = availableQuantity;
    }

    // --- EQUALS & HASHCODE SEGUROS PARA JPA ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ticket ticket)) return false;
        // Igualdad basada estrictamente en la llave primaria
        return id != null && id.equals(ticket.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}