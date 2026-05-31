package com.edu.uptc.EnVivo.logging;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class WebSocketChannelLoggingInterceptor implements ChannelInterceptor {

    private final StructuredLogService structuredLogService;

    public WebSocketChannelLoggingInterceptor(StructuredLogService structuredLogService) {
        this.structuredLogService = structuredLogService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        StompCommand command = accessor.getCommand();
        if (command == null) {
            return message;
        }

        Map<String, String> context = new LinkedHashMap<>();
        put(context, StructuredLogContext.KEY_SESSION_ID, accessor.getSessionId());

        Object transactionId = accessor.getSessionAttributes() == null ? null : accessor.getSessionAttributes().get(StructuredLogContext.KEY_TRANSACTION_ID);
        put(context, StructuredLogContext.KEY_TRANSACTION_ID, transactionId == null ? null : transactionId.toString());

        try (StructuredLogContext.Scope ignored = structuredLogService.scope(context)) {
            if (StompCommand.CONNECT.equals(command)) {
                structuredLogService.logSuccess("websocket", "WS_CONNECTED", "CONNECTED", "WebSocket session established.");
            } else if (StompCommand.SEND.equals(command)) {
                structuredLogService.logInfo("websocket", "WS_MESSAGE_RECEIVED", "Inbound STOMP message received.");
            }
        }

        return message;
    }

    private void put(Map<String, String> values, String key, String value) {
        if (value != null && !value.isBlank()) {
            values.put(key, value);
        }
    }
}