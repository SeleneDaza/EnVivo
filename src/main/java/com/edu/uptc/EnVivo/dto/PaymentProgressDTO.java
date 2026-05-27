package com.edu.uptc.EnVivo.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)  // ← también agrega esto por seguridad
public class PaymentProgressDTO {
    private String fase;
    private String mensaje;
    private String detalle;

    @JsonProperty("estado_transaccion")  // ← mapea el campo snake_case al atributo camelCase
    private String estadoTransaccion;

    private String timestamp;
}
