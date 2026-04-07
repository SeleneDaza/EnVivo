package com.edu.uptc.EnVivo.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PurchaseConfirmationDTO {
    Long purchaseId;
    String purchaseDate;
    String eventName;
    String ticketType;
    Integer quantity;
    Integer total;
    String buyerFullName;
    String buyerEmail;
    String buyerDocument;
    String maskedCard;
    String message;
}

