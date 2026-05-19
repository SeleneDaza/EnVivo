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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentGatewayService {

    private static final Logger log = LoggerFactory.getLogger(PaymentGatewayService.class);
    private static final String GATEWAY_URL = "http://localhost:8002/pagos";
    private static final String EMPRESA_ID = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11";

    private final RestTemplate restTemplate = new RestTemplate();

    public GatewayResult processPayment(long monto, String tipoTarjeta, String numeroTarjeta, String cvv) {
        String maskedCard = maskCardNumber(numeroTarjeta);
        
        // Log inicial de procesamiento
        log.info("Iniciando procesamiento de pago - monto: {}, tarjeta: {}", monto, maskedCard);
        
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
                log.warn("Pasarela respondió con respuesta vacia");
                return new GatewayResult(false, "Respuesta vacia de la pasarela.");
            }
            
            // Evaluar la respuesta
            if (body.success) {
                log.info("Pago procesado exitosamente - Respuesta: Transacción aprobada ID:{}", body.message);
                return new GatewayResult(true, body.message);
            } else {
                // Error de negocio (rechazo del pago)
                log.warn("Pasarela respondió con error de negocio - Status: 200, Mensaje: {}", body.message);
                return new GatewayResult(false, body.message);
            }
        } catch (HttpServerErrorException e) {
            // Error HTTP 5xx
            log.error("Pasarela respondió con error HTTP - Status: {}, Mensaje: {}, Body: {}", 
                e.getStatusCode(), e.getStatusText(), e.getResponseBodyAsString());
            throw new GatewayConnectionException("Pasarela respondió con error HTTP " + e.getStatusCode() + ": " + e.getStatusText());
        } catch (HttpClientErrorException e) {
            // Error HTTP 4xx
            log.error("Pasarela respondió con error HTTP - Status: {}, Mensaje: {}", 
                e.getStatusCode(), e.getStatusText());
            throw new GatewayConnectionException("Pasarela respondió con error HTTP " + e.getStatusCode() + ": " + e.getStatusText());
        } catch (ResourceAccessException e) {
            // Error de conexión (Connection refused, timeout, etc.)
            String errorMessage = e.getMessage();
            log.error("NO SE PUDO CONECTAR CON LA PASARELA DE PAGOS (sin respuesta) - Error: {}", errorMessage);
            throw new GatewayConnectionException("Error conectando con la pasarela de pagos");
        } catch (RestClientException e) {
            // Otros errores
            String errorMessage = e.getMessage();
            log.error("Error al conectar con la pasarela de pagos - Error: {}", errorMessage);
            throw new GatewayConnectionException("Error conectando con la pasarela de pagos");
        }
    }
    
    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        String digits = cardNumber.replaceAll("\\D", "");
        if (digits.length() <= 4) {
            return "****" + digits;
        }
        return "****" + digits.substring(digits.length() - 4);
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
