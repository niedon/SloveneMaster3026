package com.bcadaval.esloveno.structures.frases;

import com.bcadaval.esloveno.beans.enums.CaracteristicaGramatical;
import com.bcadaval.esloveno.beans.enums.Caso;
import com.bcadaval.esloveno.beans.palabra.NumeralFlexion;
import com.bcadaval.esloveno.beans.palabra.SustantivoFlexion;
import com.bcadaval.esloveno.structures.CriterioBusqueda;
import com.bcadaval.esloveno.structures.ElementoFrase;
import com.bcadaval.esloveno.structures.EstructuraFrase;
import com.bcadaval.esloveno.structures.ExcluirDeFrases;
import com.bcadaval.esloveno.structures.extractores.ExtraccionApoyoEstandar;
import com.bcadaval.esloveno.structures.extractores.ExtraccionSlotEstandar;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.stereotype.Component;

/**
 * Estructura de frase: Sustantivo en caso Locativo
 * <p>
 * Ejemplo: "v mestu" (en la ciudad), "na mizi" (sobre la mesa)
 * <p>
 * El locativo se usa con preposiciones como "v" (en) y "na" (sobre/en),
 * fundamental para expresar ubicación en esloveno.
 * <p>
 * Elementos:
 * 1. NUMERO (apoyo): numeral que concuerda con el sustantivo
 * 2. SUSTANTIVO (slot): SustantivoFlexion con caso LOCATIVO
 */
@Component
@ExcluirDeFrases(razon = "Estructura excluida temporalmente")
public class FraseSoloSustantivoLocativo extends EstructuraFrase {

    @Getter
    private final String identificador = "SOLO_SUSTANTIVO_LOCATIVO";
    @Getter
    private final String nombreMostrar = "Sustantivo (LOC)";

    public FraseSoloSustantivoLocativo() {
        super();
    }

    @PostConstruct
    public void configurarEstructura() {
        // Definir slot de sustantivo en locativo
        ElementoFrase<SustantivoFlexion> sustantivo = ElementoFrase.<SustantivoFlexion>builder()
                .nombre("SUSTANTIVO")
                .criterio(CriterioBusqueda.de(SustantivoFlexion.class)
                        .con(CaracteristicaGramatical.CASO, Caso.LOCATIVO)
                        .build())
                .extractor(ExtraccionSlotEstandar.get())
                .extractorAEsloveno(p ->
                        String.format("(%s) %s", p.getSustantivoBase().getGenero().getEmoji(), p.getAcentuado()))
                .extractorAEspanol(p ->
                        String.format("(%s) %s", p.getSustantivoBase().getGenero().getEmoji(), p.getSignificado()))
                .build();

        // Definir apoyo de número (depende del sustantivo)
        ElementoFrase<NumeralFlexion> numero = ElementoFrase.<NumeralFlexion>builder()
                .nombre("NUMERO")
                .generador(sustantivo, palabra -> numeralService.getNumeral((SustantivoFlexion) palabra))
                .extractor(ExtraccionApoyoEstandar.get())
                .extractorDeEsloveno(p -> "nº")
                .build();

        // Agregar en orden de visualización
        agregarElemento(numero);
        agregarElemento(sustantivo);
    }
}
