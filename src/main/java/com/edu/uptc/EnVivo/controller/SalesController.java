package com.edu.uptc.EnVivo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import lombok.RequiredArgsConstructor;

// IMPORTANTE: Debe ser @Controller, NO @RestController
@Controller 
@RequiredArgsConstructor
public class SalesController {

    // private final SalesService salesService; // Ellos inyectarán su servicio aquí

    @GetMapping("/sales")
    public String showSalesPage(Model model) {
        
        // Aquí es donde ellos consultarán la base de datos
        // List<VentaDTO> ventas = salesService.obtenerReporteVentas();
        
        // Y aquí mandan esos datos a tu HTML para que el th:each funcione
        // model.addAttribute("ventas", ventas);

        // Esto le dice a Spring Boot que busque el archivo "sales.html" en la carpeta templates
        return "sales"; 
    }
}