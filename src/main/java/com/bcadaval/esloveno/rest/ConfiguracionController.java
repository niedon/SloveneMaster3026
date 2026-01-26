package com.bcadaval.esloveno.rest;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bcadaval.esloveno.beans.Variable;
import com.bcadaval.esloveno.services.EstructuraFraseService;
import com.bcadaval.esloveno.services.VariablesService;

import lombok.extern.log4j.Log4j2;

/**
 * Controlador para gestionar la configuración del sistema SRS
 */
@Log4j2
@Controller
public class ConfiguracionController {

    @Autowired
    private VariablesService variablesService;

    @Autowired
    private EstructuraFraseService estructuraFraseService;

    /**
     * Muestra la página de configuración
     */
    @GetMapping("/configuracion")
    public String mostrarConfiguracion(Model model) {
        log.debug("Accediendo a la página de configuración");

        List<Variable> variables = variablesService.obtenerTodasLasVariables();

        // Convertir lista a mapa para fácil acceso en JSP
        Map<String, Variable> variablesMap = variables.stream()
                .collect(Collectors.toMap(Variable::getClave, v -> v));

        model.addAttribute("variablesMap", variablesMap);

        // Estructuras de frase para activar/desactivar
        model.addAttribute("estructuras", estructuraFraseService.getTodasParaConfiguracion());

        // Casos activos derivados de las estructuras activas (solo para mostrar info)
        model.addAttribute("casosActivos", estructuraFraseService.getCasosActivos());

        // Formas verbales activas derivadas de las estructuras activas
        model.addAttribute("formasVerbalesActivas", estructuraFraseService.getFormasVerbalesActivas());

        return "configuracion";
    }

    /**
     * Guarda la configuración de variables
     */
    @PostMapping("/api/guardarConfiguracion")
    @ResponseBody
    public String guardarConfiguracion(
            @RequestParam(required = false) String intervaloInicial,
            @RequestParam(required = false) String intervaloSegunda,
            @RequestParam(required = false) String intervaloReaprendizaje,
            @RequestParam(required = false) String factorFacilidadMinimo,
            @RequestParam(required = false) String factorFacilidadInicial,
            @RequestParam(required = false) String penalizacionFallo,
            @RequestParam(required = false) String maxTarjetasNuevas,
            @RequestParam(required = false) String maxTarjetasRevision,
            @RequestParam(required = false) String mezclarTarjetas) {

        log.info("Guardando configuración");

        try {
            if (intervaloInicial != null) {
                actualizarVariable(VariablesService.INTERVALO_INICIAL_SEGUNDOS, intervaloInicial, "LONG");
            }
            if (intervaloSegunda != null) {
                actualizarVariable(VariablesService.INTERVALO_SEGUNDA_SEGUNDOS, intervaloSegunda, "LONG");
            }
            if (intervaloReaprendizaje != null) {
                actualizarVariable(VariablesService.INTERVALO_REAPRENDIZAJE_SEGUNDOS, intervaloReaprendizaje, "LONG");
            }
            if (factorFacilidadMinimo != null) {
                actualizarVariable(VariablesService.FACTOR_FACILIDAD_MINIMO, factorFacilidadMinimo, "DOUBLE");
            }
            if (factorFacilidadInicial != null) {
                actualizarVariable(VariablesService.FACTOR_FACILIDAD_INICIAL, factorFacilidadInicial, "DOUBLE");
            }
            if (penalizacionFallo != null) {
                actualizarVariable(VariablesService.PENALIZACION_FALLO, penalizacionFallo, "DOUBLE");
            }
            if (maxTarjetasNuevas != null) {
                actualizarVariable(VariablesService.MAX_TARJETAS_NUEVAS_DIA, maxTarjetasNuevas, "INTEGER");
            }
            if (maxTarjetasRevision != null) {
                actualizarVariable(VariablesService.MAX_TARJETAS_REVISION_DIA, maxTarjetasRevision, "INTEGER");
            }
            if (mezclarTarjetas != null) {
                actualizarVariable(VariablesService.MEZCLAR_TARJETAS, mezclarTarjetas, "BOOLEAN");
            }

            log.info("Configuración guardada correctamente");
            return "{\"exito\": true, \"mensaje\": \"Configuración guardada correctamente\"}";

        } catch (Exception e) {
            log.error("Error al guardar configuración: {}", e.getMessage(), e);
            return "{\"exito\": false, \"mensaje\": \"Error: " + e.getMessage() + "\"}";
        }
    }

    /**
     * Activa o desactiva una estructura de frase
     */
    @PostMapping("/api/toggleEstructura")
    @ResponseBody
    public String toggleEstructura(
            @RequestParam String identificador,
            @RequestParam boolean activa) {

        log.info("Cambiando estado de estructura '{}' a {}", identificador, activa);

        try {
            estructuraFraseService.setActiva(identificador, activa);
            return "{\"exito\": true, \"mensaje\": \"Estructura actualizada\"}";
        } catch (Exception e) {
            log.error("Error al actualizar estructura: {}", e.getMessage(), e);
            return "{\"exito\": false, \"mensaje\": \"Error: " + e.getMessage() + "\"}";
        }
    }

    private void actualizarVariable(String clave, String valor, String tipo) {
        Variable variable = Variable.builder()
                .clave(clave)
                .valor(valor)
                .tipo(tipo)
                .build();
        variablesService.guardarVariable(variable);
    }

    /**
     * Obtiene todas las variables en formato JSON
     */
    @GetMapping("/api/variables")
    @ResponseBody
    public List<Variable> obtenerVariables() {
        return variablesService.obtenerTodasLasVariables();
    }
}

