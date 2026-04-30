package com.bcadaval.esloveno.structures.frase.frases;

import com.bcadaval.esloveno.beans.enums.Caso;
import com.bcadaval.esloveno.beans.enums.CategoriaFrase;
import com.bcadaval.esloveno.beans.palabra.NumeralFlexion;
import com.bcadaval.esloveno.beans.palabra.SustantivoFlexion;
import com.bcadaval.esloveno.structures.DificultadFrase;
import com.bcadaval.esloveno.structures.frase.Frase;
import com.bcadaval.esloveno.structures.frase.PalabraFrase;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

/**
 * Frase: Numeral (1-4) + Sustantivo en Nominativo.
 * <p>
 * Ejemplo: "ena knjiga" (1 libro), "dve knjigi" (2 libros)
 * <p>
 * Huecos:
 * <ol>
 *   <li><strong>NUMERO</strong> (obligatorio, slot SRS): {@link NumeralFlexion} con caso NOMINATIVO
 *       y cantidad entre 1 y 4. El número y género se resuelven como dependencia del sustantivo:
 *       singular→1, dual→2, plural→3-4.</li>
 *   <li><strong>SUSTANTIVO</strong> (obligatorio, slot SRS): {@link SustantivoFlexion} con caso NOMINATIVO</li>
 * </ol>
 * <p>
 * Ambas palabras participan en SRS. El numeral depende del sustantivo para concordar
 * en número y género gramatical, y su cantidad se restringe según el número:
 * <ul>
 *   <li>Sustantivo singular → numeral con cantidad = 1</li>
 *   <li>Sustantivo dual → numeral con cantidad = 2</li>
 *   <li>Sustantivo plural → numeral con cantidad 3 o 4</li>
 * </ul>
 */
@Component
@DificultadFrase(categoria = CategoriaFrase.SUSTANTIVOS)
public class FraseSoloSustantivoNominativo extends Frase {

    @Override
    public String getIdentificador() {
        return "SOLO_SUSTANTIVO_NOMINATIVO";
    }

    @Override
    public String getNombreMostrar() {
        return "Sustantivos";
    }

    @PostConstruct
    public void configurarEstructura() {
        PalabraFrase<SustantivoFlexion> sustantivo =  palabraFraseFactory.crearSustantivoAncla("SUSTANTIVO", Caso.NOMINATIVO);

        PalabraFrase<NumeralFlexion> numeral = palabraFraseFactory.crearNumeralOpcional("NUMERO", sustantivo, Caso.NOMINATIVO);

        agregarElemento(numeral);
        agregarElemento(sustantivo);
    }
}
