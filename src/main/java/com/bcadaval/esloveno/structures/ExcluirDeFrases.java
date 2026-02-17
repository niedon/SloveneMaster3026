package com.bcadaval.esloveno.structures;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación para excluir una estructura de frase del registro automático.
 * <p>
 * Uso: Cuando una estructura de frase hereda de {@link EstructuraFrase} y está marcada
 * con {@code @Component} para inyección de dependencias, pero NO debe estar disponible
 * para los usuarios en la aplicación (por ejemplo, clases base, prototipos, o
 * estructuras en desarrollo).
 * <p>
 * Ejemplo:
 * <pre>
 * {@code
 * @Component
 * @ExcluirDeFrases
 * public class FraseEnDesarrollo extends EstructuraFrase {
 *     // ...
 * }
 * }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcluirDeFrases {
    /**
     * Razón opcional para la exclusión (útil para documentación).
     */
    String razon() default "";
}
