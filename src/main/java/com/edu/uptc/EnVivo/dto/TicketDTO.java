package com.edu.uptc.EnVivo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketDTO {
    private Long id;
    private Long ticketTypeId;
    private String ticketTypeName;
    private Integer price;
    private Integer availableQuantity;
}

