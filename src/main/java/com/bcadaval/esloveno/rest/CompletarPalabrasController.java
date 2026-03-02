package com.bcadaval.esloveno.rest;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.bcadaval.esloveno.beans.base.PalabraFlexion;
import com.bcadaval.esloveno.beans.enums.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bcadaval.esloveno.beans.palabra.AdjetivoFlexion;
import com.bcadaval.esloveno.beans.palabra.NumeralFlexion;
import com.bcadaval.esloveno.beans.palabra.ParticulaFlexion;
import com.bcadaval.esloveno.beans.palabra.PronombreFlexion;
import com.bcadaval.esloveno.beans.palabra.SustantivoFlexion;
import com.bcadaval.esloveno.beans.palabra.VerboFlexion;
import com.bcadaval.esloveno.repo.AdjetivoFlexionRepo;
import com.bcadaval.esloveno.repo.AdjetivoRepo;
import com.bcadaval.esloveno.repo.NumeralFlexionRepo;
import com.bcadaval.esloveno.repo.NumeralRepo;
import com.bcadaval.esloveno.repo.ParticulaFlexionRepo;
import com.bcadaval.esloveno.repo.ParticulaRepo;
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
    private ParticulaRepo particulaRepo;

    @Autowired
    private ParticulaFlexionRepo particulaFlexionRepo;

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
     * Obtiene la lista de todas las palabras con indicación de completitud.
     * El frontend filtrará por JS (por defecto solo muestra incompletas).
     */
    @GetMapping("/api/palabrasIncompletas")
    @ResponseBody
    public List<PalabraIncompletaDTO> obtenerPalabras() {
        log.info("Obteniendo palabras para completar/editar");
        List<PalabraIncompletaDTO> palabras = new ArrayList<>();

        // Sustantivos
        sustantivoRepo.findAll().stream()
                .map(sustantivo -> PalabraIncompletaDTO.builder()
                        .id(sustantivo.getSloleksId())
                        .palabra(sustantivo.getPrincipal())
                        .tipo(TipoPalabra.SUSTANTIVO.getXmlCode())
                        .significado(sustantivo.getSignificado())
                        .animacidad(sustantivo.getAnimacidad() != null ? sustantivo.getAnimacidad().name() : null)
                        .contabilidad(sustantivo.getContabilidad() != null ? sustantivo.getContabilidad().name() : null)
                        .claseSemantica(sustantivo.getClaseSemantica() != null ? sustantivo.getClaseSemantica().name() : null)
                        .cabezaRelacional(sustantivo.getCabezaRelacional() != null ? sustantivo.getCabezaRelacional().name() : null)
                        .completa(sustantivo.getSignificado() != null
                                && sustantivo.getAnimacidad() != null
                                && sustantivo.getContabilidad() != null
                                && sustantivo.getClaseSemantica() != null
                                && sustantivo.getCabezaRelacional() != null)
                        .build())
                .forEach(palabras::add);

        // Verbos
        verboRepo.findAll().stream()
                .map(verbo -> PalabraIncompletaDTO.builder()
                        .id(verbo.getSloleksId())
                        .palabra(verbo.getPrincipal())
                        .tipo(TipoPalabra.VERBO.getXmlCode())
                        .significado(verbo.getSignificado())
                        .transitividad(verbo.getTransitividad() != null ? verbo.getTransitividad().name() : null)
                        .requiereSujetoAnimado(verbo.getRequiereSujetoAnimado() != null ? verbo.getRequiereSujetoAnimado().name() : null)
                        .requiereObjetoAnimado(verbo.getRequiereObjetoAnimado() != null ? verbo.getRequiereObjetoAnimado().name() : null)
                        .completa(verbo.getSignificado() != null
                                && verbo.getTransitividad() != null
                                && verbo.getRequiereSujetoAnimado() != null
                                && verbo.getRequiereObjetoAnimado() != null)
                        .build())
                .forEach(palabras::add);

        // Adjetivos
        adjetivoRepo.findAll().stream()
                .map(adjetivo -> PalabraIncompletaDTO.builder()
                        .id(adjetivo.getSloleksId())
                        .palabra(adjetivo.getPrincipal())
                        .tipo(TipoPalabra.ADJETIVO.getXmlCode())
                        .significado(adjetivo.getSignificado())
                        .completa(adjetivo.getSignificado() != null)
                        .build())
                .forEach(palabras::add);

        // Pronombres
        pronombreFlexionRepo.findAll().stream()
                .map(pf -> PalabraIncompletaDTO.builder()
                        .id(pf.getId().toString())
                        .palabra(pf.getFlexion())
                        .tipo(TipoPalabra.PRONOMBRE.getXmlCode())
                        .significado(pf.getSignificado())
                        .completa(pf.getSignificado() != null)
                        .build())
                .forEach(palabras::add);

        // Numerales
        numeralRepo.findAll().stream()
                .map(numeral -> PalabraIncompletaDTO.builder()
                        .id(numeral.getSloleksId())
                        .palabra(numeral.getPrincipal())
                        .tipo(TipoPalabra.NUMERAL.getXmlCode())
                        .significado(numeral.getSignificado())
                        .cantidad(numeral.getCantidad())
                        .completa(numeral.getSignificado() != null && numeral.getCantidad() != null)
                        .build())
                .forEach(palabras::add);

        // Partículas
        particulaRepo.findAll().stream()
                .map(particula -> PalabraIncompletaDTO.builder()
                        .id(particula.getSloleksId())
                        .palabra(particula.getPrincipal())
                        .tipo(TipoPalabra.PARTICULA.getXmlCode())
                        .significado(particula.getSignificado())
                        .completa(particula.getSignificado() != null)
                        .build())
                .forEach(palabras::add);

        log.info("Encontradas {} palabras totales", palabras.size());
        return palabras;
    }

    /**
     * Actualiza una palabra con los datos proporcionados.
     * Soporta tanto palabras incompletas como completas.
     * Los campos SRS solo se inicializan si aún no lo están.
     */
    @PostMapping("/api/actualizarPalabra")
    @ResponseBody
    public ActualizarPalabraResponse actualizarPalabra(
            @RequestParam String id,
            @RequestParam String tipo,
            @RequestParam String significado,
            @RequestParam(required = false) String transitividad,
            @RequestParam(required = false) String animacidad,
            @RequestParam(required = false) String contabilidad,
            @RequestParam(required = false) String claseSemantica,
            @RequestParam(required = false) String cabezaRelacional,
            @RequestParam(required = false) String requiereSujetoAnimado,
            @RequestParam(required = false) String requiereObjetoAnimado,
            @RequestParam(required = false) Integer cantidad) {

        log.info("Actualizando palabra: id={}, tipo={}", id, tipo);

        try {

            switch (TipoPalabra.valueOf(tipo)) {
                case SUSTANTIVO -> sustantivoRepo.save(sustantivoRepo.findById(id)
                        .orElseThrow(() -> new RuntimeException("Sustantivo no encontrado: " + id))
                        .setSignificado(significado)
                        .setAnimacidad(animacidad != null && !animacidad.isBlank() ? Animacidad.valueOf(animacidad) : null)
                        .setContabilidad(contabilidad != null && !contabilidad.isBlank() ? Contabilidad.valueOf(contabilidad) : null)
                        .setClaseSemantica(claseSemantica != null && !claseSemantica.isBlank() ? ClaseSemantica.valueOf(claseSemantica) : null)
                        .setCabezaRelacional(cabezaRelacional != null && !cabezaRelacional.isBlank() ? CabezaRelacional.valueOf(cabezaRelacional) : null)
                );
                case VERBO -> verboRepo.save(verboRepo.findById(id)
                        .orElseThrow(() -> new RuntimeException("Verbo no encontrado: " + id))
                        .setSignificado(significado)
                        .setTransitividad(transitividad != null && !transitividad.isBlank() ? Transitividad.valueOf(transitividad) : null)
                        .setRequiereSujetoAnimado(requiereSujetoAnimado != null && !requiereSujetoAnimado.isBlank() ? RequiereSujetoAnimado.valueOf(requiereSujetoAnimado) : null)
                        .setRequiereObjetoAnimado(requiereObjetoAnimado != null && !requiereObjetoAnimado.isBlank() ? RequiereObjetoAnimado.valueOf(requiereObjetoAnimado) : null)
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
                        .setCantidad(cantidad)
                );
                case PARTICULA -> particulaRepo.save(particulaRepo.findById(id)
                        .orElseThrow(() -> new RuntimeException("Partícula no encontrada: " + id))
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
                case PARTICULA -> particulaFlexionRepo.findBySloleksId(id);
            };
            lista.forEach(flexion -> inicializarCamposSrs(flexion, ahora, factorInicial));
            switch (TipoPalabra.valueOf(tipo)) {
                case SUSTANTIVO -> sustantivoFlexionRepo.saveAll(lista.stream().map(f -> (SustantivoFlexion) f).toList());
                case VERBO -> verboFlexionRepo.saveAll(lista.stream().map(f -> (VerboFlexion) f).toList());
                case ADJETIVO -> adjetivoFlexionRepo.saveAll(lista.stream().map(f -> (AdjetivoFlexion) f).toList());
                case PRONOMBRE -> pronombreFlexionRepo.saveAll(lista.stream().map(f -> (PronombreFlexion) f).toList());
                case NUMERAL -> numeralFlexionRepo.saveAll(lista.stream().map(f -> (NumeralFlexion) f).toList());
                case PARTICULA -> particulaFlexionRepo.saveAll(lista.stream().map(f -> (com.bcadaval.esloveno.beans.palabra.ParticulaFlexion) f).toList());
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
