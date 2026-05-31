package com.edu.uptc.EnVivo.logging;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;
import java.util.UUID;

@Component
public class WebSocketHandshakeLoggingInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String sessionId = UUID.randomUUID().toString();
        String transactionId = StructuredLogContext.currentTransactionId();

        if (request instanceof ServletServerHttpRequest servletRequest) {
            var httpSession = servletRequest.getServletRequest().getSession(true);
            sessionId = httpSession.getId();
        }

        attributes.put(StructuredLogContext.KEY_SESSION_ID, sessionId);
        if (transactionId != null && !transactionId.isBlank()) {
            attributes.put(StructuredLogContext.KEY_TRANSACTION_ID, transactionId);
        }

        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // Connection logging is handled when STOMP CONNECT frames arrive.
    }
}