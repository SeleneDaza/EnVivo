package com.edu.uptc.EnVivo.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_name", unique = true)
    private String userName;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "document", unique = true)
    private String document;

    @Column(unique = true)
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(nullable = false)
    private String password;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "user_favorites",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "event_id")
    )
    private Set<Event> favoriteEvents = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<Purchase> purchases = new HashSet<>();

    // Constructor vacío exigido por JPA
    public User() {
    }

    // --- GETTERS (Firmas exactas de Lombok) ---

    public Long getId() { return id; }
    public String getUserName() { return userName; }
    public String getFullName() { return fullName; }
    public String getDocument() { return document; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getPassword() { return password; }
    public Set<Role> getRoles() { return roles; }
    public Set<Event> getFavoriteEvents() { return favoriteEvents; }
    public Set<Purchase> getPurchases() { return purchases; }

    // --- SETTERS DEFENSIVOS ---

    public void setId(Long id) {
        if (this.id != null && !this.id.equals(id)) {
            throw new IllegalStateException("No se puede modificar el ID de un usuario existente.");
        }
        this.id = id;
    }

    public void setUserName(String userName) {
        if (userName == null || userName.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de usuario no puede ser nulo ni estar vacío.");
        }
        this.userName = userName.trim();
    }

    public void setFullName(String fullName) {
        this.fullName = fullName != null ? fullName.trim() : null;
    }

    public void setDocument(String document) {
        this.document = document != null ? document.trim() : null;
    }

    public void setEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("El email no puede ser nulo ni estar vacío.");
        }
        this.email = email.trim();
    }

    public void setPhone(String phone) {
        this.phone = phone != null ? phone.trim() : null;
    }

    public void setPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede ser nula ni estar vacía.");
        }
        this.password = password; // Nota: Aquí ya debería llegar encriptada desde tu servicio
    }

    // --- PROTECCIÓN DE COLECCIONES ---

    public void setRoles(Set<Role> newRoles) {
        this.roles.clear();
        if (newRoles != null) {
            this.roles.addAll(newRoles);
        }
    }

    public void setFavoriteEvents(Set<Event> newFavoriteEvents) {
        // Limpiamos las referencias bidireccionales de los eventos actuales
        this.favoriteEvents.forEach(event -> event.getFavoritedByUsers().remove(this));
        this.favoriteEvents.clear();
        
        if (newFavoriteEvents != null) {
            this.favoriteEvents.addAll(newFavoriteEvents);
            // Establecemos las referencias bidireccionales de los nuevos eventos
            this.favoriteEvents.forEach(event -> event.getFavoritedByUsers().add(this));
        }
    }

    public void setPurchases(Set<Purchase> newPurchases) {
        this.purchases.clear();
        if (newPurchases != null) {
            this.purchases.addAll(newPurchases);
            // Mantenemos la relación bidireccional
            this.purchases.forEach(purchase -> purchase.setUser(this));
        }
    }

    // --- MÉTODOS DE UTILIDAD (Los que ya tenías, mejorados con validación de nulos) ---

    public void addFavoriteEvent(Event event) {
        if (event != null) {
            this.favoriteEvents.add(event);
            event.getFavoritedByUsers().add(this);
        }
    }

    public void removeFavoriteEvent(Event event) {
        if (event != null) {
            this.favoriteEvents.remove(event);
            event.getFavoritedByUsers().remove(this);
        }
    }

    // --- EQUALS & HASHCODE SEGUROS PARA JPA ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        // Igualdad basada estrictamente en la llave primaria
        return id != null && id.equals(user.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}