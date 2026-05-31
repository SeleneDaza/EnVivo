package com.edu.uptc.EnVivo.controller;

import com.edu.uptc.EnVivo.dto.PaymentProgressDTO;
import com.edu.uptc.EnVivo.logging.StructuredLogContext;
import com.edu.uptc.EnVivo.logging.StructuredLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class PaymentProgressController {

    private final SimpMessagingTemplate messagingTemplate;
    private final StructuredLogService structuredLogService;

    public void sendProgress(String sessionId, PaymentProgressDTO dto) {
        String transactionId = StructuredLogContext.currentTransactionId();
        try (StructuredLogContext.Scope ignored = structuredLogService.scope(StructuredLogContext.KEY_SESSION_ID, sessionId)) {
            structuredLogService.logSuccess("websocket", "WS_MESSAGE_SENT", transactionId, sessionId,
                    StructuredLogContext.currentUserId(), StructuredLogContext.currentClientIp(),
                    StructuredLogContext.currentPaymentProvider(), "DELIVERED",
                    "Payment progress message sent to websocket topic.");
        }
        messagingTemplate.convertAndSend("/topic/payment-progress/" + sessionId, dto);
    }
}
