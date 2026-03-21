package com.bcadaval.esloveno.beans.palabra;

import com.bcadaval.esloveno.beans.base.PalabraFlexion;
import com.bcadaval.esloveno.beans.enums.*;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

/**
 * Representa una flexión específica de un adjetivo en esloveno.
 * Contiene información sobre género, número, caso y grado.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Entity
public class AdjetivoFlexion extends PalabraFlexion<Adjetivo> {

    private Genero genero;

    private Numero numero;

    private Caso caso;

    private Grado grado;

    private Definitud definitud;

    /**
     * Referencia a la palabra base (adjetivo en forma principal)
     * Usa SLOLEKS_ID como clave foránea
     */
    @ManyToOne
    @JoinColumn(name = "SLOLEKS_ID", nullable = false)
    private Adjetivo adjetivoBase;

    @Override
    public void setPalabraBase(Adjetivo palabra) {
        this.adjetivoBase = palabra;
    }

    @Override
    public Adjetivo getPalabraBase() {
        return adjetivoBase;
    }


}
