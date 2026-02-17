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
 * Estructura de frase: Sustantivo en caso Dativo
 * <p>
 * Ejemplo: "prijatelju" (al amigo), "otroku" (al niño)
 * <p>
 * El dativo indica el objeto indirecto (a quién/para quién).
 * Se usa para:
 * - Objeto indirecto: dam knjigo prijatelju (doy el libro al amigo)
 * - Con verbos de comunicación: povem mu (le digo a él)
 * - Con ciertas preposiciones: k, proti
 * <p>
 * Importante para expresar destinatarios y direcciones hacia.
 * <p>
 * Elementos:
 * 1. NUMERO (apoyo): numeral que concuerda con el sustantivo
 * 2. SUSTANTIVO (slot): SustantivoFlexion con caso DATIVO
 */
@Component
@ExcluirDeFrases(razon = "Estructura excluida temporalmente")
public class FraseSoloSustantivoDativo extends EstructuraFrase {

    @Getter
    private final String identificador = "SOLO_SUSTANTIVO_DATIVO";
    @Getter
    private final String nombreMostrar = "Sustantivo (DAT)";

    public FraseSoloSustantivoDativo() {
        super();
    }

    @PostConstruct
    public void configurarEstructura() {
        // Definir slot de sustantivo en dativo
        ElementoFrase<SustantivoFlexion> sustantivo = ElementoFrase.<SustantivoFlexion>builder()
                .nombre("SUSTANTIVO")
                .criterio(CriterioBusqueda.de(SustantivoFlexion.class)
                        .con(CaracteristicaGramatical.CASO, Caso.DATIVO)
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
