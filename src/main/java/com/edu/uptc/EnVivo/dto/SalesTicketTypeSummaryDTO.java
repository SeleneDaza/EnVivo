package com.edu.uptc.EnVivo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SalesTicketTypeSummaryDTO {

    private Long eventId;
    private String ticketTypeName;
    private Integer unitPrice;
    private long soldQuantity;
    private long revenue;
}

