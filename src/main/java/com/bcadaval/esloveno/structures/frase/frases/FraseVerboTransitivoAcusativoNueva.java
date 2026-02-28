package com.bcadaval.esloveno.structures.frase.frases;

import com.bcadaval.esloveno.beans.enums.Caso;
import com.bcadaval.esloveno.beans.enums.FormaVerbal;
import com.bcadaval.esloveno.beans.enums.NivelDificultad;
import com.bcadaval.esloveno.beans.enums.Transitividad;
import com.bcadaval.esloveno.beans.palabra.NumeralFlexion;
import com.bcadaval.esloveno.beans.palabra.PronombreFlexion;
import com.bcadaval.esloveno.beans.palabra.SustantivoFlexion;
import com.bcadaval.esloveno.beans.palabra.VerboFlexion;
import com.bcadaval.esloveno.services.palabra.NumeralService;
import com.bcadaval.esloveno.services.palabra.PronombreService;
import com.bcadaval.esloveno.services.palabra.verbo.VerbosService;
import com.bcadaval.esloveno.structures.DificultadFrase;
import com.bcadaval.esloveno.structures.extractores.ExtractorNumero;
import com.bcadaval.esloveno.structures.extractores.ExtractorPronombre;
import com.bcadaval.esloveno.structures.extractores.ExtractorSustantivo;
import com.bcadaval.esloveno.structures.extractores.ExtractorVerbo;
import com.bcadaval.esloveno.structures.frase.Frase;
import com.bcadaval.esloveno.structures.frase.PalabraFrase;
import com.bcadaval.esloveno.structures.frase.criterio.SustantivoCriterioBuilder;
import com.bcadaval.esloveno.structures.frase.criterio.VerboCriterioBuilder;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Frase: Pronombre + Verbo transitivo en presente + Número + Sustantivo en Acusativo.
 * <p>
 * Ejemplo: "jaz vidim 1 knjigo" (yo veo 1 libro)
 * <p>
 * Huecos:
 * <ol>
 *   <li><strong>PRONOMBRE</strong> (apoyo): generado a partir del verbo</li>
 *   <li><strong>VERBO</strong> (opcional): {@link VerboFlexion} transitivo en presente, con generador fallback</li>
 *   <li><strong>NUMERO</strong> (apoyo): numeral que concuerda con el sustantivo</li>
 *   <li><strong>CD</strong> (obligatorio): {@link SustantivoFlexion} con caso ACUSATIVO</li>
 * </ol>
 */
@Component
@DificultadFrase(NivelDificultad.ELEMENTAL)
public class FraseVerboTransitivoAcusativoNueva extends Frase {

    @Autowired
    private PronombreService pronombreService;

    @Autowired
    private NumeralService numeralService;

    @Autowired
    private VerbosService verbosService;

    @Override
    public String getIdentificador() {
        return "VERBO_TRANSITIVO_ACUSATIVO";
    }

    @Override
    public String getNombreMostrar() {
        return "Verbo (tr) + Sustantivo (ACU)";
    }

    @PostConstruct
    public void configurarEstructura() {
        PalabraFrase<SustantivoFlexion> cd = PalabraFrase.<SustantivoFlexion>builder()
                .nombre("CD")
                .criterio(SustantivoCriterioBuilder.crear()
                        .conCaso(Caso.ACUSATIVO)
                        .build())
                .extractor(ExtractorSustantivo.get())
                .build();

        PalabraFrase<VerboFlexion> verbo = PalabraFrase.<VerboFlexion>builder()
                .nombre("VERBO")
                .criterio(VerboCriterioBuilder.crear()
                        .conFormaVerbal(FormaVerbal.PRESENT)
                        .conTransitividad(Transitividad.TRANSITIVO)
                        .conNegativo(false)
                        .build())
                .generador(() -> verbosService.getVerboTransitivoPresenteAleatorio())
                .extractor(ExtractorVerbo.get())
                .build();

        PalabraFrase<PronombreFlexion> pronombre = PalabraFrase.<PronombreFlexion>builder()
                .nombre("PRONOMBRE")
                .generador(verbo, v -> pronombreService.getPronombre(v))
                .extractor(ExtractorPronombre.get())
                .build();

        PalabraFrase<NumeralFlexion> numero = PalabraFrase.<NumeralFlexion>builder()
                .nombre("NUMERO")
                .generador(cd, sust -> numeralService.getNumeral(sust))
                .extractor(ExtractorNumero.get())
                .build();

        agregarElemento(pronombre);
        agregarElemento(verbo);
        agregarElemento(numero);
        agregarElemento(cd);
    }
}
