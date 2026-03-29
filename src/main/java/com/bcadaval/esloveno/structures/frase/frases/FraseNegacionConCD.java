package com.bcadaval.esloveno.structures.frase.frases;

import com.bcadaval.esloveno.beans.enums.Caso;
import com.bcadaval.esloveno.beans.enums.FormaVerbal;
import com.bcadaval.esloveno.beans.enums.NivelDificultad;
import com.bcadaval.esloveno.beans.enums.Transitividad;
import com.bcadaval.esloveno.beans.palabra.*;
import com.bcadaval.esloveno.services.palabra.verbo.VerbosService;
import com.bcadaval.esloveno.structures.DificultadFrase;
import com.bcadaval.esloveno.structures.extractores.ExtractorVerbo;
import com.bcadaval.esloveno.structures.frase.Frase;
import com.bcadaval.esloveno.structures.frase.PalabraFrase;
import com.bcadaval.esloveno.structures.frase.criterio.VerboCriterioBuilder;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Frase: Pronombre + "ne" + Verbo transitivo presente + Numeral genitivo + Sustantivo genitivo.
 * <p>
 * En esloveno, la negación de un verbo transitivo con complemento directo cambia
 * el caso del CD de acusativo a genitivo (genitivo de negación).
 * <p>
 * Excluye biti/imeti/hoteti que tienen formas negativas especiales.
 * <p>
 * Ejemplo: "jaz ne vidim ene knjige" (yo no veo un libro)
 * <p>
 * Huecos:
 * <ol>
 *   <li><strong>PRONOMBRE</strong> (opcional): pronombre personal concordante con el verbo</li>
 *   <li><strong>PARTICULA</strong> (apoyo): partícula "ne"</li>
 *   <li><strong>VERBO</strong> (opcional): verbo transitivo en presente, con generador fallback</li>
 *   <li><strong>NUMERO</strong> (opcional): numeral en genitivo concordante con sustantivo</li>
 *   <li><strong>SUSTANTIVO</strong> (obligatorio): sustantivo en genitivo</li>
 * </ol>
 */
@Component
@DificultadFrase(NivelDificultad.INTERMEDIO)
public class FraseNegacionConCD extends Frase {

    @Autowired
    private VerbosService verbosService;

    @Override
    public String getIdentificador() {
        return "NEGACION_CON_CD";
    }

    @Override
    public String getNombreMostrar() {
        return "Negación con CD (ne + verbo + sust. GEN)";
    }

    @PostConstruct
    public void configurarEstructura() {
        // Sustantivo en genitivo - elemento nuclear (obligatorio)
        PalabraFrase<SustantivoFlexion> sustantivoCD = palabraFraseFactory.crearSustantivoAncla("SUSTANTIVO", Caso.GENITIVO);

        // Verbo transitivo en presente (opcional, con generador)
        PalabraFrase<VerboFlexion> verbo = PalabraFrase.<VerboFlexion>builder()
                .nombre("VERBO")
                .criterio(VerboCriterioBuilder.crear()
                        .conFormaVerbal(FormaVerbal.PRESENT)
                        .conTransitividad(Transitividad.TRANSITIVO, Transitividad.AMBITRANSITIVO)
                        .conNegativo(false)
                        .conPrincipalExcepto("biti", "imeti", "hoteti")
                        .build())
                .generador(() -> verbosService.getVerboTransitivoPresenteAleatorio(Arrays.asList("biti", "imeti", "hoteti")))
                .extractor(ExtractorVerbo.get())
                .build();

        // Pronombre (opcional, depende del verbo)
        PalabraFrase<PronombreFlexion> pronombre = palabraFraseFactory.crearPronombreParaVerboPresente("PRONOMBRE", verbo);

        // Partícula "ne" (apoyo)
        PalabraFrase<ParticulaFlexion> particula = palabraFraseFactory.crearParticulaNe("PARTICULA_NE", verbo);

        // Numeral en genitivo (opcional, depende del sustantivo)
        PalabraFrase<NumeralFlexion> numeral = palabraFraseFactory.crearNumeralOpcional("NUMERO", sustantivoCD, Caso.GENITIVO);

        agregarElemento(pronombre);
        agregarElemento(particula);
        agregarElemento(verbo);
        agregarElemento(numeral);
        agregarElemento(sustantivoCD);
    }
}

