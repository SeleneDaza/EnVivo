package com.edu.uptc.EnVivo.entity;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ticket_types")
@NoArgsConstructor // Requerido por JPA y tus servicios
public class TicketType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_type_id")
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    // --- GETTERS (Firmas exactas de Lombok) ---

    public Long getId() { 
        return id; 
    }

    public String getName() { 
        return name; 
    }

    // --- SETTERS DEFENSIVOS ---

    public void setId(Long id) {
        if (this.id != null && !this.id.equals(id)) {
            throw new IllegalStateException("No se puede modificar el ID de un tipo de ticket existente.");
        }
        this.id = id;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del tipo de ticket no puede ser nulo ni estar vacío.");
        }
        if (name.length() > 50) {
            throw new IllegalArgumentException("El nombre del tipo de ticket no puede exceder los 50 caracteres.");
        }
        this.name = name.trim();
    }

    // --- EQUALS & HASHCODE SEGUROS PARA JPA ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TicketType ticketType)) return false;
        // Igualdad basada estrictamente en la llave primaria
        return id != null && id.equals(ticketType.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}