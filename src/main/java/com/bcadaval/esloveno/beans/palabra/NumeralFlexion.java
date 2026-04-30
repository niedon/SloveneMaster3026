package com.bcadaval.esloveno.beans.palabra;

import com.bcadaval.esloveno.beans.base.PalabraFlexion;
import com.bcadaval.esloveno.beans.enums.Caso;
import com.bcadaval.esloveno.beans.enums.Genero;
import com.bcadaval.esloveno.beans.enums.Numero;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Accessors(chain = true)
@Entity
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class NumeralFlexion extends PalabraFlexion<Numeral> {

    private Genero genero;

    private Numero numero;

    private Caso caso;

    /**
     * Referencia a la palabra base (numeral en forma principal)
     * Usa SLOLEKS_ID como clave foránea
     */
    @ManyToOne
    @JoinColumn(name = "SLOLEKS_ID", nullable = false)
    private Numeral numeralBase;

    /**
     * Delega a la palabra base para obtener la representación numérica del numeral.
     *
     * @return la cantidad (ej. 1 para "en", 2 para "dva"), o null si no asignada
     */
    @SuppressWarnings("unused")
    public Integer getCantidad() {
        return numeralBase.getCantidad();
    }

    @Override
    public void setPalabraBase(Numeral palabra) {
        this.numeralBase = palabra;
    }

    @Override
    public Numeral getPalabraBase() {
        return numeralBase;
    }

}
