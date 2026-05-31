package com.edu.uptc.EnVivo.service;

import com.edu.uptc.EnVivo.controller.PaymentProgressController;
import com.edu.uptc.EnVivo.dto.PaymentProgressDTO;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PaymentResultConsumer {

    private final PaymentProgressController paymentProgressController;

    public PaymentResultConsumer(PaymentProgressController paymentProgressController) {
        this.paymentProgressController = paymentProgressController;
    }

    @RabbitListener(queues = "${rabbitmq.queue.resultados}")
    public void listen(Map<String, Object> message) {
        String tipo = (String) message.get("tipo");
        String contenido = (String) message.get("contenido");
        String sessionId = (String) message.get("sessionId");

        PaymentProgressDTO dto = new PaymentProgressDTO(tipo, "Resultado del pago", contenido, null);
        paymentProgressController.sendProgress(sessionId, dto);
    }
}
