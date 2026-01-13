package com.bcadaval.esloveno.beans.palabra;

import com.bcadaval.esloveno.beans.base.Palabra;
import com.bcadaval.esloveno.beans.base.PalabraFlexion;
import com.bcadaval.esloveno.beans.enums.Caso;
import com.bcadaval.esloveno.beans.enums.Genero;
import com.bcadaval.esloveno.beans.enums.Numero;
import com.bcadaval.esloveno.beans.enums.Persona;
import com.bcadaval.esloveno.config.InstantConverter;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Accessors(chain = true)
public class PronombreFlexion implements PalabraFlexion<Pronombre> {

    /**
     * ID único autoincrementado de la flexión
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "PRINCIPAL", insertable = false, updatable = false)
    private String principal;

    /** Persona gramatical (puede ser null para pronombres sin persona) */
    private Persona persona;

    /** Género (puede ser null para pronombres que aplican a todos los géneros) */
    private Genero genero;

    /** Número (puede ser null) */
    private Numero numero;

    /** Caso (puede ser null) */
    private Caso caso;

    /** Clítico: true=sí, false=no, null=no especificado */
    private Boolean clitico;

    private String flexion;

    private String acentuado;

    private String significado;

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
     * Referencia a la palabra base (sustantivo en forma principal)
     * Usa PRINCIPAL como clave foránea
     */
    @ManyToOne
    @JoinColumn(name = "PRINCIPAL", nullable = false)
    private Pronombre pronombreBase;

//    public String getSignificado() {
//        return getPronombreBase().getSignificado();
//    }

    @Override
    public void setPalabraBase(Pronombre palabra) {
        this.pronombreBase = palabra;
    }

}
