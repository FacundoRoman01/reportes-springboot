package com.example.reportesspringboot.controller;

import com.example.reportesspringboot.dto.ResumenDashboardDTO;
import com.example.reportesspringboot.service.ReporteService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping("/dashboard")
    public ResumenDashboardDTO obtenerDashboard() {
        return reporteService.generarResumenDashboard();
    }
}
