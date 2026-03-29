package com.bcadaval.esloveno.structures.frase.frases;

import com.bcadaval.esloveno.beans.enums.FormaVerbal;
import com.bcadaval.esloveno.beans.enums.NivelDificultad;
import com.bcadaval.esloveno.beans.palabra.ParticulaFlexion;
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
 * Frase: Pronombre + Verbo especial en forma negativa (biti/imeti/hoteti).
 * <p>
 * Los verbos biti, imeti y hoteti tienen formas negativas especiales en esloveno
 * (ej: nisem, nimam, nočem) por lo que no usan la partícula "ne" separada.
 * <p>
 * Ejemplo: "jaz nisem" (yo no soy), "jaz nimam" (yo no tengo)
 * <p>
 * Huecos:
 * <ol>
 *   <li><strong>PRONOMBRE</strong> (opcional): pronombre personal nominativo concordante con el verbo</li>
 *   <li><strong>VERBO</strong> (obligatorio): verbo biti/imeti/hoteti en presente, forma negativa</li>
 * </ol>
 */
@Component
@DificultadFrase(NivelDificultad.PRINCIPIANTE)
public class FraseNegacionVerboEspecial extends Frase {

    @Override
    public String getIdentificador() {
        return "NEGACION_VERBO_ESPECIAL";
    }

    @Override
    public String getNombreMostrar() {
        return "Negación verbo especial (biti/imeti/hoteti)";
    }

    @PostConstruct
    public void configurarEstructura() {
        PalabraFrase<VerboFlexion> verbo = PalabraFrase.<VerboFlexion>builder()
                .nombre("VERBO")
                .criterio(VerboCriterioBuilder.crear()
                        .conFormaVerbal(FormaVerbal.PRESENT)
                        .conNegativo(true)
                        .conPrincipal("biti", "imeti", "hoteti")
                        .build())
                .extractor(ExtractorVerbo.get())
                .build();

        PalabraFrase<PronombreFlexion> pronombre = palabraFraseFactory.crearPronombreParaVerboPresente("PRONOMBRE", verbo);

        PalabraFrase<ParticulaFlexion> particula = palabraFraseFactory.crearParticulaNe("PARTICULA_NE", verbo);

        agregarElemento(pronombre);
        agregarElemento(particula);
        agregarElemento(verbo);
    }
}

