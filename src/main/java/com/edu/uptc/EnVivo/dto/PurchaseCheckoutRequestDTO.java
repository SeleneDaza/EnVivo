package com.edu.uptc.EnVivo.dto;

import lombok.Data;

@Data
public class PurchaseCheckoutRequestDTO {
    private Long eventId;
    private Long ticketId;
    private Integer quantity;
    private BuyerInfoDTO buyer;
    private PaymentInfoDTO payment;
}

