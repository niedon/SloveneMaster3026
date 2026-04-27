package com.bcadaval.esloveno.structures;

import com.bcadaval.esloveno.beans.enums.NivelDificultad;
import com.bcadaval.esloveno.beans.enums.CategoriaFrase;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación para especificar el nivel de dificultad de una estructura de frase.
 * <p>
 * Esta anotación se aplica a las clases que extienden {@link com.bcadaval.esloveno.structures.frase.Frase}
 * para indicar el nivel de dificultad de la estructura.
 * <p>
 * Si una estructura no tiene esta anotación, se considera de nivel {@link NivelDificultad#PRINCIPIANTE}
 * por defecto.
 * <p>
 * Ejemplo:
 * <pre>
 * {@code
 * @Component
 * @DificultadFrase(categoria = CategoriaFrase.PRESENTE_SIMPLE)
 * public class FraseCompleja extends Frase {
 *     // ...
 * }
 * }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DificultadFrase {
    /**
     * Categoría descriptiva a la que pertenece esta frase.
     * Determina implícitamente el nivel de dificultad.
     * @return categoria descriptiva
     */
    CategoriaFrase categoria();
}
