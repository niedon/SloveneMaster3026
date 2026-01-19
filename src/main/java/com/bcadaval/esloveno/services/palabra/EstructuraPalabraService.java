package com.bcadaval.esloveno.services.palabra;

import com.bcadaval.esloveno.beans.base.PalabraFlexion;
import com.bcadaval.esloveno.beans.palabra.AdjetivoFlexion;
import com.bcadaval.esloveno.beans.palabra.SustantivoFlexion;
import com.bcadaval.esloveno.beans.palabra.VerboFlexion;
import com.bcadaval.esloveno.services.ConsultaPalabrasService;
import com.bcadaval.esloveno.services.EstructuraFraseService;
import com.bcadaval.esloveno.structures.CriterioGramatical;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio para buscar palabras según los criterios definidos en las estructuras de frase.
 * Usa tarjetas listas para estudiar (proximaRevision != null AND <= ahora).
 */
@Log4j2
@Service
public class EstructuraPalabraService {

    @Lazy
    @Autowired
    private EstructuraFraseService estructuraFraseService;

    @Lazy
    @Autowired
    private ConsultaPalabrasService consultaPalabrasService;

    /**
     * Obtiene sustantivos listos para estudiar que cumplen criterios de estructuras activas.
     */
    public List<SustantivoFlexion> buscarSustantivosParaEstructurasActivas() {
        List<CriterioGramatical> criterios =
                estructuraFraseService.getCriteriosGramaticalesPorTipo(SustantivoFlexion.class);

        List<SustantivoFlexion> resultado = consultaPalabrasService.listSustantivosListos(criterios);
        log.debug("Encontrados {} SustantivoFlexion para estructuras activas", resultado.size());
        return resultado;
    }

    /**
     * Obtiene verbos listos para estudiar que cumplen criterios de estructuras activas.
     */
    public List<VerboFlexion> buscarVerbosParaEstructurasActivas() {
        List<CriterioGramatical> criterios =
                estructuraFraseService.getCriteriosGramaticalesPorTipo(VerboFlexion.class);

        List<VerboFlexion> resultado = consultaPalabrasService.listVerbosListos(criterios);
        log.debug("Encontrados {} VerboFlexion para estructuras activas", resultado.size());
        return resultado;
    }

    /**
     * Obtiene adjetivos listos para estudiar que cumplen criterios de estructuras activas.
     */
    public List<AdjetivoFlexion> buscarAdjetivosParaEstructurasActivas() {
        List<CriterioGramatical> criterios =
                estructuraFraseService.getCriteriosGramaticalesPorTipo(AdjetivoFlexion.class);

        List<AdjetivoFlexion> resultado = consultaPalabrasService.listAdjetivosListos(criterios);
        log.debug("Encontrados {} AdjetivoFlexion para estructuras activas", resultado.size());
        return resultado;
    }
}

