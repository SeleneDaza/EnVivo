package com.edu.uptc.EnVivo.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class StructuredLoggingFilter extends OncePerRequestFilter {

    public static final String HEADER_TRANSACTION_ID = "X-Transaction-Id";

    private final StructuredLogService structuredLogService;

    public StructuredLoggingFilter(StructuredLogService structuredLogService) {
        this.structuredLogService = structuredLogService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String transactionId = request.getHeader(HEADER_TRANSACTION_ID);
        if (transactionId == null || transactionId.isBlank()) {
            transactionId = UUID.randomUUID().toString();
        }

        Map<String, String> context = new LinkedHashMap<>();
        context.put(StructuredLogContext.KEY_TRANSACTION_ID, transactionId);
        context.put(StructuredLogContext.KEY_CLIENT_IP, resolveClientIp(request));

        try (StructuredLogContext.Scope ignored = structuredLogService.scope(context)) {
            response.setHeader(HEADER_TRANSACTION_ID, transactionId);
            filterChain.doFilter(request, response);
        }
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }

        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }

        return request.getRemoteAddr();
    }
}