package com.bcadaval.esloveno.structures.frase.frases;

import com.bcadaval.esloveno.beans.enums.CategoriaFrase;
import com.bcadaval.esloveno.beans.enums.FormaVerbal;
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
 * Frase: Pronombre + Partícula "ne" + Verbo conjugado en presente (negación estándar).
 * <p>
 * Excluye los verbos biti, imeti y hoteti, que tienen formas negativas especiales.
 * <p>
 * Ejemplo: "jaz ne tečem" (yo no corro)
 * <p>
 * Huecos:
 * <ol>
 *   <li><strong>PRONOMBRE</strong> (opcional): pronombre personal nominativo concordante con el verbo</li>
 *   <li><strong>PARTICULA</strong> (apoyo): partícula "ne"</li>
 *   <li><strong>VERBO</strong> (obligatorio): verbo en presente, no negativo, excluyendo biti/imeti/hoteti</li>
 * </ol>
 */
@Component
@DificultadFrase(categoria = CategoriaFrase.PRESENTE_NEGADO)
public class FraseNegacionVerbo extends Frase {

    @Override
    public String getIdentificador() {
        return "NEGACION_VERBO";
    }

    @Override
    public String getNombreMostrar() {
        return "Negación verbo (ne + verbo)";
    }

    @PostConstruct
    public void configurarEstructura() {
        PalabraFrase<VerboFlexion> verbo = PalabraFrase.<VerboFlexion>builder()
                .nombre("VERBO")
                .criterio(VerboCriterioBuilder.crear()
                        .conFormaVerbal(FormaVerbal.PRESENT)
                        .conNegativo(false)
                        .conPrincipalExcepto("biti", "imeti", "hoteti")
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

