package com.edu.uptc.EnVivo.entity;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "purchase_details")
@NoArgsConstructor // Requerido por JPA y tus servicios
public class PurchaseDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "detail_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "purchase_id", nullable = false)
    private Purchase purchase;

    @ManyToOne
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price")
    private Integer unitPrice;

    @Column(name = "subtotal")
    private Integer subtotal;

    // --- GETTERS (Firmas exactas de Lombok) ---
    
    public Long getId() { return id; }
    public Purchase getPurchase() { return purchase; }
    public Ticket getTicket() { return ticket; }
    public Integer getQuantity() { return quantity; }
    public Integer getUnitPrice() { return unitPrice; }
    public Integer getSubtotal() { return subtotal; }

    // --- SETTERS DEFENSIVOS ---

    public void setId(Long id) {
        if (this.id != null && !this.id.equals(id)) {
            throw new IllegalStateException("No se puede modificar el ID de un detalle de compra existente.");
        }
        this.id = id;
    }

    public void setPurchase(Purchase purchase) {
        if (purchase == null) {
            throw new IllegalArgumentException("El detalle de compra debe estar asociado a una factura (Purchase).");
        }
        this.purchase = purchase;
    }

    public void setTicket(Ticket ticket) {
        if (ticket == null) {
            throw new IllegalArgumentException("El detalle de compra debe estar asociado a un Ticket.");
        }
        this.ticket = ticket;
    }

    public void setQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("La cantidad comprada debe ser mayor a cero.");
        }
        this.quantity = quantity;
    }

    public void setUnitPrice(Integer unitPrice) {
        if (unitPrice != null && unitPrice < 0) {
            throw new IllegalArgumentException("El precio unitario no puede ser negativo.");
        }
        this.unitPrice = unitPrice;
    }

    public void setSubtotal(Integer subtotal) {
        if (subtotal != null && subtotal < 0) {
            throw new IllegalArgumentException("El subtotal no puede ser negativo.");
        }
        this.subtotal = subtotal;
    }

    // --- EQUALS & HASHCODE SEGUROS PARA JPA ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PurchaseDetail detail)) return false;
        // Igualdad basada estrictamente en la llave primaria
        return id != null && id.equals(detail.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}