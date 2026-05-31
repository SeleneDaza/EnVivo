package com.edu.uptc.EnVivo.service;

import com.edu.uptc.EnVivo.dto.PaymentProgressDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private static final Logger log = LoggerFactory.getLogger(PaymentEventPublisher.class);

    @Value("${rabbitmq.queue.recibidos}")
    private String queueRecibidos;

    public PaymentEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void publishPhase(PaymentProgressDTO dto, String sessionId) {
        String tipo = "aprobado".equals(dto.getEstadoTransaccion()) ? "EXITO" : "ERROR";

        Map<String, Object> message = new HashMap<>();
        message.put("tipo", tipo);
        message.put("contenido", dto.getDetalle());
        message.put("sessionId", sessionId);

        log.info("Publicando en RabbitMQ — tipo: {}, contenido: {}", tipo, dto.getDetalle());
        rabbitTemplate.convertAndSend(queueRecibidos, message);
    }
}