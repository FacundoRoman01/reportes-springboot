package com.example.reportesspringboot.service;

import com.example.reportesspringboot.model.Importacion;
import com.example.reportesspringboot.model.Venta;
import com.example.reportesspringboot.repository.ImportacionRepository;
import com.example.reportesspringboot.repository.VentaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class ImportacionService {

    private static final Logger log = LoggerFactory.getLogger(ImportacionService.class);
    private static final int TAMANO_LOTE = 500;

    private final ImportacionRepository importacionRepository;
    private final VentaRepository ventaRepository;

    public ImportacionService(ImportacionRepository importacionRepository,
                              VentaRepository ventaRepository) {
        this.importacionRepository = importacionRepository;
        this.ventaRepository = ventaRepository;
    }

    /** Registra el ticket de forma síncrona para responder rápidamente al cliente. */
    public Importacion crearTicketImportacion(String nombreArchivo) {
        if (nombreArchivo == null || nombreArchivo.isBlank()) {
            throw new IllegalArgumentException("El nombre del archivo es obligatorio");
        }

        return importacionRepository.save(new Importacion(nombreArchivo, "PENDIENTE"));
    }

    /**
     * Procesa un CSV en el pool asíncrono. Se espera una columna por campo, en este orden:
     * cliente, producto, categoria, cantidad, precioUnitario, fechaVenta (yyyy-MM-dd).
     * Puede incluir una primera línea de encabezados.
     */
    @Async("taskExecutor")
    public void procesarArchivoAsync(Long importacionId, List<String> lineasCsv) {
        Importacion importacion = buscarImportacion(importacionId);
        int registrosProcesados = 0;

        importacion.setEstado("PROCESANDO");
        importacion.setRegistrosProcesados(0);
        importacionRepository.save(importacion);

        try {
            if (lineasCsv == null) {
                throw new IllegalArgumentException("Las líneas del CSV no pueden ser null");
            }

            List<Venta> lote = new ArrayList<>(TAMANO_LOTE);
            for (int i = 0; i < lineasCsv.size(); i++) {
                String linea = lineasCsv.get(i);
                if (linea == null || linea.isBlank() || esEncabezado(linea)) {
                    continue;
                }

                try {
                    lote.add(convertirAVenta(linea));
                } catch (RuntimeException e) {
                    throw new IllegalArgumentException("CSV inválido en la línea " + (i + 1) + ": " + e.getMessage(), e);
                }

                if (lote.size() == TAMANO_LOTE) {
                    registrosProcesados += guardarLote(lote);
                    actualizarProgreso(importacion, registrosProcesados);
                }
            }

            if (!lote.isEmpty()) {
                registrosProcesados += guardarLote(lote);
            }

            importacion.setRegistrosProcesados(registrosProcesados);
            importacion.setEstado("COMPLETADO");
            importacionRepository.save(importacion);
            log.info("Importación {} completada: {} ventas procesadas", importacionId, registrosProcesados);
        } catch (RuntimeException e) {
            importacion.setRegistrosProcesados(registrosProcesados);
            importacion.setEstado("ERROR");
            importacionRepository.save(importacion);
            log.error("Falló la importación {} después de procesar {} ventas", importacionId, registrosProcesados, e);
        }
    }

    /** Devuelve el ticket para que el cliente consulte su estado actual. */
    public Importacion obtenerEstadoImportacion(Long id) {
        return buscarImportacion(id);
    }

    private Importacion buscarImportacion(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El id de importación es obligatorio");
        }
        return importacionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe la importación con id " + id));
    }

    private int guardarLote(List<Venta> lote) {
        int cantidad = lote.size();
        ventaRepository.saveAll(lote);
        lote.clear();
        return cantidad;
    }

    private void actualizarProgreso(Importacion importacion, int registrosProcesados) {
        importacion.setRegistrosProcesados(registrosProcesados);
        importacionRepository.save(importacion);
    }

    private boolean esEncabezado(String linea) {
        return linea.stripLeading().toLowerCase(Locale.ROOT).startsWith("cliente,");
    }

    private Venta convertirAVenta(String linea) {
        List<String> columnas = separarColumnasCsv(linea);
        if (columnas.size() != 6) {
            throw new IllegalArgumentException("se esperaban 6 columnas y se recibieron " + columnas.size());
        }

        String cliente = columnas.get(0).trim();
        String producto = columnas.get(1).trim();
        String categoria = columnas.get(2).trim();
        int cantidad = Integer.parseInt(columnas.get(3).trim());
        double precioUnitario = Double.parseDouble(columnas.get(4).trim());
        LocalDate fechaVenta = LocalDate.parse(columnas.get(5).trim());

        if (cliente.isBlank() || producto.isBlank() || categoria.isBlank() || cantidad <= 0
                || !Double.isFinite(precioUnitario) || precioUnitario < 0) {
            throw new IllegalArgumentException("hay campos vacíos o valores numéricos inválidos");
        }

        return new Venta(cliente, producto, categoria, cantidad, precioUnitario, fechaVenta);
    }

    /** Separa comas respetando campos entrecomillados y comillas escapadas como "". */
    private List<String> separarColumnasCsv(String linea) {
        List<String> columnas = new ArrayList<>();
        StringBuilder columna = new StringBuilder();
        boolean entreComillas = false;

        for (int i = 0; i < linea.length(); i++) {
            char caracter = linea.charAt(i);
            if (caracter == '"') {
                if (entreComillas && i + 1 < linea.length() && linea.charAt(i + 1) == '"') {
                    columna.append('"');
                    i++;
                } else {
                    entreComillas = !entreComillas;
                }
            } else if (caracter == ',' && !entreComillas) {
                columnas.add(columna.toString());
                columna.setLength(0);
            } else {
                columna.append(caracter);
            }
        }

        if (entreComillas) {
            throw new IllegalArgumentException("hay un campo entrecomillado sin cerrar");
        }
        columnas.add(columna.toString());
        return columnas;
    }
}
