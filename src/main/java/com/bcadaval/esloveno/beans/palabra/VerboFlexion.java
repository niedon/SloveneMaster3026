package com.bcadaval.esloveno.beans.palabra;

import com.bcadaval.esloveno.beans.base.PalabraFlexion;
import com.bcadaval.esloveno.beans.enums.FormaVerbal;
import com.bcadaval.esloveno.beans.enums.Genero;
import com.bcadaval.esloveno.beans.enums.Numero;
import com.bcadaval.esloveno.beans.enums.Persona;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

/**
 * Representa una flexión específica de un verbo en esloveno.
 * Contiene información sobre forma verbal, persona, número y género (para participios).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Entity
public class VerboFlexion extends PalabraFlexion<Verbo> {

    /** Forma verbal (infinitive, supine, participle, present, imperative) */
    private FormaVerbal formaVerbal;

    /** Persona gramatical (puede ser null para infinitivo, supino) */
    private Persona persona;

    /** Número gramatical (puede ser null para infinitivo) */
    private Numero numero;

    /** Género (solo aplica para participios) */
    private Genero genero;

    private Boolean negativo;

    /**
     * Referencia a la palabra base (verbo en infinitivo)
     * Usa SLOLEKS_ID como clave foránea
     */
    @ManyToOne
    @JoinColumn(name = "SLOLEKS_ID", nullable = false)
    private Verbo verboBase;

    @Override
    public void setPalabraBase(Verbo palabra) {
        this.verboBase = palabra;
    }

    @Override
    public Verbo getPalabraBase() {
        return verboBase;
    }

}

