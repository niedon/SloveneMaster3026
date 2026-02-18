package com.bcadaval.esloveno.structures;

import com.bcadaval.esloveno.beans.enums.NivelDificultad;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación para especificar el nivel de dificultad de una estructura de frase.
 * <p>
 * Esta anotación se aplica a las clases que extienden {@link EstructuraFrase}
 * para indicar el nivel de dificultad de la estructura.
 * <p>
 * Si una estructura no tiene esta anotación, se considera de nivel {@link NivelDificultad#PRINCIPIANTE}
 * por defecto.
 * <p>
 * Ejemplo:
 * <pre>
 * {@code
 * @Component
 * @DificultadFrase(NivelDificultad.INTERMEDIO)
 * public class FraseCompleja extends EstructuraFrase {
 *     // ...
 * }
 * }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DificultadFrase {
    /**
     * El nivel de dificultad de la estructura de frase.
     * @return nivel de dificultad
     */
    NivelDificultad value() default NivelDificultad.PRINCIPIANTE;
}

