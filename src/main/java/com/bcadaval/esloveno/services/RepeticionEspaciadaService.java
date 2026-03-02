package com.bcadaval.esloveno.services;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.bcadaval.esloveno.beans.palabra.NumeralFlexion;
import com.bcadaval.esloveno.beans.palabra.ParticulaFlexion;
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
import com.bcadaval.esloveno.repo.ParticulaFlexionRepo;
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

    @Autowired
    private ParticulaFlexionRepo particulaFlexionRepo;

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
            case ParticulaFlexion paf -> particulaFlexionRepo.save(paf);
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
        List<CriterioBusquedaNuevo<ParticulaFlexion>> criteriosParticula = fraseService.getCriteriosPorTipo(ParticulaFlexion.class);

        List<PalabraFlexion<?>> todasActivas = Stream.of(
                consultaPalabrasNuevoService.listVerbosActivos(criteriosVerbo),
                consultaPalabrasNuevoService.listSustantivosActivos(criteriosSustantivo),
                consultaPalabrasNuevoService.listAdjetivosActivos(criteriosAdjetivo),
                consultaPalabrasNuevoService.listNumeralesActivos(criteriosNumeral),
                consultaPalabrasNuevoService.listPronombresActivos(criteriosPronombre),
                consultaPalabrasNuevoService.listParticulasActivas(criteriosParticula)
        )
        .flatMap(List::stream)
        .collect(Collectors.toList());

        Instant ahora = Instant.now();

        long totalTarjetas = todasActivas.size();
        long tarjetasNuevas = 0;
        long tarjetasEstudiadas = 0;
        long tarjetasDisponiblesAhora = 0;
        long tarjetasEnReaprendizaje = 0;
        long totalRevisiones = 0;
        long totalAciertos = 0;

        for (PalabraFlexion<?> f : todasActivas) {
            int revisiones = Optional.ofNullable(f.getTotalRevisiones()).orElse(0);
            int aciertos = Optional.ofNullable(f.getTotalAciertos()).orElse(0);

            totalRevisiones += revisiones;
            totalAciertos += aciertos;

            if (f.getProximaRevision() == null) {
                // Tarjeta nueva: palabra completa pero nunca introducida al SRS
                tarjetasNuevas++;
            } else {
                if (f.getUltimaRevision() != null) {
                    tarjetasEstudiadas++;
                } else {
                    // Inicializada pero nunca estudiada → también nueva funcionalmente
                    tarjetasNuevas++;
                }

                if (Boolean.TRUE.equals(f.getEnReaprendizaje())) {
                    tarjetasEnReaprendizaje++;
                }

                // Disponible ahora = proximaRevision != null AND proximaRevision <= ahora
                Instant proxima = f.getProximaRevision();
                if (!proxima.isAfter(ahora)) {
                    tarjetasDisponiblesAhora++;
                }
            }
        }

        // Las tarjetas nuevas también son disponibles ahora (se pueden introducir al estudio)
        tarjetasDisponiblesAhora += tarjetasNuevas;

        double tasaAciertos = totalRevisiones > 0 ? (double) totalAciertos / totalRevisiones * 100 : 0;

        return EstadisticasDTO.builder()
            .totalTarjetas((int) totalTarjetas)
            .tarjetasEstudiadas((int) tarjetasEstudiadas)
            .tarjetasNuevas((int) tarjetasNuevas)
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
     *   <li>Consulta BD filtrando por criterios gramaticales, incluyendo tarjetas de revisión
     *       (proximaRevision &lt;= ahora) y tarjetas nuevas (proximaRevision IS NULL, palabra completa)</li>
     *   <li>Separa tarjetas de revisión y nuevas, aplicando límites independientes</li>
     *   <li>Ordena: reaprendizaje primero, luego por proximaRevision ASC, nuevas al final</li>
     *   <li>Aplica Algoritmo de Desplazamiento Limitado (ventana=5) para variabilidad controlada</li>
     * </ol>
     *
     * @param limiteRevision número máximo de tarjetas de revisión
     * @return lista de PalabraFlexion ordenadas y desplazadas, listas para asignar a frases
     */
    public List<PalabraFlexion<?>> obtenerTarjetasDisponiblesNuevo(int limiteRevision) {
        int limiteNuevas = variablesService.getMaxTarjetasNuevasDia();

        List<CriterioBusquedaNuevo<VerboFlexion>> criteriosVerbo = fraseService.getCriteriosPorTipo(VerboFlexion.class);
        List<CriterioBusquedaNuevo<SustantivoFlexion>> criteriosSustantivo = fraseService.getCriteriosPorTipo(SustantivoFlexion.class);
        List<CriterioBusquedaNuevo<AdjetivoFlexion>> criteriosAdjetivo = fraseService.getCriteriosPorTipo(AdjetivoFlexion.class);
        List<CriterioBusquedaNuevo<NumeralFlexion>> criteriosNumeral = fraseService.getCriteriosPorTipo(NumeralFlexion.class);
        List<CriterioBusquedaNuevo<PronombreFlexion>> criteriosPronombre = fraseService.getCriteriosPorTipo(PronombreFlexion.class);
        List<CriterioBusquedaNuevo<ParticulaFlexion>> criteriosParticula = fraseService.getCriteriosPorTipo(ParticulaFlexion.class);

        List<PalabraFlexion<?>> todasLasTarjetas = Stream.of(
                        consultaPalabrasNuevoService.listVerbosListos(criteriosVerbo),
                        consultaPalabrasNuevoService.listSustantivosListos(criteriosSustantivo),
                        consultaPalabrasNuevoService.listAdjetivosListos(criteriosAdjetivo),
                        consultaPalabrasNuevoService.listNumeralesListos(criteriosNumeral),
                        consultaPalabrasNuevoService.listPronombresListos(criteriosPronombre),
                        consultaPalabrasNuevoService.listParticulasListas(criteriosParticula)
                )
                .flatMap(List::stream)
                .collect(Collectors.toList());

        // Separar tarjetas de revisión (proximaRevision != null) y nuevas (proximaRevision == null)
        List<PalabraFlexion<?>> tarjetasRevision = todasLasTarjetas.stream()
                .filter(f -> f.getProximaRevision() != null)
                .sorted(Comparator
                        .comparing((PalabraFlexion<?> f) -> !Boolean.TRUE.equals(f.getEnReaprendizaje()))
                        .thenComparing((PalabraFlexion<?> f) -> f.getUltimaRevision() == null ? 1 : 0)
                        .thenComparing((PalabraFlexion<?> f) -> f.getProximaRevision()))
                .collect(Collectors.toList());

        List<PalabraFlexion<?>> tarjetasNuevas = todasLasTarjetas.stream()
                .filter(f -> f.getProximaRevision() == null)
                .collect(Collectors.toList());

        // Aplicar límites por separado
        if (tarjetasRevision.size() > limiteRevision) {
            tarjetasRevision = tarjetasRevision.subList(0, limiteRevision);
        }
        if (tarjetasNuevas.size() > limiteNuevas) {
            // Mezclar antes de cortar para dar variabilidad a cuáles entran
            Collections.shuffle(tarjetasNuevas);
            tarjetasNuevas = tarjetasNuevas.subList(0, limiteNuevas);
        }

        // Combinar: revisiones primero, nuevas al final
        List<PalabraFlexion<?>> tarjetas = new ArrayList<>(tarjetasRevision);
        tarjetas.addAll(tarjetasNuevas);

        log.info("Tarjetas obtenidas: {} revisión + {} nuevas = {} total",
                tarjetasRevision.size(), tarjetasNuevas.size(), tarjetas.size());

        // Aplicar Algoritmo de Desplazamiento Limitado (ventana = 5)
        tarjetas = aplicarDesplazamientoLimitado(tarjetas, 5);

        return tarjetas;
    }

    /**
     * Inicializa los campos SRS de una tarjeta nueva (proximaRevision == null).
     * Se invoca cuando una tarjeta nueva es asignada a una frase por primera vez.
     * Establece proximaRevision = ahora para que entre al ciclo de estudio.
     *
     * @param flexion la flexión a inicializar
     */
    @Transactional
    public void inicializarTarjetaNueva(PalabraFlexion<?> flexion) {
        if (flexion.getProximaRevision() != null) return;

        Double factorInicial = variablesService.getFactorFacilidadInicial();
        Instant ahora = Instant.now();

        flexion.setFactorFacilidad(factorInicial);
        flexion.setIntervaloRepeticionSegundos(0L);
        flexion.setVecesConsecutivasCorrectas(0);
        flexion.setTotalRevisiones(0);
        flexion.setTotalAciertos(0);
        flexion.setEnReaprendizaje(false);
        flexion.setProximaRevision(ahora);

        guardarFlexion(flexion);
        log.debug("Tarjeta nueva inicializada: {} ({})", flexion.getFlexion(), flexion.getClass().getSimpleName());
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
