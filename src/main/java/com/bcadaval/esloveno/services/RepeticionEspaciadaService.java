package com.bcadaval.esloveno.services;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.bcadaval.esloveno.structures.CriterioGramatical;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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

    @Lazy
    @Autowired
    private ConsultaPalabrasService consultaPalabrasService;

    @Autowired
    private VerboFlexionRepo verboFlexionRepo;

    @Autowired
    private SustantivoFlexionRepo sustantivoFlexionRepo;

    @Autowired
    private AdjetivoFlexionRepo adjetivoFlexionRepo;

    /**
     * Procesa la respuesta del usuario y actualiza el estado de la tarjeta.
     * Implementa el algoritmo SM-2 con precisión de segundos.
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

        flexion.setFactorFacilidad(factorFacilidad);
        flexion.setIntervaloRepeticionSegundos(nuevoIntervalo);
        flexion.setVecesConsecutivasCorrectas(vecesCorrectas);
        flexion.setUltimaRevision(ahora);
        flexion.setProximaRevision(ahora.plusSeconds(nuevoIntervalo));
        flexion.setTotalRevisiones(totalRevisiones);
        flexion.setTotalAciertos(totalAciertos);
    }

    /**
     * Obtiene las tarjetas listas para estudiar.
     * Una tarjeta está lista si: proximaRevision != null AND proximaRevision <= ahora
     * La consulta a BD ya filtra esto, solo se aplica filtro gramatical en memoria.
     */
    public List<PalabraFlexion<?>> obtenerTarjetasDisponibles(int limite) {
        // Obtener criterios activos por tipo
        List<CriterioGramatical> criteriosVerbo = estructuraFraseService.getCriteriosGramaticalesPorTipo(VerboFlexion.class);
        List<CriterioGramatical> criteriosSustantivo = estructuraFraseService.getCriteriosGramaticalesPorTipo(SustantivoFlexion.class);
        List<CriterioGramatical> criteriosAdjetivo = estructuraFraseService.getCriteriosGramaticalesPorTipo(AdjetivoFlexion.class);

        // Combinar streams con filtro gramatical ya aplicado
        // Importante: recolectar cada stream a lista antes de combinar
        // porque los streams de JPA se cierran al finalizar su procesamiento
        List<PalabraFlexion<?>> tarjetas = Stream.of(
            consultaPalabrasService.listVerbosListos(criteriosVerbo),
            consultaPalabrasService.listSustantivosListos(criteriosSustantivo),
            consultaPalabrasService.listAdjetivosListos(criteriosAdjetivo)
        )
        .flatMap(List::stream)
        // Ordenar: reaprendizaje primero, luego por antigüedad
        .sorted(Comparator
            .comparing((PalabraFlexion<?> f) -> !Boolean.TRUE.equals(f.getEnReaprendizaje()))
            .thenComparing(PalabraFlexion::getProximaRevision))
        .collect(Collectors.toList());

        // Mezclar aleatoriamente y limitar
        Collections.shuffle(tarjetas);
        return tarjetas.size() > limite ? tarjetas.subList(0, limite) : tarjetas;
    }

    /**
     * Obtiene estadísticas del sistema de estudio.
     * Usa streamActivos para obtener TODAS las tarjetas activas (proximaRevision != null)
     */
    public EstadisticasDTO obtenerEstadisticas() {
        List<CriterioGramatical> criteriosVerbo = estructuraFraseService.getCriteriosGramaticalesPorTipo(VerboFlexion.class);
        List<CriterioGramatical> criteriosSustantivo = estructuraFraseService.getCriteriosGramaticalesPorTipo(SustantivoFlexion.class);
        List<CriterioGramatical> criteriosAdjetivo = estructuraFraseService.getCriteriosGramaticalesPorTipo(AdjetivoFlexion.class);

        // Obtener todas las tarjetas activas que cumplen criterios
        // Importante: recolectar cada stream a lista antes de combinar
        List<PalabraFlexion<?>> todasActivas = Stream.of(
                consultaPalabrasService.listVerbosActivos(criteriosVerbo),
                consultaPalabrasService.listSustantivosActivos(criteriosSustantivo),
                consultaPalabrasService.listAdjetivosActivos(criteriosAdjetivo)
        )
        .flatMap(List::stream)
        .collect(Collectors.toList());

        Instant ahora = Instant.now();

        long totalTarjetas = todasActivas.size();
        long tarjetasDisponiblesAhora = 0;
        long tarjetasEnReaprendizaje = 0;
        long totalRevisiones = 0;
        long totalAciertos = 0;

        for (PalabraFlexion<?> f : todasActivas) {
            int revisiones = Optional.ofNullable(f.getTotalRevisiones()).orElse(0);
            int aciertos = Optional.ofNullable(f.getTotalAciertos()).orElse(0);

            totalRevisiones += revisiones;
            totalAciertos += aciertos;

            if (Boolean.TRUE.equals(f.getEnReaprendizaje())) {
                tarjetasEnReaprendizaje++;
            }

            // Disponible ahora = proximaRevision <= ahora
            if (!f.getProximaRevision().isAfter(ahora)) {
                tarjetasDisponiblesAhora++;
            }
        }

        double tasaAciertos = totalRevisiones > 0 ? (double) totalAciertos / totalRevisiones * 100 : 0;

        return EstadisticasDTO.builder()
            .totalTarjetas((int) totalTarjetas)
            .tarjetasEstudiadas((int) totalTarjetas) // Todas activas han sido estudiadas al menos una vez
            .tarjetasNuevas(0) // Ya no hay concepto de "nuevas" - todas las activas fueron inicializadas
            .tarjetasDisponiblesAhora((int) tarjetasDisponiblesAhora)
            .tarjetasEnReaprendizaje((int) tarjetasEnReaprendizaje)
            .totalRevisiones((int) totalRevisiones)
            .totalAciertos((int) totalAciertos)
            .tasaAciertos(tasaAciertos)
            .build();
    }

}
