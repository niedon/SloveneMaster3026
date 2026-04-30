package com.bcadaval.esloveno.structures.frase.frases;

import com.bcadaval.esloveno.beans.enums.CategoriaFrase;
import com.bcadaval.esloveno.beans.enums.FormaVerbal;
import com.bcadaval.esloveno.beans.palabra.PronombreFlexion;
import com.bcadaval.esloveno.beans.palabra.VerboFlexion;
import com.bcadaval.esloveno.structures.DificultadFrase;
import com.bcadaval.esloveno.structures.extractores.ExtractorVerbo;
import com.bcadaval.esloveno.structures.frase.Frase;
import com.bcadaval.esloveno.structures.frase.PalabraFrase;
import com.bcadaval.esloveno.structures.frase.criterio.VerboCriterioBuilder;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

/**
 * Frase: Pronombre + Verbo en presente.
 * <p>
 * Ejemplo: "yo corro" → "jaz tečem"
 * <p>
 * Huecos:
 * <ol>
 *   <li><strong>PRONOMBRE</strong> (apoyo): generado a partir del verbo para concordar en persona/número</li>
 *   <li><strong>VERBO</strong> (obligatorio): {@link VerboFlexion} con forma PRESENT y no negativo</li>
 * </ol>
 */
@Component
@DificultadFrase(categoria = CategoriaFrase.PRESENTE)
public class FraseSoloVerboPresente extends Frase {

    @Override
    public String getIdentificador() {
        return "SOLO_VERBO_PRESENTE";
    }

    @Override
    public String getNombreMostrar() {
        return "Verbos en presente";
    }

    @PostConstruct
    public void configurarEstructura() {
        PalabraFrase<VerboFlexion> verbo = PalabraFrase.<VerboFlexion>builder()
                .nombre("VERBO")
                .criterio(VerboCriterioBuilder.crear()
                        .conFormaVerbal(FormaVerbal.PRESENT)
                        .conNegativo(false)
                        .build())
                .extractor(ExtractorVerbo.get())
                .build();

        PalabraFrase<PronombreFlexion> pronombre = palabraFraseFactory.crearPronombreParaVerboPresente("PRONOMBRE", verbo);

        agregarElemento(pronombre);
        agregarElemento(verbo);
    }
}
