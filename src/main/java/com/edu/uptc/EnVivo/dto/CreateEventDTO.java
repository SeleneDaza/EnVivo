package com.edu.uptc.EnVivo.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Data
public class CreateEventDTO {
    private Long event_id;
    private String name;
    private String description;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
    private Integer price;
    private String image;
    private MultipartFile imagenArchivo;
    private String category;
}
