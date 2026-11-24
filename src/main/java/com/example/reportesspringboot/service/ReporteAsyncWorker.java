package com.example.reportesspringboot.service;

import com.example.reportesspringboot.dto.ResumenDashboardDTO.FacturacionPorCategoriaDTO;
import com.example.reportesspringboot.dto.ResumenDashboardDTO.ProductoTopDTO;
import com.example.reportesspringboot.dto.ResumenDashboardDTO.SegmentacionClienteDTO;
import com.example.reportesspringboot.repository.VentaRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/** Ejecuta de forma independiente cada consulta pesada del dashboard. */
@Service
public class ReporteAsyncWorker {

    private final VentaRepository ventaRepository;

    public ReporteAsyncWorker(VentaRepository ventaRepository) {
        this.ventaRepository = ventaRepository;
    }

    @Async("taskExecutor")
    public CompletableFuture<List<FacturacionPorCategoriaDTO>> calcularFacturacionPorCategoria() {
        List<FacturacionPorCategoriaDTO> resultado = ventaRepository.obtenerFacturacionPorCategoria()
                .stream()
                .map(fila -> new FacturacionPorCategoriaDTO(texto(fila[0]), decimal(fila[1])))
                .toList();
        return CompletableFuture.completedFuture(resultado);
    }

    @Async("taskExecutor")
    public CompletableFuture<List<SegmentacionClienteDTO>> calcularSegmentacionClientes() {
        List<SegmentacionClienteDTO> resultado = ventaRepository.obtenerSegmentacionClientes()
                .stream()
                .map(fila -> new SegmentacionClienteDTO(
                        texto(fila[0]), decimal(fila[1]), texto(fila[2])))
                .toList();
        return CompletableFuture.completedFuture(resultado);
    }

    @Async("taskExecutor")
    public CompletableFuture<List<ProductoTopDTO>> calcularTopProductos() {
        List<ProductoTopDTO> resultado = ventaRepository.obtenerTopProductos()
                .stream()
                .map(fila -> new ProductoTopDTO(texto(fila[0]), enteroLargo(fila[1])))
                .toList();
        return CompletableFuture.completedFuture(resultado);
    }

    private static String texto(Object valor) {
        return valor == null ? null : valor.toString();
    }

    private static Double decimal(Object valor) {
        return valor == null ? null : ((Number) valor).doubleValue();
    }

    private static Long enteroLargo(Object valor) {
        return valor == null ? null : ((Number) valor).longValue();
    }
}
