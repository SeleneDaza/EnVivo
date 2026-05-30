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

    private final AtomicInteger faseCounter = new AtomicInteger(1);

    public PaymentEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishPhase(PaymentProgressDTO dto) {
        boolean esFinal = "aprobado".equalsIgnoreCase(dto.getEstadoTransaccion())
                || "rechazado".equalsIgnoreCase(dto.getEstadoTransaccion());

        String tipo;
        if (esFinal) {
            tipo = "aprobado".equalsIgnoreCase(dto.getEstadoTransaccion()) ? "EXITO" : "ERROR";
        } else {
            tipo = dto.getFase();
        }

        Map<String, Object> mensaje = new HashMap<>();
        mensaje.put("tipo", tipo);
        mensaje.put("contenido", dto.getDetalle());
        mensaje.put("fase", faseCounter.getAndIncrement());

        rabbitTemplate.convertAndSend(queueRecibidos, mensaje);

        if (esFinal) {
            faseCounter.set(1);
        }
    }
}
