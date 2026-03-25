package com.bcadaval.esloveno.rest;

import com.bcadaval.esloveno.rest.dto.ChartDataDTO;
import com.bcadaval.esloveno.rest.dto.EstadisticasDTO;
import com.bcadaval.esloveno.services.EstadisticasService;
import com.bcadaval.esloveno.services.RepeticionEspaciadaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/stats")
public class EstadisticasRestController {

    @Autowired
    private EstadisticasService estadisticasService;

    @Autowired
    private RepeticionEspaciadaService repeticionEspaciadaService;

    @GetMapping("/resumen")
    public EstadisticasDTO getResumen() {
        return repeticionEspaciadaService.obtenerEstadisticas();
    }

    @GetMapping("/historico")
    public ChartDataDTO getHistorico(
            @RequestParam("inicio") Long inicioEpoch,
            @RequestParam("fin") Long finEpoch,
            @RequestParam("granularidad") String granularidad,
            @RequestParam(value = "zonaHoraria", required = false, defaultValue = "UTC") String zonaHoraria) {
        
        Instant inicio = Instant.ofEpochMilli(inicioEpoch);
        Instant fin = Instant.ofEpochMilli(finEpoch);
        return estadisticasService.getAciertosFallosTotales(inicio, fin, granularidad, zonaHoraria);
    }
    
    @GetMapping("/historico-totales")
    public ChartDataDTO getHistoricoTotales(
            @RequestParam("inicio") Long inicioEpoch,
            @RequestParam("fin") Long finEpoch,
            @RequestParam("granularidad") String granularidad,
            @RequestParam(value = "zonaHoraria", required = false, defaultValue = "UTC") String zonaHoraria) {
        
        Instant inicio = Instant.ofEpochMilli(inicioEpoch);
        Instant fin = Instant.ofEpochMilli(finEpoch);
        return estadisticasService.getAciertosFallosTotales(inicio, fin, granularidad, zonaHoraria);
    }

    @GetMapping("/historico-porcentaje")
    public ChartDataDTO getHistoricoPorcentaje(
            @RequestParam("inicio") Long inicioEpoch,
            @RequestParam("fin") Long finEpoch,
            @RequestParam("granularidad") String granularidad,
            @RequestParam(value = "zonaHoraria", required = false, defaultValue = "UTC") String zonaHoraria) {
        
        Instant inicio = Instant.ofEpochMilli(inicioEpoch);
        Instant fin = Instant.ofEpochMilli(finEpoch);
        return estadisticasService.getPorcentajeAciertos(inicio, fin, granularidad, zonaHoraria);
    }
    
    @GetMapping("/tiempos")
    public ChartDataDTO getTiempos(
            @RequestParam("inicio") Long inicioEpoch,
            @RequestParam("fin") Long finEpoch,
            @RequestParam("granularidad") String granularidad,
            @RequestParam(value = "zonaHoraria", required = false, defaultValue = "UTC") String zonaHoraria) {

        Instant inicio = Instant.ofEpochMilli(inicioEpoch);
        Instant fin = Instant.ofEpochMilli(finEpoch);
        return estadisticasService.getTiempoPromedio(inicio, fin, granularidad, zonaHoraria);
    }

    @GetMapping("/tipos")
    public ChartDataDTO getTipos(
            @RequestParam("inicio") Long inicioEpoch,
            @RequestParam("fin") Long finEpoch) {
        
        Instant inicio = Instant.ofEpochMilli(inicioEpoch);
        Instant fin = Instant.ofEpochMilli(finEpoch);
        return estadisticasService.getEstadisticasPorTipo(inicio, fin);
    }

    @GetMapping("/pronostico")
    public ChartDataDTO getPronostico(
            @RequestParam("inicio") Long inicioEpoch,
            @RequestParam("fin") Long finEpoch,
            @RequestParam("granularidad") String granularidad,
            @RequestParam(value = "zonaHoraria", required = false, defaultValue = "UTC") String zonaHoraria) {
        
        Instant inicio = Instant.ofEpochMilli(inicioEpoch);
        Instant fin = Instant.ofEpochMilli(finEpoch);
        return estadisticasService.getPronostico(inicio, fin, granularidad, zonaHoraria);
    }
}
