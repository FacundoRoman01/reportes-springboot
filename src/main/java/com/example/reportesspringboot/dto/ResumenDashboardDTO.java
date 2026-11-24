package com.example.reportesspringboot.dto;

import java.util.List;

/**
 * Resumen agregado que consume el dashboard, construido con las consultas de VentaRepository.
 */
public class ResumenDashboardDTO {

    private long tiempoEjecucionMs;
    private List<FacturacionPorCategoriaDTO> facturacionPorCategoria;
    private List<SegmentacionClienteDTO> segmentacionClientes;
    private List<ProductoTopDTO> topProductos;

    public ResumenDashboardDTO() {
    }

    public ResumenDashboardDTO(long tiempoEjecucionMs,
                               List<FacturacionPorCategoriaDTO> facturacionPorCategoria,
                               List<SegmentacionClienteDTO> segmentacionClientes,
                               List<ProductoTopDTO> topProductos) {
        this.tiempoEjecucionMs = tiempoEjecucionMs;
        this.facturacionPorCategoria = facturacionPorCategoria;
        this.segmentacionClientes = segmentacionClientes;
        this.topProductos = topProductos;
    }

    public long getTiempoEjecucionMs() {
        return tiempoEjecucionMs;
    }

    public void setTiempoEjecucionMs(long tiempoEjecucionMs) {
        this.tiempoEjecucionMs = tiempoEjecucionMs;
    }

    public List<FacturacionPorCategoriaDTO> getFacturacionPorCategoria() {
        return facturacionPorCategoria;
    }

    public void setFacturacionPorCategoria(List<FacturacionPorCategoriaDTO> facturacionPorCategoria) {
        this.facturacionPorCategoria = facturacionPorCategoria;
    }

    public List<SegmentacionClienteDTO> getSegmentacionClientes() {
        return segmentacionClientes;
    }

    public void setSegmentacionClientes(List<SegmentacionClienteDTO> segmentacionClientes) {
        this.segmentacionClientes = segmentacionClientes;
    }

    public List<ProductoTopDTO> getTopProductos() {
        return topProductos;
    }

    public void setTopProductos(List<ProductoTopDTO> topProductos) {
        this.topProductos = topProductos;
    }

    public static class FacturacionPorCategoriaDTO {
        private String categoria;
        private Double total;

        public FacturacionPorCategoriaDTO() {
        }

        public FacturacionPorCategoriaDTO(String categoria, Double total) {
            this.categoria = categoria;
            this.total = total;
        }

        public String getCategoria() {
            return categoria;
        }

        public void setCategoria(String categoria) {
            this.categoria = categoria;
        }

        public Double getTotal() {
            return total;
        }

        public void setTotal(Double total) {
            this.total = total;
        }
    }

    public static class SegmentacionClienteDTO {
        private String cliente;
        private Double totalGastado;
        private String segmento;

        public SegmentacionClienteDTO() {
        }

        public SegmentacionClienteDTO(String cliente, Double totalGastado, String segmento) {
            this.cliente = cliente;
            this.totalGastado = totalGastado;
            this.segmento = segmento;
        }

        public String getCliente() {
            return cliente;
        }

        public void setCliente(String cliente) {
            this.cliente = cliente;
        }

        public Double getTotalGastado() {
            return totalGastado;
        }

        public void setTotalGastado(Double totalGastado) {
            this.totalGastado = totalGastado;
        }

        public String getSegmento() {
            return segmento;
        }

        public void setSegmento(String segmento) {
            this.segmento = segmento;
        }
    }

    public static class ProductoTopDTO {
        private String producto;
        private Long unidades;

        public ProductoTopDTO() {
        }

        public ProductoTopDTO(String producto, Long unidades) {
            this.producto = producto;
            this.unidades = unidades;
        }

        public String getProducto() {
            return producto;
        }

        public void setProducto(String producto) {
            this.producto = producto;
        }

        public Long getUnidades() {
            return unidades;
        }

        public void setUnidades(Long unidades) {
            this.unidades = unidades;
        }
    }
}
