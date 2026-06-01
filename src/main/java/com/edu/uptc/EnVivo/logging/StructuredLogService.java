package com.edu.uptc.EnVivo.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class StructuredLogService {

    private final Logger logger = LoggerFactory.getLogger(StructuredLogService.class);

    public StructuredLogContext.Scope scope(Map<String, String> values) {
        return StructuredLogContext.push(values);
    }

    public StructuredLogContext.Scope scope(String key, String value) {
        return StructuredLogContext.push(key, value);
    }

    public String ensureTransactionId() {
        return StructuredLogContext.ensureTransactionId();
    }

    public void logInfo(String module, String event, String message) {
        log("INFO", module, event, null, null, null, null, null, null, null, null, message, null);
    }

    public void logSuccess(String module, String event, String status, String message) {
        log("SUCCESS", module, event, null, null, null, null, null, status, null, null, message, null);
    }

    public void logError(String module, String event, String errorCode, String technicalDescription,
                         String functionalDescription) {
        String message = "technical=" + safe(technicalDescription) + " | functional=" + safe(functionalDescription);
        log("ERROR", module, event, null, null, null, null, null, "FAILED", errorCode, null, message, null);
    }

    public void logInfo(String module, String event, String transactionId, String sessionId, String userId,
                        String clientIp, String paymentProvider, String status, String message) {
        log("INFO", module, event, transactionId, sessionId, userId, clientIp, paymentProvider, status, null, null, message, null);
    }

    public void logSuccess(String module, String event, String transactionId, String sessionId, String userId,
                           String clientIp, String paymentProvider, String status, String message) {
        log("SUCCESS", module, event, transactionId, sessionId, userId, clientIp, paymentProvider, status, null, null, message, null);
    }

    public void logError(String module, String event, String transactionId, String sessionId, String userId,
                         String clientIp, String paymentProvider, String errorCode, String technicalDescription,
                         String functionalDescription, Long durationMs) {
        String message = "technical=" + safe(technicalDescription) + " | functional=" + safe(functionalDescription);
        log("ERROR", module, event, transactionId, sessionId, userId, clientIp, paymentProvider, "FAILED", errorCode, durationMs, message, null);
    }

    private void log(String semanticLevel, String module, String event, String transactionId, String sessionId,
                     String userId, String clientIp, String paymentProvider, String status, String errorCode,
                     Long durationMs, String message, Throwable throwable) {
        Map<String, String> values = new LinkedHashMap<>();
        values.put(StructuredLogContext.KEY_LOG_ID, StructuredLogContext.nextLogId());
        values.put(StructuredLogContext.KEY_LEVEL, semanticLevel);
        put(values, StructuredLogContext.KEY_EVENT, event);
        put(values, StructuredLogContext.KEY_MODULE, module);
        put(values, StructuredLogContext.KEY_TRANSACTION_ID, transactionId);
        put(values, StructuredLogContext.KEY_SESSION_ID, sessionId);
        put(values, StructuredLogContext.KEY_USER_ID, userId);
        put(values, StructuredLogContext.KEY_CLIENT_IP, clientIp);
        put(values, StructuredLogContext.KEY_PAYMENT_PROVIDER, paymentProvider);
        put(values, StructuredLogContext.KEY_STATUS, status);
        put(values, StructuredLogContext.KEY_ERROR_CODE, errorCode);
        put(values, StructuredLogContext.KEY_DURATION_MS, durationMs == null ? null : String.valueOf(durationMs));

        try (StructuredLogContext.Scope ignored = StructuredLogContext.push(values)) {
            if (throwable != null) {
                logger.error(message, throwable);
                return;
            }

            if ("ERROR".equals(semanticLevel)) {
                logger.error(message);
            } else {
                logger.info(message);
            }
        }
    }

    private void put(Map<String, String> values, String key, String value) {
        if (value != null && !value.isBlank()) {
            values.put(key, value);
        }
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "n/a" : value;
    }
}