package com.edu.uptc.EnVivo.service;

import com.edu.uptc.EnVivo.controller.PaymentProgressController;
import com.edu.uptc.EnVivo.dto.PaymentProgressDTO;
import com.edu.uptc.EnVivo.logging.StructuredLogService;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class PaymentDirectSender {

    private final PaymentProgressController paymentProgressController;
    private final StructuredLogService structuredLogService;
    private final ConcurrentHashMap<String, Integer> errorCountBySession = new ConcurrentHashMap<>();

    public PaymentDirectSender(PaymentProgressController paymentProgressController,
                               StructuredLogService structuredLogService) {
        this.paymentProgressController = paymentProgressController;
        this.structuredLogService = structuredLogService;
    }

    public void sendDirect(String tipo, String contenido, String sessionId, String transactionId) {
        structuredLogService.logInfo("rabbitmq", "PAYMENT_RESULT_FALLBACK", transactionId, sessionId,
                null, null, null, "DIRECT",
                "Broker/worker unavailable — sending fase [" + tipo + "] directly via WebSocket.");

        if ("INFO".equals(tipo)) {
            paymentProgressController.sendProgress(sessionId,
                    new PaymentProgressDTO("fase_progreso", "INFO", contenido, null));
            return;
        }

        if ("EXITO".equals(tipo)) {
            errorCountBySession.remove(sessionId);
            paymentProgressController.sendProgress(sessionId,
                    new PaymentProgressDTO("MENSAJE_BONITO", "Asistente IA", contenido, "aprobado"));
            return;
        }

        int errorCount = errorCountBySession.merge(sessionId, 1, Integer::sum);
        if (errorCount < 2) {
            paymentProgressController.sendProgress(sessionId,
                    new PaymentProgressDTO("fase_progreso", "ERROR", contenido, null));
        } else {
            errorCountBySession.remove(sessionId);
            paymentProgressController.sendProgress(sessionId,
                    new PaymentProgressDTO("MENSAJE_BONITO", "Asistente IA", contenido, "rechazado"));
        }
    }
}
