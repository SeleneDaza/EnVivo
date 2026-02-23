package com.edu.uptc.EnVivo.entity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "eventos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evento")
    private Long idEvento;

    @Column(nullable = false, length = 100)
    private String nombre;

    private LocalDate fecha;

    private Integer valor;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    private String imagen;

    @Column(name = "conteo_interes")
    private Integer conteoInteres;

    @ManyToOne
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria categoria;
}
