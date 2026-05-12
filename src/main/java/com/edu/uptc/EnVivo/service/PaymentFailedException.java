package com.edu.uptc.EnVivo.service;

public class PaymentFailedException extends RuntimeException {
    private final String gatewayRawResponse;

    public PaymentFailedException(String message, String gatewayRawResponse) {
        super(message);
        this.gatewayRawResponse = gatewayRawResponse;
    }

    public String getGatewayRawResponse() {
        return gatewayRawResponse;
    }
}
