package com.bcadaval.esloveno.beans.palabra;

import com.bcadaval.esloveno.beans.base.Palabra;
import jakarta.persistence.Entity;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Numeral extends Palabra<NumeralFlexion> {

    /**
     * Representación numérica del numeral (ej. "en"→1, "dva"→2, "pet"→5).
     * Nullable: se asigna manualmente en la pantalla de completar palabras.
     */
    private Integer cantidad;

}
