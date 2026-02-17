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
import com.bcadaval.esloveno.structures.ExcluirDeFrases;
import com.bcadaval.esloveno.structures.extractores.ExtraccionApoyoEstandar;
import com.bcadaval.esloveno.structures.extractores.ExtraccionSlotEstandar;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.stereotype.Component;

/**
 * Estructura de frase: Verbo transitivo + Sustantivo en Dativo (objeto indirecto)
 * <p>
 * Ejemplo: "dam prijatelju" (doy al amigo), "povem otroku" (digo al niño)
 * <p>
 * Esta estructura practica verbos que requieren un objeto indirecto en dativo.
 * Es muy común con verbos como:
 * - dati (dar): dam ti knjigo (te doy un libro)
 * - povedati (decir): povem mu (le digo)
 * - pokazati (mostrar): pokažem ji (le muestro a ella)
 * <p>
 * Fundamental para comunicación básica en esloveno.
 * <p>
 * Elementos:
 * 1. PRONOMBRE (apoyo): pronombre personal del sujeto
 * 2. VERBO (slot): VerboFlexion transitivo en presente
 * 3. NUMERO (apoyo): numeral que concuerda con el sustantivo
 * 4. OI (slot): SustantivoFlexion en DATIVO (objeto indirecto)
 */
@Component
@ExcluirDeFrases(razon = "Estructura excluida temporalmente")
public class FraseVerboTransitivoDativo extends EstructuraFrase {

    @Getter
    private final String identificador = "VERBO_TRANSITIVO_DATIVO";
    @Getter
    private final String nombreMostrar = "Verbo (tr) + Sustantivo (DAT)";

    public FraseVerboTransitivoDativo() {
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

        // Definir slot de sustantivo en dativo (objeto indirecto)
        ElementoFrase<SustantivoFlexion> oi = ElementoFrase.<SustantivoFlexion>builder()
                .nombre("OI")
                .criterio(CriterioBusqueda.de(SustantivoFlexion.class)
                        .con(CaracteristicaGramatical.CASO, Caso.DATIVO)
                        .build())
                .extractor(ExtraccionSlotEstandar.get())
                .build();

        // Definir apoyo de pronombre (depende del verbo)
        ElementoFrase<PronombreFlexion> pronombre = ElementoFrase.<PronombreFlexion>builder()
                .nombre("PRONOMBRE")
                .generador(verbo, palabra -> pronombreService.getPronombre((VerboFlexion) palabra))
                .extractor(ExtraccionApoyoEstandar.get())
                .build();

        // Definir apoyo de número (depende del OI)
        ElementoFrase<NumeralFlexion> numero = ElementoFrase.<NumeralFlexion>builder()
                .nombre("NUMERO")
                .generador(oi, palabra -> numeralService.getNumeral((SustantivoFlexion) palabra))
                .extractor(ExtraccionApoyoEstandar.get())
                .build();

        // Agregar en orden de visualización
        agregarElemento(pronombre);
        agregarElemento(verbo);
        agregarElemento(numero);
        agregarElemento(oi);
    }
}
