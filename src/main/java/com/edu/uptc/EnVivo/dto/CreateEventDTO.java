package com.edu.uptc.EnVivo.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Data
public class CreateEventDTO {
    private Long event_id;
    private String name;
    private String description;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
    private String image;
    private String category;
    private List<CreateTicketDTO> tickets;
}
