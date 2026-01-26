package com.bcadaval.esloveno.beans.palabra;

import java.time.Instant;

import com.bcadaval.esloveno.beans.base.PalabraFlexion;
import com.bcadaval.esloveno.beans.enums.CaracteristicaGramatical;
import com.bcadaval.esloveno.beans.enums.*;
import com.bcadaval.esloveno.config.InstantConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * Representa una flexión específica de un adjetivo en esloveno.
 * Contiene información sobre género, número, caso y grado.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
@Entity
@ToString
public class AdjetivoFlexion implements PalabraFlexion<Adjetivo> {

    /**
     * ID único autoincrementado de la flexión
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "SLOLEKS_ID", insertable = false, updatable = false)
    private String sloleksId;

    private String principal;

    private Genero genero;

    private Numero numero;

    private Caso caso;

    private Grado grado;

    private Definitud definitud;

    private String flexion;

    private String acentuado;

    private String pronunciacionIpa;
    private String pronunciacionSampa;

    // =====================================================
    // Campos del Sistema de Repetición Espaciada (SRS)
    // =====================================================

    /**
     * Factor de facilidad (Ease Factor).
     * Determina qué tan fácil es recordar esta tarjeta.
     * Valor inicial: 2.5, rango: [1.3, 2.5]
     */
    @Builder.Default
    private Double factorFacilidad = 2.5;

    /**
     * Intervalo de repetición en SEGUNDOS.
     * Segundos hasta la próxima revisión.
     */
    @Builder.Default
    private Long intervaloRepeticionSegundos = 0L;

    /**
     * Número de veces consecutivas que se ha recordado correctamente.
     * Se resetea a 0 si fallas.
     */
    @Builder.Default
    private Integer vecesConsecutivasCorrectas = 0;

    /**
     * Fecha y hora exacta de la última revisión.
     */
    @Convert(converter = InstantConverter.class)
    private Instant ultimaRevision;

    /**
     * Fecha y hora exacta de la próxima revisión programada.
     * NULL = tarjeta nueva (nunca estudiada)
     */
    @Convert(converter = InstantConverter.class)
    private Instant proximaRevision;

    /**
     * Número total de revisiones realizadas (estadística).
     */
    @Builder.Default
    private Integer totalRevisiones = 0;

    /**
     * Número total de aciertos (estadística).
     */
    @Builder.Default
    private Integer totalAciertos = 0;

    /**
     * Indica si la tarjeta está en proceso de reaprendizaje.
     * true = falló recientemente y está en reaprendizaje
     * false = tarjeta normal
     */
    @Builder.Default
    private Boolean enReaprendizaje = false;

    // =====================================================
    // Fin campos SRS
    // =====================================================

    /**
     * Referencia a la palabra base (adjetivo en forma principal)
     * Usa SLOLEKS_ID como clave foránea
     */
    @ManyToOne
    @JoinColumn(name = "SLOLEKS_ID", nullable = false)
    private Adjetivo adjetivoBase;

    public String getSignificado() {
        return this.adjetivoBase.getSignificado();
    }

    @Override
    public void setPalabraBase(Adjetivo palabra) {
        this.adjetivoBase = palabra;
    }

    @Override
    public Object getCaracteristica(CaracteristicaGramatical caracteristica) {
        return switch (caracteristica) {
            case CASO -> this.caso;
            case GENERO -> this.genero;
            case NUMERO -> this.numero;
            case GRADO -> this.grado;
            case DEFINITUD -> this.definitud;
            case FORMA_VERBAL, PERSONA, TRANSITIVIDAD, TIPO_NUMERAL, NEGATIVO -> null;
        };
    }

}
