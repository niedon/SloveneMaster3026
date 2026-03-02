package com.bcadaval.esloveno.services;

import com.bcadaval.esloveno.beans.base.PalabraFlexion;
import com.bcadaval.esloveno.beans.palabra.*;
import com.bcadaval.esloveno.repo.AdjetivoFlexionRepo;
import com.bcadaval.esloveno.repo.NumeralFlexionRepo;
import com.bcadaval.esloveno.repo.ParticulaFlexionRepo;
import com.bcadaval.esloveno.repo.PronombreFlexionRepo;
import com.bcadaval.esloveno.repo.SustantivoFlexionRepo;
import com.bcadaval.esloveno.repo.VerboFlexionRepo;
import com.bcadaval.esloveno.structures.frase.criterio.CriterioBusquedaNuevo;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
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

    @Autowired
    private ParticulaFlexionRepo particulaFlexionRepo;

    /**
     * Obtiene la lista de verbos listos para estudiar que cumplen al menos uno de los criterios dados.
     * Incluye tarjetas de revisión (proximaRevision &lt;= ahora) y tarjetas nuevas (proximaRevision IS NULL, palabra completa).
     *
     * @param criterios criterios expandidos (OR entre ellos)
     * @return lista filtrada de verbos listos para estudio
     */
    public List<VerboFlexion> listVerbosListos(List<CriterioBusquedaNuevo<VerboFlexion>> criterios) {
        if (criterios.isEmpty()) return List.of();
        Instant ahora = Instant.now();
        List<VerboFlexion> revision = verboFlexionRepo.streamListosParaEstudiar(ahora)
                .filter(vf -> criterios.stream().anyMatch(c -> c.cumpleCriteriosFijos(vf)))
                .toList();
        List<VerboFlexion> nuevos = verboFlexionRepo.streamNuevos()
                .filter(vf -> criterios.stream().anyMatch(c -> c.cumpleCriteriosFijos(vf)))
                .toList();
        List<VerboFlexion> resultado = new ArrayList<>(revision);
        resultado.addAll(nuevos);
        return resultado;
    }

    /**
     * Obtiene la lista de sustantivos listos para estudiar que cumplen al menos uno de los criterios dados.
     * Incluye tarjetas de revisión y tarjetas nuevas.
     *
     * @param criterios criterios expandidos (OR entre ellos)
     * @return lista filtrada de sustantivos listos para estudio
     */
    public List<SustantivoFlexion> listSustantivosListos(List<CriterioBusquedaNuevo<SustantivoFlexion>> criterios) {
        if (criterios.isEmpty()) return List.of();
        Instant ahora = Instant.now();
        List<SustantivoFlexion> revision = sustantivoFlexionRepo.streamListosParaEstudiar(ahora)
                .filter(sf -> criterios.stream().anyMatch(c -> c.cumpleCriteriosFijos(sf)))
                .toList();
        List<SustantivoFlexion> nuevos = sustantivoFlexionRepo.streamNuevos()
                .filter(sf -> criterios.stream().anyMatch(c -> c.cumpleCriteriosFijos(sf)))
                .toList();
        List<SustantivoFlexion> resultado = new ArrayList<>(revision);
        resultado.addAll(nuevos);
        return resultado;
    }

    /**
     * Obtiene la lista de adjetivos listos para estudiar que cumplen al menos uno de los criterios dados.
     * Incluye tarjetas de revisión y tarjetas nuevas.
     *
     * @param criterios criterios expandidos (OR entre ellos)
     * @return lista filtrada de adjetivos listos para estudio
     */
    public List<AdjetivoFlexion> listAdjetivosListos(List<CriterioBusquedaNuevo<AdjetivoFlexion>> criterios) {
        if (criterios.isEmpty()) return List.of();
        Instant ahora = Instant.now();
        List<AdjetivoFlexion> revision = adjetivoFlexionRepo.streamListosParaEstudiar(ahora)
                .filter(af -> criterios.stream().anyMatch(c -> c.cumpleCriteriosFijos(af)))
                .toList();
        List<AdjetivoFlexion> nuevos = adjetivoFlexionRepo.streamNuevos()
                .filter(af -> criterios.stream().anyMatch(c -> c.cumpleCriteriosFijos(af)))
                .toList();
        List<AdjetivoFlexion> resultado = new ArrayList<>(revision);
        resultado.addAll(nuevos);
        return resultado;
    }

    /**
     * Obtiene la lista de numerales listos para estudiar que cumplen al menos uno de los criterios dados.
     * Incluye tarjetas de revisión y tarjetas nuevas.
     *
     * @param criterios criterios expandidos (OR entre ellos)
     * @return lista filtrada de numerales listos para estudio
     */
    public List<NumeralFlexion> listNumeralesListos(List<CriterioBusquedaNuevo<NumeralFlexion>> criterios) {
        if (criterios.isEmpty()) return List.of();
        Instant ahora = Instant.now();
        List<NumeralFlexion> revision = numeralFlexionRepo.streamListosParaEstudiar(ahora)
                .filter(nf -> criterios.stream().anyMatch(c -> c.cumpleCriteriosFijos(nf)))
                .toList();
        List<NumeralFlexion> nuevos = numeralFlexionRepo.streamNuevos()
                .filter(nf -> criterios.stream().anyMatch(c -> c.cumpleCriteriosFijos(nf)))
                .toList();
        List<NumeralFlexion> resultado = new ArrayList<>(revision);
        resultado.addAll(nuevos);
        return resultado;
    }

    /**
     * Obtiene la lista de pronombres listos para estudiar que cumplen al menos uno de los criterios dados.
     * Incluye tarjetas de revisión y tarjetas nuevas.
     *
     * @param criterios criterios expandidos (OR entre ellos)
     * @return lista filtrada de pronombres listos para estudio
     */
    public List<PronombreFlexion> listPronombresListos(List<CriterioBusquedaNuevo<PronombreFlexion>> criterios) {
        if (criterios.isEmpty()) return List.of();
        Instant ahora = Instant.now();
        List<PronombreFlexion> revision = pronombreFlexionRepo.streamListosParaEstudiar(ahora)
                .filter(pf -> criterios.stream().anyMatch(c -> c.cumpleCriteriosFijos(pf)))
                .toList();
        List<PronombreFlexion> nuevos = pronombreFlexionRepo.streamNuevos()
                .filter(pf -> criterios.stream().anyMatch(c -> c.cumpleCriteriosFijos(pf)))
                .toList();
        List<PronombreFlexion> resultado = new ArrayList<>(revision);
        resultado.addAll(nuevos);
        return resultado;
    }

    // ============================================
    // Métodos para estadísticas (todos los activos)
    // ============================================

    /**
     * Obtiene todos los verbos activos que cumplen criterios (para estadísticas).
     * Incluye tarjetas con proximaRevision != null y tarjetas nuevas (proximaRevision IS NULL, palabra completa).
     */
    public List<VerboFlexion> listVerbosActivos(List<CriterioBusquedaNuevo<VerboFlexion>> criterios) {
        if (criterios.isEmpty()) return List.of();
        List<VerboFlexion> activos = verboFlexionRepo.streamActivos()
                .filter(vf -> criterios.stream().anyMatch(c -> c.cumpleCriteriosFijos(vf)))
                .toList();
        List<VerboFlexion> nuevos = verboFlexionRepo.streamNuevos()
                .filter(vf -> criterios.stream().anyMatch(c -> c.cumpleCriteriosFijos(vf)))
                .toList();
        List<VerboFlexion> resultado = new ArrayList<>(activos);
        resultado.addAll(nuevos);
        return resultado;
    }

    /**
     * Obtiene todos los sustantivos activos que cumplen criterios (para estadísticas).
     * Incluye tarjetas activas y nuevas.
     */
    public List<SustantivoFlexion> listSustantivosActivos(List<CriterioBusquedaNuevo<SustantivoFlexion>> criterios) {
        if (criterios.isEmpty()) return List.of();
        List<SustantivoFlexion> activos = sustantivoFlexionRepo.streamActivos()
                .filter(sf -> criterios.stream().anyMatch(c -> c.cumpleCriteriosFijos(sf)))
                .toList();
        List<SustantivoFlexion> nuevos = sustantivoFlexionRepo.streamNuevos()
                .filter(sf -> criterios.stream().anyMatch(c -> c.cumpleCriteriosFijos(sf)))
                .toList();
        List<SustantivoFlexion> resultado = new ArrayList<>(activos);
        resultado.addAll(nuevos);
        return resultado;
    }

    /**
     * Obtiene todos los adjetivos activos que cumplen criterios (para estadísticas).
     * Incluye tarjetas activas y nuevas.
     */
    public List<AdjetivoFlexion> listAdjetivosActivos(List<CriterioBusquedaNuevo<AdjetivoFlexion>> criterios) {
        if (criterios.isEmpty()) return List.of();
        List<AdjetivoFlexion> activos = adjetivoFlexionRepo.streamActivos()
                .filter(af -> criterios.stream().anyMatch(c -> c.cumpleCriteriosFijos(af)))
                .toList();
        List<AdjetivoFlexion> nuevos = adjetivoFlexionRepo.streamNuevos()
                .filter(af -> criterios.stream().anyMatch(c -> c.cumpleCriteriosFijos(af)))
                .toList();
        List<AdjetivoFlexion> resultado = new ArrayList<>(activos);
        resultado.addAll(nuevos);
        return resultado;
    }

    /**
     * Obtiene todos los numerales activos que cumplen criterios (para estadísticas).
     * Incluye tarjetas activas y nuevas.
     */
    public List<NumeralFlexion> listNumeralesActivos(List<CriterioBusquedaNuevo<NumeralFlexion>> criterios) {
        if (criterios.isEmpty()) return List.of();
        List<NumeralFlexion> activos = numeralFlexionRepo.streamActivos()
                .filter(nf -> criterios.stream().anyMatch(c -> c.cumpleCriteriosFijos(nf)))
                .toList();
        List<NumeralFlexion> nuevos = numeralFlexionRepo.streamNuevos()
                .filter(nf -> criterios.stream().anyMatch(c -> c.cumpleCriteriosFijos(nf)))
                .toList();
        List<NumeralFlexion> resultado = new ArrayList<>(activos);
        resultado.addAll(nuevos);
        return resultado;
    }

    /**
     * Obtiene todos los pronombres activos que cumplen criterios (para estadísticas).
     * Incluye tarjetas activas y nuevas.
     */
    public List<PronombreFlexion> listPronombresActivos(List<CriterioBusquedaNuevo<PronombreFlexion>> criterios) {
        if (criterios.isEmpty()) return List.of();
        List<PronombreFlexion> activos = pronombreFlexionRepo.streamActivos()
                .filter(pf -> criterios.stream().anyMatch(c -> c.cumpleCriteriosFijos(pf)))
                .toList();
        List<PronombreFlexion> nuevos = pronombreFlexionRepo.streamNuevos()
                .filter(pf -> criterios.stream().anyMatch(c -> c.cumpleCriteriosFijos(pf)))
                .toList();
        List<PronombreFlexion> resultado = new ArrayList<>(activos);
        resultado.addAll(nuevos);
        return resultado;
    }

    // ============================================
    // Partículas
    // ============================================

    /**
     * Obtiene la lista de partículas listas para estudiar que cumplen al menos uno de los criterios dados.
     * Incluye tarjetas de revisión y tarjetas nuevas.
     *
     * @param criterios criterios expandidos (OR entre ellos)
     * @return lista filtrada de partículas listas para estudio
     */
    public List<ParticulaFlexion> listParticulasListas(List<CriterioBusquedaNuevo<ParticulaFlexion>> criterios) {
        if (criterios.isEmpty()) return List.of();
        Instant ahora = Instant.now();
        List<ParticulaFlexion> revision = particulaFlexionRepo.streamListosParaEstudiar(ahora)
                .filter(paf -> criterios.stream().anyMatch(c -> c.cumpleCriteriosFijos(paf)))
                .toList();
        List<ParticulaFlexion> nuevos = particulaFlexionRepo.streamNuevos()
                .filter(paf -> criterios.stream().anyMatch(c -> c.cumpleCriteriosFijos(paf)))
                .toList();
        List<ParticulaFlexion> resultado = new ArrayList<>(revision);
        resultado.addAll(nuevos);
        return resultado;
    }

    /**
     * Obtiene todas las partículas activas que cumplen criterios (para estadísticas).
     * Incluye tarjetas activas y nuevas.
     */
    public List<ParticulaFlexion> listParticulasActivas(List<CriterioBusquedaNuevo<ParticulaFlexion>> criterios) {
        if (criterios.isEmpty()) return List.of();
        List<ParticulaFlexion> activos = particulaFlexionRepo.streamActivos()
                .filter(paf -> criterios.stream().anyMatch(c -> c.cumpleCriteriosFijos(paf)))
                .toList();
        List<ParticulaFlexion> nuevos = particulaFlexionRepo.streamNuevos()
                .filter(paf -> criterios.stream().anyMatch(c -> c.cumpleCriteriosFijos(paf)))
                .toList();
        List<ParticulaFlexion> resultado = new ArrayList<>(activos);
        resultado.addAll(nuevos);
        return resultado;
    }
}
