package com.bcadaval.esloveno.rest;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.bcadaval.esloveno.beans.base.PalabraFlexion;
import com.bcadaval.esloveno.beans.enums.TipoPalabra;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bcadaval.esloveno.beans.palabra.AdjetivoFlexion;
import com.bcadaval.esloveno.beans.palabra.NumeralFlexion;
import com.bcadaval.esloveno.beans.palabra.PronombreFlexion;
import com.bcadaval.esloveno.beans.palabra.SustantivoFlexion;
import com.bcadaval.esloveno.beans.palabra.VerboFlexion;
import com.bcadaval.esloveno.beans.enums.Transitividad;
import com.bcadaval.esloveno.repo.AdjetivoFlexionRepo;
import com.bcadaval.esloveno.repo.AdjetivoRepo;
import com.bcadaval.esloveno.repo.NumeralFlexionRepo;
import com.bcadaval.esloveno.repo.NumeralRepo;
import com.bcadaval.esloveno.repo.PronombreFlexionRepo;
import com.bcadaval.esloveno.repo.SustantivoFlexionRepo;
import com.bcadaval.esloveno.repo.SustantivoRepo;
import com.bcadaval.esloveno.repo.VerboFlexionRepo;
import com.bcadaval.esloveno.repo.VerboRepo;
import com.bcadaval.esloveno.rest.dto.PalabraIncompletaDTO;
import com.bcadaval.esloveno.rest.dto.ActualizarPalabraResponse;
import com.bcadaval.esloveno.services.VariablesService;

import lombok.extern.log4j.Log4j2;

/**
 * Controlador para completar palabras que tienen datos faltantes.
 * Al completar una palabra, inicializa los campos SRS de sus flexiones
 * para que estén disponibles para el estudio.
 */
@Log4j2
@Controller
public class CompletarPalabrasController {

    @Autowired
    private VerboRepo verboRepo;

    @Autowired
    private SustantivoRepo sustantivoRepo;

    @Autowired
    private AdjetivoRepo adjetivoRepo;

    @Autowired
    private VerboFlexionRepo verboFlexionRepo;

    @Autowired
    private SustantivoFlexionRepo sustantivoFlexionRepo;

    @Autowired
    private AdjetivoFlexionRepo adjetivoFlexionRepo;

    @Autowired
    private PronombreFlexionRepo pronombreFlexionRepo;

    @Autowired
    private NumeralRepo numeralRepo;

    @Autowired
    private NumeralFlexionRepo numeralFlexionRepo;

    @Autowired
    private VariablesService variablesService;

    /**
     * Muestra la página para completar palabras incompletas
     */
    @GetMapping("/completarPalabras")
    public String mostrarPaginaCompletarPalabras() {
        log.debug("Accediendo a la página de completar palabras");
        return "completarPalabras";
    }

    /**
     * Obtiene la lista de palabras incompletas
     * - Todas: significado null
     * - Verbos: transitividad null
     * - Sustantivos: animado null
     * - Adjetivos: significado null
     */
    @GetMapping("/api/palabrasIncompletas")
    @ResponseBody
    public List<PalabraIncompletaDTO> obtenerPalabrasIncompletas() {
        log.info("Obteniendo palabras incompletas");
        List<PalabraIncompletaDTO> palabrasIncompletas = new ArrayList<>();

        Stream.of(TipoPalabra.values()).map(tipoPalabra -> switch (tipoPalabra) {
            case SUSTANTIVO -> sustantivoRepo.findBySignificadoIsNullOrAnimadoIsNull().stream()
                .map(sustantivo -> PalabraIncompletaDTO.builder()
                    .id(sustantivo.getSloleksId())
                    .palabra(sustantivo.getPrincipal())
                    .tipo(TipoPalabra.SUSTANTIVO.getXmlCode())
                    .significado(sustantivo.getSignificado())
                    .animado(sustantivo.getAnimado()));
            case VERBO -> verboRepo.findBySignificadoIsNullOrTransitividadIsNull().stream()
                .map(verbo -> PalabraIncompletaDTO.builder()
                    .id(verbo.getSloleksId())
                    .palabra(verbo.getPrincipal())
                    .tipo(TipoPalabra.VERBO.getXmlCode())
                    .significado(verbo.getSignificado())
                    .transitividad(verbo.getTransitividad() != null ? verbo.getTransitividad().name() : null));
            case ADJETIVO -> adjetivoRepo.findBySignificadoIsNull().stream()
                .map(adjetivo -> PalabraIncompletaDTO.builder()
                    .id(adjetivo.getSloleksId())
                    .palabra(adjetivo.getPrincipal())
                    .tipo(TipoPalabra.ADJETIVO.getXmlCode())
                    .significado(adjetivo.getSignificado()));
            case PRONOMBRE -> pronombreFlexionRepo.findBySignificadoIsNull().stream()
                .map(pronombreFlexion -> PalabraIncompletaDTO.builder()
                    .id(pronombreFlexion.getId().toString())
                    .palabra(pronombreFlexion.getFlexion())
                    .tipo(TipoPalabra.PRONOMBRE.getXmlCode())
                    .significado(pronombreFlexion.getSignificado()));
            case NUMERAL -> numeralRepo.findBySignificadoIsNull().stream()
                .map(numeral -> PalabraIncompletaDTO.builder()
                    .id(numeral.getSloleksId())
                    .palabra(numeral.getPrincipal())
                    .tipo(TipoPalabra.NUMERAL.getXmlCode())
                    .significado(numeral.getSignificado()));
                }
        ).forEach(el -> palabrasIncompletas.addAll(el.map(PalabraIncompletaDTO.PalabraIncompletaDTOBuilder::build).toList()));


        log.info("Encontradas {} palabras incompletas", palabrasIncompletas.size());
        return palabrasIncompletas;
    }

    /**
     * Actualiza una palabra incompleta con los datos proporcionados
     */
    @PostMapping("/api/actualizarPalabra")
    @ResponseBody
    public ActualizarPalabraResponse actualizarPalabra(
            @RequestParam String id,
            @RequestParam String tipo,
            @RequestParam String significado,
            @RequestParam(required = false) String transitividad,
            @RequestParam(required = false) Boolean animado) {

        log.info("Actualizando palabra: id={}, tipo={}", id, tipo);

        try {

            switch(TipoPalabra.valueOf(tipo)) {
                case SUSTANTIVO -> sustantivoRepo.save(sustantivoRepo.findById(id)
                        .orElseThrow(() -> new RuntimeException("Sustantivo no encontrado: " + id))
                        .setSignificado(significado)
                        .setAnimado(animado)
                );
                case VERBO -> verboRepo.save(verboRepo.findById(id)
                        .orElseThrow(() -> new RuntimeException("Verbo no encontrado: " + id))
                        .setSignificado(significado)
                        .setTransitividad(Transitividad.valueOf(transitividad))
                );
                case ADJETIVO -> adjetivoRepo.save(adjetivoRepo.findById(id)
                        .orElseThrow(() -> new RuntimeException("Adjetivo no encontrado: " + id))
                        .setSignificado(significado)
                );
                case PRONOMBRE -> pronombreFlexionRepo.save(pronombreFlexionRepo.findById(Integer.valueOf(id))
                        .orElseThrow(() -> new RuntimeException("Pronombre no encontrado: " + id))
                        .setSignificado(significado)
                );
                case NUMERAL -> numeralRepo.save(numeralRepo.findById(id)
                        .orElseThrow(() -> new RuntimeException("Numeral no encontrado: " + id))
                        .setSignificado(significado)
                );
            }


            // Inicializar campos SRS en flexiones relacionadas para que estén disponibles
            Instant ahora = Instant.now();
            Double factorInicial = variablesService.getFactorFacilidadInicial();

            List<? extends PalabraFlexion<?>> lista = switch (TipoPalabra.valueOf(tipo)) {
                case SUSTANTIVO -> sustantivoFlexionRepo.findBySloleksId(id);
                case VERBO -> verboFlexionRepo.findBySloleksId(id);
                case ADJETIVO -> adjetivoFlexionRepo.findBySloleksId(id);
                case PRONOMBRE -> pronombreFlexionRepo.findById(Integer.valueOf(id)).stream().toList();
                case NUMERAL -> numeralFlexionRepo.findBySloleksId(id);
            };
            lista.forEach(flexion -> inicializarCamposSrs(flexion, ahora, factorInicial));
            switch (TipoPalabra.valueOf(tipo)) {
                case SUSTANTIVO -> sustantivoFlexionRepo.saveAll(lista.stream().map(f -> (SustantivoFlexion) f).toList());
                case VERBO -> verboFlexionRepo.saveAll(lista.stream().map(f -> (VerboFlexion) f).toList());
                case ADJETIVO -> adjetivoFlexionRepo.saveAll(lista.stream().map(f -> (AdjetivoFlexion) f).toList());
                case PRONOMBRE -> pronombreFlexionRepo.saveAll(lista.stream().map(f -> (PronombreFlexion) f).toList());
                case NUMERAL -> numeralFlexionRepo.saveAll(lista.stream().map(f -> (NumeralFlexion) f).toList());
            }
            log.info("Inicializadas {} flexiones de {} {}", lista.size(), tipo, id);

            return ActualizarPalabraResponse.builder()
                    .exito(true)
                    .mensaje("Palabra actualizada correctamente")
                    .palabra(id)
                    .build();

        } catch (Exception e) {
            log.error("Error al actualizar palabra: {}", e.getMessage(), e);
            return ActualizarPalabraResponse.builder()
                    .exito(false)
                    .mensaje("Error al actualizar la palabra: " + e.getMessage())
                    .palabra(id)
                    .build();
        }
    }

    private void inicializarCamposSrs(PalabraFlexion<?> palabra, Instant ahora, Double factorInicial) {
        if (palabra.getProximaRevision() == null) {
            palabra.setFactorFacilidad(factorInicial);
            palabra.setIntervaloRepeticionSegundos(0L);
            palabra.setVecesConsecutivasCorrectas(0);
            palabra.setTotalRevisiones(0);
            palabra.setTotalAciertos(0);
            palabra.setEnReaprendizaje(false);
            palabra.setProximaRevision(ahora);
        }
    }
}
