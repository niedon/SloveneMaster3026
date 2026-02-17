package com.bcadaval.esloveno.structures.frases;

import com.bcadaval.esloveno.beans.enums.CaracteristicaGramatical;
import com.bcadaval.esloveno.beans.enums.FormaVerbal;
import com.bcadaval.esloveno.beans.palabra.PronombreFlexion;
import com.bcadaval.esloveno.beans.palabra.VerboFlexion;
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
 * Estructura de frase: Verbo en participio pasado
 * <p>
 * Ejemplo: "jaz sem delal" (yo he trabajado/trabajé)
 * <p>
 * El pasado en esloveno se forma con el verbo "biti" (ser) + participio.
 * El participio concuerda en género y número con el sujeto:
 * - delal (masc. sing.), delala (fem. sing.), delalo (neut. sing.)
 * - delala (masc. dual), delali (fem./neut. dual)
 * - delali (masc. pl.), delale (fem. pl.), delala (neut. pl.)
 * <p>
 * Esta estructura es esencial para comunicación básica.
 * <p>
 * Elementos:
 * 1. PRONOMBRE (apoyo): pronombre personal que indica persona y número
 * 2. VERBO (slot): VerboFlexion con forma PARTICIPLE
 */
@Component
@ExcluirDeFrases(razon = "Estructura excluida temporalmente")
public class FraseVerboPasado extends EstructuraFrase {

    @Getter
    private final String identificador = "VERBO_PASADO";
    @Getter
    private final String nombreMostrar = "Verbo (pasado)";

    public FraseVerboPasado() {
        super();
    }

    @PostConstruct
    public void configurarEstructura() {
        // Definir slot de verbo en participio (pasado)
        ElementoFrase<VerboFlexion> verbo = ElementoFrase.<VerboFlexion>builder()
                .nombre("VERBO")
                .criterio(CriterioBusqueda.de(VerboFlexion.class)
                        .con(CaracteristicaGramatical.FORMA_VERBAL, FormaVerbal.PARTICIPLE)
                        .con(CaracteristicaGramatical.NEGATIVO, false)
                        .build())
                .extractor(ExtraccionSlotEstandar.get())
                .extractorDeEspanol(p ->
                        String.format("(%s) %s", p.getVerboBase().getAspecto().getEmoji(), p.getSignificado()))
                .extractorAEspanol(p ->
                        String.format("(%s) %s", p.getVerboBase().getAspecto().getEmoji(), p.getSignificado()))
                .extractorAEsloveno(p ->
                        String.format("(%s/%s) %s",
                                p.getGenero() != null ? p.getGenero().getEmoji() : "?",
                                p.getNumero() != null ? p.getNumero().getCode() : "?",
                                p.getAcentuado()))
                .build();

        // Definir apoyo de pronombre (depende del verbo)
        ElementoFrase<PronombreFlexion> pronombre = ElementoFrase.<PronombreFlexion>builder()
                .nombre("PRONOMBRE")
                .generador(verbo, palabra -> pronombreService.getPronombre((VerboFlexion) palabra))
                .extractor(ExtraccionApoyoEstandar.get())
                .extractorDeEsloveno(pf -> "")
                .build();

        // Agregar en orden de visualización
        agregarElemento(pronombre);
        agregarElemento(verbo);
    }
}
