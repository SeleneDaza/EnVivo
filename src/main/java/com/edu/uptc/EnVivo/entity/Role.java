package com.edu.uptc.EnVivo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    // Constructor vacío exigido por JPA (Lombok @Data lo generaba por debajo al no haber campos final)
    public Role() {
    }

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
            throw new IllegalStateException("No se puede modificar el ID de un rol existente.");
        }
        this.id = id;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del rol no puede ser nulo ni estar vacío.");
        }
        /* * Nota: Si usas Spring Security, los roles suelen tener el prefijo "ROLE_".
         * Si quisieras asegurarlo a nivel de entidad, podrías concatenarlo aquí, 
         * pero para no romper tus servicios actuales, guardamos exactamente lo que envíen.
         */
        this.name = name.trim(); // Un pequeño favor: limpiamos espacios accidentales
    }

    // --- EQUALS & HASHCODE SEGUROS PARA JPA ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Role role)) return false;
        // Igualdad basada estrictamente en la llave primaria
        return id != null && id.equals(role.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}