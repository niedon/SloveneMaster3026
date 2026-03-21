package com.bcadaval.esloveno.beans.palabra;

import com.bcadaval.esloveno.beans.base.PalabraFlexion;
import com.bcadaval.esloveno.beans.enums.Caso;
import com.bcadaval.esloveno.beans.enums.Numero;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

/**
 * Representa una flexión específica de un sustantivo en esloveno.
 * Contiene información sobre número y caso.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Entity
public class SustantivoFlexion extends PalabraFlexion<Sustantivo> {

    private Numero numero;

    private Caso caso;

    @ManyToOne
    @JoinColumn(name = "SLOLEKS_ID", nullable = false)
    private Sustantivo sustantivoBase;

    @Override
    public void setPalabraBase(Sustantivo palabra) {
        this.sustantivoBase = palabra;
    }

    @Override
    public Sustantivo getPalabraBase() {
        return sustantivoBase;
    }


}
