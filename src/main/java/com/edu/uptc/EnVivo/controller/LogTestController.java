package com.edu.uptc.EnVivo.controller;

import com.edu.uptc.EnVivo.logging.StructuredLogContext;
import com.edu.uptc.EnVivo.logging.StructuredLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/logs")
@RequiredArgsConstructor
public class LogTestController {

    private final StructuredLogService structuredLogService;

    @GetMapping("/test")
    public String testLogs() {
    String transactionId = structuredLogService.ensureTransactionId();
    String paymentProvider = "VISA";

    try (StructuredLogContext.Scope ignored = structuredLogService.scope(java.util.Map.of(
        StructuredLogContext.KEY_TRANSACTION_ID, transactionId,
        StructuredLogContext.KEY_PAYMENT_PROVIDER, paymentProvider,
        StructuredLogContext.KEY_USER_ID, "test-user"
    ))) {
        structuredLogService.logInfo("logging", "PAYMENT_REQUEST_SENT", transactionId, null, "test-user",
            null, paymentProvider, "SENT", "Structured log test started.");
        structuredLogService.logSuccess("logging", "PAYMENT_AUTHORIZED", transactionId, null, "test-user",
            null, paymentProvider, "AUTHORIZED", "Simulated payment authorization.");
        structuredLogService.logError("logging", "PAYMENT_DECLINED", transactionId, null, "test-user",
            null, paymentProvider, "SIMULATED_ERROR", "Simulated technical failure",
            "Simulated functional rejection", null);
    }

    return "Logs emitted with transactionId=" + transactionId;
    }

}
