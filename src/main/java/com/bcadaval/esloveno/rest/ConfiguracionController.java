package com.bcadaval.esloveno.rest;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bcadaval.esloveno.beans.Variable;
import com.bcadaval.esloveno.rest.dto.ConfiguracionDTO;
import com.bcadaval.esloveno.services.FraseService;
import com.bcadaval.esloveno.services.FraseService.FraseConfigDTO;
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
    private FraseService fraseService;

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


        // Estructuras de frase agrupadas por nivel de dificultad
        model.addAttribute("estructurasPorDificultad", fraseService.getFrasesAgrupadasPorDificultad());

        // Casos activos derivados de las estructuras activas (solo para mostrar info)
        model.addAttribute("casosActivos", fraseService.getCasosActivos());

        // Formas verbales activas derivadas de las estructuras activas
        model.addAttribute("formasVerbalesActivas", fraseService.getFormasVerbalesActivas());

        return "configuracion";
    }

    /**
     * Guarda la configuración de variables y estructuras
     */
    @PostMapping("/api/guardarConfiguracion")
    @ResponseBody
    public String guardarConfiguracion(@ModelAttribute ConfiguracionDTO dto) {

        log.info("Guardando configuración completa");

        try {
            //TODO ver si se puede quitar if
            // 1. Guardar variables generales
            if (dto.getIntervaloInicial() != null) {
                actualizarVariable(VariablesService.INTERVALO_INICIAL_SEGUNDOS, dto.getIntervaloInicial(), "LONG");
            }
            if (dto.getIntervaloSegunda() != null) {
                actualizarVariable(VariablesService.INTERVALO_SEGUNDA_SEGUNDOS, dto.getIntervaloSegunda(), "LONG");
            }
            if (dto.getIntervaloReaprendizaje() != null) {
                actualizarVariable(VariablesService.INTERVALO_REAPRENDIZAJE_SEGUNDOS, dto.getIntervaloReaprendizaje(), "LONG");
            }
            if (dto.getFactorFacilidadMinimo() != null) {
                actualizarVariable(VariablesService.FACTOR_FACILIDAD_MINIMO, dto.getFactorFacilidadMinimo(), "DOUBLE");
            }
            if (dto.getFactorFacilidadInicial() != null) {
                actualizarVariable(VariablesService.FACTOR_FACILIDAD_INICIAL, dto.getFactorFacilidadInicial(), "DOUBLE");
            }
            if (dto.getPenalizacionFallo() != null) {
                actualizarVariable(VariablesService.PENALIZACION_FALLO, dto.getPenalizacionFallo(), "DOUBLE");
            }
            if (dto.getMaxTarjetasNuevas() != null) {
                actualizarVariable(VariablesService.MAX_TARJETAS_NUEVAS_DIA, dto.getMaxTarjetasNuevas(), "INTEGER");
            }
            if (dto.getMaxTarjetasRevision() != null) {
                actualizarVariable(VariablesService.MAX_TARJETAS_REVISION_DIA, dto.getMaxTarjetasRevision(), "INTEGER");
            }
            if (dto.getMezclarTarjetas() != null) {
                actualizarVariable(VariablesService.MEZCLAR_TARJETAS, dto.getMezclarTarjetas(), "BOOLEAN");
            }

            // 2. Actualizar estructuras
            // Obtener el conjunto de IDs que deben estar activos
            Set<String> estructurasActivas = dto.getEstructuras() != null 
                    ? Set.copyOf(dto.getEstructuras()) 
                    : Set.of();
            
            // Obtener todas las estructuras posibles para actualizar su estado
            List<FraseConfigDTO> todasLasFrases = fraseService.getTodasParaConfiguracion();
            
            for (FraseConfigDTO frase : todasLasFrases) {
                String id = frase.identificador();
                boolean debeEstarActiva = estructurasActivas.contains(id);
                
                // Actualizamos el estado solo si ha cambiado (para evitar escrituras innecesarias si fuera el caso)
                // Aunque setActiva probablemente sea barato si solo hace save.
                fraseService.setActiva(id, debeEstarActiva);
            }

            // 3. Recalcular criterios una única vez al final
            fraseService.onConfiguracionGuardada();

            log.info("Configuración guardada correctamente");
            return "{\"exito\": true, \"mensaje\": \"Configuración guardada correctamente\"}";

        } catch (Exception e) {
            log.error("Error al guardar configuración: {}", e.getMessage(), e);
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
