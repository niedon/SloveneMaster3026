package com.bcadaval.esloveno.structures.frase.frases;

import com.bcadaval.esloveno.beans.enums.CategoriaFrase;
import com.bcadaval.esloveno.beans.enums.NivelDificultad;
import com.bcadaval.esloveno.beans.palabra.VerboFlexion;
import com.bcadaval.esloveno.structures.DificultadFrase;
import com.bcadaval.esloveno.structures.frase.Frase;
import com.bcadaval.esloveno.structures.frase.PalabraFrase;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

/**
 * Frase: Verbo en Infinitivo.
 * <p>
 * Ejemplo: "delati" (trabajar), "gledati" (mirar)
 * <p>
 * Huecos:
 * <ol>
 *   <li><strong>VERBO</strong> (obligatorio): {@link VerboFlexion} con forma INFINITIVE</li>
 * </ol>
 */
@Component
@DificultadFrase(categoria = CategoriaFrase.VERBOS)
public class FraseSoloVerboInfinitivo extends Frase {

    @Override
    public String getIdentificador() {
        return "SOLO_VERBO_INFINITIVO";
    }

    @Override
    public String getNombreMostrar() {
        return "Verbos en infinitivo";
    }

    @PostConstruct
    public void configurarEstructura() {
        PalabraFrase<VerboFlexion> verbo = palabraFraseFactory.crearVerboInfinitivoAncla();

        agregarElemento(verbo);
    }
}
