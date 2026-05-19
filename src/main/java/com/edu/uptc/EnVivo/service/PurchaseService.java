package com.edu.uptc.EnVivo.service;

import com.edu.uptc.EnVivo.dto.BuyerInfoDTO;
import com.edu.uptc.EnVivo.dto.PaymentInfoDTO;
import com.edu.uptc.EnVivo.dto.ProfilePurchaseDTO;
import com.edu.uptc.EnVivo.dto.PurchaseCheckoutItemDTO;
import com.edu.uptc.EnVivo.dto.PurchaseCheckoutRequestDTO;
import com.edu.uptc.EnVivo.dto.PurchaseConfirmationDTO;
import com.edu.uptc.EnVivo.dto.PurchaseItemSummaryDTO;
import com.edu.uptc.EnVivo.entity.Purchase;
import com.edu.uptc.EnVivo.entity.PurchaseDetail;
import com.edu.uptc.EnVivo.entity.Ticket;
import com.edu.uptc.EnVivo.entity.User;
import com.edu.uptc.EnVivo.repository.PurchaseRepository;
import com.edu.uptc.EnVivo.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PurchaseService {

    private static final Logger log = LoggerFactory.getLogger(PurchaseService.class);

    private final PurchaseRepository purchaseRepository;
    private final TicketRepository ticketRepository;
    private final UserService userService;
    private final PdfTicketService pdfTicketService;
    private final PaymentGatewayService paymentGatewayService;

    @Transactional
    public PurchaseConfirmationDTO checkout(String principalName, PurchaseCheckoutRequestDTO request) {
        validateRequest(request);
        Map<Long, Integer> requestedItems = normalizeItems(request);

        User user = getUser(principalName);
        
        // Log inicial de checkout
        String tipoTarjeta = request.getPayment().getTipoTarjeta();
        long monto = request.getItems().stream()
            .mapToLong(item -> {
                Ticket ticket = ticketRepository.findById(item.getTicketId()).orElse(null);
                return ticket != null ? (long) ticket.getPrice() * item.getQuantity() : 0;
            }).sum();
        log.info("Iniciando checkout - Usuario: {}, Monto: {}, Tipo tarjeta: {}", 
            user.getUserName(), monto, tipoTarjeta.toLowerCase());
        
        Purchase purchase = initializePurchase(user, request);
        List<PurchaseItemSummaryDTO> ticketItems = new ArrayList<>();

        processAllItems(requestedItems, request.getEventId(), purchase, ticketItems);
        
        // Antes de persistir, procesar el pago con la pasarela externa
        long montoFinal = purchase.getTotalAmount();
        String numeroTarjeta = request.getPayment().getCardNumber();
        String cvv = request.getPayment().getCvv();

        try {
            var gatewayResult = paymentGatewayService.processPayment(montoFinal, tipoTarjeta, numeroTarjeta, cvv);
            if (!gatewayResult.isSuccess()) {
                log.warn("FALLO EN CONEXIÓN CON PASARELA - Usuario: {}, Monto: {}, Error: {}", 
                    user.getUserName(), montoFinal, gatewayResult.getMessage());
                throw new IllegalStateException(gatewayResult.getMessage());
            }
        } catch (PaymentGatewayService.GatewayConnectionException e) {
            log.error("FALLO EN CONEXIÓN CON PASARELA - Usuario: {}, Monto: {}, Error: No hay conexión con la pasarela de pagos. Verifique que esté disponible.", 
                user.getUserName(), montoFinal);
            throw new IllegalStateException(e.getMessage());
        }

        Purchase saved = purchaseRepository.save(purchase);
        
        // Log de compra registrada exitosamente
        log.info("Compra registrada exitosamente - Usuario: {}, ID Compra: {}, Monto: {}", 
            user.getUserName(), saved.getId(), montoFinal);
        
        return buildConfirmation(saved, ticketItems, request.getPayment().getCardNumber());
    }

    private User getUser(String principalName) {
        return userService.findByUserName(principalName)
                .or(() -> userService.findByEmail(principalName))
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
    }

    private Purchase initializePurchase(User user, PurchaseCheckoutRequestDTO request) {
        Purchase purchase = new Purchase();
        purchase.setUser(user);
        purchase.setPurchaseDate(LocalDateTime.now());
        purchase.setBuyerFullName(request.getBuyer().getFullName().trim());
        purchase.setBuyerDocument(request.getBuyer().getDocument().trim());
        purchase.setBuyerEmail(request.getBuyer().getEmail().trim());
        purchase.setBuyerPhone(request.getBuyer().getPhone().trim());
        purchase.setTotalAmount(0);
        return purchase;
    }

    private void processAllItems(Map<Long, Integer> items, Long eventId,
                                 Purchase purchase, List<PurchaseItemSummaryDTO> summaries) {
        for (Map.Entry<Long, Integer> item : items.entrySet()) {
            processSingleItem(item.getKey(), item.getValue(), eventId, purchase, summaries);
        }
    }

    private void processSingleItem(Long ticketId, int quantity, Long reqEventId,
                                   Purchase purchase, List<PurchaseItemSummaryDTO> summaries) {
        Ticket ticket = fetchAndValidateTicket(ticketId, reqEventId, quantity);
        int subtotal = Math.multiplyExact(ticket.getPrice(), quantity);

        ticket.setAvailableQuantity(ticket.getAvailableQuantity() - quantity);
        purchase.addDetail(createPurchaseDetail(ticket, quantity, subtotal));
        purchase.setTotalAmount(Math.addExact(purchase.getTotalAmount(), subtotal));

        summaries.add(buildItemSummary(ticket, quantity, subtotal));
    }

    private Ticket fetchAndValidateTicket(Long ticketId, Long eventId, int quantity) {
        Ticket ticket = ticketRepository.findByIdForUpdate(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Tipo de boleta no encontrado."));

        if (!ticket.getEvent().getEvent_id().equals(eventId)) {
            throw new IllegalArgumentException("La boleta no pertenece al evento seleccionado.");
        }
        if (isHistoricalEvent(ticket.getEvent().getDate())) {
            throw new IllegalStateException("No puedes comprar boletas para eventos historicos.");
        }
        if (ticket.getAvailableQuantity() < quantity) {
            throw new IllegalStateException("No hay disponibilidad suficiente.");
        }
        return ticket;
    }

    private PurchaseDetail createPurchaseDetail(Ticket ticket, int quantity, int subtotal) {
        PurchaseDetail detail = new PurchaseDetail();
        detail.setTicket(ticket);
        detail.setQuantity(quantity);
        detail.setUnitPrice(ticket.getPrice());
        detail.setSubtotal(subtotal);
        return detail;
    }

    private PurchaseItemSummaryDTO buildItemSummary(Ticket ticket, int quantity, int subtotal) {
        String typeName = ticket.getTicketType() != null ? ticket.getTicketType().getName() : "Sin tipo";
        return PurchaseItemSummaryDTO.builder()
                .ticketId(ticket.getId())
                .ticketType(typeName)
                .quantity(quantity)
                .unitPrice(ticket.getPrice())
                .subtotal(subtotal)
                .build();
    }

    private PurchaseConfirmationDTO buildConfirmation(Purchase saved, List<PurchaseItemSummaryDTO> items,
                                                      String cardNumber) {
        String ticketSummary = items.size() == 1 ? items.get(0).getTicketType() : "Multiples tipos";
        int totalQuantity = items.stream().mapToInt(PurchaseItemSummaryDTO::getQuantity).sum();
        String eventName = saved.getDetails().isEmpty() ? "" :
                saved.getDetails().get(0).getTicket().getEvent().getName();

        return PurchaseConfirmationDTO.builder()
                .purchaseId(saved.getId())
                .purchaseDate(saved.getPurchaseDate().toString())
                .eventName(eventName)
                .ticketType(ticketSummary)
                .quantity(totalQuantity)
                .total(saved.getTotalAmount())
                .ticketItems(items)
                .buyerFullName(saved.getBuyerFullName())
                .buyerEmail(saved.getBuyerEmail())
                .buyerDocument(saved.getBuyerDocument())
                .maskedCard(maskCard(cardNumber))
                .message("Compra registrada exitosamente.")
                .build();
    }

    private void validateRequest(PurchaseCheckoutRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("No se recibieron datos de compra.");
        }
        if (request.getEventId() == null || request.getEventId() <= 0) {
            throw new IllegalArgumentException("Evento invalido.");
        }

        if (hasCheckoutItems(request)) {
            for (PurchaseCheckoutItemDTO item : request.getItems()) {
                if (item == null || item.getTicketId() == null || item.getTicketId() <= 0) {
                    throw new IllegalArgumentException("Tipo de boleta invalido.");
                }
                if (item.getQuantity() == null || item.getQuantity() <= 0) {
                    throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
                }
            }
        } else {
            if (request.getTicketId() == null || request.getTicketId() <= 0) {
                throw new IllegalArgumentException("Tipo de boleta invalido.");
            }
            if (request.getQuantity() == null || request.getQuantity() <= 0) {
                throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
            }
        }

        validateBuyer(request.getBuyer());
        validatePayment(request.getPayment());
    }

    private Map<Long, Integer> normalizeItems(PurchaseCheckoutRequestDTO request) {
        Map<Long, Integer> normalized = new LinkedHashMap<>();

        if (hasCheckoutItems(request)) {
            for (PurchaseCheckoutItemDTO item : request.getItems()) {
                normalized.merge(item.getTicketId(), item.getQuantity(), Math::addExact);
            }
            return normalized;
        }

        normalized.put(request.getTicketId(), request.getQuantity());
        return normalized;
    }

    private boolean hasCheckoutItems(PurchaseCheckoutRequestDTO request) {
        return request.getItems() != null
                && request.getItems().stream().anyMatch(Objects::nonNull);
    }

    private void validateBuyer(BuyerInfoDTO buyer) {
        if (buyer == null) {
            throw new IllegalArgumentException("Los datos personales son obligatorios.");
        }
        if (isBlank(buyer.getFullName()) || isBlank(buyer.getDocument()) || isBlank(buyer.getEmail()) || isBlank(buyer.getPhone())) {
            throw new IllegalArgumentException("Debes completar nombre, documento, correo y telefono.");
        }
    }

    private void validatePayment(PaymentInfoDTO payment) {
        if (payment == null) {
            throw new IllegalArgumentException("Los datos bancarios son obligatorios para confirmar la compra.");
        }
        if (isBlank(payment.getCardHolder()) || isBlank(payment.getCardNumber()) || isBlank(payment.getExpiry()) || isBlank(payment.getCvv())) {
            throw new IllegalArgumentException("Debes completar titular, tarjeta, vencimiento y CVV.");
        }

        String cardDigits = payment.getCardNumber().replaceAll("\\D", "");
        if (cardDigits.length() < 13 || cardDigits.length() > 19) {
            throw new IllegalArgumentException("El numero de tarjeta debe tener entre 13 y 19 digitos.");
        }

        String cvvDigits = payment.getCvv().replaceAll("\\D", "");
        if (cvvDigits.length() < 3 || cvvDigits.length() > 4) {
            throw new IllegalArgumentException("El CVV debe tener 3 o 4 digitos.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String maskCard(String rawCardNumber) {
        String digits = rawCardNumber == null ? "" : rawCardNumber.replaceAll("\\D", "");
        if (digits.length() <= 4) {
            return "**** " + (digits.isEmpty() ? "0000" : digits);
        }
        return "**** **** **** " + digits.substring(digits.length() - 4);
    }

    @Transactional(readOnly = true)
    public byte[] downloadPurchasePdf(Long purchaseId, String principalName) {
        Purchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new IllegalArgumentException("Compra no encontrada."));
        User user = userService.findByUserName(principalName)
                .or(() -> userService.findByEmail(principalName))
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
        if (!purchase.getUser().getId().equals(user.getId())) {
            throw new SecurityException("No tienes permiso para descargar estas entradas.");
        }
        if (purchaseHasHistoricalEvent(purchase)) {
            throw new IllegalStateException("No puedes descargar comprobantes de eventos historicos.");
        }
        return pdfTicketService.generateTicketsPdf(purchase);
    }

    @Transactional(readOnly = true)
    public List<ProfilePurchaseDTO> getProfilePurchaseHistory(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }

        return purchaseRepository.findByUserIdOrderByPurchaseDateDesc(userId)
                .stream()
                .map(this::toProfilePurchase)
                .toList();
    }

    private ProfilePurchaseDTO toProfilePurchase(Purchase purchase) {
        String eventName = "Evento no disponible";
        java.time.LocalDate eventDate = null;
        int totalTickets = 0;
        boolean historical = false;

        if (purchase.getDetails() != null && !purchase.getDetails().isEmpty()) {
            PurchaseDetail firstDetail = purchase.getDetails().get(0);
            if (firstDetail.getTicket() != null && firstDetail.getTicket().getEvent() != null) {
                eventName = firstDetail.getTicket().getEvent().getName();
                eventDate = firstDetail.getTicket().getEvent().getDate();
                historical = isHistoricalEvent(eventDate);
            }

            totalTickets = purchase.getDetails().stream()
                    .map(PurchaseDetail::getQuantity)
                    .filter(java.util.Objects::nonNull)
                    .mapToInt(Integer::intValue)
                    .sum();
        }

        return new ProfilePurchaseDTO(purchase.getId(), eventName, eventDate, totalTickets, historical);
    }

    private boolean purchaseHasHistoricalEvent(Purchase purchase) {
        if (purchase.getDetails() == null || purchase.getDetails().isEmpty()) {
            return false;
        }

        return purchase.getDetails().stream().anyMatch(detail -> {
            if (detail == null || detail.getTicket() == null || detail.getTicket().getEvent() == null) {
                return false;
            }
            return isHistoricalEvent(detail.getTicket().getEvent().getDate());
        });
    }

    private boolean isHistoricalEvent(LocalDate eventDate) {
        return eventDate != null && eventDate.isBefore(LocalDate.now());
    }

    @Transactional(readOnly = true)
    public long getTotalGanancias() {
        Long total = purchaseRepository.getTotalRevenueAllSales();
        return total != null ? total : 0L;
    }
}

