package com.example.reportesspringboot.repository;

import com.example.reportesspringboot.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

    // Consulta 1 (correrá en el Hilo 1): Total facturado por categoría
    @Query(value = "SELECT categoria, SUM(total_venta) AS total FROM ventas GROUP BY categoria ORDER BY total DESC", nativeQuery = true)
    List<Object[]> obtenerFacturacionPorCategoria();

    // Consulta 2 (correrá en el Hilo 2): Segmentación de clientes con CASE WHEN
    @Query(value = """
            SELECT cliente,
                   SUM(total_venta) AS total_gastado,
                   CASE
                       WHEN SUM(total_venta) >= 100000 THEN 'VIP'
                       WHEN SUM(total_venta) >= 40000 THEN 'FRECUENTE'
                       ELSE 'REGULAR'
                   END AS segmento
            FROM ventas
            GROUP BY cliente
            ORDER BY total_gastado DESC
            """, nativeQuery = true)
    List<Object[]> obtenerSegmentacionClientes();

    // Consulta 3 (correrá en el Hilo 3): Top 5 productos más vendidos
    @Query(value = "SELECT producto, SUM(cantidad) AS unidades FROM ventas GROUP BY producto ORDER BY unidades DESC LIMIT 5", nativeQuery = true)
    List<Object[]> obtenerTopProductos();
}