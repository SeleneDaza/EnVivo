package com.edu.uptc.EnVivo.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.LayoutBase;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class StructuredCsvLayout extends LayoutBase<ILoggingEvent> {

    private static final DateTimeFormatter ISO_UTC = DateTimeFormatter.ISO_INSTANT.withZone(ZoneOffset.UTC);

    @Override
    public String doLayout(ILoggingEvent event) {
        Map<String, String> mdc = event.getMDCPropertyMap();
        String message = event.getFormattedMessage();
        if (event.getThrowableProxy() != null) {
            String throwable = event.getThrowableProxy().getClassName();
            String throwableMessage = event.getThrowableProxy().getMessage();
            if (throwableMessage != null && !throwableMessage.isBlank()) {
                message = message + " | throwable=" + throwable + ": " + throwableMessage;
            } else {
                message = message + " | throwable=" + throwable;
            }
        }

        return csv(ISO_UTC.format(Instant.ofEpochMilli(event.getTimeStamp()))) + ','
            + csv(valueOrDefault(mdc, StructuredLogContext.KEY_LOG_ID, StructuredLogContext.nextLogId())) + ','
                + csv(valueOrDefault(mdc, StructuredLogContext.KEY_LEVEL, event.getLevel().toString())) + ','
                + csv(valueOrDefault(mdc, StructuredLogContext.KEY_EVENT, "")) + ','
                + csv(valueOrDefault(mdc, StructuredLogContext.KEY_MODULE, "")) + ','
                + csv(valueOrDefault(mdc, StructuredLogContext.KEY_TRANSACTION_ID, "")) + ','
                + csv(valueOrDefault(mdc, StructuredLogContext.KEY_SESSION_ID, "")) + ','
                + csv(valueOrDefault(mdc, StructuredLogContext.KEY_USER_ID, "")) + ','
                + csv(valueOrDefault(mdc, StructuredLogContext.KEY_CLIENT_IP, "")) + ','
                + csv(valueOrDefault(mdc, StructuredLogContext.KEY_PAYMENT_PROVIDER, "")) + ','
                + csv(valueOrDefault(mdc, StructuredLogContext.KEY_STATUS, "")) + ','
                + csv(valueOrDefault(mdc, StructuredLogContext.KEY_ERROR_CODE, "")) + ','
                + csv(valueOrDefault(mdc, StructuredLogContext.KEY_DURATION_MS, "")) + ','
                + csv(message) + System.lineSeparator();
    }

    private String valueOrDefault(Map<String, String> mdc, String key, String fallback) {
        String value = mdc.get(key);
        return value == null ? fallback : value;
    }

    private String csv(String value) {
        String normalized = value == null ? "" : value.replace("\r", " ").replace("\n", " ").replace("\"", "\"\"");
        return "\"" + normalized + "\"";
    }
}