package com.example.reportesspringboot.repository;

import com.example.reportesspringboot.model.Importacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImportacionRepository extends JpaRepository<Importacion, Long> {
    
}