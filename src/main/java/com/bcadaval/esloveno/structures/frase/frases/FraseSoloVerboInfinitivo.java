package com.bcadaval.esloveno.structures.frase.frases;

import com.bcadaval.esloveno.beans.enums.FormaVerbal;
import com.bcadaval.esloveno.beans.enums.NivelDificultad;
import com.bcadaval.esloveno.beans.palabra.VerboFlexion;
import com.bcadaval.esloveno.structures.DificultadFrase;
import com.bcadaval.esloveno.structures.extractores.ExtractorVerbo;
import com.bcadaval.esloveno.structures.frase.Frase;
import com.bcadaval.esloveno.structures.frase.PalabraFrase;
import com.bcadaval.esloveno.structures.frase.criterio.VerboCriterioBuilder;
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
@DificultadFrase(NivelDificultad.PRINCIPIANTE)
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
        PalabraFrase<VerboFlexion> verbo = PalabraFrase.<VerboFlexion>builder()
                .nombre("VERBO")
                .criterio(VerboCriterioBuilder.crear()
                        .conFormaVerbal(FormaVerbal.INFINITIVE)
                        .build())
                .extractor(ExtractorVerbo.get())
                .build();

        agregarElemento(verbo);
    }
}
