package com.bcadaval.esloveno.services;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import com.bcadaval.esloveno.beans.enums.TipoPalabra;
import com.bcadaval.esloveno.beans.palabra.NumeralFlexion;
import com.bcadaval.esloveno.beans.palabra.ParticulaFlexion;
import com.bcadaval.esloveno.beans.palabra.PronombreFlexion;
import com.bcadaval.esloveno.beans.palabra.SustantivoFlexion;
import com.bcadaval.esloveno.beans.HistoricoRespuesta;
import com.bcadaval.esloveno.rest.dto.EstadisticasDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bcadaval.esloveno.beans.base.PalabraFlexion;
import com.bcadaval.esloveno.beans.palabra.AdjetivoFlexion;
import com.bcadaval.esloveno.beans.palabra.VerboFlexion;
import com.bcadaval.esloveno.repo.AdjetivoFlexionRepo;
import com.bcadaval.esloveno.repo.NumeralFlexionRepo;
import com.bcadaval.esloveno.repo.ParticulaFlexionRepo;
import com.bcadaval.esloveno.repo.PronombreFlexionRepo;
import com.bcadaval.esloveno.repo.SustantivoFlexionRepo;
import com.bcadaval.esloveno.repo.VerboFlexionRepo;
import com.bcadaval.esloveno.repo.HistoricoRespuestaRepo;

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


    @Autowired
    private HistoricoRespuestaRepo historicoRespuestaRepo;

    /**
     * Busca una entidad de flexión por su tipo y ID.
     * Centraliza la lógica de selección de repositorio.
     *
     * @param tipo El TipoPalabra de la entidad
     * @param id El identificador numérico de la flexión
     * @return Optional con la flexión encontrada, o vacío si no existe
     */
    public Optional<? extends PalabraFlexion<?>> findFlexionById(TipoPalabra tipo, Integer id) {
        if (tipo == null || id == null) {
            return Optional.empty();
        }

        return switch (tipo) {
            case VERBO -> verboFlexionRepo.findById(id);
            case SUSTANTIVO -> sustantivoFlexionRepo.findById(id);
            case ADJETIVO -> adjetivoFlexionRepo.findById(id);
            case NUMERAL -> numeralFlexionRepo.findById(id);
            case PRONOMBRE -> pronombreFlexionRepo.findById(id);
            case PARTICULA -> particulaFlexionRepo.findById(id);
        };
    }

    /**
     * Procesa la respuesta de un usuario para una tarjeta, actualizando su estado SRS
     * y guardando el histórico.
     *
     * @param flexion Tarjeta que se ha evaluado.
     * @param recordado Si el usuario recordó la tarjeta o no.
     * @param segundosEnResponder El tiempo (opcional) en segundos.
     */
    @Transactional
    public void procesarRespuesta(PalabraFlexion<?> flexion, boolean recordado, Integer segundosEnResponder) {
        actualizarCamposSRS(flexion, recordado);
        guardarFlexion(flexion);
        
        // Guardar el historial de respuestas
        guardarHistorico(flexion, recordado, segundosEnResponder);
        
        log.debug("{} actualizado: {} - Recordó: {}",
            flexion.getClass().getSimpleName(), flexion.getFlexion(), recordado);
    }

    /**
     * Guarda en la base de datos el histórico de la respuesta para las estadísticas.
     */
    private void guardarHistorico(PalabraFlexion<?> flexion, boolean recordado, Integer segundos) {
        try {
            TipoPalabra tipo = findTipoByClass(flexion.getClass());
            HistoricoRespuesta historico = HistoricoRespuesta.builder()
                .sloleksId(flexion.getSloleksId())
                .id(flexion.getId())
                .tipoPalabra(tipo != null ? tipo.getXmlCode() : "desconocido")
                .tsRespuesta(Instant.now())
                .acierto(recordado)
                .segundosEnResponder(segundos)
                .build();
            historicoRespuestaRepo.save(historico);
        } catch (Exception e) {
            log.error("Error al guardar el histórico para la palabra {}: {}", flexion.getSloleksId(), e.getMessage());
        }
    }
    
    private TipoPalabra findTipoByClass(Class<?> clazz) {
        if (AdjetivoFlexion.class.isAssignableFrom(clazz)) return TipoPalabra.ADJETIVO;
        if (SustantivoFlexion.class.isAssignableFrom(clazz)) return TipoPalabra.SUSTANTIVO;
        if (VerboFlexion.class.isAssignableFrom(clazz)) return TipoPalabra.VERBO;
        if (NumeralFlexion.class.isAssignableFrom(clazz)) return TipoPalabra.NUMERAL;
        if (PronombreFlexion.class.isAssignableFrom(clazz)) return TipoPalabra.PRONOMBRE;
        if (ParticulaFlexion.class.isAssignableFrom(clazz)) return TipoPalabra.PARTICULA;
        return null;
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
                default -> Math.round(intervaloSegundos * factorFacilidad * (1 + (Math.random() * 0.10 - 0.05)));
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
     * Obtiene estadísticas generales del sistema de estudio (KPIs).
     * Utilizado para el panel de resumen.
     */
    public EstadisticasDTO obtenerEstadisticas() {
        List<PalabraFlexion<?>> todasActivas = new ArrayList<>();
        todasActivas.addAll(verboFlexionRepo.findAll(FlexionSpecs.elegible()));
        todasActivas.addAll(sustantivoFlexionRepo.findAll(FlexionSpecs.elegible()));
        todasActivas.addAll(adjetivoFlexionRepo.findAll(FlexionSpecs.elegible()));
        todasActivas.addAll(numeralFlexionRepo.findAll(FlexionSpecs.elegible()));
        todasActivas.addAll(pronombreFlexionRepo.findAll(FlexionSpecs.elegible()));
        todasActivas.addAll(particulaFlexionRepo.findAll(FlexionSpecs.elegible()));

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
                tarjetasNuevas++;
            } else {
                if (f.getUltimaRevision() != null) {
                    tarjetasEstudiadas++;
                } else {
                    tarjetasNuevas++;
                }

                if (Boolean.TRUE.equals(f.getEnReaprendizaje())) {
                    tarjetasEnReaprendizaje++;
                }

                Instant proxima = f.getProximaRevision();
                if (!proxima.isAfter(ahora)) {
                    tarjetasDisponiblesAhora++;
                }
            }
        }

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
     * Obtiene las tarjetas listas para estudiar usando el campo {@code elegible} precalculado.
     * <p>
     * Proceso:
     * <ol>
     *   <li>Consulta cada repositorio de flexión por {@code elegible = true}
     *       y tarjetas de revisión ({@code proximaRevision IS NOT NULL AND proximaRevision &lt;= ahora})
     *       o tarjetas nuevas ({@code proximaRevision IS NULL})</li>
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
        Instant ahora = Instant.now();

        // Spec para tarjetas de revisión: elegible + proximaRevision <= ahora
        List<PalabraFlexion<?>> todasLasTarjetas = new ArrayList<>();

        // Verbos
        todasLasTarjetas.addAll(verboFlexionRepo.findAll(FlexionSpecs.listaParaEstudiar(ahora)));
        // Sustantivos
        todasLasTarjetas.addAll(sustantivoFlexionRepo.findAll(FlexionSpecs.listaParaEstudiar(ahora)));
        // Adjetivos
        todasLasTarjetas.addAll(adjetivoFlexionRepo.findAll(FlexionSpecs.listaParaEstudiar(ahora)));
        // Numerales
        todasLasTarjetas.addAll(numeralFlexionRepo.findAll(FlexionSpecs.listaParaEstudiar(ahora)));
        // Pronombres
        todasLasTarjetas.addAll(pronombreFlexionRepo.findAll(FlexionSpecs.listaParaEstudiar(ahora)));
        // Partículas
        todasLasTarjetas.addAll(particulaFlexionRepo.findAll(FlexionSpecs.listaParaEstudiar(ahora)));

        // Separar tarjetas de revisión (proximaRevision != null) y nuevas (proximaRevision == null)
        List<PalabraFlexion<?>> tarjetasRevision = todasLasTarjetas.stream()
                .filter(f -> f.getProximaRevision() != null)
                .sorted(Comparator
                        .comparing((PalabraFlexion<?> f) -> !Boolean.TRUE.equals(f.getEnReaprendizaje()))
                        .thenComparing((PalabraFlexion<?> f) -> f.getUltimaRevision() == null ? 1 : 0)
                        .thenComparing(PalabraFlexion::getProximaRevision))
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
    @SuppressWarnings("SameParameterValue")
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
