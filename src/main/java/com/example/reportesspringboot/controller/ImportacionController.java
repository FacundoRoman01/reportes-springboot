package com.example.reportesspringboot.controller;

import com.example.reportesspringboot.model.Importacion;
import com.example.reportesspringboot.service.ImportacionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/importaciones")
public class ImportacionController {

    private final ImportacionService importacionService;

    public ImportacionController(ImportacionService importacionService) {
        this.importacionService = importacionService;
    }

    @PostMapping("/subir")
    public ResponseEntity<Importacion> subirArchivo(@RequestParam("file") MultipartFile file) {
        List<String> lineasCsv;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            lineasCsv = reader.lines().toList();
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pudo leer el archivo CSV", e);
        }

        Importacion ticket = importacionService.crearTicketImportacion(file.getOriginalFilename());
        importacionService.procesarArchivoAsync(ticket.getId(), lineasCsv);
        return ResponseEntity.accepted().body(ticket);
    }

    @GetMapping("/{id}")
    public Importacion obtenerEstado(@PathVariable Long id) {
        try {
            return importacionService.obtenerEstadoImportacion(id);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }
}
