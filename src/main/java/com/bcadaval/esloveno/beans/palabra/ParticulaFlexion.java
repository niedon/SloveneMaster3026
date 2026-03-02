package com.bcadaval.esloveno.beans.palabra;

import com.bcadaval.esloveno.beans.base.PalabraFlexion;
import com.bcadaval.esloveno.config.InstantConverter;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;

import java.time.Instant;

/**
 * Representa una flexión de una partícula en esloveno.
 * <p>
 * Aunque las partículas suelen ser invariables (una sola forma),
 * la estructura soporta múltiples flexiones por coherencia con el resto del sistema.
 * <p>
 * Incluye todos los campos SRS para participar en el sistema de repetición espaciada.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
@Entity
@ToString
public class ParticulaFlexion implements PalabraFlexion<Particula> {

    /**
     * ID único autoincrementado de la flexión
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "SLOLEKS_ID", insertable = false, updatable = false)
    private String sloleksId;

    private String principal;

    private String flexion;

    private String acentuado;

    private String pronunciacionIpa;
    private String pronunciacionSampa;

    // =====================================================
    // Campos del Sistema de Repetición Espaciada (SRS)
    // =====================================================

    @Builder.Default
    private Double factorFacilidad = 2.5;

    @Builder.Default
    private Long intervaloRepeticionSegundos = 0L;

    @Builder.Default
    private Integer vecesConsecutivasCorrectas = 0;

    @Convert(converter = InstantConverter.class)
    private Instant ultimaRevision;

    @Convert(converter = InstantConverter.class)
    private Instant proximaRevision;

    @Builder.Default
    private Integer totalRevisiones = 0;

    @Builder.Default
    private Integer totalAciertos = 0;

    @Builder.Default
    private Boolean enReaprendizaje = false;

    // =====================================================
    // Fin campos SRS
    // =====================================================

    /**
     * Referencia a la palabra base (partícula en forma principal).
     * Usa SLOLEKS_ID como clave foránea.
     */
    @ManyToOne
    @JoinColumn(name = "SLOLEKS_ID", nullable = false)
    private Particula particulaBase;

    public String getSignificado() {
        return getParticulaBase().getSignificado();
    }

    @Override
    public void setPalabraBase(Particula palabra) {
        this.particulaBase = palabra;
    }
}

