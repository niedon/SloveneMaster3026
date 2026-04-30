package com.bcadaval.esloveno.beans.palabra;

import com.bcadaval.esloveno.beans.base.PalabraFlexion;
import com.bcadaval.esloveno.beans.enums.Caso;
import com.bcadaval.esloveno.beans.enums.Genero;
import com.bcadaval.esloveno.beans.enums.Numero;
import com.bcadaval.esloveno.beans.enums.Persona;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PronombreFlexion extends PalabraFlexion<Pronombre> {

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

    private String significado;

    /**
     * Referencia a la palabra base (pronombre en forma principal)
     * Usa SLOLEKS_ID como clave foránea
     */
    @ManyToOne
    @JoinColumn(name = "SLOLEKS_ID", nullable = false)
    private Pronombre pronombreBase;

    @Override
    public void setPalabraBase(Pronombre palabra) {
        this.pronombreBase = palabra;
    }

    @Override
    public Pronombre getPalabraBase() {
        return pronombreBase;
    }

    @Override
    public String getSignificado() {
        return significado;
    }

}
