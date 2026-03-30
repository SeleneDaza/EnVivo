package com.edu.uptc.EnVivo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "purchase_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
}