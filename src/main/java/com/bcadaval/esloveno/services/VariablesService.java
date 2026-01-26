package com.bcadaval.esloveno.services;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcadaval.esloveno.beans.Variable;
import com.bcadaval.esloveno.beans.enums.Caso;
import com.bcadaval.esloveno.repo.VariablesRepo;

import lombok.extern.log4j.Log4j2;

/**
 * Servicio para gestionar las variables de configuración del sistema SRS
 */
@Log4j2
@Service
public class VariablesService {

    @Autowired
    private VariablesRepo variablesRepo;

    // Claves de variables
    public static final String INTERVALO_INICIAL_SEGUNDOS = "INTERVALO_INICIAL_SEGUNDOS";
    public static final String INTERVALO_SEGUNDA_SEGUNDOS = "INTERVALO_SEGUNDA_SEGUNDOS";
    public static final String INTERVALO_REAPRENDIZAJE_SEGUNDOS = "INTERVALO_REAPRENDIZAJE_SEGUNDOS";
    public static final String FACTOR_FACILIDAD_MINIMO = "FACTOR_FACILIDAD_MINIMO";
    public static final String FACTOR_FACILIDAD_INICIAL = "FACTOR_FACILIDAD_INICIAL";
    public static final String PENALIZACION_FALLO = "PENALIZACION_FALLO";
    public static final String MAX_TARJETAS_NUEVAS_DIA = "MAX_TARJETAS_NUEVAS_DIA";
    public static final String MAX_TARJETAS_REVISION_DIA = "MAX_TARJETAS_REVISION_DIA";
    public static final String MEZCLAR_TARJETAS = "MEZCLAR_TARJETAS";

    /**
     * Obtiene todas las variables de configuración
     */
    public List<Variable> obtenerTodasLasVariables() {
        return variablesRepo.findAll();
    }

    /**
     * Guarda una variable
     */
    public Variable guardarVariable(Variable variable) {
        return variablesRepo.save(variable);
    }

    /**
     * Obtiene el intervalo inicial en segundos (primera repetición)
     */
    public Long getIntervaloInicialSegundos() {
        return variablesRepo.findByClave(INTERVALO_INICIAL_SEGUNDOS)
                .map(Variable::getValorAsLong)
                .orElse(600L); // 10 minutos por defecto
    }

    /**
     * Obtiene el intervalo de segunda repetición en segundos
     */
    public Long getIntervaloSegundaSegundos() {
        return variablesRepo.findByClave(INTERVALO_SEGUNDA_SEGUNDOS)
                .map(Variable::getValorAsLong)
                .orElse(3600L); // 1 hora por defecto
    }

    /**
     * Obtiene el intervalo de reaprendizaje en segundos (tras fallar)
     */
    public Long getIntervaloReaprendizajeSegundos() {
        return variablesRepo.findByClave(INTERVALO_REAPRENDIZAJE_SEGUNDOS)
                .map(Variable::getValorAsLong)
                .orElse(30L); // 30 segundos por defecto
    }

    /**
     * Obtiene el factor de facilidad mínimo
     */
    public Double getFactorFacilidadMinimo() {
        return variablesRepo.findByClave(FACTOR_FACILIDAD_MINIMO)
                .map(Variable::getValorAsDouble)
                .orElse(1.3);
    }

    /**
     * Obtiene el factor de facilidad inicial
     */
    public Double getFactorFacilidadInicial() {
        return variablesRepo.findByClave(FACTOR_FACILIDAD_INICIAL)
                .map(Variable::getValorAsDouble)
                .orElse(2.5);
    }

    /**
     * Obtiene la penalización por fallo
     */
    public Double getPenalizacionFallo() {
        return variablesRepo.findByClave(PENALIZACION_FALLO)
                .map(Variable::getValorAsDouble)
                .orElse(0.2);
    }

    /**
     * Obtiene el máximo de tarjetas nuevas por día
     */
    public Integer getMaxTarjetasNuevasDia() {
        return variablesRepo.findByClave(MAX_TARJETAS_NUEVAS_DIA)
                .map(Variable::getValorAsInteger)
                .orElse(20);
    }

    /**
     * Obtiene el máximo de revisiones por día
     */
    public Integer getMaxTarjetasRevisionDia() {
        return variablesRepo.findByClave(MAX_TARJETAS_REVISION_DIA)
                .map(Variable::getValorAsInteger)
                .orElse(100);
    }

    public Boolean getMezclarTarjetas() {
        return variablesRepo.findByClave(MEZCLAR_TARJETAS)
                .map(Variable::getValorAsBoolean)
                .orElse(true);
    }

}

