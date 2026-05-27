package com.edu.uptc.EnVivo.dto;

import lombok.Data;

@Data
public class PaymentProgressDTO {
    private String fase;
    private String mensaje;
    private String detalle;
    private String estadoTransaccion;
    private String timestamp;
}
