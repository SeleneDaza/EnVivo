package com.edu.uptc.EnVivo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PaymentInfoDTO {
    private String cardHolder;
    private String cardNumber;
    private String expiry;
    private String cvv;

    @JsonProperty("tipo_tarjeta")
    private String tipoTarjeta;

}

