package com.bcadaval.esloveno.beans.palabra;

import com.bcadaval.esloveno.beans.base.Palabra;
import jakarta.persistence.Entity;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

/**
 * Representa una partícula en esloveno (ej: "ne", "baje", "že").
 * <p>
 * Las partículas son palabras invariables que no se declinan ni conjugan,
 * pero pueden tener múltiples formas (wordForms) en el XML.
 * <p>
 * Campos:
 * <ul>
 *   <li>{@code sloleksId}: identificador único de Sloleks</li>
 *   <li>{@code principal}: forma principal (lema)</li>
 *   <li>{@code sloleksKey}: clave Sloleks</li>
 *   <li>{@code significado}: significado en español (se asigna manualmente)</li>
 *   <li>{@code subcategoria}: subcategoría del XML (ej: "pronunciation"), almacenada por si fuera útil</li>
 * </ul>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Particula extends Palabra<ParticulaFlexion> {

    /**
     * Subcategoría del XML (ej: "pronunciation").
     * Se almacena como String para futura extensión.
     */
    private String subcategoria;

}
