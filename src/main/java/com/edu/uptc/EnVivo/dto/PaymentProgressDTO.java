package com.edu.uptc.EnVivo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentProgressDTO {
    private String fase;
    private String mensaje;
    private String detalle;
    private String estadoTransaccion;
}
