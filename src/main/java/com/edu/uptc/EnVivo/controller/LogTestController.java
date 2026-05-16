package com.edu.uptc.EnVivo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/logs")
public class LogTestController {

    private static final Logger logger = LoggerFactory.getLogger(LogTestController.class);

    @GetMapping("/test")
    public String testLogs() {
        String transactionId = java.util.UUID.randomUUID().toString();
        double amount = 123.45;

        logger.info("event=payment status=approved transactionId={} amount={}", transactionId, amount);
        logger.warn("event=payment status=delayed transactionId={} amount={}", transactionId, amount);
        logger.error("event=payment status=error transactionId={} reason={}", transactionId, "simulated-error");

        return "Logs emitted with transactionId=" + transactionId;
    }

}
