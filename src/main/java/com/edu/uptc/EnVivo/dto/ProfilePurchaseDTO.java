package com.edu.uptc.EnVivo.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProfilePurchaseDTO {
    private Long purchaseId;
    private String eventName;
    private LocalDate eventDate;
    private Integer totalTickets;
}


