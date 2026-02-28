package com.bcadaval.esloveno.services;

import com.bcadaval.esloveno.beans.base.PalabraFlexion;
import com.bcadaval.esloveno.beans.palabra.*;
import com.bcadaval.esloveno.repo.AdjetivoFlexionRepo;
import com.bcadaval.esloveno.repo.NumeralFlexionRepo;
import com.bcadaval.esloveno.repo.PronombreFlexionRepo;
import com.bcadaval.esloveno.repo.SustantivoFlexionRepo;
import com.bcadaval.esloveno.repo.VerboFlexionRepo;
import com.bcadaval.esloveno.structures.frase.criterio.CriterioBusquedaNuevo;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Servicio de consulta de palabras para estudio usando el sistema de criterios.
 * <p>
 * Usa {@link CriterioBusquedaNuevo} con sus restricciones tipadas para filtrar en memoria.
 * <p>
 * Estrategia:
 * <ul>
 *   <li>La BD filtra por campos SRS (proximaRevision != null AND proximaRevision &lt;= ahora)</li>
 *   <li>El filtrado gramatical se aplica en memoria con {@link CriterioBusquedaNuevo#cumpleCriteriosFijos(PalabraFlexion)}</li>
 * </ul>
 * <p>
 * IMPORTANTE: Todos los métodos son {@code @Transactional(readOnly = true)} para permitir
 * el consumo de streams desde JPA sin cerrar la conexión.
 */
@Log4j2
@Service
@Transactional(readOnly = true)
public class ConsultaPalabrasNuevoService {

    @Autowired
    private VerboFlexionRepo verboFlexionRepo;

    @Autowired
    private SustantivoFlexionRepo sustantivoFlexionRepo;

    @Autowired
    private AdjetivoFlexionRepo adjetivoFlexionRepo;

    @Autowired
    private NumeralFlexionRepo numeralFlexionRepo;

    @Autowired
    private PronombreFlexionRepo pronombreFlexionRepo;

    /**
     * Obtiene la lista de verbos listos para estudiar que cumplen al menos uno de los criterios dados.
     *
     * @param criterios criterios expandidos (OR entre ellos)
     * @return lista filtrada de verbos listos para estudio
     */
    public List<VerboFlexion> listVerbosListos(List<CriterioBusquedaNuevo<VerboFlexion>> criterios) {
        if (criterios.isEmpty()) return List.of();
        Instant ahora = Instant.now();
        return verboFlexionRepo.streamListosParaEstudiar(ahora)
                .filter(vf -> criterios.stream().anyMatch(c -> c.cumpleCriteriosFijos(vf)))
                .toList();
    }

    /**
     * Obtiene la lista de sustantivos listos para estudiar que cumplen al menos uno de los criterios dados.
     *
     * @param criterios criterios expandidos (OR entre ellos)
     * @return lista filtrada de sustantivos listos para estudio
     */
    public List<SustantivoFlexion> listSustantivosListos(List<CriterioBusquedaNuevo<SustantivoFlexion>> criterios) {
        if (criterios.isEmpty()) return List.of();
        Instant ahora = Instant.now();
        return sustantivoFlexionRepo.streamListosParaEstudiar(ahora)
                .filter(sf -> criterios.stream().anyMatch(c -> c.cumpleCriteriosFijos(sf)))
                .toList();
    }

    /**
     * Obtiene la lista de adjetivos listos para estudiar que cumplen al menos uno de los criterios dados.
     *
     * @param criterios criterios expandidos (OR entre ellos)
     * @return lista filtrada de adjetivos listos para estudio
     */
    public List<AdjetivoFlexion> listAdjetivosListos(List<CriterioBusquedaNuevo<AdjetivoFlexion>> criterios) {
        if (criterios.isEmpty()) return List.of();
        Instant ahora = Instant.now();
        return adjetivoFlexionRepo.streamListosParaEstudiar(ahora)
                .filter(af -> criterios.stream().anyMatch(c -> c.cumpleCriteriosFijos(af)))
                .toList();
    }

    /**
     * Obtiene la lista de numerales listos para estudiar que cumplen al menos uno de los criterios dados.
     *
     * @param criterios criterios expandidos (OR entre ellos)
     * @return lista filtrada de numerales listos para estudio
     */
    public List<NumeralFlexion> listNumeralesListos(List<CriterioBusquedaNuevo<NumeralFlexion>> criterios) {
        if (criterios.isEmpty()) return List.of();
        Instant ahora = Instant.now();
        return numeralFlexionRepo.streamListosParaEstudiar(ahora)
                .filter(nf -> criterios.stream().anyMatch(c -> c.cumpleCriteriosFijos(nf)))
                .toList();
    }

    /**
     * Obtiene la lista de pronombres listos para estudiar que cumplen al menos uno de los criterios dados.
     *
     * @param criterios criterios expandidos (OR entre ellos)
     * @return lista filtrada de pronombres listos para estudio
     */
    public List<PronombreFlexion> listPronombresListos(List<CriterioBusquedaNuevo<PronombreFlexion>> criterios) {
        if (criterios.isEmpty()) return List.of();
        Instant ahora = Instant.now();
        return pronombreFlexionRepo.streamListosParaEstudiar(ahora)
                .filter(pf -> criterios.stream().anyMatch(c -> c.cumpleCriteriosFijos(pf)))
                .toList();
    }

    // ============================================
    // Métodos para estadísticas (todos los activos)
    // ============================================

    /**
     * Obtiene todos los verbos activos que cumplen criterios (para estadísticas).
     */
    public List<VerboFlexion> listVerbosActivos(List<CriterioBusquedaNuevo<VerboFlexion>> criterios) {
        if (criterios.isEmpty()) return List.of();
        return verboFlexionRepo.streamActivos()
                .filter(vf -> criterios.stream().anyMatch(c -> c.cumpleCriteriosFijos(vf)))
                .toList();
    }

    /**
     * Obtiene todos los sustantivos activos que cumplen criterios (para estadísticas).
     */
    public List<SustantivoFlexion> listSustantivosActivos(List<CriterioBusquedaNuevo<SustantivoFlexion>> criterios) {
        if (criterios.isEmpty()) return List.of();
        return sustantivoFlexionRepo.streamActivos()
                .filter(sf -> criterios.stream().anyMatch(c -> c.cumpleCriteriosFijos(sf)))
                .toList();
    }

    /**
     * Obtiene todos los adjetivos activos que cumplen criterios (para estadísticas).
     */
    public List<AdjetivoFlexion> listAdjetivosActivos(List<CriterioBusquedaNuevo<AdjetivoFlexion>> criterios) {
        if (criterios.isEmpty()) return List.of();
        return adjetivoFlexionRepo.streamActivos()
                .filter(af -> criterios.stream().anyMatch(c -> c.cumpleCriteriosFijos(af)))
                .toList();
    }

    /**
     * Obtiene todos los numerales activos que cumplen criterios (para estadísticas).
     */
    public List<NumeralFlexion> listNumeralesActivos(List<CriterioBusquedaNuevo<NumeralFlexion>> criterios) {
        if (criterios.isEmpty()) return List.of();
        return numeralFlexionRepo.streamActivos()
                .filter(nf -> criterios.stream().anyMatch(c -> c.cumpleCriteriosFijos(nf)))
                .toList();
    }

    /**
     * Obtiene todos los pronombres activos que cumplen criterios (para estadísticas).
     */
    public List<PronombreFlexion> listPronombresActivos(List<CriterioBusquedaNuevo<PronombreFlexion>> criterios) {
        if (criterios.isEmpty()) return List.of();
        return pronombreFlexionRepo.streamActivos()
                .filter(pf -> criterios.stream().anyMatch(c -> c.cumpleCriteriosFijos(pf)))
                .toList();
    }
}



