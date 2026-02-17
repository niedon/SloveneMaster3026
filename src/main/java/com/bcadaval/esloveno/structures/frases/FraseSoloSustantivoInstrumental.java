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
 * Estructura de frase: Sustantivo en caso Instrumental
 * <p>
 * Ejemplo: "z avtom" (con el coche), "s prijateljem" (con el amigo)
 * <p>
 * El instrumental es esencial en esloveno para expresar:
 * - Instrumento o medio: pišem s svinčnikom (escribo con lápiz)
 * - Compañía: grem s teboj (voy contigo)
 * - Profesiones con "biti": sem učitelj (soy profesor, pero "delam za učitelja" con instrumental)
 * <p>
 * Las preposiciones principales son "s/z" (con).
 * <p>
 * Elementos:
 * 1. NUMERO (apoyo): numeral que concuerda con el sustantivo
 * 2. SUSTANTIVO (slot): SustantivoFlexion con caso INSTRUMENTAL
 */
@Component
@ExcluirDeFrases(razon = "Estructura excluida temporalmente")
public class FraseSoloSustantivoInstrumental extends EstructuraFrase {

    @Getter
    private final String identificador = "SOLO_SUSTANTIVO_INSTRUMENTAL";
    @Getter
    private final String nombreMostrar = "Sustantivo (INST)";

    public FraseSoloSustantivoInstrumental() {
        super();
    }

    @PostConstruct
    public void configurarEstructura() {
        // Definir slot de sustantivo en instrumental
        ElementoFrase<SustantivoFlexion> sustantivo = ElementoFrase.<SustantivoFlexion>builder()
                .nombre("SUSTANTIVO")
                .criterio(CriterioBusqueda.de(SustantivoFlexion.class)
                        .con(CaracteristicaGramatical.CASO, Caso.INSTRUMENTAL)
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
