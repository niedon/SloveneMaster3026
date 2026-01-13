package com.bcadaval.esloveno.services;

import java.time.Instant;
import java.util.*;
import java.util.function.Predicate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bcadaval.esloveno.beans.base.PalabraFlexion;
import com.bcadaval.esloveno.beans.enums.Caso;
import com.bcadaval.esloveno.beans.enums.FormaVerbal;
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
     * Filtra por: tiempo vencido, palabra completa, caso habilitado, forma verbal habilitada.
     * Ordena por: reaprendizaje primero, luego antigüedad, luego aleatorio.
     *
     * @param limite Máximo de tarjetas a devolver
     * @return Lista de flexiones disponibles
     */
    public List<PalabraFlexion<?>> obtenerTarjetasDisponibles(int limite) {
        Instant ahora = Instant.now();
        Set<Caso> casosActivos = estructuraFraseService.getCasosActivos();
        Set<FormaVerbal> formasVerbalesActivas = estructuraFraseService.getFormasVerbalesActivas();

        // Predicados reutilizables
        Predicate<PalabraFlexion<?>> tieneProximaRevisionVencida = f ->
            f.getProximaRevision() != null && !f.getProximaRevision().isAfter(ahora);

        Predicate<VerboFlexion> verboElegible = v ->
            v.getSignificado() != null &&
            v.getVerboBase() != null &&
            v.getVerboBase().getTransitividad() != null &&
            (formasVerbalesActivas.isEmpty() || formasVerbalesActivas.contains(v.getFormaVerbal()));

        Predicate<SustantivoFlexion> sustantivoElegible = s ->
            s.getSignificado() != null &&
            s.getSustantivoBase() != null &&
            s.getSustantivoBase().getAnimado() != null &&
            (s.getCaso() == null || casosActivos.contains(s.getCaso()));

        Predicate<AdjetivoFlexion> adjetivoElegible = a ->
            a.getSignificado() != null &&
            a.getAdjetivoBase() != null &&
            (a.getCaso() == null || casosActivos.contains(a.getCaso()));

        // Obtener tarjetas de cada tipo
        List<PalabraFlexion<?>> tarjetas = new ArrayList<>();

        tarjetas.addAll(verboFlexionRepo.findAll().stream()
            .filter(verboElegible)
            .filter(tieneProximaRevisionVencida)
            .toList());

        tarjetas.addAll(sustantivoFlexionRepo.findAll().stream()
            .filter(sustantivoElegible)
            .filter(tieneProximaRevisionVencida)
            .toList());

        tarjetas.addAll(adjetivoFlexionRepo.findAll().stream()
            .filter(adjetivoElegible)
            .filter(tieneProximaRevisionVencida)
            .toList());

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
     * Solo incluye palabras completas y con casos/formas verbales activos.
     *
     * @param limite Máximo de tarjetas a devolver
     * @return Lista de flexiones nuevas
     */
    public List<PalabraFlexion<?>> obtenerTarjetasNuevas(int limite) {
        Set<Caso> casosActivos = estructuraFraseService.getCasosActivos();
        Set<FormaVerbal> formasVerbalesActivas = estructuraFraseService.getFormasVerbalesActivas();

        // Predicado: nunca estudiada (proximaRevision == null)
        Predicate<PalabraFlexion<?>> esNueva = f -> f.getProximaRevision() == null;

        Predicate<VerboFlexion> verboElegible = v ->
            v.getVerboBase() != null &&
            v.getVerboBase().getSignificado() != null &&
            v.getVerboBase().getTransitividad() != null &&
            (formasVerbalesActivas.isEmpty() || formasVerbalesActivas.contains(v.getFormaVerbal()));

        Predicate<SustantivoFlexion> sustantivoElegible = s ->
            s.getSustantivoBase() != null &&
            s.getSustantivoBase().getSignificado() != null &&
            s.getSustantivoBase().getAnimado() != null &&
            (s.getCaso() == null || casosActivos.contains(s.getCaso()));

        Predicate<AdjetivoFlexion> adjetivoElegible = a ->
            a.getAdjetivoBase() != null &&
            a.getAdjetivoBase().getSignificado() != null &&
            (a.getCaso() == null || casosActivos.contains(a.getCaso()));

        List<PalabraFlexion<?>> tarjetas = new ArrayList<>();

        tarjetas.addAll(verboFlexionRepo.findAll().stream()
            .filter(verboElegible)
            .filter(esNueva)
            .toList());

        tarjetas.addAll(sustantivoFlexionRepo.findAll().stream()
            .filter(sustantivoElegible)
            .filter(esNueva)
            .toList());

        tarjetas.addAll(adjetivoFlexionRepo.findAll().stream()
            .filter(adjetivoElegible)
            .filter(esNueva)
            .toList());

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
     *
     * NOTA: Una tarjeta es "estudiada" si tiene totalRevisiones > 0.
     *       Una tarjeta es "nueva" si proximaRevision == null O totalRevisiones == 0.
     */
    public EstadisticasDTO obtenerEstadisticas() {
        Set<Caso> casosActivos = estructuraFraseService.getCasosActivos();
        Set<FormaVerbal> formasVerbalesActivas = estructuraFraseService.getFormasVerbalesActivas();

        // Predicados reutilizables
        Predicate<VerboFlexion> verboElegible = v ->
            v.getVerboBase() != null &&
            v.getVerboBase().getSignificado() != null &&
            v.getVerboBase().getTransitividad() != null &&
            (formasVerbalesActivas.isEmpty() || formasVerbalesActivas.contains(v.getFormaVerbal()));

        Predicate<SustantivoFlexion> sustantivoElegible = s ->
            s.getSustantivoBase() != null &&
            s.getSustantivoBase().getSignificado() != null &&
            s.getSustantivoBase().getAnimado() != null &&
            (s.getCaso() == null || casosActivos.contains(s.getCaso()));

        Predicate<AdjetivoFlexion> adjetivoElegible = a ->
            a.getAdjetivoBase() != null &&
            a.getAdjetivoBase().getSignificado() != null &&
            (a.getCaso() == null || casosActivos.contains(a.getCaso()));

        // Recopilar todas las tarjetas elegibles
        List<PalabraFlexion<?>> todasElegibles = new ArrayList<>();
        todasElegibles.addAll(verboFlexionRepo.findAll().stream().filter(verboElegible).toList());
        todasElegibles.addAll(sustantivoFlexionRepo.findAll().stream().filter(sustantivoElegible).toList());
        todasElegibles.addAll(adjetivoFlexionRepo.findAll().stream().filter(adjetivoElegible).toList());

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

