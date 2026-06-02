package com.edu.uptc.EnVivo.service;

import com.edu.uptc.EnVivo.dto.PaymentProgressDTO;
import com.edu.uptc.EnVivo.logging.StructuredLogContext;
import com.edu.uptc.EnVivo.logging.StructuredLogService;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Service
public class PaymentEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final RabbitAdmin rabbitAdmin;
    private final PaymentDirectSender paymentDirectSender;
    private final StructuredLogService structuredLogService;

    @Value("${rabbitmq.queue.recibidos}")
    private String queueRecibidos;

    public PaymentEventPublisher(RabbitTemplate rabbitTemplate, RabbitAdmin rabbitAdmin,
                                 PaymentDirectSender paymentDirectSender,
                                 StructuredLogService structuredLogService) {
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitAdmin = rabbitAdmin;
        this.paymentDirectSender = paymentDirectSender;
        this.structuredLogService = structuredLogService;
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void publishFase(String tipo, String contenido, String sessionId) {
        String transactionId = StructuredLogContext.currentTransactionId();

        if (!hasBrokerConsumers()) {
            paymentDirectSender.sendDirect(tipo, contenido, sessionId, transactionId);
            return;
        }

        Map<String, Object> message = new HashMap<>();
        message.put("tipo", tipo);
        message.put("contenido", contenido);
        message.put("sessionId", sessionId);
        message.put("transactionId", transactionId);

        try {
            structuredLogService.logInfo("rabbitmq", "PAYMENT_PHASE_QUEUED", transactionId, sessionId,
                    StructuredLogContext.currentUserId(), StructuredLogContext.currentClientIp(),
                    StructuredLogContext.currentPaymentProvider(), "QUEUED",
                    "Payment fase [" + tipo + "] published to RabbitMQ.");
            rabbitTemplate.convertAndSend(queueRecibidos, message);
        } catch (AmqpException e) {
            paymentDirectSender.sendDirect(tipo, contenido, sessionId, transactionId);
        }
    }

    private boolean hasBrokerConsumers() {
        try {
            Properties props = rabbitAdmin.getQueueProperties(queueRecibidos);
            if (props == null) return false;
            Object count = props.get(RabbitAdmin.QUEUE_CONSUMER_COUNT);
            return count instanceof Integer && (Integer) count > 0;
        } catch (Exception e) {
            return false;
        }
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void publishPhase(PaymentProgressDTO dto, String sessionId) {
        String tipo = "aprobado".equals(dto.getEstadoTransaccion()) ? "EXITO" : "ERROR";
        String transactionId = StructuredLogContext.currentTransactionId();

        Map<String, Object> message = new HashMap<>();
        message.put("tipo", tipo);
        message.put("contenido", dto.getDetalle());
        message.put("sessionId", sessionId);
        message.put("transactionId", transactionId);

        structuredLogService.logInfo("rabbitmq", "PAYMENT_REQUEST_SENT", transactionId, sessionId,
                StructuredLogContext.currentUserId(), StructuredLogContext.currentClientIp(),
                StructuredLogContext.currentPaymentProvider(), "QUEUED",
                "Payment progress event published to RabbitMQ.");
        rabbitTemplate.convertAndSend(queueRecibidos, message);
    }
}