package com.bcadaval.esloveno.beans;

import com.bcadaval.esloveno.config.InstantConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Entidad que registra el histórico de las respuestas dadas por el usuario.
 * Utilizada para alimentar el sistema de estadísticas.
 */
@Entity
@Table(name = "HISTORICO_RESPUESTAS")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoricoRespuesta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idHistorico;

    // Identificador unívoco de la palabra
    @Column(name = "SLOLEKS_ID", nullable = false)
    private String sloleksId;

    // Identificador junto al tipo de palabra (id de la flexión)
    @Column(name = "ID", nullable = false)
    private Integer id;

    // xmlCode del Enum TipoPalabra
    @Column(name = "TIPO_PALABRA", nullable = false)
    private String tipoPalabra;

    @Convert(converter = InstantConverter.class)
    @Column(name = "TS_RESPUESTA", nullable = false)
    private Instant tsRespuesta;

    @Column(name = "ACIERTO", nullable = false)
    private Boolean acierto;

    // Segundos en responder (puede ser nulo si es mayor a 2 minutos o hubo algún error)
    @Column(name = "SEGUNDOS_EN_RESPONDER")
    private Integer segundosEnResponder;
}

