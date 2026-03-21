package com.bcadaval.esloveno.beans.palabra;

import com.bcadaval.esloveno.beans.base.PalabraFlexion;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

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
@SuperBuilder
@Accessors(chain = true)
@Entity
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ParticulaFlexion extends PalabraFlexion<Particula> {

    /**
     * Referencia a la palabra base (partícula en forma principal).
     * Usa SLOLEKS_ID como clave foránea.
     */
    @ManyToOne
    @JoinColumn(name = "SLOLEKS_ID", nullable = false)
    private Particula particulaBase;

    @Override
    public void setPalabraBase(Particula palabra) {
        this.particulaBase = palabra;
    }

    @Override
    public Particula getPalabraBase() {
        return particulaBase;
    }

}
