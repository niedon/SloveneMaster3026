package com.bcadaval.esloveno.services;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.bcadaval.esloveno.beans.palabra.NumeralFlexion;
import com.bcadaval.esloveno.beans.palabra.PronombreFlexion;
import com.bcadaval.esloveno.structures.frase.criterio.CriterioBusquedaNuevo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bcadaval.esloveno.beans.base.PalabraFlexion;
import com.bcadaval.esloveno.beans.palabra.AdjetivoFlexion;
import com.bcadaval.esloveno.beans.palabra.SustantivoFlexion;
import com.bcadaval.esloveno.beans.palabra.VerboFlexion;
import com.bcadaval.esloveno.repo.AdjetivoFlexionRepo;
import com.bcadaval.esloveno.repo.NumeralFlexionRepo;
import com.bcadaval.esloveno.repo.PronombreFlexionRepo;
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
    private FraseService fraseService;


    @Lazy
    @Autowired
    private ConsultaPalabrasNuevoService consultaPalabrasNuevoService;

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
     * Calcula el próximo intervalo en segundos que se aplicaría a una flexión si se responde.
     * Esta función NO modifica la flexión, solo calcula de forma predictiva.
     *
     * @param flexion La flexión a evaluar
     * @param recordo Si la respuesta sería correcta (true) o incorrecta (false)
     * @return El intervalo en segundos hasta la próxima revisión
     */
    public long calcularProximoIntervalo(PalabraFlexion<?> flexion, boolean recordo) {
        double factorFacilidad = Optional.ofNullable(flexion.getFactorFacilidad())
            .orElse(variablesService.getFactorFacilidadInicial());
        long intervaloSegundos = Optional.ofNullable(flexion.getIntervaloRepeticionSegundos()).orElse(0L);
        int vecesCorrectas = Optional.ofNullable(flexion.getVecesConsecutivasCorrectas()).orElse(0);

        if (recordo) {
            vecesCorrectas++;
            return switch (vecesCorrectas) {
                case 1 -> variablesService.getIntervaloInicialSegundos();
                case 2 -> variablesService.getIntervaloSegundaSegundos();
                default -> (long) (intervaloSegundos * factorFacilidad);
            };
        } else {
            return variablesService.getIntervaloReaprendizajeSegundos();
        }
    }

    /**
     * Guarda una flexión en su repositorio correspondiente
     */
    private void guardarFlexion(PalabraFlexion<?> flexion) {
        switch (flexion) {
            case VerboFlexion vf -> verboFlexionRepo.save(vf);
            case SustantivoFlexion sf -> sustantivoFlexionRepo.save(sf);
            case AdjetivoFlexion af -> adjetivoFlexionRepo.save(af);
            case NumeralFlexion nf -> numeralFlexionRepo.save(nf);
            case PronombreFlexion pf -> pronombreFlexionRepo.save(pf);
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
     * Obtiene estadísticas del sistema de estudio.
     * Usa el nuevo sistema de criterios para obtener todas las tarjetas activas.
     */
    public EstadisticasDTO obtenerEstadisticas() {
        List<CriterioBusquedaNuevo<VerboFlexion>> criteriosVerbo = fraseService.getCriteriosPorTipo(VerboFlexion.class);
        List<CriterioBusquedaNuevo<SustantivoFlexion>> criteriosSustantivo = fraseService.getCriteriosPorTipo(SustantivoFlexion.class);
        List<CriterioBusquedaNuevo<AdjetivoFlexion>> criteriosAdjetivo = fraseService.getCriteriosPorTipo(AdjetivoFlexion.class);
        List<CriterioBusquedaNuevo<NumeralFlexion>> criteriosNumeral = fraseService.getCriteriosPorTipo(NumeralFlexion.class);
        List<CriterioBusquedaNuevo<PronombreFlexion>> criteriosPronombre = fraseService.getCriteriosPorTipo(PronombreFlexion.class);

        List<PalabraFlexion<?>> todasActivas = Stream.of(
                consultaPalabrasNuevoService.listVerbosActivos(criteriosVerbo),
                consultaPalabrasNuevoService.listSustantivosActivos(criteriosSustantivo),
                consultaPalabrasNuevoService.listAdjetivosActivos(criteriosAdjetivo),
                consultaPalabrasNuevoService.listNumeralesActivos(criteriosNumeral),
                consultaPalabrasNuevoService.listPronombresActivos(criteriosPronombre)
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

            // Disponible ahora = proximaRevision != null AND proximaRevision <= ahora
            Instant proxima = f.getProximaRevision();
            if (proxima != null && !proxima.isAfter(ahora)) {
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

    /**
     * Obtiene las tarjetas listas para estudiar usando el nuevo sistema de criterios.
     * <p>
     * Proceso:
     * <ol>
     *   <li>Obtiene criterios expandidos del {@link FraseService} por tipo de flexión</li>
     *   <li>Consulta BD filtrando por SRS (proximaRevision &lt;= ahora) y criterios gramaticales</li>
     *   <li>Ordena: reaprendizaje primero, luego por proximaRevision ASC, nuevas al final</li>
     *   <li>Aplica Algoritmo de Desplazamiento Limitado (ventana=5) para variabilidad controlada</li>
     * </ol>
     *
     * @param limite número máximo de tarjetas a devolver
     * @return lista de PalabraFlexion ordenadas y desplazadas, listas para asignar a frases
     */
    public List<PalabraFlexion<?>> obtenerTarjetasDisponiblesNuevo(int limite) {
        List<CriterioBusquedaNuevo<VerboFlexion>> criteriosVerbo = fraseService.getCriteriosPorTipo(VerboFlexion.class);
        List<CriterioBusquedaNuevo<SustantivoFlexion>> criteriosSustantivo = fraseService.getCriteriosPorTipo(SustantivoFlexion.class);
        List<CriterioBusquedaNuevo<AdjetivoFlexion>> criteriosAdjetivo = fraseService.getCriteriosPorTipo(AdjetivoFlexion.class);
        List<CriterioBusquedaNuevo<NumeralFlexion>> criteriosNumeral = fraseService.getCriteriosPorTipo(NumeralFlexion.class);
        List<CriterioBusquedaNuevo<PronombreFlexion>> criteriosPronombre = fraseService.getCriteriosPorTipo(PronombreFlexion.class);

        List<PalabraFlexion<?>> tarjetas = Stream.of(
                        consultaPalabrasNuevoService.listVerbosListos(criteriosVerbo),
                        consultaPalabrasNuevoService.listSustantivosListos(criteriosSustantivo),
                        consultaPalabrasNuevoService.listAdjetivosListos(criteriosAdjetivo),
                        consultaPalabrasNuevoService.listNumeralesListos(criteriosNumeral),
                        consultaPalabrasNuevoService.listPronombresListos(criteriosPronombre)
                )
                .flatMap(List::stream)
                // Ordenar: reaprendizaje primero, luego por proximaRevision ASC, nuevas al final
                .sorted(Comparator
                        .comparing((PalabraFlexion<?> f) -> !Boolean.TRUE.equals(f.getEnReaprendizaje()))
                        .thenComparing((PalabraFlexion<?> f) -> f.getUltimaRevision() == null ? 1 : 0)
                        .thenComparing((PalabraFlexion<?> f) ->
                                f.getProximaRevision() != null ? f.getProximaRevision() : Instant.MAX))
                .collect(Collectors.toList());

        // Aplicar Algoritmo de Desplazamiento Limitado (ventana = 5)
        tarjetas = aplicarDesplazamientoLimitado(tarjetas, 5);

        return tarjetas.size() > limite ? tarjetas.subList(0, limite) : tarjetas;
    }

    /**
     * Algoritmo de Desplazamiento Limitado.
     * <p>
     * Para cada elemento en posición i de la lista ordenada, lo reasigna aleatoriamente
     * a cualquier posición dentro del rango [max(0, i-ventana), min(N-1, i+ventana)].
     * Introduce variabilidad controlada sin destruir el orden de prioridad SRS.
     *
     * @param lista   lista ordenada de tarjetas
     * @param ventana tamaño de la ventana de desplazamiento
     * @return nueva lista con desplazamiento aplicado
     */
    private List<PalabraFlexion<?>> aplicarDesplazamientoLimitado(List<PalabraFlexion<?>> lista, int ventana) {
        if (lista.size() <= 1) return lista;

        Random random = new Random();
        int n = lista.size();
        // Asignar posiciones desplazadas
        double[] posiciones = new double[n];
        for (int i = 0; i < n; i++) {
            int limiteInferior = Math.max(0, i - ventana);
            int limiteSuperior = Math.min(n - 1, i + ventana);
            // Posición aleatoria dentro del rango, usando double para desempate
            posiciones[i] = limiteInferior + random.nextDouble() * (limiteSuperior - limiteInferior);
        }

        // Crear pares (posición desplazada, elemento) y ordenar por posición
        Integer[] indices = new Integer[n];
        for (int i = 0; i < n; i++) indices[i] = i;
        Arrays.sort(indices, Comparator.comparingDouble(i -> posiciones[i]));

        List<PalabraFlexion<?>> resultado = new ArrayList<>(n);
        for (int idx : indices) {
            resultado.add(lista.get(idx));
        }
        return resultado;
    }

}
