package com.edu.uptc.EnVivo.controller;

import com.edu.uptc.EnVivo.dto.SalesReportDTO;
import com.edu.uptc.EnVivo.service.SalesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class SalesController {

    private final SalesService salesService;

    @GetMapping("/sales")
    public String showSalesPage(Model model) {
        SalesReportDTO report = salesService.getSalesReport();

        model.addAttribute("salesReport", report);
        model.addAttribute("eventSales", report.getEvents());
        model.addAttribute("eventsCount", report.getEvents().size());

        return "sales";
    }

    @GetMapping("/api/sales/report")
    @ResponseBody
    public ResponseEntity<?> getSalesReport(@RequestParam(required = false) Long eventId) {
        try {
            SalesReportDTO report = salesService.getSalesReport(eventId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "report", report
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error al generar el reporte de ventas."));
        }
    }
}