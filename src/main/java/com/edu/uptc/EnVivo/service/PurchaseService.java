package com.edu.uptc.EnVivo.service;

import com.edu.uptc.EnVivo.dto.BuyerInfoDTO;
import com.edu.uptc.EnVivo.dto.PaymentInfoDTO;
import com.edu.uptc.EnVivo.dto.ProfilePurchaseDTO;
import com.edu.uptc.EnVivo.dto.PurchaseCheckoutRequestDTO;
import com.edu.uptc.EnVivo.dto.PurchaseConfirmationDTO;
import com.edu.uptc.EnVivo.entity.Purchase;
import com.edu.uptc.EnVivo.entity.PurchaseDetail;
import com.edu.uptc.EnVivo.entity.Ticket;
import com.edu.uptc.EnVivo.entity.User;
import com.edu.uptc.EnVivo.repository.PurchaseRepository;
import com.edu.uptc.EnVivo.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final TicketRepository ticketRepository;
    private final UserService userService;
    private final PdfTicketService pdfTicketService;

    @Transactional
    public PurchaseConfirmationDTO checkout(String principalName, PurchaseCheckoutRequestDTO request) {
        validateRequest(request);

        User user = userService.findByUserName(principalName)
                .or(() -> userService.findByEmail(principalName))
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

        Ticket ticket = ticketRepository.findByIdForUpdate(request.getTicketId())
                .orElseThrow(() -> new IllegalArgumentException("Tipo de boleta no encontrado."));

        if (!ticket.getEvent().getEvent_id().equals(request.getEventId())) {
            throw new IllegalArgumentException("La boleta no pertenece al evento seleccionado.");
        }

        if (isHistoricalEvent(ticket.getEvent().getDate())) {
            throw new IllegalStateException("No puedes comprar boletas para eventos historicos.");
        }

        int quantity = request.getQuantity();
        if (ticket.getAvailableQuantity() < quantity) {
            throw new IllegalStateException("No hay disponibilidad suficiente para la cantidad solicitada.");
        }

        int unitPrice = ticket.getPrice();
        int subtotal = Math.multiplyExact(unitPrice, quantity);

        ticket.setAvailableQuantity(ticket.getAvailableQuantity() - quantity);

        Purchase purchase = new Purchase();
        purchase.setUser(user);
        purchase.setPurchaseDate(LocalDateTime.now());
        purchase.setBuyerFullName(request.getBuyer().getFullName().trim());
        purchase.setBuyerDocument(request.getBuyer().getDocument().trim());
        purchase.setBuyerEmail(request.getBuyer().getEmail().trim());
        purchase.setBuyerPhone(request.getBuyer().getPhone().trim());
        purchase.setTotalAmount(subtotal);

        PurchaseDetail detail = new PurchaseDetail();
        detail.setTicket(ticket);
        detail.setQuantity(quantity);
        detail.setUnitPrice(unitPrice);
        detail.setSubtotal(subtotal);
        purchase.addDetail(detail);

        Purchase saved = purchaseRepository.save(purchase);

        return PurchaseConfirmationDTO.builder()
                .purchaseId(saved.getId())
                .purchaseDate(saved.getPurchaseDate().toString())
                .eventName(ticket.getEvent().getName())
                .ticketType(ticket.getTicketType().getName())
                .quantity(quantity)
                .total(subtotal)
                .buyerFullName(saved.getBuyerFullName())
                .buyerEmail(saved.getBuyerEmail())
                .buyerDocument(saved.getBuyerDocument())
                .maskedCard(maskCard(request.getPayment().getCardNumber()))
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
        if (request.getTicketId() == null || request.getTicketId() <= 0) {
            throw new IllegalArgumentException("Tipo de boleta invalido.");
        }
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
        }

        validateBuyer(request.getBuyer());
        validatePayment(request.getPayment());
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
}

