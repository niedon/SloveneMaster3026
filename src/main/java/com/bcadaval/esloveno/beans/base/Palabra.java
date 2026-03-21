package com.bcadaval.esloveno.beans.base;

import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * Representa una palabra genérica en esloveno. La palabra tendrá varias formas que dependan de las categorías
 * gramaticales como Aspecto, Caso, Género, Grado, Número, Persona o Transitividad.
 *
 * @param <E> Clase que representa la flexión de la palabra.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@MappedSuperclass
public abstract class Palabra<E extends PalabraFlexion<?>> {

    /**
     * Obtiene el identificador único de Sloleks.
     */
    @Id
    protected String sloleksId;

    /**
     * Obtiene la forma principal de la palabra.
     */
    protected String principal;

    protected String sloleksKey;

    protected String significado;

    /**
     * Obtiene la lista de flexiones asociadas a la palabra.
     */
    @Transient
    protected List<E> listaFlexiones;

}
