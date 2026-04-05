package com.edu.uptc.EnVivo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTicketDTO {
    private Long ticketTypeId;
    private Integer price;
    private Integer availableQuantity;
}

