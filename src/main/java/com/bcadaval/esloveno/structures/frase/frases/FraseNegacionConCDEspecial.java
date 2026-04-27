package com.bcadaval.esloveno.structures.frase.frases;

import com.bcadaval.esloveno.beans.enums.*;
import com.bcadaval.esloveno.beans.palabra.*;
import com.bcadaval.esloveno.structures.DificultadFrase;
import com.bcadaval.esloveno.structures.extractores.ExtractorVerbo;
import com.bcadaval.esloveno.structures.frase.Frase;
import com.bcadaval.esloveno.structures.frase.PalabraFrase;
import com.bcadaval.esloveno.structures.frase.criterio.VerboCriterioBuilder;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

/**
 * Frase: Pronombre + Verbo especial negativo (biti/imeti/hoteti) + Numeral genitivo + Sustantivo genitivo.
 * <p>
 * Versión para los verbos con formas negativas especiales (nisem, nimam, nočem).
 * No usa la partícula "ne" separada.
 * <p>
 * Ejemplo: "jaz nimam ene knjige" (yo no tengo un libro)
 * <p>
 * Huecos:
 * <ol>
 *   <li><strong>PRONOMBRE</strong> (opcional): pronombre personal concordante con el verbo</li>
 *   <li><strong>VERBO</strong> (obligatorio): biti/imeti/hoteti en presente, forma negativa</li>
 *   <li><strong>NUMERO</strong> (opcional): numeral en genitivo concordante con sustantivo</li>
 *   <li><strong>SUSTANTIVO</strong> (obligatorio): sustantivo en genitivo</li>
 * </ol>
 */
@Component
@DificultadFrase(categoria = CategoriaFrase.NEGACION_PRESENTE_CON_CD)
public class FraseNegacionConCDEspecial extends Frase {

    @Override
    public String getIdentificador() {
        return "NEGACION_CON_CD_ESPECIAL";
    }

    @Override
    public String getNombreMostrar() {
        return "Negación con CD especial (biti/imeti/hoteti + sust. GEN)";
    }

    @PostConstruct
    public void configurarEstructura() {
        // Sustantivo en genitivo - elemento nuclear (obligatorio)
        PalabraFrase<SustantivoFlexion> sustantivoCD =  palabraFraseFactory.crearSustantivoAncla("SUSTANTIVO", Caso.GENITIVO);

        // Verbo especial negativo (obligatorio)
        PalabraFrase<VerboFlexion> verbo = PalabraFrase.<VerboFlexion>builder()
                .nombre("VERBO")
                .criterio(VerboCriterioBuilder.crear()
                        .conFormaVerbal(FormaVerbal.PRESENT)
                        .conTransitividad(Transitividad.TRANSITIVO, Transitividad.AMBITRANSITIVO)
                        .conNegativo(true)
                        .conPrincipal(/*"biti", */"imeti", "hoteti")
                        .build())
                .extractor(ExtractorVerbo.get())
                .build();

        // Pronombre (opcional, depende del verbo)
        PalabraFrase<PronombreFlexion> pronombre = palabraFraseFactory.crearPronombreParaVerboPresente("PRONOMBRE", verbo);

        // Numeral en genitivo (opcional, depende del sustantivo)
        PalabraFrase<NumeralFlexion> numeral = palabraFraseFactory.crearNumeralOpcional("NUMERO", sustantivoCD, Caso.GENITIVO);


        // Partícula "ne" (apoyo)
        PalabraFrase<ParticulaFlexion> particula = palabraFraseFactory.crearParticulaNe("PARTICULA_NE", verbo);

        agregarElemento(pronombre);
        agregarElemento(particula);
        agregarElemento(verbo);
        agregarElemento(numeral);
        agregarElemento(sustantivoCD);
    }
}

