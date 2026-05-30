package com.edu.uptc.EnVivo.service;

import com.edu.uptc.EnVivo.dto.PaymentProgressDTO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class PaymentEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.queue.recibidos}")
    private String queueRecibidos;

    private final AtomicInteger counter = new AtomicInteger(1);

    public PaymentEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishPhase(PaymentProgressDTO dto) {
        String tipo = "aprobado".equals(dto.getEstadoTransaccion()) ? "EXITO" : "ERROR";

        Map<String, Object> message = new HashMap<>();
        message.put("tipo", tipo);
        message.put("contenido", dto.getDetalle());
        message.put("fase", counter.getAndIncrement());

        rabbitTemplate.convertAndSend(queueRecibidos, message);
        counter.set(1);
    }
}
