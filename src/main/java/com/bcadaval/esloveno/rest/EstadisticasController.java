package com.bcadaval.esloveno.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bcadaval.esloveno.rest.dto.EstadisticasDTO;
import com.bcadaval.esloveno.services.RepeticionEspaciadaService;

import lombok.extern.log4j.Log4j2;

/**
 * Controlador para mostrar las estadísticas del sistema de estudio
 */
@Log4j2
@Controller
public class EstadisticasController {

    @Autowired
    private RepeticionEspaciadaService repeticionEspaciadaService;

    /**
     * Muestra la página de estadísticas con gráficos
     */
    @GetMapping("/estadisticas")
    public String mostrarEstadisticas(Model model) {
        log.debug("Accediendo a la página de estadísticas");
        EstadisticasDTO estadisticas = repeticionEspaciadaService.obtenerEstadisticas();
        model.addAttribute("estadisticas", estadisticas);
        return "estadisticas";
    }

    /**
     * Devuelve las estadísticas en formato JSON (para actualización dinámica)
     */
    @GetMapping("/api/estadisticas")
    @ResponseBody
    public EstadisticasDTO obtenerEstadisticasJson() {
        log.debug("Obteniendo estadísticas en JSON");
        return repeticionEspaciadaService.obtenerEstadisticas();
    }
}

