package com.edu.uptc.EnVivo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventDetailDTO {
    private Long eventId;
    private String name;
    private String description;
    private LocalDate date;
    private String image;
    private String category;
    private List<TicketDTO> tickets;
}

