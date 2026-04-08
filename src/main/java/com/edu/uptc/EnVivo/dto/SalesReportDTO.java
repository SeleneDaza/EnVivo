package com.edu.uptc.EnVivo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class SalesReportDTO {

    private List<SalesEventSummaryDTO> events;
    private long totalTicketsSoldAllSales;
    private long totalRevenueAllSales;
}

