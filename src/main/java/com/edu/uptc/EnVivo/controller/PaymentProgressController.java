package com.edu.uptc.EnVivo.controller;

import com.edu.uptc.EnVivo.dto.PaymentProgressDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class PaymentProgressController {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendProgress(String sessionId, PaymentProgressDTO dto) {
        messagingTemplate.convertAndSend("/topic/payment-progress/" + sessionId, dto);
    }
}
