package com.bcadaval.esloveno.services;

import com.bcadaval.esloveno.beans.palabra.AdjetivoFlexion;
import com.bcadaval.esloveno.beans.palabra.SustantivoFlexion;
import com.bcadaval.esloveno.beans.palabra.VerboFlexion;
import com.bcadaval.esloveno.repo.AdjetivoFlexionRepo;
import com.bcadaval.esloveno.repo.SustantivoFlexionRepo;
import com.bcadaval.esloveno.repo.VerboFlexionRepo;
import com.bcadaval.esloveno.structures.CriterioGramatical;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

/**
 * Servicio unificado de consulta de palabras para estudio.
 * <p>
 * Estrategia:
 * - Una tarjeta está ACTIVA si proximaRevision != null (se inicializa al completarla)
 * - Una tarjeta está LISTA PARA ESTUDIAR si proximaRevision <= ahora
 * - El filtrado gramatical se aplica en memoria con CriterioGramatical
 * <p>
 * IMPORTANTE: Todos los métodos son @Transactional(readOnly = true) para permitir
 * el consumo de streams desde JPA sin cerrar la conexión.
 */
@Log4j2
@Service
@Transactional(readOnly = true)
public class ConsultaPalabrasService {

    @Autowired
    private VerboFlexionRepo verboFlexionRepo;

    @Autowired
    private SustantivoFlexionRepo sustantivoFlexionRepo;

    @Autowired
    private AdjetivoFlexionRepo adjetivoFlexionRepo;

    /**
     * Obtiene un Stream de verbos listos para estudiar.
     * Filtra en BD: proximaRevision != null AND proximaRevision <= ahora
     * Filtra en memoria: criterios gramaticales
     */
    private Stream<VerboFlexion> streamVerbosListos(List<CriterioGramatical> criterios) {
        Instant ahora = Instant.now();
        return verboFlexionRepo.streamListosParaEstudiar(ahora)
                .filter(vf -> vf.getSignificado() != null)/*perogrullo*/
                .filter(vf -> criterios.isEmpty() || criterios.stream().anyMatch(c -> c.cumple(vf)));
    }

    public List<VerboFlexion> listVerbosListos(List<CriterioGramatical> criterios) {
        return streamVerbosListos(criterios).toList();
    }

    /**
     * Obtiene un Stream de sustantivos listos para estudiar.
     */
    private Stream<SustantivoFlexion> streamSustantivosListos(List<CriterioGramatical> criterios) {
        Instant ahora = Instant.now();

        return sustantivoFlexionRepo.streamListosParaEstudiar(ahora)
                .filter(sf -> sf.getSignificado() != null)/*perogrullo*/
                .filter(sf -> criterios.isEmpty() || criterios.stream().anyMatch(c -> c.cumple(sf)));
    }

    public List<SustantivoFlexion> listSustantivosListos(List<CriterioGramatical> criterios) {
        return streamSustantivosListos(criterios).toList();
    }

    /**
     * Obtiene un Stream de adjetivos listos para estudiar.
     */
    private Stream<AdjetivoFlexion> streamAdjetivosListos(List<CriterioGramatical> criterios) {
        Instant ahora = Instant.now();

        return adjetivoFlexionRepo.streamListosParaEstudiar(ahora)
                .filter(af -> af.getSignificado() != null)/*perogrullo*/
                .filter(af -> criterios.isEmpty() || criterios.stream().anyMatch(c -> c.cumple(af)));
    }

    public List<AdjetivoFlexion> listAdjetivosListos(List<CriterioGramatical> criterios) {
        return streamAdjetivosListos(criterios).toList();
    }

    /**
     * Obtiene un Stream de TODOS los verbos activos (para estadísticas).
     * Solo filtra por proximaRevision != null
     */
    private Stream<VerboFlexion> streamVerbosActivos(List<CriterioGramatical> criterios) {
        return verboFlexionRepo.streamActivos()
                .filter(vf -> vf.getSignificado() != null)/*perogrullo*/
                .filter(vf -> criterios.isEmpty() || criterios.stream().anyMatch(c -> c.cumple(vf)));
    }

    public List<VerboFlexion> listVerbosActivos(List<CriterioGramatical> criterios) {
        return streamVerbosActivos(criterios).toList();
    }

    /**
     * Obtiene un Stream de TODOS los sustantivos activos (para estadísticas).
     */
    private Stream<SustantivoFlexion> streamSustantivosActivos(List<CriterioGramatical> criterios) {
        return sustantivoFlexionRepo.streamActivos()
                .filter(sf -> sf.getSignificado() != null)/*perogrullo*/
                .filter(sf -> criterios.isEmpty() || criterios.stream().anyMatch(c -> c.cumple(sf)));
    }

    public List<SustantivoFlexion> listSustantivosActivos(List<CriterioGramatical> criterios) {
        return streamSustantivosActivos(criterios).toList();
    }

    /**
     * Obtiene un Stream de TODOS los adjetivos activos (para estadísticas).
     */
    private Stream<AdjetivoFlexion> streamAdjetivosActivos(List<CriterioGramatical> criterios) {
        return adjetivoFlexionRepo.streamActivos()
                .filter(af -> af.getSignificado() != null)/*perogrullo*/
                .filter(af -> criterios.isEmpty() || criterios.stream().anyMatch(c -> c.cumple(af)));
    }

    public List<AdjetivoFlexion> listAdjetivosActivos(List<CriterioGramatical> criterios) {
        return streamAdjetivosActivos(criterios).toList();
    }
}
