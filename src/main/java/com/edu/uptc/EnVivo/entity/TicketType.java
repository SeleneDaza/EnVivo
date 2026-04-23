package com.edu.uptc.EnVivo.entity;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ticket_types")
@NoArgsConstructor
public class TicketType extends BaseEntity {

    @Column(nullable = false, length = 50)
    private String name;

    public String getName() {
        return name;
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
}