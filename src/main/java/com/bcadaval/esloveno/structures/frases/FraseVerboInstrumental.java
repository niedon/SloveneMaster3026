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
 * Estructura de frase: Verbo + Sustantivo en Instrumental (compañía/instrumento)
 * <p>
 * Ejemplo: "grem s prijateljem" (voy con el amigo), "pišem s svinčnikom" (escribo con lápiz)
 * <p>
 * Esta estructura practica el uso del instrumental con preposiciones "s/z" (con).
 * Muy común para expresar:
 * - Compañía: grem s teboj (voy contigo)
 * - Instrumento: režem z nožem (corto con cuchillo)
 * - Medio de transporte: potoval z vlakom (viajé en tren)
 * <p>
 * Fundamental para describir cómo o con quién se hace algo.
 * <p>
 * Elementos:
 * 1. PRONOMBRE (apoyo): pronombre personal del sujeto
 * 2. VERBO (slot): VerboFlexion en presente
 * 3. NUMERO (apoyo): numeral que concuerda con el sustantivo
 * 4. INSTRUMENTO (slot): SustantivoFlexion en INSTRUMENTAL
 */
@Component
@ExcluirDeFrases(razon = "Estructura excluida temporalmente")
public class FraseVerboInstrumental extends EstructuraFrase {

    @Getter
    private final String identificador = "VERBO_INSTRUMENTAL";
    @Getter
    private final String nombreMostrar = "Verbo + Sustantivo (INST)";

    public FraseVerboInstrumental() {
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

        // Definir slot de sustantivo en instrumental
        ElementoFrase<SustantivoFlexion> instrumento = ElementoFrase.<SustantivoFlexion>builder()
                .nombre("INSTRUMENTO")
                .criterio(CriterioBusqueda.de(SustantivoFlexion.class)
                        .con(CaracteristicaGramatical.CASO, Caso.INSTRUMENTAL)
                        .build())
                .extractor(ExtraccionSlotEstandar.get())
                .build();

        // Definir apoyo de pronombre (depende del verbo)
        ElementoFrase<PronombreFlexion> pronombre = ElementoFrase.<PronombreFlexion>builder()
                .nombre("PRONOMBRE")
                .generador(verbo, palabra -> pronombreService.getPronombre((VerboFlexion) palabra))
                .extractor(ExtraccionApoyoEstandar.get())
                .build();

        // Definir apoyo de número (depende del instrumento)
        ElementoFrase<NumeralFlexion> numero = ElementoFrase.<NumeralFlexion>builder()
                .nombre("NUMERO")
                .generador(instrumento, palabra -> numeralService.getNumeral((SustantivoFlexion) palabra))
                .extractor(ExtraccionApoyoEstandar.get())
                .build();

        // Agregar en orden de visualización
        agregarElemento(pronombre);
        agregarElemento(verbo);
        agregarElemento(numero);
        agregarElemento(instrumento);
    }
}
