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
 * Estructura de frase: Verbo en modo Imperativo
 * <p>
 * Ejemplo: "govori!" (¡habla!), "pojdi!" (¡ve!)
 * <p>
 * El imperativo en esloveno es fundamental para:
 * - Dar órdenes: pojdi! (¡ve!)
 * - Instrucciones: preberi! (¡lee!)
 * - Peticiones corteses: prosim, počakaj! (¡por favor, espera!)
 * <p>
 * Existe en singular, dual y plural (2ª persona principalmente).
 * <p>
 * Elementos:
 * 1. PRONOMBRE (apoyo): pronombre que indica la persona gramatical (ti/vidva/vi)
 * 2. VERBO (slot): VerboFlexion con forma IMPERATIVE
 */
@Component
@ExcluirDeFrases(razon = "Estructura excluida temporalmente")
public class FraseSoloVerboImperativo extends EstructuraFrase {

    @Getter
    private final String identificador = "SOLO_VERBO_IMPERATIVO";
    @Getter
    private final String nombreMostrar = "Verbo (imperativo)";

    public FraseSoloVerboImperativo() {
        super();
    }

    @PostConstruct
    public void configurarEstructura() {
        // Definir slot de verbo en imperativo
        ElementoFrase<VerboFlexion> verbo = ElementoFrase.<VerboFlexion>builder()
                .nombre("VERBO")
                .criterio(CriterioBusqueda.de(VerboFlexion.class)
                        .con(CaracteristicaGramatical.FORMA_VERBAL, FormaVerbal.IMPERATIVE)
                        .con(CaracteristicaGramatical.NEGATIVO, false)
                        .build())
                .extractor(ExtraccionSlotEstandar.get())
                .extractorDeEspanol(p ->
                        String.format("(%s) %s", p.getVerboBase().getAspecto().getEmoji(), p.getSignificado()))
                .extractorAEspanol(p ->
                        String.format("(%s) %s", p.getVerboBase().getAspecto().getEmoji(), p.getSignificado()))
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
