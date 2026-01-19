package com.bcadaval.esloveno.structures.frases;

import com.bcadaval.esloveno.beans.enums.CaracteristicaGramatical;
import com.bcadaval.esloveno.beans.enums.Caso;
import com.bcadaval.esloveno.beans.enums.FormaVerbal;
import com.bcadaval.esloveno.beans.enums.Transitividad;
import com.bcadaval.esloveno.beans.palabra.NumeralFlexion;
import com.bcadaval.esloveno.beans.palabra.PronombreFlexion;
import com.bcadaval.esloveno.beans.palabra.SustantivoFlexion;
import com.bcadaval.esloveno.beans.palabra.VerboFlexion;
import com.bcadaval.esloveno.structures.CriterioBusqueda;
import com.bcadaval.esloveno.structures.ElementoFrase;
import com.bcadaval.esloveno.structures.EstructuraFrase;
import com.bcadaval.esloveno.structures.extractores.ExtraccionApoyoEstandar;
import com.bcadaval.esloveno.structures.extractores.ExtraccionSlotEstandar;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.stereotype.Component;

/**
 * Estructura de frase: Pronombre + Verbo transitivo + Número + Sustantivo Acusativo
 * <p>
 * Ejemplo: "jaz vidim 1 knjigo" (yo veo 1 libro)
 * <p>
 * Elementos (en orden):
 * 1. PRONOMBRE (apoyo): generado a partir del verbo
 * 2. VERBO (slot): VerboFlexion con transitividad TRANSITIVO y forma PRESENT
 * 3. NUMERO (apoyo): numeral que concuerda con el sustantivo
 * 4. CD (slot): SustantivoFlexion con caso ACUSATIVO
 */
@Component
public class FraseVerboTransitivoAcusativo extends EstructuraFrase {

    @Getter
    private final String identificador = "VERBO_TRANSITIVO_ACUSATIVO";
    @Getter
    private final String nombreMostrar = "Verbo (tr) + Sustantivo (ACU)";

    public FraseVerboTransitivoAcusativo() {
        super();
    }

    @PostConstruct
    public void configurarEstructura() {
        // Definir slot de verbo transitivo en presente
        ElementoFrase<VerboFlexion> verbo = ElementoFrase.<VerboFlexion>builder()
                .nombre("VERBO")
                .criterio(CriterioBusqueda.de(VerboFlexion.class)
                        .con(CaracteristicaGramatical.FORMA_VERBAL, FormaVerbal.PRESENT)
                        .con(CaracteristicaGramatical.TRANSITIVIDAD, Transitividad.TRANSITIVO)
                        .build())
                .extractor(ExtraccionSlotEstandar.get())
                .build();

        // Definir slot de sustantivo en acusativo
        ElementoFrase<SustantivoFlexion> cd = ElementoFrase.<SustantivoFlexion>builder()
                .nombre("CD")
                .criterio(CriterioBusqueda.de(SustantivoFlexion.class)
                        .con(CaracteristicaGramatical.CASO, Caso.ACUSATIVO)
                        .build())
                .extractor(ExtraccionSlotEstandar.get())
                .build();

        // Definir apoyo de pronombre (depende del verbo)
        ElementoFrase<PronombreFlexion> pronombre = ElementoFrase.<PronombreFlexion>builder()
                .nombre("PRONOMBRE")
                .generador(verbo, palabra -> pronombreService.getPronombre((VerboFlexion) palabra))
                .extractor(ExtraccionApoyoEstandar.get())
                .build();

        // Definir apoyo de número (depende del CD)
        ElementoFrase<NumeralFlexion> numero = ElementoFrase.<NumeralFlexion>builder()
                .nombre("NUMERO")
                .generador(cd, palabra -> numeralService.getNumeral((SustantivoFlexion) palabra))
                .extractor(ExtraccionApoyoEstandar.get())
                .build();

        // Agregar en orden de visualización
        agregarElemento(pronombre);
        agregarElemento(verbo);
        agregarElemento(numero);
        agregarElemento(cd);
    }
}
