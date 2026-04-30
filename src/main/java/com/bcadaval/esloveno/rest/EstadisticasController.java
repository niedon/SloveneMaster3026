package com.bcadaval.esloveno.rest;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador para mostrar las estadísticas del sistema de estudio
 */
@Log4j2
@Controller
public class EstadisticasController {

    /**
     * Muestra la página de estadísticas con gráficos
     */
    @SuppressWarnings("SameReturnValue")
    @GetMapping("/estadisticas")
    public String mostrarEstadisticas() {
        log.debug("Accediendo a la página de estadísticas");
        return "estadisticas";
    }
}

