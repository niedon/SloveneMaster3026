package com.bcadaval.esloveno.repo;

import com.bcadaval.esloveno.beans.HistoricoRespuesta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@SuppressWarnings("unused")
@Repository
public interface HistoricoRespuestaRepo extends JpaRepository<HistoricoRespuesta, Long> {

    // QUERY 2: Para gráfico de Tiempo Promedio
    // Devuelve: [FechaString, PromedioSegundos]
    @Query("SELECT function('strftime', :format, h.tsRespuesta) as fecha, " +
            "AVG(h.segundosEnResponder) " +
            "FROM HistoricoRespuesta h " +
            "WHERE h.tsRespuesta >= :inicio AND h.tsRespuesta <= :fin " +
            "AND h.segundosEnResponder IS NOT NULL " +
            "GROUP BY function('strftime', :format, h.tsRespuesta)")
    List<Object[]> avgTiempoGrouped(
            @Param("inicio") Instant inicio,
            @Param("fin") Instant fin,
            @Param("format") String format
    );

    // QUERY 3: Para gráfico de Tipos de Palabra
    // Devuelve: [TipoPalabra, TotalAciertos, TotalRegistros]
    @Query("SELECT h.tipoPalabra, " +
            "SUM(CASE WHEN h.acierto = true THEN 1 ELSE 0 END), " +
            "COUNT(*) " +
            "FROM HistoricoRespuesta h " +
            "WHERE h.tsRespuesta >= :inicio AND h.tsRespuesta <= :fin " +
            "GROUP BY h.tipoPalabra")
    List<Object[]> statsPorTipoGrouped(
            @Param("inicio") Instant inicio,
            @Param("fin") Instant fin
    );

    // QUERY 1: Para gráfico de Aciertos vs Fallos
    // Devuelve: [FechaString, TotalAciertos, TotalFallos]
    @Query("SELECT function('strftime', :format, h.tsRespuesta) as fecha, " +
            "SUM(CASE WHEN h.acierto = true THEN 1 ELSE 0 END) as aciertos, " +
            "SUM(CASE WHEN h.acierto = false THEN 1 ELSE 0 END) as fallos " +
            "FROM HistoricoRespuesta h " +
            "WHERE h.tsRespuesta >= :inicio AND h.tsRespuesta <= :fin " +
            "GROUP BY function('strftime', :format, h.tsRespuesta)")
    List<Object[]> countAciertosFallosGrouped(
            @Param("inicio") Instant inicio,
            @Param("fin") Instant fin,
            @Param("format") String format
    );
}
