package com.edu.uptc.EnVivo.entity;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "events")
@NoArgsConstructor // Requerido por JPA y tus servicios
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long event_id; // Mantenemos el nombre original para no romper el código legacy

    @Column(nullable = false, length = 100)
    private String name;

    private LocalDate date;

    @Column(length = 5000)
    private String description;

    private String image;

    @Column(name = "interest_count")
    private Integer interestCount = 0;

    @ManyToOne(optional = true)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToMany(mappedBy = "favoriteEvents")
    private Set<User> favoritedByUsers = new HashSet<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Ticket> tickets = new HashSet<>();

    // --- GETTERS ---
    // Respetamos la firma exacta de Lombok para que el código actual siga compilando
    public Long getEvent_id() { return event_id; }
    public String getName() { return name; }
    public LocalDate getDate() { return date; }
    public String getDescription() { return description; }
    public String getImage() { return image; }
    public Integer getInterestCount() { return interestCount; }
    public Category getCategory() { return category; }
    public Set<User> getFavoritedByUsers() { return favoritedByUsers; }
    public Set<Ticket> getTickets() { return tickets; }

    // --- SETTERS DEFENSIVOS ---

    public void setEvent_id(Long event_id) {
        if (this.event_id != null && !this.event_id.equals(event_id)) {
            throw new IllegalStateException("No se puede modificar el ID de un evento existente.");
        }
        this.event_id = event_id;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del evento no puede estar vacío.");
        }
        this.name = name;
    }

    public void setDate(LocalDate date) { this.date = date; }
    public void setDescription(String description) { this.description = description; }
    public void setImage(String image) { this.image = image; }

    public void setInterestCount(Integer interestCount) {
        if (interestCount != null && interestCount < 0) {
            throw new IllegalArgumentException("El contador de interés no puede ser negativo.");
        }
        this.interestCount = interestCount != null ? interestCount : 0;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    // --- PROTECCIÓN DE COLECCIONES ---
    
    // Mantiene la colección proxy de Hibernate intacta y sincroniza la relación bidireccional
    public void setTickets(Set<Ticket> newTickets) {
        this.tickets.clear();
        if (newTickets != null) {
            this.tickets.addAll(newTickets);
            this.tickets.forEach(ticket -> ticket.setEvent(this));
        }
    }

    public void setFavoritedByUsers(Set<User> newUsers) {
        this.favoritedByUsers.clear();
        if (newUsers != null) {
            this.favoritedByUsers.addAll(newUsers);
        }
    }

    // --- MÉTODOS DE UTILIDAD RECOMENDADOS ---
    
    public void addTicket(Ticket ticket) {
        this.tickets.add(ticket);
        ticket.setEvent(this);
    }

    public void removeTicket(Ticket ticket) {
        this.tickets.remove(ticket);
        ticket.setEvent(null);
    }

    // --- EQUALS & HASHCODE SEGUROS PARA JPA ---
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Event event)) return false;
        // Basado únicamente en el ID respetando el nombre exacto
        return event_id != null && event_id.equals(event.getEvent_id());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}