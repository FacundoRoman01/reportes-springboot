package com.example.reportesspringboot.service;

import com.example.reportesspringboot.dto.ResumenDashboardDTO;
import com.example.reportesspringboot.dto.ResumenDashboardDTO.FacturacionPorCategoriaDTO;
import com.example.reportesspringboot.dto.ResumenDashboardDTO.ProductoTopDTO;
import com.example.reportesspringboot.dto.ResumenDashboardDTO.SegmentacionClienteDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class ReporteService {

    private final ReporteAsyncWorker reporteAsyncWorker;

    public ReporteService(ReporteAsyncWorker reporteAsyncWorker) {
        this.reporteAsyncWorker = reporteAsyncWorker;
    }

    /** Lanza las tres consultas en paralelo y arma el resumen cuando todas terminan. */
    public ResumenDashboardDTO generarResumenDashboard() {
        long inicio = System.nanoTime();

        CompletableFuture<List<FacturacionPorCategoriaDTO>> facturacionFuture =
                reporteAsyncWorker.calcularFacturacionPorCategoria();
        CompletableFuture<List<SegmentacionClienteDTO>> segmentacionFuture =
                reporteAsyncWorker.calcularSegmentacionClientes();
        CompletableFuture<List<ProductoTopDTO>> topProductosFuture =
                reporteAsyncWorker.calcularTopProductos();

        CompletableFuture.allOf(facturacionFuture, segmentacionFuture, topProductosFuture).join();
        long tiempoEjecucionMs = (System.nanoTime() - inicio) / 1_000_000;

        return new ResumenDashboardDTO(
                tiempoEjecucionMs,
                facturacionFuture.join(),
                segmentacionFuture.join(),
                topProductosFuture.join()
        );
    }
}
