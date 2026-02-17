package com.bcadaval.esloveno.structures.frases;

import com.bcadaval.esloveno.beans.enums.CaracteristicaGramatical;
import com.bcadaval.esloveno.beans.enums.Caso;
import com.bcadaval.esloveno.beans.enums.FormaVerbal;
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
 * Estructura de frase: Verbo + Sustantivo en Locativo (ubicación)
 * <p>
 * Ejemplo: "sem v mestu" (estoy en la ciudad), "živim v Ljubljani" (vivo en Ljubljana)
 * <p>
 * Esta estructura practica la ubicación con preposiciones "v" (en) o "na" (sobre/en).
 * Muy común con verbos como:
 * - biti (estar/ser): sem v šoli (estoy en la escuela)
 * - živeti (vivir): živim v hiši (vivo en una casa)
 * - delati (trabajar): delam v pisarni (trabajo en la oficina)
 * <p>
 * Esencial para describir dónde está algo o alguien.
 * <p>
 * Elementos:
 * 1. PRONOMBRE (apoyo): pronombre personal del sujeto
 * 2. VERBO (slot): VerboFlexion en presente
 * 3. NUMERO (apoyo): numeral que concuerda con el sustantivo
 * 4. LUGAR (slot): SustantivoFlexion en LOCATIVO
 */
@Component
@ExcluirDeFrases(razon = "Estructura excluida temporalmente")
public class FraseVerboLocativo extends EstructuraFrase {

    @Getter
    private final String identificador = "VERBO_LOCATIVO";
    @Getter
    private final String nombreMostrar = "Verbo + Sustantivo (LOC)";

    public FraseVerboLocativo() {
        super();
    }

    @PostConstruct
    public void configurarEstructura() {
        // Definir slot de verbo en presente
        ElementoFrase<VerboFlexion> verbo = ElementoFrase.<VerboFlexion>builder()
                .nombre("VERBO")
                .criterio(CriterioBusqueda.de(VerboFlexion.class)
                        .con(CaracteristicaGramatical.FORMA_VERBAL, FormaVerbal.PRESENT)
                        .con(CaracteristicaGramatical.NEGATIVO, false)
                        .build())
                .extractor(ExtraccionSlotEstandar.get())
                .build();

        // Definir slot de sustantivo en locativo (lugar)
        ElementoFrase<SustantivoFlexion> lugar = ElementoFrase.<SustantivoFlexion>builder()
                .nombre("LUGAR")
                .criterio(CriterioBusqueda.de(SustantivoFlexion.class)
                        .con(CaracteristicaGramatical.CASO, Caso.LOCATIVO)
                        .build())
                .extractor(ExtraccionSlotEstandar.get())
                .build();

        // Definir apoyo de pronombre (depende del verbo)
        ElementoFrase<PronombreFlexion> pronombre = ElementoFrase.<PronombreFlexion>builder()
                .nombre("PRONOMBRE")
                .generador(verbo, palabra -> pronombreService.getPronombre((VerboFlexion) palabra))
                .extractor(ExtraccionApoyoEstandar.get())
                .build();

        // Definir apoyo de número (depende del lugar)
        ElementoFrase<NumeralFlexion> numero = ElementoFrase.<NumeralFlexion>builder()
                .nombre("NUMERO")
                .generador(lugar, palabra -> numeralService.getNumeral((SustantivoFlexion) palabra))
                .extractor(ExtraccionApoyoEstandar.get())
                .build();

        // Agregar en orden de visualización
        agregarElemento(pronombre);
        agregarElemento(verbo);
        agregarElemento(numero);
        agregarElemento(lugar);
    }
}
