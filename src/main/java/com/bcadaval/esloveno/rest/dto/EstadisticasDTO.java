package com.bcadaval.esloveno.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para las estadísticas del sistema de repetición espaciada
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadisticasDTO {

    /**
     * Total de tarjetas en el sistema (palabras completas y casos habilitados)
     */
    private Integer totalTarjetas;

    /**
     * Tarjetas que han sido estudiadas al menos una vez
     */
    private Integer tarjetasEstudiadas;

    /**
     * Tarjetas nuevas (nunca estudiadas)
     */
    private Integer tarjetasNuevas;

    /**
     * Tarjetas disponibles para estudiar ahora
     */
    private Integer tarjetasDisponiblesAhora;

    /**
     * Tarjetas en proceso de reaprendizaje (fallaron recientemente)
     */
    private Integer tarjetasEnReaprendizaje;

    /**
     * Total de revisiones realizadas
     */
    private Integer totalRevisiones;

    /**
     * Total de aciertos
     */
    private Integer totalAciertos;

    /**
     * Tasa de aciertos en porcentaje
     */
    private Double tasaAciertos;
}

