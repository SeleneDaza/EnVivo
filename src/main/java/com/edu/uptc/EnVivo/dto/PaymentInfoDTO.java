package com.edu.uptc.EnVivo.dto;

import lombok.Data;

@Data
public class PaymentInfoDTO {
    private String cardHolder;
    private String cardNumber;
    private String expiry;
    private String cvv;
}

