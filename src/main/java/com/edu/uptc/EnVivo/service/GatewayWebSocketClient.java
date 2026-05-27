package com.edu.uptc.EnVivo.service;

import com.edu.uptc.EnVivo.dto.PaymentProgressDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.function.Consumer;

public class GatewayWebSocketClient extends WebSocketClient {

    private static final Logger log = LoggerFactory.getLogger(GatewayWebSocketClient.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    private final String paymentPayload;
    private final Consumer<PaymentProgressDTO> onProgress;
    private final Consumer<PaymentProgressDTO> onComplete;
    private final Consumer<String> onError;

    public GatewayWebSocketClient(URI serverUri,
                                   String paymentPayload,
                                   Consumer<PaymentProgressDTO> onProgress,
                                   Consumer<PaymentProgressDTO> onComplete,
                                   Consumer<String> onError) {
        super(serverUri);
        this.paymentPayload = paymentPayload;
        this.onProgress = onProgress;
        this.onComplete = onComplete;
        this.onError = onError;
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        send(paymentPayload);
    }

    @Override
    public void onMessage(String message) {
        try {
            PaymentProgressDTO dto = mapper.readValue(message, PaymentProgressDTO.class);
            onProgress.accept(dto);
            if ("resultado_final".equals(dto.getFase())) {
                onComplete.accept(dto);
            }
        } catch (Exception e) {
            onError.accept(e.getMessage());
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        log.info("WebSocket closed — code: {}, reason: {}", code, reason);
    }

    @Override
    public void onError(Exception ex) {
        onError.accept(ex.getMessage());
    }
}
