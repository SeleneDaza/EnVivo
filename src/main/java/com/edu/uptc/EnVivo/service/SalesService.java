package com.edu.uptc.EnVivo.service;

import com.edu.uptc.EnVivo.dto.SalesEventSummaryDTO;
import com.edu.uptc.EnVivo.dto.SalesReportDTO;
import com.edu.uptc.EnVivo.dto.SalesTicketTypeSummaryDTO;
import com.edu.uptc.EnVivo.entity.Event;
import com.edu.uptc.EnVivo.repository.EventRepository;
import com.edu.uptc.EnVivo.repository.PurchaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SalesService {

    private final PurchaseRepository purchaseRepository;
    private final EventRepository eventRepository;

    @Transactional(readOnly = true)
    public SalesReportDTO getSalesReport(Long eventId) {
        validateEventIfPresent(eventId);

        List<PurchaseRepository.EventSalesProjection> eventSales =
                purchaseRepository.findEventSalesByEvent(eventId);
        List<PurchaseRepository.TicketTypeSalesProjection> breakdown =
                purchaseRepository.findTicketTypeSalesByEvent(eventId);

        List<SalesEventSummaryDTO> events = processEventSummaries(eventId, eventSales, breakdown);

        return buildFinalReport(events);
    }

    private List<SalesEventSummaryDTO> processEventSummaries(Long eventId,
                                                             List<PurchaseRepository.EventSalesProjection> sales,
                                                             List<PurchaseRepository.TicketTypeSalesProjection> breakdown) {

        Map<Long, List<SalesTicketTypeSummaryDTO>> breakdownMap = groupBreakdown(breakdown);
        List<SalesEventSummaryDTO> events = mapToEventDtos(sales, breakdownMap);

        return handleEmptyEventCase(eventId, events);
    }

    private Map<Long, List<SalesTicketTypeSummaryDTO>> groupBreakdown(
            List<PurchaseRepository.TicketTypeSalesProjection> breakdown) {

        return breakdown.stream()
                .collect(Collectors.groupingBy(
                        PurchaseRepository.TicketTypeSalesProjection::getEventId,
                        Collectors.mapping(this::toTicketTypeSummary, Collectors.toList())
                ));
    }

    private List<SalesEventSummaryDTO> mapToEventDtos(
            List<PurchaseRepository.EventSalesProjection> eventSales,
            Map<Long, List<SalesTicketTypeSummaryDTO>> breakdownByEvent) {

        return eventSales.stream()
                .map(event -> new SalesEventSummaryDTO(
                        event.getEventId(),
                        event.getEventName(),
                        toLong(event.getTicketsSold()),
                        toLong(event.getRevenue()),
                        breakdownByEvent.getOrDefault(event.getEventId(), Collections.emptyList())
                ))
                .toList();
    }

    private List<SalesEventSummaryDTO> handleEmptyEventCase(Long eventId,
                                                            List<SalesEventSummaryDTO> events) {

        if (eventId == null || !events.isEmpty()) {
            return events;
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado."));

        return List.of(new SalesEventSummaryDTO(
                event.getEvent_id(),
                event.getName(),
                0L,
                0L,
                Collections.emptyList()
        ));
    }

    private SalesReportDTO buildFinalReport(List<SalesEventSummaryDTO> events) {
        long totalTickets = toLong(purchaseRepository.getTotalTicketsSoldAllSales());
        long totalRevenue = toLong(purchaseRepository.getTotalRevenueAllSales());

        return new SalesReportDTO(events, totalTickets, totalRevenue);
    }

    @Transactional(readOnly = true)
    public SalesReportDTO getSalesReport() {
        return getSalesReport(null);
    }

    private SalesTicketTypeSummaryDTO toTicketTypeSummary(PurchaseRepository.TicketTypeSalesProjection row) {
        return new SalesTicketTypeSummaryDTO(
                row.getEventId(),
                row.getTicketTypeName(),
                row.getUnitPrice(),
                toLong(row.getSoldQuantity()),
                toLong(row.getRevenue())
        );
    }

    private void validateEventIfPresent(Long eventId) {
        if (eventId == null) {
            return;
        }

        if (eventId <= 0) {
            throw new IllegalArgumentException("El evento seleccionado no es valido.");
        }

        Optional<Event> event = eventRepository.findById(eventId);
        if (event.isEmpty()) {
            throw new IllegalArgumentException("Evento no encontrado.");
        }
    }

    private long toLong(Number value) {
        return value == null ? 0L : value.longValue();
    }
}

