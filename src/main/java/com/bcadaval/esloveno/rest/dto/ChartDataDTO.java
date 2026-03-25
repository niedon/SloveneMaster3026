package com.bcadaval.esloveno.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChartDataDTO {
    private List<String> labels;
    private List<DatasetDTO> datasets;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DatasetDTO {
        private String label;
        private List<Number> data; // Puede ser Integer, Double, etc.
        private String backgroundColor; // Opcional, para estilizado desde backend
        private String borderColor; // Opcional
        private Boolean fill; // Opcional, para gráficas de área
        // Se pueden añadir más propiedades de Chart.js según necesidad
    }
}

