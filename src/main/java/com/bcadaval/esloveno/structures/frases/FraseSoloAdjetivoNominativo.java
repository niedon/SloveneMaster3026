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
 * Estructura de frase: Adjetivo solo en caso Nominativo
 * <p>
 * Ejemplo: "lep" (hermoso), "velik" (grande)
 * <p>
 * Esta estructura permite practicar la concordancia de adjetivos
 * en género, número y caso sin la complejidad adicional del sustantivo.
 * Ideal para nivel A1-A2 para memorizar las terminaciones básicas.
 * <p>
 * Elementos:
 * 1. NUMERO (apoyo): numeral que indica el número gramatical
 * 2. ADJETIVO (slot): AdjetivoFlexion con caso NOMINATIVO y grado POSITIVO
 */
@Component
@ExcluirDeFrases(razon = "Estructura excluida temporalmente")
public class FraseSoloAdjetivoNominativo extends EstructuraFrase {

    @Getter
    private final String identificador = "SOLO_ADJETIVO_NOMINATIVO";
    @Getter
    private final String nombreMostrar = "Adjetivo (NOM)";

    public FraseSoloAdjetivoNominativo() {
        super();
    }

    @PostConstruct
    public void configurarEstructura() {
        // Definir slot de adjetivo en nominativo y grado positivo
        ElementoFrase<AdjetivoFlexion> adjetivo = ElementoFrase.<AdjetivoFlexion>builder()
                .nombre("ADJETIVO")
                .criterio(CriterioBusqueda.de(AdjetivoFlexion.class)
                        .con(CaracteristicaGramatical.CASO, Caso.NOMINATIVO)
                        .con(CaracteristicaGramatical.GRADO, Grado.POSITIVO)
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
