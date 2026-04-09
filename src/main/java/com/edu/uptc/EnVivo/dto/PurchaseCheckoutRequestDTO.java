package com.edu.uptc.EnVivo.dto;

import lombok.Data;

import java.util.List;

@Data
public class PurchaseCheckoutRequestDTO {
    private Long eventId;
    private List<PurchaseCheckoutItemDTO> items;

    // Compatibilidad temporal con payload antiguo.
    private Long ticketId;
    private Integer quantity;
    private BuyerInfoDTO buyer;
    private PaymentInfoDTO payment;
}

