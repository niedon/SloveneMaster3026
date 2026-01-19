package com.bcadaval.esloveno.structures.frases;

import com.bcadaval.esloveno.beans.enums.CaracteristicaGramatical;
import com.bcadaval.esloveno.beans.enums.Caso;
import com.bcadaval.esloveno.beans.enums.Grado;
import com.bcadaval.esloveno.beans.palabra.AdjetivoFlexion;
import com.bcadaval.esloveno.beans.palabra.NumeralFlexion;
import com.bcadaval.esloveno.beans.palabra.SustantivoFlexion;
import com.bcadaval.esloveno.structures.CriterioBusqueda;
import com.bcadaval.esloveno.structures.ElementoFrase;
import com.bcadaval.esloveno.structures.EstructuraFrase;
import com.bcadaval.esloveno.structures.extractores.ExtraccionApoyoEstandar;
import com.bcadaval.esloveno.structures.extractores.ExtraccionSlotEstandar;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.stereotype.Component;

/**
 * Estructura de frase: Adjetivo + Sustantivo
 * <p>
 * Ejemplo: "lep dan" (hermoso día)
 * <p>
 * Elementos (en orden):
 * 1. ADJETIVO (slot): AdjetivoFlexion con caso NOMINATIVO y grado POSITIVO
 * 2. SUSTANTIVO (apoyo): SustantivoFlexion que coincide en caso, género y número con el adjetivo
 */
@Component
public class FraseSustantivoAdjetivo extends EstructuraFrase {

    @Getter
    private final String identificador = "SUSTANTIVO_ADJETIVO";
    @Getter
    private final String nombreMostrar = "Adjetivo + Sustantivo";

    public FraseSustantivoAdjetivo() {
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
                .build();


        // Definir apoyo de numeral (depende del adjetivo, debe depender en caso, género y número)
        ElementoFrase<NumeralFlexion> numeral = ElementoFrase.<NumeralFlexion>builder()
                .nombre("NUMERAL")
                .generador(adjetivo, palabra -> numeralService.getNumeral(adjetivo.getPalabraAsignada()))
                .extractor(ExtraccionApoyoEstandar.get())
                .build();

        // Definir apoyo de sustantivo (depende del adjetivo, debe coincidir en caso, género y número)
        ElementoFrase<SustantivoFlexion> sustantivo = ElementoFrase.<SustantivoFlexion>builder()
                .nombre("SUSTANTIVO")
                .generador(adjetivo, palabra -> sustantivoService.getSustantivo(adjetivo.getPalabraAsignada()))
                .extractor(ExtraccionApoyoEstandar.get())
                .build();

        // Agregar en orden de visualización (adjetivo antes del sustantivo en esloveno)
        agregarElemento(numeral);
        agregarElemento(sustantivo);
        agregarElemento(adjetivo);
    }
}
