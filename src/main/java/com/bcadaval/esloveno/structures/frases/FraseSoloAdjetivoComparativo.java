package com.bcadaval.esloveno.structures.frases;

import com.bcadaval.esloveno.beans.enums.CaracteristicaGramatical;
import com.bcadaval.esloveno.beans.enums.Caso;
import com.bcadaval.esloveno.beans.enums.Grado;
import com.bcadaval.esloveno.beans.palabra.AdjetivoFlexion;
import com.bcadaval.esloveno.beans.palabra.NumeralFlexion;
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
 * Estructura de frase: Adjetivo en grado Comparativo
 * <p>
 * Ejemplo: "lepši" (más hermoso), "večji" (más grande)
 * <p>
 * El comparativo en esloveno se forma típicamente con sufijos:
 * - lep → lepši (hermoso → más hermoso)
 * - velik → večji (grande → más grande)
 * <p>
 * Se usa en comparaciones:
 * - Ta hiša je večja kot tista (Esta casa es más grande que aquella)
 * <p>
 * Importante para nivel A2-B1 para expresar comparaciones.
 * <p>
 * Elementos:
 * 1. NUMERAL (apoyo): indica número gramatical
 * 2. ADJETIVO (slot): AdjetivoFlexion con grado COMPARATIVO en NOMINATIVO
 */
@Component
@ExcluirDeFrases(razon = "Estructura excluida temporalmente")
public class FraseSoloAdjetivoComparativo extends EstructuraFrase {

    @Getter
    private final String identificador = "SOLO_ADJETIVO_COMPARATIVO";
    @Getter
    private final String nombreMostrar = "Adjetivo (comparativo)";

    public FraseSoloAdjetivoComparativo() {
        super();
    }

    @PostConstruct
    public void configurarEstructura() {
        // Definir slot de adjetivo en nominativo y grado comparativo
        ElementoFrase<AdjetivoFlexion> adjetivo = ElementoFrase.<AdjetivoFlexion>builder()
                .nombre("ADJETIVO")
                .criterio(CriterioBusqueda.de(AdjetivoFlexion.class)
                        .con(CaracteristicaGramatical.CASO, Caso.NOMINATIVO)
                        .con(CaracteristicaGramatical.GRADO, Grado.COMPARATIVO)
                        .build())
                .extractor(ExtraccionSlotEstandar.get())
                .extractorAEsloveno(p ->
                        String.format("(%s/%s) %s",
                                p.getGenero().getEmoji(),
                                p.getNumero().getCode(),
                                p.getAcentuado()))
                .extractorAEspanol(p ->
                        String.format("(%s/%s) %s",
                                p.getGenero().getEmoji(),
                                p.getNumero().getCode(),
                                p.getSignificado()))
                .build();

        // Definir apoyo de numeral (indica el número gramatical)
        ElementoFrase<NumeralFlexion> numeral = ElementoFrase.<NumeralFlexion>builder()
                .nombre("NUMERAL")
                .generador(adjetivo, palabra -> numeralService.getNumeral((AdjetivoFlexion) palabra))
                .extractor(ExtraccionApoyoEstandar.get())
                .extractorDeEsloveno(p -> "nº")
                .build();

        // Agregar en orden de visualización
        agregarElemento(numeral);
        agregarElemento(adjetivo);
    }
}
