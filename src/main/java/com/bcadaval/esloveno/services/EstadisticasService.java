package com.bcadaval.esloveno.services;

import com.bcadaval.esloveno.repo.*;
import com.bcadaval.esloveno.rest.dto.ChartDataDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Log4j2
@Service
public class EstadisticasService {

    @Autowired
    private HistoricoRespuestaRepo historicoRespuestaRepo;

    @Autowired private AdjetivoFlexionRepo adjetivoRepo;
    @Autowired private SustantivoFlexionRepo sustantivoRepo;
    @Autowired private VerboFlexionRepo verboRepo;
    @Autowired private NumeralFlexionRepo numeralRepo;
    @Autowired private PronombreFlexionRepo pronombreRepo;
    @Autowired private ParticulaFlexionRepo particulaRepo;

    // --- MÉTODOS PÚBLICOS ---

    public ChartDataDTO getAciertosFallosTotales(Instant inicio, Instant fin, String granularidad, String zonaHoraria) {
        ChartDataMap dataMap = getStatsAciertosFallosBase(inicio, fin, granularidad, zonaHoraria);
        List<String> labels = dataMap.getLabels();

        List<ChartDataDTO.DatasetDTO> datasets = new ArrayList<>();
        datasets.add(new ChartDataDTO.DatasetDTO("Aciertos", dataMap.getData("aciertos"), "rgba(75, 192, 192, 0.8)", "rgba(75, 192, 192, 1)", true));
        datasets.add(new ChartDataDTO.DatasetDTO("Fallos", dataMap.getData("fallos"), "rgba(255, 99, 132, 0.8)", "rgba(255, 99, 132, 1)", true));
        
        return ChartDataDTO.builder().labels(labels).datasets(datasets).build();
    }
    
    public ChartDataDTO getPorcentajeAciertos(Instant inicio, Instant fin, String granularidad, String zonaHoraria) {
        ChartDataMap dataMap = getStatsAciertosFallosBase(inicio, fin, granularidad, zonaHoraria);
        List<String> labels = dataMap.getLabels();
        List<Number> aciertos = dataMap.getData("aciertos");
        List<Number> fallos = dataMap.getData("fallos");
        
        List<Number> porcentajes = new ArrayList<>();
        for (int i = 0; i < aciertos.size(); i++) {
            double ac = aciertos.get(i).doubleValue();
            double fa = fallos.get(i).doubleValue();
            double total = ac + fa;
            porcentajes.add(total > 0 ? (ac / total * 100.0) : 0.0);
        }

        List<ChartDataDTO.DatasetDTO> datasets = new ArrayList<>();
        datasets.add(new ChartDataDTO.DatasetDTO("Tasa Acierto (%)", porcentajes, "rgba(153, 102, 255, 0.4)", "rgba(153, 102, 255, 1)", true)); // Fill true for area chart perception

        return ChartDataDTO.builder().labels(labels).datasets(datasets).build();
    }
    
    public ChartDataDTO getTiempoPromedio(Instant inicio, Instant fin, String granularidad, String zonaHoraria) {
        ZoneId zoneId = ZoneId.of(zonaHoraria != null ? zonaHoraria : "UTC");
        boolean isMinutos = "minutos".equalsIgnoreCase(granularidad);
        String sqlFormat = isMinutos ? "%Y-%m-%d %H:%M:00" : "%Y-%m-%d %H:00:00";

        List<Object[]> resultados = historicoRespuestaRepo.avgTiempoGrouped(inicio, fin, sqlFormat);
        Map<String, Double> tiemposMap = new HashMap<>();

        DateTimeFormatter dbParser = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter keyFormatter = getKeyFormatter(granularidad);

        for (Object[] row : resultados) {
            String fechaStr = (String) row[0];
            Number promedio = (Number) row[1]; // Double

            if (fechaStr != null && promedio != null) {
                // Parseo asumiendo UTC desde BD (para strftime)
                LocalDateTime ldt = LocalDateTime.parse(fechaStr, dbParser);
                ZonedDateTime zdtUser = ldt.atZone(ZoneOffset.UTC).withZoneSameInstant(zoneId);
                String key = keyFormatter.format(zdtUser);
                tiemposMap.put(key, promedio.doubleValue());
            }
        }

        List<LabelPair> pairs = generarEtiquetas(inicio, fin, granularidad, zoneId);
        List<String> labels = new ArrayList<>();
        List<Number> data = new ArrayList<>();
        
        for (LabelPair pair : pairs) {
            labels.add(pair.displayLabel);
            data.add(tiemposMap.getOrDefault(pair.key, 0.0));
        }

        List<ChartDataDTO.DatasetDTO> datasets = new ArrayList<>();
        datasets.add(new ChartDataDTO.DatasetDTO("Tiempo Promedio (s)", data, "rgba(54, 162, 235, 0.6)", "rgba(54, 162, 235, 1)", false));

        return ChartDataDTO.builder()
                .labels(labels)
                .datasets(datasets)
                .build();
    }
    
    public ChartDataDTO getEstadisticasPorTipo(Instant inicio, Instant fin) {
        List<Object[]> resultados = historicoRespuestaRepo.statsPorTipoGrouped(inicio, fin);

        List<String> labels = new ArrayList<>();
        List<Number> data = new ArrayList<>();

        for (Object[] row : resultados) {
            String tipo = (String) row[0];
            Number aciertosNr = (Number) row[1];
            Number totalNr = (Number) row[2];

            // Protección contra nulos y tipos
            tipo = (tipo == null) ? "Desconocido" : tipo;
            double aciertos = (aciertosNr != null) ? aciertosNr.doubleValue() : 0.0;
            double total = (totalNr != null) ? totalNr.doubleValue() : 0.0;

            labels.add(tipo);
            data.add(total > 0 ? (aciertos / total * 100) : 0.0);
        }
        
        List<ChartDataDTO.DatasetDTO> datasets = new ArrayList<>();
        datasets.add(new ChartDataDTO.DatasetDTO("Aciertos (%)", data, "rgba(255, 206, 86, 0.6)", "rgba(255, 206, 86, 1)", true));

        return ChartDataDTO.builder()
                .labels(labels)
                .datasets(datasets)
                .build();
    }

    public ChartDataDTO getPronostico(Instant inicio, Instant fin, String granularidad, String zonaHoraria) {
        ZoneId zoneId = ZoneId.of(zonaHoraria != null ? zonaHoraria : "UTC");

        // Formato para SQLite strftime (agrupación en UTC)
        boolean isMinutos = "minutos".equalsIgnoreCase(granularidad);
        String sqliteFormat = isMinutos ? "%Y-%m-%d %H:%M:00" : "%Y-%m-%d %H:00:00";

        Map<String, Long> conteoAgregado = new HashMap<>();
        DateTimeFormatter dbParser = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter keyFormatter = getKeyFormatter(granularidad);

        List<FlexionBaseRepo<?, ?>> repos = List.of(
                adjetivoRepo, sustantivoRepo, verboRepo, numeralRepo, pronombreRepo, particulaRepo
        );

        for (FlexionBaseRepo<?, ?> repo : repos) {
            List<Object[]> resultados = repo.countByProximaRevisionGrouped(inicio, fin, sqliteFormat);

            for (Object[] row : resultados) {
                String fechaStr = (String) row[0];
                Number count = (Number) row[1];

                if (fechaStr != null) {
                    LocalDateTime ldt = LocalDateTime.parse(fechaStr, dbParser);
                    ZonedDateTime zdtUser = ldt.atZone(ZoneOffset.UTC).withZoneSameInstant(zoneId);

                    String key = keyFormatter.format(zdtUser);
                    conteoAgregado.merge(key, count != null ? count.longValue() : 0L, Long::sum);
                }
            }
        }

        // Rellenar huecos
        List<LabelPair> pairs = generarEtiquetas(inicio, fin, granularidad, zoneId);
        List<String> labels = new ArrayList<>();
        List<Number> data = new ArrayList<>();
        
        for (LabelPair pair : pairs) {
            labels.add(pair.displayLabel);
            data.add(conteoAgregado.getOrDefault(pair.key, 0L));
        }

        List<ChartDataDTO.DatasetDTO> datasets = new ArrayList<>();
        datasets.add(new ChartDataDTO.DatasetDTO("Revisiones Programadas", data, "rgba(255, 159, 64, 0.6)", "rgba(255, 159, 64, 1)", true));

        return ChartDataDTO.builder()
                .labels(labels)
                .datasets(datasets)
                .build();
    }

    // --- HELPERS PRIVADOS ---

    private ChartDataMap getStatsAciertosFallosBase(Instant inicio, Instant fin, String granularidad, String zonaHoraria) {
        ZoneId zoneId = ZoneId.of(zonaHoraria != null ? zonaHoraria : "UTC");
        boolean isMinutos = "minutos".equalsIgnoreCase(granularidad);
        String sqlFormat = isMinutos ? "%Y-%m-%d %H:%M:00" : "%Y-%m-%d %H:00:00";

        List<Object[]> resultados = historicoRespuestaRepo.countAciertosFallosGrouped(inicio, fin, sqlFormat);

        Map<String, Long> aciertosMap = new HashMap<>();
        Map<String, Long> fallosMap = new HashMap<>();
        DateTimeFormatter dbParser = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter keyFormatter = getKeyFormatter(granularidad);

        for (Object[] row : resultados) {
            String fechaStr = (String) row[0];
            Number aciertos = (Number) row[1];
            Number fallos = (Number) row[2];

            if (fechaStr != null) {
                LocalDateTime ldt = LocalDateTime.parse(fechaStr, dbParser);
                ZonedDateTime zdtUser = ldt.atZone(ZoneOffset.UTC).withZoneSameInstant(zoneId);
                String key = keyFormatter.format(zdtUser);
                
                aciertosMap.merge(key, aciertos != null ? aciertos.longValue() : 0L, Long::sum);
                fallosMap.merge(key, fallos != null ? fallos.longValue() : 0L, Long::sum);
            }
        }

        List<LabelPair> pairs = generarEtiquetas(inicio, fin, granularidad, zoneId);
        List<String> labels = new ArrayList<>();
        List<Number> aciertosList = new ArrayList<>();
        List<Number> fallosList = new ArrayList<>();

        for (LabelPair pair : pairs) {
            labels.add(pair.displayLabel);
            aciertosList.add(aciertosMap.getOrDefault(pair.key, 0L));
            fallosList.add(fallosMap.getOrDefault(pair.key, 0L));
        }

        ChartDataMap result = new ChartDataMap();
        result.setLabels(labels);
        result.putData("aciertos", aciertosList);
        result.putData("fallos", fallosList);
        return result;
    }

    /**
     * Clase auxiliar para evitar casteos inseguros con Maps genéricos.
     */
    private static class ChartDataMap {
        @Getter
        @Setter
        private List<String> labels = new ArrayList<>();
        private final Map<String, List<Number>> datasets = new HashMap<>();

        public void putData(String key, List<Number> data) {
            datasets.put(key, data);
        }

        public List<Number> getData(String key) {
            return datasets.getOrDefault(key, new ArrayList<>());
        }
    }

    /**
     * @param key          Para buscar en Map
     * @param displayLabel Para mostrar en Gráfica
     */
    private record LabelPair(String key, String displayLabel) {
    }

    private List<LabelPair> generarEtiquetas(Instant inicio, Instant fin, String granularidad, ZoneId zoneId) {
        List<LabelPair> etiquetas = new ArrayList<>();
        DateTimeFormatter keyFormatter = getKeyFormatter(granularidad);
        DateTimeFormatter displayFormatter = getDisplayFormatter(granularidad);
        
        ZonedDateTime actual = inicio.atZone(zoneId);
        ZonedDateTime finZdt = fin.atZone(zoneId);
        actual = truncarInicio(actual, granularidad);

        while (!actual.isAfter(finZdt)) {
            etiquetas.add(new LabelPair(
                keyFormatter.format(actual),
                displayFormatter.format(actual)
            ));
            actual = sumarUnidad(actual, granularidad);
        }
        return etiquetas;
    }
    
    private ZonedDateTime truncarInicio(ZonedDateTime zdt, String granularidad) {
        return switch (granularidad.toLowerCase()) {
            case "minutos" -> zdt.withSecond(0).withNano(0);
            case "horas" -> zdt.withMinute(0).withSecond(0).withNano(0);
            case "meses" -> zdt.withDayOfMonth(1).toLocalDate().atStartOfDay(zdt.getZone());
            default -> zdt.toLocalDate().atStartOfDay(zdt.getZone()); // días y default
        };
    }

    private ZonedDateTime sumarUnidad(ZonedDateTime zdt, String granularidad) {
        return switch (granularidad.toLowerCase()) {
            case "minutos" -> zdt.plusMinutes(1);
            case "horas" -> zdt.plusHours(1);
            case "meses" -> zdt.plusMonths(1);
            default -> zdt.plusDays(1); // días y default
        };
    }

    private DateTimeFormatter getKeyFormatter(String granularidad) {
        return switch (granularidad.toLowerCase()) {
            case "minutos" -> DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            case "horas" -> DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00");
            default -> DateTimeFormatter.ofPattern("yyyy-MM-dd");
        };
    }
    
    private DateTimeFormatter getDisplayFormatter(String granularidad) {
        Locale es = new Locale.Builder().setLanguage("es").setRegion("ES").build();
        return switch (granularidad.toLowerCase()) {
            case "minutos" -> DateTimeFormatter.ofPattern("HH:mm");
            case "horas" -> DateTimeFormatter.ofPattern("HH:00");
            case "dias" -> DateTimeFormatter.ofPattern("d MMM", es);
            case "meses" -> DateTimeFormatter.ofPattern("MMM yyyy", es);
            default -> DateTimeFormatter.ofPattern("dd/MM/yyyy");
        };
    }
}
