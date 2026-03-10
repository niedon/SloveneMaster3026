package com.bcadaval.esloveno.structures.frase.frases;

import com.bcadaval.esloveno.beans.enums.*;
import com.bcadaval.esloveno.beans.palabra.NumeralFlexion;
import com.bcadaval.esloveno.beans.palabra.SustantivoFlexion;
import com.bcadaval.esloveno.services.palabra.NumeralService;
import com.bcadaval.esloveno.structures.DificultadFrase;
import com.bcadaval.esloveno.structures.extractores.ExtractorNumero;
import com.bcadaval.esloveno.structures.extractores.ExtractorSustantivo;
import com.bcadaval.esloveno.structures.frase.Frase;
import com.bcadaval.esloveno.structures.frase.PalabraFrase;
import com.bcadaval.esloveno.structures.frase.criterio.NumeralCriterioBuilder;
import com.bcadaval.esloveno.structures.frase.criterio.SustantivoCriterioBuilder;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Frase: Numeral (≥5) + Sustantivo en genitivo plural.
 * <p>
 * En esloveno, los numerales a partir de 5 rigen genitivo plural en el sustantivo.
 * El numeral va en nominativo ya que es una frase suelta tipo "5 perros".
 * <p>
 * Ejemplo: "pet psov" (5 perros), "šest hiš" (6 casas)
 * <p>
 * Huecos:
 * <ol>
 *   <li><strong>NUMERO</strong> (opcional): {@link NumeralFlexion} con cantidad ≥ 5, caso nominativo,
 *       género concordante con el sustantivo</li>
 *   <li><strong>SUSTANTIVO</strong> (obligatorio): {@link SustantivoFlexion} en genitivo plural</li>
 * </ol>
 * <p>
 * El sustantivo es el elemento nuclear del aprendizaje. El numeral es opcional:
 * si hay un numeral disponible para SRS se usa, si no, se genera por el generador.
 */
@Component
@DificultadFrase(NivelDificultad.INTERMEDIO)
public class FraseCantidadGrande extends Frase {

    @Autowired
    private NumeralService numeralService;

    @Override
    public String getIdentificador() {
        return "CANTIDAD_GRANDE";
    }

    @Override
    public String getNombreMostrar() {
        return "Cantidad grande (≥5 + sust. GEN PL)";
    }

    @PostConstruct
    public void configurarEstructura() {
        // Sustantivo en genitivo plural - elemento nuclear (obligatorio)
        PalabraFrase<SustantivoFlexion> sustantivo = PalabraFrase.<SustantivoFlexion>builder()
                .nombre("SUSTANTIVO")
                .criterio(SustantivoCriterioBuilder.crear()
                        .conCaso(Caso.GENITIVO)
                        .conNumero(Numero.PLURAL)
                        .build())
                .extractor(ExtractorSustantivo.get())
                .build();

        // Numeral ≥5 en nominativo (opcional, con generador)
        PalabraFrase<NumeralFlexion> numeral = PalabraFrase.<NumeralFlexion>builder()
                .nombre("NUMERO")
                .criterio(NumeralCriterioBuilder.crear()
                        .conCaso(Caso.NOMINATIVO)
                        .conCantidadMayorQue(4)
                        .build())
                .generador(sustantivo, sust -> numeralService.getNumeralGrande(sust))
                .extractor(ExtractorNumero.get())
                .build();

        agregarElemento(numeral);
        agregarElemento(sustantivo);
    }
}

