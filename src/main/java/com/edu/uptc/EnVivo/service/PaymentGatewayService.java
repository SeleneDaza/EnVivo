package com.edu.uptc.EnVivo.service;

import com.edu.uptc.EnVivo.logging.StructuredLogContext;
import com.edu.uptc.EnVivo.logging.StructuredLogService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
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

    private static final String GATEWAY_URL = "http://localhost:8002/pagos";
    private static final String EMPRESA_ID = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11";
    private static final String MODULE = "payment_gateway";

    private final RestTemplate restTemplate = new RestTemplate();
    private final StructuredLogService structuredLogService;

    public GatewayResult processPayment(long monto, String tipoTarjeta, String numeroTarjeta, String cvv) {
        String transactionId = StructuredLogContext.ensureTransactionId();
        String paymentProvider = normalizePaymentProvider(tipoTarjeta);

        structuredLogService.logInfo(MODULE, "PAYMENT_REQUEST_SENT", transactionId,
                StructuredLogContext.currentSessionId(), StructuredLogContext.currentUserId(),
                StructuredLogContext.currentClientIp(), paymentProvider, "SENT",
                "Payment request sent to external gateway.");
        
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
                structuredLogService.logError(MODULE, "PAYMENT_DECLINED", transactionId,
                        StructuredLogContext.currentSessionId(), StructuredLogContext.currentUserId(),
                        StructuredLogContext.currentClientIp(), paymentProvider,
                        "GATEWAY_EMPTY_RESPONSE",
                        "Gateway returned an empty response body.",
                        "The payment provider did not return a valid decision.", null);
                return new GatewayResult(false, "Respuesta vacia de la pasarela.");
            }
            
            if (body.success) {
                structuredLogService.logSuccess(MODULE, "PAYMENT_AUTHORIZED", transactionId,
                        StructuredLogContext.currentSessionId(), StructuredLogContext.currentUserId(),
                        StructuredLogContext.currentClientIp(), paymentProvider,
                        "AUTHORIZED", "Payment authorized by provider: " + safeMessage(body.message));
                return new GatewayResult(true, body.message);
            } else {
                structuredLogService.logError(MODULE, "PAYMENT_DECLINED", transactionId,
                        StructuredLogContext.currentSessionId(), StructuredLogContext.currentUserId(),
                        StructuredLogContext.currentClientIp(), paymentProvider,
                        "PAYMENT_DECLINED",
                        "Gateway returned a business rejection: " + safeMessage(body.message),
                        "The card issuer declined the transaction.", null);
                return new GatewayResult(false, body.message);
            }
        } catch (HttpServerErrorException e) {
            structuredLogService.logError(MODULE, "PAYMENT_DECLINED", transactionId,
                    StructuredLogContext.currentSessionId(), StructuredLogContext.currentUserId(),
                    StructuredLogContext.currentClientIp(), paymentProvider,
                    "PAYMENT_GATEWAY_HTTP_5XX",
                    "Gateway HTTP 5xx: " + e.getStatusCode() + " " + e.getStatusText(),
                    "The payment provider is temporarily unavailable.", null);
            throw new GatewayConnectionException("Pasarela respondió con error HTTP " + e.getStatusCode() + ": " + e.getStatusText());
        } catch (HttpClientErrorException e) {
            structuredLogService.logError(MODULE, "PAYMENT_DECLINED", transactionId,
                    StructuredLogContext.currentSessionId(), StructuredLogContext.currentUserId(),
                    StructuredLogContext.currentClientIp(), paymentProvider,
                    "PAYMENT_GATEWAY_HTTP_4XX",
                    "Gateway HTTP 4xx: " + e.getStatusCode() + " " + e.getStatusText(),
                    "The payment provider rejected the submitted request.", null);
            throw new GatewayConnectionException("Pasarela respondió con error HTTP " + e.getStatusCode() + ": " + e.getStatusText());
        } catch (ResourceAccessException e) {
            structuredLogService.logError(MODULE, "PAYMENT_DECLINED", transactionId,
                    StructuredLogContext.currentSessionId(), StructuredLogContext.currentUserId(),
                    StructuredLogContext.currentClientIp(), paymentProvider,
                    "PAYMENT_GATEWAY_UNREACHABLE",
                    "Gateway connection failed: " + safeMessage(e.getMessage()),
                    "The payment provider cannot be reached right now.", null);
            throw new GatewayConnectionException("Error conectando con la pasarela de pagos");
        } catch (RestClientException e) {
            structuredLogService.logError(MODULE, "PAYMENT_DECLINED", transactionId,
                    StructuredLogContext.currentSessionId(), StructuredLogContext.currentUserId(),
                    StructuredLogContext.currentClientIp(), paymentProvider,
                    "PAYMENT_GATEWAY_ERROR",
                    "Gateway client error: " + safeMessage(e.getMessage()),
                    "The payment provider request could not be completed.", null);
            throw new GatewayConnectionException("Error conectando con la pasarela de pagos");
        }
    }

    private String normalizePaymentProvider(String tipoTarjeta) {
        if (tipoTarjeta == null || tipoTarjeta.isBlank()) {
            return "UNKNOWN";
        }

        String normalized = tipoTarjeta.trim().toUpperCase();
        if (normalized.contains("VISA")) {
            return "VISA";
        }
        if (normalized.contains("MASTER")) {
            return "MASTERCARD";
        }
        return normalized;
    }

    private String safeMessage(String value) {
        return value == null || value.isBlank() ? "n/a" : value;
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
