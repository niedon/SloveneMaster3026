package com.bcadaval.esloveno.beans.palabra;

import java.time.Instant;

import com.bcadaval.esloveno.beans.base.PalabraFlexion;
import com.bcadaval.esloveno.beans.enums.CaracteristicaGramatical;
import com.bcadaval.esloveno.beans.enums.FormaVerbal;
import com.bcadaval.esloveno.beans.enums.Genero;
import com.bcadaval.esloveno.beans.enums.Numero;
import com.bcadaval.esloveno.beans.enums.Persona;
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
import lombok.experimental.Accessors;

/**
 * Representa una flexión específica de un verbo en esloveno.
 * Contiene información sobre forma verbal, persona, número y género (para participios).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
@Entity
public class VerboFlexion implements PalabraFlexion<Verbo> {

    /**
     * ID único autoincrementado de la flexión
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "PRINCIPAL", insertable = false, updatable = false)
    private String principal;

    /** Forma verbal (infinitive, supine, participle, present, imperative) */
    private FormaVerbal formaVerbal;

    /** Persona gramatical (puede ser null para infinitivo, supino) */
    private Persona persona;

    /** Número gramatical (puede ser null para infinitivo) */
    private Numero numero;

    /** Género (solo aplica para participios) */
    private Genero genero;

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
     * Referencia a la palabra base (verbo en infinitivo)
     * Usa PRINCIPAL como clave foránea
     */
    @ManyToOne
    @JoinColumn(name = "PRINCIPAL", nullable = false)
    private Verbo verboBase;

    public String getSignificado() {
        return verboBase.getSignificado();
    }

    @Override
    public void setPalabraBase(Verbo palabra) {
        this.verboBase = palabra;
    }

    @Override
    public Object getCaracteristica(CaracteristicaGramatical caracteristica) {
        return switch (caracteristica) {
            case FORMA_VERBAL -> this.formaVerbal;
            case PERSONA -> this.persona;
            case NUMERO -> this.numero;
            case GENERO -> this.genero;
            case TRANSITIVIDAD -> verboBase.getTransitividad();
            // Características que no aplican a verbos
            case CASO, GRADO, DEFINITUD, TIPO_NUMERAL -> null;
        };
    }

    @Override
    public String toString() {
        return String.format("VerboFlexion[id=%d, principal='%s', flexion='%s', formaVerbal=%s, persona=%s, numero=%s, genero=%s]",
                id, principal, flexion, formaVerbal, persona, numero, genero);
    }
}

