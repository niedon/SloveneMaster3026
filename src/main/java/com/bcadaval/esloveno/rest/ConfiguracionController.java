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

    public record CategoriaUI(
            String titulo,
            boolean activa,
            boolean invalida,
            String motivoInvalidez,
            String dataCasos,
            String dataFormasVerbales,
            List<String> idsEstructuras
    ) {}

    public record NivelUI(String titulo, String descripcion, List<CategoriaUI> categorias) {}

    @Autowired
    private VariablesService variablesService;

    @Autowired
    private FraseService fraseService;

    /**
     * Muestra la página de configuración
     */
    @SuppressWarnings("SameReturnValue")
    @GetMapping("/configuracion")
    public String mostrarConfiguracion(Model model) {
        log.debug("Accediendo a la página de configuración");

        List<Variable> variables = variablesService.obtenerTodasLasVariables();

        // Convertir lista a mapa para fácil acceso en JSP/Thymeleaf
        Map<String, Variable> variablesMap = variables.stream()
                .collect(Collectors.toMap(Variable::getClave, v -> v));

        model.addAttribute("variablesMap", variablesMap);

        // Estructuras de frase agrupadas por nivel de dificultad y categoría
        var estructurasAgrupadas = fraseService.getFrasesAgrupadasPorNivelYCategoria();
        
        List<NivelUI> nivelesUI = estructurasAgrupadas.entrySet().stream().map(entryNivel -> {
            var nivel = entryNivel.getKey();
            var categoriasMap = entryNivel.getValue();

            List<CategoriaUI> categoriasUI = categoriasMap.entrySet().stream().map(entryCat -> {
                var categoria = entryCat.getKey();
                var frases = entryCat.getValue();

                boolean activa = frases.stream().anyMatch(FraseConfigDTO::activa);
                
                var fraseInvalidaOpt = frases.stream().filter(FraseConfigDTO::invalida).findFirst();
                boolean invalida = fraseInvalidaOpt.isPresent();
                String motivoInvalidez = invalida ? fraseInvalidaOpt.get().motivoInvalidez() : "";

                String dataCasos = frases.stream()
                        .flatMap(f -> f.casosUsados().stream())
                        .map(Enum::name)
                        .distinct()
                        .collect(Collectors.joining(","));

                String dataFormasVerbales = frases.stream()
                        .flatMap(f -> f.formasVerbalesUsadas().stream())
                        .map(Enum::name)
                        .distinct()
                        .collect(Collectors.joining(","));

                List<String> idsEstructuras = frases.stream()
                        .map(FraseConfigDTO::identificador)
                        .toList();

                return new CategoriaUI(
                        categoria.getTitulo(),
                        activa,
                        invalida,
                        motivoInvalidez,
                        dataCasos,
                        dataFormasVerbales,
                        idsEstructuras
                );
            }).toList();

            return new NivelUI(nivel.getTitulo(), nivel.getDescripcion(), categoriasUI);
        }).toList();

        model.addAttribute("nivelesUI", nivelesUI);

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
            // 1. Guardar variables generales
            actualizarVariable(VariablesService.INTERVALO_INICIAL_SEGUNDOS, dto.getIntervaloInicial(), "LONG");
            actualizarVariable(VariablesService.INTERVALO_SEGUNDA_SEGUNDOS, dto.getIntervaloSegunda(), "LONG");
            actualizarVariable(VariablesService.INTERVALO_REAPRENDIZAJE_SEGUNDOS, dto.getIntervaloReaprendizaje(), "LONG");
            actualizarVariable(VariablesService.FACTOR_FACILIDAD_MINIMO, dto.getFactorFacilidadMinimo(), "DOUBLE");
            actualizarVariable(VariablesService.FACTOR_FACILIDAD_INICIAL, dto.getFactorFacilidadInicial(), "DOUBLE");
            actualizarVariable(VariablesService.PENALIZACION_FALLO, dto.getPenalizacionFallo(), "DOUBLE");
            actualizarVariable(VariablesService.MAX_TARJETAS_NUEVAS_DIA, dto.getMaxTarjetasNuevas(), "INTEGER");
            actualizarVariable(VariablesService.MEZCLAR_TARJETAS, dto.getMezclarTarjetas(), "BOOLEAN");
            actualizarVariable(VariablesService.MAX_TARJETAS_REVISION_DIA, dto.getMaxTarjetasRevision(), "INTEGER");

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
        if (valor != null) {
            variablesService.guardarVariable(Variable.builder().clave(clave).valor(valor).tipo(tipo).build());
        }
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
