package com.edu.uptc.EnVivo.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PurchaseItemSummaryDTO {
    Long ticketId;
    String ticketType;
    Integer quantity;
    Integer unitPrice;
    Integer subtotal;
}

