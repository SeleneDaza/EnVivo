package com.edu.uptc.EnVivo.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class CreateEventDTO {
    private String name;
    private String description;
    private LocalDate date;
    private Integer price;
    private String image;
    private String category;
}
