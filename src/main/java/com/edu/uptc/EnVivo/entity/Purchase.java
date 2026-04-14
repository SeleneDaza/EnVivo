package com.edu.uptc.EnVivo.entity;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchases")
@NoArgsConstructor // Requerido por JPA y tus servicios
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "purchase_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "purchase_date", nullable = false)
    private LocalDateTime purchaseDate;

    @Column(name = "buyer_full_name")
    private String buyerFullName;

    @Column(name = "buyer_document")
    private String buyerDocument;

    @Column(name = "buyer_email")
    private String buyerEmail;

    @Column(name = "buyer_phone")
    private String buyerPhone;

    @Column(name = "total_amount")
    private Integer totalAmount;

    @OneToMany(mappedBy = "purchase", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PurchaseDetail> details = new ArrayList<>();

    // --- GETTERS (Firmas exactas de Lombok) ---
    public Long getId() { return id; }
    public User getUser() { return user; }
    public LocalDateTime getPurchaseDate() { return purchaseDate; }
    public String getBuyerFullName() { return buyerFullName; }
    public String getBuyerDocument() { return buyerDocument; }
    public String getBuyerEmail() { return buyerEmail; }
    public String getBuyerPhone() { return buyerPhone; }
    public Integer getTotalAmount() { return totalAmount; }
    public List<PurchaseDetail> getDetails() { return details; }

    // --- SETTERS DEFENSIVOS ---

    public void setId(Long id) {
        if (this.id != null && !this.id.equals(id)) {
            throw new IllegalStateException("No se puede modificar el ID de una compra existente.");
        }
        this.id = id;
    }

    public void setUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("El usuario de la compra no puede ser nulo.");
        }
        this.user = user;
    }

    public void setPurchaseDate(LocalDateTime purchaseDate) {
        if (purchaseDate == null) {
            throw new IllegalArgumentException("La fecha de compra no puede ser nula.");
        }
        this.purchaseDate = purchaseDate;
    }

    public void setBuyerFullName(String buyerFullName) { this.buyerFullName = buyerFullName; }
    public void setBuyerDocument(String buyerDocument) { this.buyerDocument = buyerDocument; }
    public void setBuyerEmail(String buyerEmail) { this.buyerEmail = buyerEmail; }
    public void setBuyerPhone(String buyerPhone) { this.buyerPhone = buyerPhone; }

    public void setTotalAmount(Integer totalAmount) {
        if (totalAmount != null && totalAmount < 0) {
            throw new IllegalArgumentException("El monto total de la compra no puede ser negativo.");
        }
        this.totalAmount = totalAmount;
    }

    // --- PROTECCIÓN DE COLECCIONES ---
    
    public void setDetails(List<PurchaseDetail> newDetails) {
        // Mantenemos viva la colección original (PersistentBag de Hibernate)
        this.details.clear();
        if (newDetails != null) {
            this.details.addAll(newDetails);
            // Aseguramos la relación bidireccional automáticamente
            this.details.forEach(detail -> detail.setPurchase(this));
        }
    }

    // --- MÉTODOS DE UTILIDAD (Mantenemos el tuyo y agregamos el inverso) ---
    
    public void addDetail(PurchaseDetail detail) {
        if (detail != null) {
            this.details.add(detail);
            detail.setPurchase(this);
        }
    }

    public void removeDetail(PurchaseDetail detail) {
        if (detail != null) {
            this.details.remove(detail);
            detail.setPurchase(null);
        }
    }

    // --- EQUALS & HASHCODE SEGUROS PARA JPA ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Purchase purchase)) return false;
        // La igualdad se basa estrictamente en la llave primaria
        return id != null && id.equals(purchase.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}