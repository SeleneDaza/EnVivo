package com.edu.uptc.EnVivo.service;

import com.edu.uptc.EnVivo.controller.PaymentProgressController;
import com.edu.uptc.EnVivo.dto.PaymentProgressDTO;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PaymentResultConsumer {

    private final PaymentProgressController paymentProgressController;

    private static final ConcurrentHashMap<String, String> sessionStore = new ConcurrentHashMap<>();

    public PaymentResultConsumer(PaymentProgressController paymentProgressController) {
        this.paymentProgressController = paymentProgressController;
    }

    public static void setCurrentSession(String sessionId) {
        sessionStore.put("current", sessionId);
    }

    @RabbitListener(queues = "${rabbitmq.queue.resultados}")
    public void listen(Map<String, Object> message) {
        String tipo = (String) message.get("tipo");
        String contenido = (String) message.get("contenido");
        Object faseObj = message.get("fase");
        int fase = faseObj instanceof Number ? ((Number) faseObj).intValue() : 0;

        String mensajeTexto = "MENSAJE_BONITO".equals(tipo)
                ? "Mensaje de tu asistente"
                : "Fase " + fase;

        PaymentProgressDTO dto = new PaymentProgressDTO(tipo, mensajeTexto, contenido, null);

        String sessionId = sessionStore.get("current");
        paymentProgressController.sendProgress(sessionId, dto);
    }
}
