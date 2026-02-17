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
 * Estructura de frase: Verbo + Sustantivo en Genitivo (procedencia/posesión)
 * <p>
 * Ejemplo: "prihajam iz Ljubljane" (vengo de Ljubljana), "nimam časa" (no tengo tiempo)
 * <p>
 * Esta estructura practica el uso del genitivo con preposiciones o negación.
 * Muy común para expresar:
 * - Procedencia: prihajam iz Maribora (vengo de Maribor)
 * - Pertenencia: del hiše (parte de la casa)
 * - Negación: nimam knjige (no tengo libro) - ¡el objeto directo pasa a genitivo!
 * - Cantidad: kozarec vode (vaso de agua)
 * <p>
 * Esencial ya que el genitivo es el caso más frecuente después del nominativo.
 * <p>
 * Elementos:
 * 1. PRONOMBRE (apoyo): pronombre personal del sujeto
 * 2. VERBO (slot): VerboFlexion en presente
 * 3. NUMERO (apoyo): numeral que concuerda con el sustantivo
 * 4. ORIGEN (slot): SustantivoFlexion en GENITIVO
 */
@Component
@ExcluirDeFrases(razon = "Estructura excluida temporalmente")
public class FraseVerboGenitivo extends EstructuraFrase {

    @Getter
    private final String identificador = "VERBO_GENITIVO";
    @Getter
    private final String nombreMostrar = "Verbo + Sustantivo (GEN)";

    public FraseVerboGenitivo() {
        super();
    }

    @PostConstruct
    public void configurarEstructura() {
        // Definir slot de verbo en presente
        ElementoFrase<VerboFlexion> verbo = ElementoFrase.<VerboFlexion>builder()
                .nombre("VERBO")
                .criterio(CriterioBusqueda.de(VerboFlexion.class)
                        .con(CaracteristicaGramatical.FORMA_VERBAL, FormaVerbal.PRESENT)
                        .build())
                .extractor(ExtraccionSlotEstandar.get())
                .build();

        // Definir slot de sustantivo en genitivo
        ElementoFrase<SustantivoFlexion> origen = ElementoFrase.<SustantivoFlexion>builder()
                .nombre("ORIGEN")
                .criterio(CriterioBusqueda.de(SustantivoFlexion.class)
                        .con(CaracteristicaGramatical.CASO, Caso.GENITIVO)
                        .build())
                .extractor(ExtraccionSlotEstandar.get())
                .build();

        // Definir apoyo de pronombre (depende del verbo)
        ElementoFrase<PronombreFlexion> pronombre = ElementoFrase.<PronombreFlexion>builder()
                .nombre("PRONOMBRE")
                .generador(verbo, palabra -> pronombreService.getPronombre((VerboFlexion) palabra))
                .extractor(ExtraccionApoyoEstandar.get())
                .build();

        // Definir apoyo de número (depende del origen)
        ElementoFrase<NumeralFlexion> numero = ElementoFrase.<NumeralFlexion>builder()
                .nombre("NUMERO")
                .generador(origen, palabra -> numeralService.getNumeral((SustantivoFlexion) palabra))
                .extractor(ExtraccionApoyoEstandar.get())
                .build();

        // Agregar en orden de visualización
        agregarElemento(pronombre);
        agregarElemento(verbo);
        agregarElemento(numero);
        agregarElemento(origen);
    }
}
