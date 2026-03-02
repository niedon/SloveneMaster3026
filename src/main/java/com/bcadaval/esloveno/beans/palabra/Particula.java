package com.bcadaval.esloveno.beans.palabra;

import com.bcadaval.esloveno.beans.base.Palabra;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.List;

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
@Builder
@Entity
@Accessors(chain = true)
@ToString
public class Particula implements Palabra<ParticulaFlexion> {

    @Id
    private String sloleksId;

    private String principal;

    private String sloleksKey;

    private String significado;

    /**
     * Subcategoría del XML (ej: "pronunciation").
     * Se almacena como String para futura extensión.
     */
    private String subcategoria;

    @Transient
    private List<ParticulaFlexion> listaFlexiones;
}

