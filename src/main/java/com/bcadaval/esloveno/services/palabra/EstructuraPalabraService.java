package com.bcadaval.esloveno.services.palabra;

import com.bcadaval.esloveno.beans.base.PalabraFlexion;
import com.bcadaval.esloveno.beans.palabra.SustantivoFlexion;
import com.bcadaval.esloveno.beans.palabra.VerboFlexion;
import com.bcadaval.esloveno.repo.SustantivoFlexionRepo;
import com.bcadaval.esloveno.repo.VerboFlexionRepo;
import com.bcadaval.esloveno.services.EstructuraFraseService;
import com.bcadaval.esloveno.structures.EstructuraFrase;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio para buscar palabras según los criterios definidos en las estructuras de frase.
 * Combina los criterios de todas las estructuras activas para hacer consultas eficientes.
 */
@Log4j2
@Service
public class EstructuraPalabraService {

    @Lazy
    @Autowired
    private EstructuraFraseService estructuraFraseService;

    @Autowired
    private SustantivoFlexionRepo sustantivoFlexionRepo;

    @Autowired
    private VerboFlexionRepo verboFlexionRepo;

    /**
     * Obtiene todos los SustantivoFlexion que cumplen algún criterio de las estructuras activas.
     *
     * @return Lista de SustantivoFlexion que cumplen algún criterio
     */
    public List<SustantivoFlexion> buscarSustantivosParaEstructurasActivas() {
        Specification<SustantivoFlexion> specCombinada = combinarSpecifications(
                SustantivoFlexion.class,
                estructuraFraseService.getEstructurasActivas()
        );

        if (specCombinada == null) {
            log.debug("No hay criterios de SustantivoFlexion en estructuras activas");
            return List.of();
        }

        List<SustantivoFlexion> resultado = sustantivoFlexionRepo.findAll(specCombinada);
        log.debug("Encontrados {} SustantivoFlexion para estructuras activas", resultado.size());
        return resultado;
    }

    /**
     * Obtiene todos los VerboFlexion que cumplen algún criterio de las estructuras activas.
     *
     * @return Lista de VerboFlexion que cumplen algún criterio
     */
    public List<VerboFlexion> buscarVerbosParaEstructurasActivas() {
        Specification<VerboFlexion> specCombinada = combinarSpecifications(
                VerboFlexion.class,
                estructuraFraseService.getEstructurasActivas()
        );

        if (specCombinada == null) {
            log.debug("No hay criterios de VerboFlexion en estructuras activas");
            return List.of();
        }

        List<VerboFlexion> resultado = verboFlexionRepo.findAll(specCombinada);
        log.debug("Encontrados {} VerboFlexion para estructuras activas", resultado.size());
        return resultado;
    }

    /**
     * Obtiene todas las PalabraFlexion de cualquier tipo que cumplen
     * algún criterio de las estructuras activas.
     *
     * @return Mapa de tipo de flexión a lista de palabras
     */
    public Map<Class<? extends PalabraFlexion>, List<? extends PalabraFlexion>> buscarTodasParaEstructurasActivas() {
        Map<Class<? extends PalabraFlexion>, List<? extends PalabraFlexion>> resultado = new HashMap<>();

        List<SustantivoFlexion> sustantivos = buscarSustantivosParaEstructurasActivas();
        if (!sustantivos.isEmpty()) {
            resultado.put(SustantivoFlexion.class, sustantivos);
        }

        List<VerboFlexion> verbos = buscarVerbosParaEstructurasActivas();
        if (!verbos.isEmpty()) {
            resultado.put(VerboFlexion.class, verbos);
        }

        // Añadir más tipos aquí cuando se soporten (AdjetivoFlexion, etc.)

        return resultado;
    }

    /**
     * Combina las Specifications de todas las estructuras para un tipo específico.
     * Las combina con OR (cualquier criterio es válido).
     *
     * @param tipoFlexion Clase del tipo de flexión
     * @param estructuras Lista de estructuras de las que obtener criterios
     * @return Specification combinada o null si no hay criterios de ese tipo
     */
    private <T extends PalabraFlexion<?>> Specification<T> combinarSpecifications(
            Class<T> tipoFlexion,
            List<EstructuraFrase> estructuras) {

        List<Specification<T>> todasSpecs = new ArrayList<>();

        for (EstructuraFrase estructura : estructuras) {
            Specification<T> spec = estructura.getSpecificationPorTipo(tipoFlexion);
            if (spec != null) {
                todasSpecs.add(spec);
            }
        }

        if (todasSpecs.isEmpty()) {
            return null;
        }

        // Combinar todas con OR
        return todasSpecs.stream()
                .reduce(Specification::or)
                .orElse(null);
    }

    /**
     * Busca palabras de un tipo específico usando una specification directa.
     *
     * @param tipoFlexion Clase del tipo de flexión
     * @param spec Specification a aplicar
     * @return Lista de palabras que cumplen la specification
     */
    @SuppressWarnings("unchecked")
    public <T extends PalabraFlexion> List<T> buscarPorSpecification(
            Class<T> tipoFlexion,
            Specification<T> spec) {
//TODO aplicar switch-case de enum TipoPalabra o FraseTipoPalabra para detectar falta de soporte cuando se añadan nuevas clases
        if (tipoFlexion == SustantivoFlexion.class) {
            return (List<T>) sustantivoFlexionRepo.findAll((Specification<SustantivoFlexion>) spec);
        } else if (tipoFlexion == VerboFlexion.class) {
            return (List<T>) verboFlexionRepo.findAll((Specification<VerboFlexion>) spec);
        }

        throw new IllegalArgumentException("Tipo de flexión no soportado: " + tipoFlexion.getSimpleName());
    }
}

