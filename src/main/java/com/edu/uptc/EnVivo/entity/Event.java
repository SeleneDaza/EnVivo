package com.edu.uptc.EnVivo.entity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long event_id;

    @Column(nullable = false, length = 100)
    private String name;

    private LocalDate date;

    private Integer price;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String image;

    @Column(name = "interest_count")
    private Integer interestCount;

    @ManyToOne(optional = true)
    @JoinColumn(name = "category_id", nullable = true)
    private Category category;

    @ManyToMany(mappedBy = "favoriteEvents")
    private Set<User> favoritedByUsers = new HashSet<>();
}
