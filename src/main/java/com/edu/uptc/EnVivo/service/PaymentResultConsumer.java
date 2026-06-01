package com.edu.uptc.EnVivo.service;

import com.edu.uptc.EnVivo.controller.PaymentProgressController;
import com.edu.uptc.EnVivo.dto.PaymentProgressDTO;
import com.edu.uptc.EnVivo.logging.StructuredLogContext;
import com.edu.uptc.EnVivo.logging.StructuredLogService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PaymentResultConsumer {

    private final PaymentProgressController paymentProgressController;
    private final StructuredLogService structuredLogService;
    private final ConcurrentHashMap<String, Integer> errorCountBySession = new ConcurrentHashMap<>();

    public PaymentResultConsumer(PaymentProgressController paymentProgressController,
                                 StructuredLogService structuredLogService) {
        this.paymentProgressController = paymentProgressController;
        this.structuredLogService = structuredLogService;
    }

    @RabbitListener(queues = "${rabbitmq.queue.resultados}")
    public void listen(Map<String, Object> message) {
        String tipo = (String) message.get("tipo");
        String contenido = (String) message.get("contenido");
        String sessionId = (String) message.get("sessionId");
        String transactionId = (String) message.get("transactionId");

        Map<String, String> ctx = new LinkedHashMap<>();
        ctx.put(StructuredLogContext.KEY_TRANSACTION_ID, transactionId);
        ctx.put(StructuredLogContext.KEY_SESSION_ID, sessionId);
        try (StructuredLogContext.Scope ignored = structuredLogService.scope(ctx)) {
            if ("INFO".equals(tipo)) {
                paymentProgressController.sendProgress(sessionId,
                    new PaymentProgressDTO("fase_progreso", null, contenido, null));
                return;
            }

            if ("EXITO".equals(tipo)) {
                errorCountBySession.remove(sessionId);
                paymentProgressController.sendProgress(sessionId,
                    new PaymentProgressDTO("MENSAJE_BONITO", "Asistente IA", contenido, "aprobado"));
                return;
            }

            // tipo == "ERROR": el primero es la fase 4, el segundo es el mensaje final de la IA
            int errorCount = errorCountBySession.merge(sessionId, 1, Integer::sum);
            if (errorCount < 2) {
                paymentProgressController.sendProgress(sessionId,
                    new PaymentProgressDTO("fase_progreso", null, contenido, null));
            } else {
                errorCountBySession.remove(sessionId);
                paymentProgressController.sendProgress(sessionId,
                    new PaymentProgressDTO("MENSAJE_BONITO", "Asistente IA", contenido, "rechazado"));
            }
        }
    }
}
