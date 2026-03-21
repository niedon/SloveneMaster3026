package com.bcadaval.esloveno.beans.base;

import java.time.Instant;
import com.bcadaval.esloveno.config.InstantConverter;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Clase base abstracta para todas las flexiones.
 * Centraliza la identificación, campos de texto comunes y el sistema SRS.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass // Indica a JPA que estos campos deben incluirse en las tablas hijas
public abstract class PalabraFlexion<T extends Palabra<?>> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Integer id;

    // Solo lectura, el mapeo real de la relación está en la clase hija
    @Column(name = "SLOLEKS_ID", insertable = false, updatable = false)
    protected String sloleksId;

    protected String principal;
    protected String flexion;
    protected String acentuado;
    protected String pronunciacionIpa;
    protected String pronunciacionSampa;

    // =====================================================
    // Campos SRS (Sistema de Repetición Espaciada)
    // =====================================================
    
    @Builder.Default
    protected Double factorFacilidad = 2.5;

    @Builder.Default
    protected Long intervaloRepeticionSegundos = 0L;

    @Builder.Default
    protected Integer vecesConsecutivasCorrectas = 0;

    @Convert(converter = InstantConverter.class)
    protected Instant ultimaRevision;

    @Convert(converter = InstantConverter.class)
    protected Instant proximaRevision;

    @Builder.Default
    protected Integer totalRevisiones = 0;

    @Builder.Default
    protected Integer totalAciertos = 0;

    @Builder.Default
    protected Boolean enReaprendizaje = false;

    @Builder.Default
    protected Boolean elegible = false;

    // =====================================================
    // Métodos Abstractos
    // =====================================================

    // Estos métodos dependen de la relación específica (@ManyToOne) en la clase hija
    public String getSignificado() {
        return getPalabraBase().getSignificado();
    }
    
    public abstract void setPalabraBase(T palabra);

    public abstract T getPalabraBase();
}
