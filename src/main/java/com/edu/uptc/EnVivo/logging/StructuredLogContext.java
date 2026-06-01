package com.edu.uptc.EnVivo.logging;

import org.slf4j.MDC;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public final class StructuredLogContext {

    public static final String KEY_LOG_ID = "log_id";
    public static final String KEY_LEVEL = "structured_level";
    public static final String KEY_EVENT = "event";
    public static final String KEY_MODULE = "module";
    public static final String KEY_TRANSACTION_ID = "transaction_id";
    public static final String KEY_SESSION_ID = "session_id";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_CLIENT_IP = "client_ip";
    public static final String KEY_PAYMENT_PROVIDER = "payment_provider";
    public static final String KEY_STATUS = "status";
    public static final String KEY_ERROR_CODE = "error_code";
    public static final String KEY_DURATION_MS = "duration_ms";

    private static final AtomicLong LOG_SEQUENCE = new AtomicLong(0L);

    private StructuredLogContext() {
    }

    public static String currentTransactionId() {
        return value(KEY_TRANSACTION_ID);
    }

    public static String currentSessionId() {
        return value(KEY_SESSION_ID);
    }

    public static String currentUserId() {
        return value(KEY_USER_ID);
    }

    public static String currentClientIp() {
        return value(KEY_CLIENT_IP);
    }

    public static String currentPaymentProvider() {
        return value(KEY_PAYMENT_PROVIDER);
    }

    public static String ensureTransactionId() {
        String transactionId = currentTransactionId();
        if (transactionId == null || transactionId.isBlank()) {
            transactionId = UUID.randomUUID().toString();
            MDC.put(KEY_TRANSACTION_ID, transactionId);
        }
        return transactionId;
    }

    public static String nextLogId() {
        return String.valueOf(LOG_SEQUENCE.incrementAndGet());
    }

    public static Scope push(Map<String, String> values) {
        Map<String, String> snapshot = MDC.getCopyOfContextMap();
        if (values != null) {
            for (Map.Entry<String, String> entry : values.entrySet()) {
                String value = entry.getValue();
                if (value != null && !value.isBlank()) {
                    MDC.put(entry.getKey(), value);
                }
            }
        }
        return new Scope(snapshot);
    }

    public static Scope push(String key, String value) {
        Map<String, String> values = new LinkedHashMap<>();
        values.put(key, value);
        return push(values);
    }

    private static String value(String key) {
        String value = MDC.get(key);
        return value == null || value.isBlank() ? null : value;
    }

    public static final class Scope implements AutoCloseable {
        private final Map<String, String> snapshot;

        private Scope(Map<String, String> snapshot) {
            this.snapshot = snapshot;
        }

        @Override
        public void close() {
            if (snapshot == null || snapshot.isEmpty()) {
                MDC.clear();
                return;
            }
            MDC.setContextMap(snapshot);
        }
    }
}