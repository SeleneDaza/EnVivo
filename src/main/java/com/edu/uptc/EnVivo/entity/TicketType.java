package com.edu.uptc.EnVivo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ticket_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TicketType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_type_id")
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;
}