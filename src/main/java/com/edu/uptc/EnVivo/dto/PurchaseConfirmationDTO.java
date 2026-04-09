package com.edu.uptc.EnVivo.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class PurchaseConfirmationDTO {
    Long purchaseId;
    String purchaseDate;
    String eventName;
    String ticketType;
    Integer quantity;
    Integer total;
    List<PurchaseItemSummaryDTO> ticketItems;
    String buyerFullName;
    String buyerEmail;
    String buyerDocument;
    String maskedCard;
    String message;
}

