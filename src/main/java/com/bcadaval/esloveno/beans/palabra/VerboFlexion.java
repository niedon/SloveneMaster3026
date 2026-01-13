package com.bcadaval.esloveno.beans.palabra;

import java.time.Instant;

import com.bcadaval.esloveno.beans.base.Palabra;
import com.bcadaval.esloveno.beans.base.PalabraFlexion;
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

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
@Entity
/**
 * Representa una flexión específica de un verbo en esloveno.
 * Contiene información sobre forma verbal, persona, número y género (para participios).
 */
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
        return getVerboBase().getSignificado();
    }

    @Override
    public void setPalabraBase(Verbo palabra) {
        this.verboBase = palabra;
    }
}

