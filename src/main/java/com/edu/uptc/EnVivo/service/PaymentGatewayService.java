package com.edu.uptc.EnVivo.service;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentGatewayService {

    private static final Logger log = LoggerFactory.getLogger(PaymentGatewayService.class);
    private static final String GATEWAY_URL = "http://localhost:8080/pagos";
    private static final String EMPRESA_ID = "550e8400-e29b-41d4-a716-446655440000";

    private final RestTemplate restTemplate = new RestTemplate();

    public GatewayResult processPayment(long monto, String tipoTarjeta, String numeroTarjeta, String cvv) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("empresa_id", EMPRESA_ID);
        payload.put("monto", monto);
        payload.put("tipo_tarjeta", tipoTarjeta);
        payload.put("numero_tarjeta", numeroTarjeta);
        payload.put("cvv", cvv);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<GatewayResponse> response = restTemplate.postForEntity(GATEWAY_URL, request, GatewayResponse.class);
            GatewayResponse body = response.getBody();
            if (body == null) {
                return new GatewayResult(false, "Respuesta vacia de la pasarela.");
            }
            return new GatewayResult(body.success, body.message);
        } catch (RestClientException e) {
            log.error("Error comunicando con pasarela de pagos", e);
            throw new GatewayConnectionException("Error conectando con la pasarela de pagos");
        }
    }

    @Data
    public static class GatewayResult {
        private final boolean success;
        private final String message;
    }

    @Data
    static class GatewayResponse {
        public boolean success;
        public String message;
    }

    public static class GatewayConnectionException extends RuntimeException {
        public GatewayConnectionException(String message) {
            super(message);
        }
    }
}
