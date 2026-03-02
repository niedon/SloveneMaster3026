package com.bcadaval.esloveno.structures.frase.frases;

import com.bcadaval.esloveno.beans.enums.Caso;
import com.bcadaval.esloveno.beans.enums.Grado;
import com.bcadaval.esloveno.beans.enums.NivelDificultad;
import com.bcadaval.esloveno.beans.palabra.AdjetivoFlexion;
import com.bcadaval.esloveno.beans.palabra.NumeralFlexion;
import com.bcadaval.esloveno.beans.palabra.SustantivoFlexion;
import com.bcadaval.esloveno.services.palabra.NumeralService;
import com.bcadaval.esloveno.services.palabra.sustantivo.SustantivoService;
import com.bcadaval.esloveno.structures.DificultadFrase;
import com.bcadaval.esloveno.structures.extractores.ExtraccionApoyoEstandar;
import com.bcadaval.esloveno.structures.extractores.ExtraccionSlotEstandar;
import com.bcadaval.esloveno.structures.frase.Frase;
import com.bcadaval.esloveno.structures.frase.PalabraFrase;
import com.bcadaval.esloveno.structures.frase.criterio.AdjetivoCriterioBuilder;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Frase: Numeral + Adjetivo + Sustantivo.
 * <p>
 * Ejemplo: "lep dan" (hermoso día)
 * <p>
 * Huecos:
 * <ol>
 *   <li><strong>NUMERAL</strong> (apoyo): numeral que concuerda con el adjetivo</li>
 *   <li><strong>ADJETIVO</strong> (obligatorio): {@link AdjetivoFlexion} con caso NOMINATIVO y grado POSITIVO</li>
 *   <li><strong>SUSTANTIVO</strong> (apoyo): sustantivo que concuerda con el adjetivo</li>
 * </ol>
 */
@Component
@DificultadFrase(NivelDificultad.ELEMENTAL)
public class FraseSustantivoAdjetivo extends Frase {

    @Autowired
    private NumeralService numeralService;

    @Autowired
    private SustantivoService sustantivoService;

    @Override
    public String getIdentificador() {
        return "SUSTANTIVO_ADJETIVO";
    }

    @Override
    public String getNombreMostrar() {
        return "Adjetivo + Sustantivo";
    }

    @PostConstruct
    public void configurarEstructura() {
        PalabraFrase<AdjetivoFlexion> adjetivo = PalabraFrase.<AdjetivoFlexion>builder()
                .nombre("ADJETIVO")
                .criterio(AdjetivoCriterioBuilder.crear()
                        .conCaso(Caso.NOMINATIVO)
                        .conGrado(Grado.POSITIVO)
                        .build())
                .extractor(ExtraccionSlotEstandar.get())
                .build();

        PalabraFrase<NumeralFlexion> numeral = PalabraFrase.<NumeralFlexion>builder()
                .nombre("NUMERAL")
                .generador(adjetivo, adj -> numeralService.getNumeral(adj))
                .extractor(ExtraccionApoyoEstandar.get())
                .build();

        PalabraFrase<SustantivoFlexion> sustantivo = PalabraFrase.<SustantivoFlexion>builder()
                .nombre("SUSTANTIVO")
                .generador(adjetivo, adj -> sustantivoService.getSustantivo(adj))
                .extractor(ExtraccionApoyoEstandar.get())
                .build();

        agregarElemento(numeral);
        agregarElemento(adjetivo);
        agregarElemento(sustantivo);
    }
}
