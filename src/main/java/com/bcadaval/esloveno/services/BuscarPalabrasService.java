package com.bcadaval.esloveno.services;

import com.bcadaval.esloveno.beans.base.PalabraFlexion;
import com.bcadaval.esloveno.beans.enums.TipoPalabra;
import com.bcadaval.esloveno.beans.palabra.*;
import com.bcadaval.esloveno.repo.*;
import com.bcadaval.esloveno.rest.dto.FlexionDetalleDTO;
import com.bcadaval.esloveno.rest.dto.PalabraGuardadaDTO;
import com.bcadaval.esloveno.structures.CriterioGramatical;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Servicio para buscar y explorar palabras guardadas en el sistema.
 * Proporciona búsqueda unificada por texto y detalle de flexiones con estadísticas SRS.
 */
@Log4j2
@Service
@Transactional(readOnly = true)
public class BuscarPalabrasService {

    @Autowired
    private VerboRepo verboRepo;
    @Autowired
    private SustantivoRepo sustantivoRepo;
    @Autowired
    private AdjetivoRepo adjetivoRepo;
    @Autowired
    private PronombreRepo pronombreRepo;
    @Autowired
    private NumeralRepo numeralRepo;

    @Autowired
    private VerboFlexionRepo verboFlexionRepo;
    @Autowired
    private SustantivoFlexionRepo sustantivoFlexionRepo;
    @Autowired
    private AdjetivoFlexionRepo adjetivoFlexionRepo;
    @Autowired
    private PronombreFlexionRepo pronombreFlexionRepo;
    @Autowired
    private NumeralFlexionRepo numeralFlexionRepo;

    @Autowired
    private EstructuraFraseService estructuraFraseService;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.systemDefault());

    /**
     * Obtiene los criterios de elegibilidad activos para cada tipo de flexión.
     * Una flexión es "elegible" si cumple alguno de los criterios gramaticales
     * derivados de las estructuras de frase activas.
     */
    @SuppressWarnings("unchecked")
    private Map<TipoPalabra, List<CriterioGramatical>> obtenerCriteriosActivos() {
        Map<TipoPalabra, List<CriterioGramatical>> mapa = new EnumMap<>(TipoPalabra.class);
        for (TipoPalabra tipo : TipoPalabra.values()) {
            mapa.put(tipo, estructuraFraseService.getCriteriosGramaticalesPorTipo(
                    (Class<? extends PalabraFlexion<?>>) tipo.getFlexionClazz()));
        }
        return mapa;
    }

    /**
     * Verifica si una flexión es elegible según los criterios activos de su tipo.
     */
    private boolean esElegible(PalabraFlexion<?> flexion, List<CriterioGramatical> criterios) {
        if (criterios == null || criterios.isEmpty()) return false;
        return criterios.stream().anyMatch(c -> c.cumple(flexion));
    }

    /**
     * Busca palabras en todos los tipos por texto (en principal o significado).
     * Si el texto está vacío, devuelve todas las palabras.
     */
    public List<PalabraGuardadaDTO> buscarPalabras(String texto) {
        List<PalabraGuardadaDTO> resultados = new ArrayList<>();
        Map<TipoPalabra, List<CriterioGramatical>> criteriosActivos = obtenerCriteriosActivos();

        if (texto == null || texto.isBlank()) {
            // Devolver todas las palabras
            resultados.addAll(mapearVerbos(verboRepo.findAll(), criteriosActivos));
            resultados.addAll(mapearSustantivos(sustantivoRepo.findAll(), criteriosActivos));
            resultados.addAll(mapearAdjetivos(adjetivoRepo.findAll(), criteriosActivos));
            resultados.addAll(mapearPronombres(pronombreRepo.findAll(), criteriosActivos));
            resultados.addAll(mapearNumerales(numeralRepo.findAll(), criteriosActivos));
        } else {
            // Buscar por principal y significado, eliminar duplicados
            Set<String> idsVerbos = new LinkedHashSet<>();
            List<Verbo> verbos = new ArrayList<>();
            for (Verbo v : verboRepo.findByPrincipalContainingIgnoreCase(texto)) {
                if (idsVerbos.add(v.getSloleksId())) verbos.add(v);
            }
            for (Verbo v : verboRepo.findBySignificadoContainingIgnoreCase(texto)) {
                if (idsVerbos.add(v.getSloleksId())) verbos.add(v);
            }
            resultados.addAll(mapearVerbos(verbos, criteriosActivos));

            Set<String> idsSust = new LinkedHashSet<>();
            List<Sustantivo> sustantivos = new ArrayList<>();
            for (Sustantivo s : sustantivoRepo.findByPrincipalContainingIgnoreCase(texto)) {
                if (idsSust.add(s.getSloleksId())) sustantivos.add(s);
            }
            for (Sustantivo s : sustantivoRepo.findBySignificadoContainingIgnoreCase(texto)) {
                if (idsSust.add(s.getSloleksId())) sustantivos.add(s);
            }
            resultados.addAll(mapearSustantivos(sustantivos, criteriosActivos));

            Set<String> idsAdj = new LinkedHashSet<>();
            List<Adjetivo> adjetivos = new ArrayList<>();
            for (Adjetivo a : adjetivoRepo.findByPrincipalContainingIgnoreCase(texto)) {
                if (idsAdj.add(a.getSloleksId())) adjetivos.add(a);
            }
            for (Adjetivo a : adjetivoRepo.findBySignificadoContainingIgnoreCase(texto)) {
                if (idsAdj.add(a.getSloleksId())) adjetivos.add(a);
            }
            resultados.addAll(mapearAdjetivos(adjetivos, criteriosActivos));

            Set<String> idsPron = new LinkedHashSet<>();
            List<Pronombre> pronombres = new ArrayList<>();
            for (Pronombre p : pronombreRepo.findByPrincipalContainingIgnoreCase(texto)) {
                if (idsPron.add(p.getSloleksId())) pronombres.add(p);
            }
            for (Pronombre p : pronombreRepo.findBySignificadoContainingIgnoreCase(texto)) {
                if (idsPron.add(p.getSloleksId())) pronombres.add(p);
            }
            resultados.addAll(mapearPronombres(pronombres, criteriosActivos));

            Set<String> idsNum = new LinkedHashSet<>();
            List<Numeral> numerales = new ArrayList<>();
            for (Numeral n : numeralRepo.findByPrincipalContainingIgnoreCase(texto)) {
                if (idsNum.add(n.getSloleksId())) numerales.add(n);
            }
            for (Numeral n : numeralRepo.findBySignificadoContainingIgnoreCase(texto)) {
                if (idsNum.add(n.getSloleksId())) numerales.add(n);
            }
            resultados.addAll(mapearNumerales(numerales, criteriosActivos));
        }

        // Ordenar por nombre principal
        resultados.sort(Comparator.comparing(PalabraGuardadaDTO::getPrincipal, String.CASE_INSENSITIVE_ORDER));

        log.debug("Búsqueda '{}': {} resultados", texto, resultados.size());
        return resultados;
    }

    /**
     * Obtiene el detalle de flexiones de una palabra con estadísticas SRS.
     * Calcula la elegibilidad de cada flexión según las estructuras de frase activas.
     */
    @SuppressWarnings("unchecked")
    public List<FlexionDetalleDTO> obtenerFlexiones(String sloleksId, TipoPalabra tipo) {
        List<CriterioGramatical> criterios = estructuraFraseService.getCriteriosGramaticalesPorTipo(
                (Class<? extends PalabraFlexion<?>>) tipo.getFlexionClazz());

        return switch (tipo) {
            case VERBO -> verboFlexionRepo.findBySloleksId(sloleksId).stream()
                    .map(f -> mapearFlexionVerbo(f, criterios)).toList();
            case SUSTANTIVO -> sustantivoFlexionRepo.findBySloleksId(sloleksId).stream()
                    .map(f -> mapearFlexionSustantivo(f, criterios)).toList();
            case ADJETIVO -> adjetivoFlexionRepo.findBySloleksId(sloleksId).stream()
                    .map(f -> mapearFlexionAdjetivo(f, criterios)).toList();
            case PRONOMBRE -> pronombreFlexionRepo.findBySloleksId(sloleksId).stream()
                    .map(f -> mapearFlexionPronombre(f, criterios)).toList();
            case NUMERAL -> numeralFlexionRepo.findBySloleksId(sloleksId).stream()
                    .map(f -> mapearFlexionNumeral(f, criterios)).toList();
        };
    }

    // =====================================================
    // Mapeo de palabras principales a DTO
    // =====================================================

    private List<PalabraGuardadaDTO> mapearVerbos(List<Verbo> verbos, Map<TipoPalabra, List<CriterioGramatical>> criteriosActivos) {
        List<CriterioGramatical> criterios = criteriosActivos.getOrDefault(TipoPalabra.VERBO, List.of());
        return verbos.stream().map(v -> {
            List<VerboFlexion> flexiones = verboFlexionRepo.findBySloleksId(v.getSloleksId());
            int elegibles = (int) flexiones.stream().filter(f -> esElegible(f, criterios)).count();
            return PalabraGuardadaDTO.builder()
                    .sloleksId(v.getSloleksId())
                    .principal(v.getPrincipal())
                    .significado(v.getSignificado())
                    .tipo(TipoPalabra.VERBO.getXmlCode())
                    .tipoEspanol(TipoPalabra.VERBO.getNombreEspanol())
                    .completa(v.getSignificado() != null && v.getTransitividad() != null)
                    .transitividad(v.getTransitividad() != null ? v.getTransitividad().name() : null)
                    .aspecto(v.getAspecto() != null ? v.getAspecto().name() : null)
                    .verboOtroAspecto(v.getVerboOtroAspecto())
                    .totalFlexiones(flexiones.size())
                    .flexionesActivas((int) flexiones.stream().filter(f -> f.getProximaRevision() != null).count())
                    .flexionesElegibles(elegibles)
                    .disponible(elegibles > 0)
                    .build();
        }).toList();
    }

    private List<PalabraGuardadaDTO> mapearSustantivos(List<Sustantivo> sustantivos, Map<TipoPalabra, List<CriterioGramatical>> criteriosActivos) {
        List<CriterioGramatical> criterios = criteriosActivos.getOrDefault(TipoPalabra.SUSTANTIVO, List.of());
        return sustantivos.stream().map(s -> {
            List<SustantivoFlexion> flexiones = sustantivoFlexionRepo.findBySloleksId(s.getSloleksId());
            int elegibles = (int) flexiones.stream().filter(f -> esElegible(f, criterios)).count();
            return PalabraGuardadaDTO.builder()
                    .sloleksId(s.getSloleksId())
                    .principal(s.getPrincipal())
                    .significado(s.getSignificado())
                    .tipo(TipoPalabra.SUSTANTIVO.getXmlCode())
                    .tipoEspanol(TipoPalabra.SUSTANTIVO.getNombreEspanol())
                    .completa(s.getSignificado() != null && s.getAnimado() != null)
                    .genero(s.getGenero() != null ? s.getGenero().name() : null)
                    .animado(s.getAnimado())
                    .totalFlexiones(flexiones.size())
                    .flexionesActivas((int) flexiones.stream().filter(f -> f.getProximaRevision() != null).count())
                    .flexionesElegibles(elegibles)
                    .disponible(elegibles > 0)
                    .build();
        }).toList();
    }

    private List<PalabraGuardadaDTO> mapearAdjetivos(List<Adjetivo> adjetivos, Map<TipoPalabra, List<CriterioGramatical>> criteriosActivos) {
        List<CriterioGramatical> criterios = criteriosActivos.getOrDefault(TipoPalabra.ADJETIVO, List.of());
        return adjetivos.stream().map(a -> {
            List<AdjetivoFlexion> flexiones = adjetivoFlexionRepo.findBySloleksId(a.getSloleksId());
            int elegibles = (int) flexiones.stream().filter(f -> esElegible(f, criterios)).count();
            return PalabraGuardadaDTO.builder()
                    .sloleksId(a.getSloleksId())
                    .principal(a.getPrincipal())
                    .significado(a.getSignificado())
                    .tipo(TipoPalabra.ADJETIVO.getXmlCode())
                    .tipoEspanol(TipoPalabra.ADJETIVO.getNombreEspanol())
                    .completa(a.getSignificado() != null)
                    .totalFlexiones(flexiones.size())
                    .flexionesActivas((int) flexiones.stream().filter(f -> f.getProximaRevision() != null).count())
                    .flexionesElegibles(elegibles)
                    .disponible(elegibles > 0)
                    .build();
        }).toList();
    }

    private List<PalabraGuardadaDTO> mapearPronombres(List<Pronombre> pronombres, Map<TipoPalabra, List<CriterioGramatical>> criteriosActivos) {
        List<CriterioGramatical> criterios = criteriosActivos.getOrDefault(TipoPalabra.PRONOMBRE, List.of());
        return pronombres.stream().map(p -> {
            List<PronombreFlexion> flexiones = pronombreFlexionRepo.findBySloleksId(p.getSloleksId());
            int elegibles = (int) flexiones.stream().filter(f -> esElegible(f, criterios)).count();
            return PalabraGuardadaDTO.builder()
                    .sloleksId(p.getSloleksId())
                    .principal(p.getPrincipal())
                    .significado(p.getSignificado())
                    .tipo(TipoPalabra.PRONOMBRE.getXmlCode())
                    .tipoEspanol(TipoPalabra.PRONOMBRE.getNombreEspanol())
                    .completa(p.getSignificado() != null)
                    .tipoPronombre(p.getTipoPronombre() != null ? p.getTipoPronombre().name() : null)
                    .totalFlexiones(flexiones.size())
                    .flexionesActivas((int) flexiones.stream().filter(f -> f.getProximaRevision() != null).count())
                    .flexionesElegibles(elegibles)
                    .disponible(elegibles > 0)
                    .build();
        }).toList();
    }

    private List<PalabraGuardadaDTO> mapearNumerales(List<Numeral> numerales, Map<TipoPalabra, List<CriterioGramatical>> criteriosActivos) {
        List<CriterioGramatical> criterios = criteriosActivos.getOrDefault(TipoPalabra.NUMERAL, List.of());
        return numerales.stream().map(n -> {
            List<NumeralFlexion> flexiones = numeralFlexionRepo.findBySloleksId(n.getSloleksId());
            int elegibles = (int) flexiones.stream().filter(f -> esElegible(f, criterios)).count();
            return PalabraGuardadaDTO.builder()
                    .sloleksId(n.getSloleksId())
                    .principal(n.getPrincipal())
                    .significado(n.getSignificado())
                    .tipo(TipoPalabra.NUMERAL.getXmlCode())
                    .tipoEspanol(TipoPalabra.NUMERAL.getNombreEspanol())
                    .completa(n.getSignificado() != null)
                    .totalFlexiones(flexiones.size())
                    .flexionesActivas((int) flexiones.stream().filter(f -> f.getProximaRevision() != null).count())
                    .flexionesElegibles(elegibles)
                    .disponible(elegibles > 0)
                    .build();
        }).toList();
    }

    // =====================================================
    // Mapeo de flexiones a DTO
    // =====================================================

    private FlexionDetalleDTO mapearFlexionBase(PalabraFlexion<?> f, List<CriterioGramatical> criterios) {
        boolean activa = f.getProximaRevision() != null;
        boolean estudioIniciado = f.getTotalRevisiones() != null && f.getTotalRevisiones() > 0;
        boolean elegible = esElegible(f, criterios);
        int totalRev = f.getTotalRevisiones() != null ? f.getTotalRevisiones() : 0;
        int totalAc = f.getTotalAciertos() != null ? f.getTotalAciertos() : 0;

        return FlexionDetalleDTO.builder()
                .id(f.getId())
                .flexion(f.getFlexion())
                .acentuado(f.getAcentuado())
                .factorFacilidad(f.getFactorFacilidad())
                .intervaloRepeticionSegundos(f.getIntervaloRepeticionSegundos())
                .vecesConsecutivasCorrectas(f.getVecesConsecutivasCorrectas())
                .totalRevisiones(totalRev)
                .totalAciertos(totalAc)
                .tasaAciertos(totalRev > 0 ? Math.round(totalAc * 1000.0 / totalRev) / 10.0 : 0.0)
                .enReaprendizaje(f.getEnReaprendizaje())
                .activa(activa)
                .elegible(elegible)
                .estudioIniciado(estudioIniciado)
                .proximaRevision(f.getProximaRevision() != null ? FORMATTER.format(f.getProximaRevision()) : null)
                .ultimaRevision(f.getUltimaRevision() != null ? FORMATTER.format(f.getUltimaRevision()) : null)
                .intervaloLegible(formatearIntervalo(f.getIntervaloRepeticionSegundos()))
                .build();
    }

    private FlexionDetalleDTO mapearFlexionVerbo(VerboFlexion vf, List<CriterioGramatical> criterios) {
        FlexionDetalleDTO dto = mapearFlexionBase(vf, criterios);
        dto.setFormaVerbal(vf.getFormaVerbal() != null ? vf.getFormaVerbal().name() : null);
        dto.setPersona(vf.getPersona() != null ? vf.getPersona().name() : null);
        dto.setNumero(vf.getNumero() != null ? vf.getNumero().name() : null);
        dto.setGenero(vf.getGenero() != null ? vf.getGenero().name() : null);
        dto.setNegativo(vf.getNegativo());
        return dto;
    }

    private FlexionDetalleDTO mapearFlexionSustantivo(SustantivoFlexion sf, List<CriterioGramatical> criterios) {
        FlexionDetalleDTO dto = mapearFlexionBase(sf, criterios);
        dto.setNumero(sf.getNumero() != null ? sf.getNumero().name() : null);
        dto.setCaso(sf.getCaso() != null ? sf.getCaso().name() : null);
        return dto;
    }

    private FlexionDetalleDTO mapearFlexionAdjetivo(AdjetivoFlexion af, List<CriterioGramatical> criterios) {
        FlexionDetalleDTO dto = mapearFlexionBase(af, criterios);
        dto.setGenero(af.getGenero() != null ? af.getGenero().name() : null);
        dto.setNumero(af.getNumero() != null ? af.getNumero().name() : null);
        dto.setCaso(af.getCaso() != null ? af.getCaso().name() : null);
        dto.setGrado(af.getGrado() != null ? af.getGrado().name() : null);
        dto.setDefinitud(af.getDefinitud() != null ? af.getDefinitud().name() : null);
        return dto;
    }

    private FlexionDetalleDTO mapearFlexionPronombre(PronombreFlexion pf, List<CriterioGramatical> criterios) {
        FlexionDetalleDTO dto = mapearFlexionBase(pf, criterios);
        dto.setNumero(pf.getNumero() != null ? pf.getNumero().name() : null);
        dto.setCaso(pf.getCaso() != null ? pf.getCaso().name() : null);
        dto.setGenero(pf.getGenero() != null ? pf.getGenero().name() : null);
        return dto;
    }

    private FlexionDetalleDTO mapearFlexionNumeral(NumeralFlexion nf, List<CriterioGramatical> criterios) {
        FlexionDetalleDTO dto = mapearFlexionBase(nf, criterios);
        dto.setNumero(nf.getNumero() != null ? nf.getNumero().name() : null);
        dto.setCaso(nf.getCaso() != null ? nf.getCaso().name() : null);
        dto.setGenero(nf.getGenero() != null ? nf.getGenero().name() : null);
        return dto;
    }

    /**
     * Formatea un intervalo en segundos a formato legible
     */
    private String formatearIntervalo(Long segundos) {
        if (segundos == null || segundos <= 0) return "-";
        Duration d = Duration.ofSeconds(segundos);
        long dias = d.toDays();
        long horas = d.toHoursPart();
        long minutos = d.toMinutesPart();
        if (dias > 0) return dias + "d " + horas + "h";
        if (horas > 0) return horas + "h " + minutos + "m";
        return minutos + "m";
    }
}


