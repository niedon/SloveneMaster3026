package com.bcadaval.esloveno.services;

import java.time.Instant;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bcadaval.esloveno.beans.base.PalabraFlexion;
import com.bcadaval.esloveno.beans.palabra.AdjetivoFlexion;
import com.bcadaval.esloveno.beans.palabra.SustantivoFlexion;
import com.bcadaval.esloveno.beans.palabra.VerboFlexion;
import com.bcadaval.esloveno.repo.AdjetivoFlexionRepo;
import com.bcadaval.esloveno.repo.SustantivoFlexionRepo;
import com.bcadaval.esloveno.repo.VerboFlexionRepo;
import com.bcadaval.esloveno.rest.dto.EstadisticasDTO;
import com.bcadaval.esloveno.structures.specifications.AdjetivoFlexionSpecs;
import com.bcadaval.esloveno.structures.specifications.SrsSpecs;
import com.bcadaval.esloveno.structures.specifications.SustantivoFlexionSpecs;
import com.bcadaval.esloveno.structures.specifications.VerboFlexionSpecs;

import lombok.extern.log4j.Log4j2;

/**
 * Servicio que implementa el algoritmo de repetición espaciada SM-2.
 * Gestiona la programación de tarjetas y el cálculo de intervalos.
 */
@Log4j2
@Service
public class RepeticionEspaciadaService {

    @Autowired
    private VariablesService variablesService;

    @Lazy
    @Autowired
    private EstructuraFraseService estructuraFraseService;

    @Autowired
    private VerboFlexionRepo verboFlexionRepo;

    @Autowired
    private SustantivoFlexionRepo sustantivoFlexionRepo;

    @Autowired
    private AdjetivoFlexionRepo adjetivoFlexionRepo;

    /**
     * Procesa la respuesta del usuario y actualiza el estado de la tarjeta.
     * Implementa el algoritmo SM-2 con precisión de segundos.
     *
     * @param flexion La flexión (VerboFlexion, SustantivoFlexion, AdjetivoFlexion)
     * @param recordo true si el usuario recordó la palabra, false si no
     */
    @Transactional
    public void procesarRespuesta(PalabraFlexion<?> flexion, boolean recordo) {
        actualizarCamposSRS(flexion, recordo);
        guardarFlexion(flexion);
        log.debug("{} actualizado: {} - Recordó: {}",
            flexion.getClass().getSimpleName(), flexion.getFlexion(), recordo);
    }

    /**
     * Guarda una flexión en su repositorio correspondiente
     */
    private void guardarFlexion(PalabraFlexion<?> flexion) {
        switch (flexion) {
            case VerboFlexion vf -> verboFlexionRepo.save(vf);
            case SustantivoFlexion sf -> sustantivoFlexionRepo.save(sf);
            case AdjetivoFlexion af -> adjetivoFlexionRepo.save(af);
            default -> log.warn("Tipo de flexión no soportado para guardar: {}", flexion.getClass());
        }
    }

    /**
     * Actualiza los campos SRS de una flexión según el algoritmo SM-2
     */
    private void actualizarCamposSRS(PalabraFlexion<?> flexion, boolean recordo) {
        // Obtener valores actuales con defaults
        double factorFacilidad = Optional.ofNullable(flexion.getFactorFacilidad())
            .orElse(variablesService.getFactorFacilidadInicial());
        long intervaloSegundos = Optional.ofNullable(flexion.getIntervaloRepeticionSegundos()).orElse(0L);
        int vecesCorrectas = Optional.ofNullable(flexion.getVecesConsecutivasCorrectas()).orElse(0);
        int totalRevisiones = Optional.ofNullable(flexion.getTotalRevisiones()).orElse(0) + 1;
        int totalAciertos = Optional.ofNullable(flexion.getTotalAciertos()).orElse(0);

        Instant ahora = Instant.now();
        long nuevoIntervalo;

        if (recordo) {
            totalAciertos++;
            vecesCorrectas++;

            nuevoIntervalo = switch (vecesCorrectas) {
                case 1 -> variablesService.getIntervaloInicialSegundos();
                case 2 -> variablesService.getIntervaloSegundaSegundos();
                default -> (long) (intervaloSegundos * factorFacilidad);
            };

            flexion.setEnReaprendizaje(false);
        } else {
            vecesCorrectas = 0;
            factorFacilidad = Math.max(
                variablesService.getFactorFacilidadMinimo(),
                factorFacilidad - variablesService.getPenalizacionFallo()
            );
            nuevoIntervalo = variablesService.getIntervaloReaprendizajeSegundos();
            flexion.setEnReaprendizaje(true);
        }

        // Actualizar todos los campos
        flexion.setFactorFacilidad(factorFacilidad);
        flexion.setIntervaloRepeticionSegundos(nuevoIntervalo);
        flexion.setVecesConsecutivasCorrectas(vecesCorrectas);
        flexion.setUltimaRevision(ahora);
        flexion.setProximaRevision(ahora.plusSeconds(nuevoIntervalo));
        flexion.setTotalRevisiones(totalRevisiones);
        flexion.setTotalAciertos(totalAciertos);
    }

    /**
     * Obtiene las tarjetas disponibles para estudiar ahora.
     * Filtra por: tiempo vencido, palabra completa, criterios de estructuras activas.
     * Ordena por: reaprendizaje primero, luego antigüedad, luego aleatorio.
     *
     * @param limite Máximo de tarjetas a devolver
     * @return Lista de flexiones disponibles
     */
    public List<PalabraFlexion<?>> obtenerTarjetasDisponibles(int limite) {
        List<PalabraFlexion<?>> tarjetas = new ArrayList<>();

        // Obtener verbos disponibles
        Specification<VerboFlexion> specVerbo = estructuraFraseService.getSpecificationCombinadaPorTipo(VerboFlexion.class);
        if (specVerbo != null) {
            Specification<VerboFlexion> specVerboCompleta = specVerbo
                    .and(VerboFlexionSpecs.completaParaEstudio())
                    .and(SrsSpecs.<VerboFlexion>listaParaRevisar());
            tarjetas.addAll(verboFlexionRepo.findAll(specVerboCompleta));
        }

        // Obtener sustantivos disponibles
        Specification<SustantivoFlexion> specSustantivo = estructuraFraseService.getSpecificationCombinadaPorTipo(SustantivoFlexion.class);
        if (specSustantivo != null) {
            Specification<SustantivoFlexion> specSustantivoCompleta = specSustantivo
                    .and(SustantivoFlexionSpecs.completaParaEstudio())
                    .and(SrsSpecs.<SustantivoFlexion>listaParaRevisar());
            tarjetas.addAll(sustantivoFlexionRepo.findAll(specSustantivoCompleta));
        }

        // Obtener adjetivos disponibles
        Specification<AdjetivoFlexion> specAdjetivo = estructuraFraseService.getSpecificationCombinadaPorTipo(AdjetivoFlexion.class);
        if (specAdjetivo != null) {
            Specification<AdjetivoFlexion> specAdjetivoCompleta = specAdjetivo
                    .and(AdjetivoFlexionSpecs.completaParaEstudio())
                    .and(SrsSpecs.<AdjetivoFlexion>listaParaRevisar());
            tarjetas.addAll(adjetivoFlexionRepo.findAll(specAdjetivoCompleta));
        }

        // Ordenar: reaprendizaje primero, luego por antigüedad
        tarjetas.sort(Comparator
            .comparing((PalabraFlexion<?> f) -> !Boolean.TRUE.equals(f.getEnReaprendizaje()))
            .thenComparing(PalabraFlexion::getProximaRevision));

        // Mezclar aleatoriamente
        Collections.shuffle(tarjetas);

        return tarjetas.size() > limite ? tarjetas.subList(0, limite) : tarjetas;
    }

    /**
     * Obtiene tarjetas nuevas (nunca estudiadas) para iniciar estudio.
     * Solo incluye palabras completas y que cumplan criterios de estructuras activas.
     *
     * @param limite Máximo de tarjetas a devolver
     * @return Lista de flexiones nuevas
     */
    public List<PalabraFlexion<?>> obtenerTarjetasNuevas(int limite) {
        List<PalabraFlexion<?>> tarjetas = new ArrayList<>();

        // Obtener verbos nuevos
        Specification<VerboFlexion> specVerbo = estructuraFraseService.getSpecificationCombinadaPorTipo(VerboFlexion.class);
        if (specVerbo != null) {
            Specification<VerboFlexion> specVerboNueva = specVerbo
                    .and(VerboFlexionSpecs.completaParaEstudio())
                    .and(SrsSpecs.<VerboFlexion>nueva());
            tarjetas.addAll(verboFlexionRepo.findAll(specVerboNueva));
        }

        // Obtener sustantivos nuevos
        Specification<SustantivoFlexion> specSustantivo = estructuraFraseService.getSpecificationCombinadaPorTipo(SustantivoFlexion.class);
        if (specSustantivo != null) {
            Specification<SustantivoFlexion> specSustantivoNueva = specSustantivo
                    .and(SustantivoFlexionSpecs.completaParaEstudio())
                    .and(SrsSpecs.<SustantivoFlexion>nueva());
            tarjetas.addAll(sustantivoFlexionRepo.findAll(specSustantivoNueva));
        }

        // Obtener adjetivos nuevos
        Specification<AdjetivoFlexion> specAdjetivo = estructuraFraseService.getSpecificationCombinadaPorTipo(AdjetivoFlexion.class);
        if (specAdjetivo != null) {
            Specification<AdjetivoFlexion> specAdjetivoNueva = specAdjetivo
                    .and(AdjetivoFlexionSpecs.completaParaEstudio())
                    .and(SrsSpecs.<AdjetivoFlexion>nueva());
            tarjetas.addAll(adjetivoFlexionRepo.findAll(specAdjetivoNueva));
        }

        Collections.shuffle(tarjetas);

        return tarjetas.size() > limite ? tarjetas.subList(0, limite) : tarjetas;
    }

    /**
     * Cuenta las tarjetas disponibles ahora para estudiar
     */
    public int contarTarjetasDisponibles() {
        return obtenerTarjetasDisponibles(Integer.MAX_VALUE).size();
    }

    /**
     * Cuenta las tarjetas nuevas disponibles
     */
    public int contarTarjetasNuevas() {
        return obtenerTarjetasNuevas(Integer.MAX_VALUE).size();
    }

    /**
     * Obtiene estadísticas del sistema de estudio.
     * <p>
     * NOTA: Una tarjeta es "estudiada" si tiene totalRevisiones > 0.
     *       Una tarjeta es "nueva" si proximaRevision == null O totalRevisiones == 0.
     */
    public EstadisticasDTO obtenerEstadisticas() {
        // Recopilar todas las tarjetas elegibles según estructuras activas
        List<PalabraFlexion<?>> todasElegibles = new ArrayList<>();

        // Verbos elegibles
        Specification<VerboFlexion> specVerbo = estructuraFraseService.getSpecificationCombinadaPorTipo(VerboFlexion.class);
        if (specVerbo != null) {
            Specification<VerboFlexion> specVerboCompleta = specVerbo
                    .and(VerboFlexionSpecs.completaParaEstudio());
            todasElegibles.addAll(verboFlexionRepo.findAll(specVerboCompleta));
        }

        // Sustantivos elegibles
        Specification<SustantivoFlexion> specSustantivo = estructuraFraseService.getSpecificationCombinadaPorTipo(SustantivoFlexion.class);
        if (specSustantivo != null) {
            Specification<SustantivoFlexion> specSustantivoCompleta = specSustantivo
                    .and(SustantivoFlexionSpecs.completaParaEstudio());
            todasElegibles.addAll(sustantivoFlexionRepo.findAll(specSustantivoCompleta));
        }

        // Adjetivos elegibles
        Specification<AdjetivoFlexion> specAdjetivo = estructuraFraseService.getSpecificationCombinadaPorTipo(AdjetivoFlexion.class);
        if (specAdjetivo != null) {
            Specification<AdjetivoFlexion> specAdjetivoCompleta = specAdjetivo
                    .and(AdjetivoFlexionSpecs.completaParaEstudio());
            todasElegibles.addAll(adjetivoFlexionRepo.findAll(specAdjetivoCompleta));
        }

        // Contadores
        long totalTarjetas = todasElegibles.size();
        long tarjetasEstudiadas = 0;
        long tarjetasNuevas = 0;
        long tarjetasEnReaprendizaje = 0;
        long totalRevisiones = 0;
        long totalAciertos = 0;

        for (PalabraFlexion<?> f : todasElegibles) {
            int revisiones = Optional.ofNullable(f.getTotalRevisiones()).orElse(0);
            int aciertos = Optional.ofNullable(f.getTotalAciertos()).orElse(0);

            totalRevisiones += revisiones;
            totalAciertos += aciertos;

            // Una tarjeta es "estudiada" si tiene al menos una revisión
            if (revisiones > 0) {
                tarjetasEstudiadas++;
                if (Boolean.TRUE.equals(f.getEnReaprendizaje())) {
                    tarjetasEnReaprendizaje++;
                }
            } else {
                // Nunca ha sido revisada = tarjeta nueva
                tarjetasNuevas++;
            }
        }

        double tasaAciertos = totalRevisiones > 0 ? (double) totalAciertos / totalRevisiones * 100 : 0;

        return EstadisticasDTO.builder()
            .totalTarjetas((int) totalTarjetas)
            .tarjetasEstudiadas((int) tarjetasEstudiadas)
            .tarjetasNuevas((int) tarjetasNuevas)
            .tarjetasDisponiblesAhora(contarTarjetasDisponibles())
            .tarjetasEnReaprendizaje((int) tarjetasEnReaprendizaje)
            .totalRevisiones((int) totalRevisiones)
            .totalAciertos((int) totalAciertos)
            .tasaAciertos(tasaAciertos)
            .build();
    }

    /**
     * Inicializa los campos SRS para una tarjeta nueva.
     * Se llama al completar una palabra en CompletarPalabrasController.
     */
    public void inicializarTarjeta(PalabraFlexion<?> flexion) {
        flexion.setFactorFacilidad(variablesService.getFactorFacilidadInicial());
        flexion.setIntervaloRepeticionSegundos(0L);
        flexion.setVecesConsecutivasCorrectas(0);
        flexion.setTotalRevisiones(0);
        flexion.setTotalAciertos(0);
        flexion.setEnReaprendizaje(false);
        flexion.setProximaRevision(Instant.now()); // Disponible inmediatamente

        guardarFlexion(flexion);
    }
}

