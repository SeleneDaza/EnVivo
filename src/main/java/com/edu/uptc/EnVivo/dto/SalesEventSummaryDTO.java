package com.edu.uptc.EnVivo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class SalesEventSummaryDTO {

    private Long eventId;
    private String eventName;
    private long totalTicketsSold;
    private long totalRevenue;
    private List<SalesTicketTypeSummaryDTO> ticketTypeBreakdown;
}

