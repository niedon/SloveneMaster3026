package com.bcadaval.esloveno.structures.frase.frases;

import com.bcadaval.esloveno.beans.enums.*;
import com.bcadaval.esloveno.beans.palabra.NumeralFlexion;
import com.bcadaval.esloveno.beans.palabra.SustantivoFlexion;
import com.bcadaval.esloveno.services.palabra.NumeralService;
import com.bcadaval.esloveno.structures.DificultadFrase;
import com.bcadaval.esloveno.structures.extractores.ExtractorNumero;
import com.bcadaval.esloveno.structures.extractores.ExtractorSustantivo;
import com.bcadaval.esloveno.structures.frase.Frase;
import com.bcadaval.esloveno.structures.frase.PalabraFrase;
import com.bcadaval.esloveno.structures.frase.criterio.NumeralCriterioBuilder;
import com.bcadaval.esloveno.structures.frase.criterio.SustantivoCriterioBuilder;
import com.bcadaval.esloveno.structures.frase.dependencia.DependenciaBuilder;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Frase: Numeral (1-4) + Sustantivo en Nominativo.
 * <p>
 * Ejemplo: "ena knjiga" (1 libro), "dve knjigi" (2 libros)
 * <p>
 * Huecos:
 * <ol>
 *   <li><strong>NUMERO</strong> (obligatorio, slot SRS): {@link NumeralFlexion} con caso NOMINATIVO
 *       y cantidad entre 1 y 4. El número y género se resuelven como dependencia del sustantivo:
 *       singular→1, dual→2, plural→3-4.</li>
 *   <li><strong>SUSTANTIVO</strong> (obligatorio, slot SRS): {@link SustantivoFlexion} con caso NOMINATIVO</li>
 * </ol>
 * <p>
 * Ambas palabras participan en SRS. El numeral depende del sustantivo para concordar
 * en número y género gramatical, y su cantidad se restringe según el número:
 * <ul>
 *   <li>Sustantivo singular → numeral con cantidad = 1</li>
 *   <li>Sustantivo dual → numeral con cantidad = 2</li>
 *   <li>Sustantivo plural → numeral con cantidad 3 o 4</li>
 * </ul>
 */
@Component
@DificultadFrase(NivelDificultad.PRINCIPIANTE)
public class FraseSoloSustantivoNominativo extends Frase {

    @Autowired
    private NumeralService numeralService;

    @Override
    public String getIdentificador() {
        return "SOLO_SUSTANTIVO_NOMINATIVO";
    }

    @Override
    public String getNombreMostrar() {
        return "Sustantivos";
    }

    @PostConstruct
    public void configurarEstructura() {
        PalabraFrase<SustantivoFlexion> sustantivo = PalabraFrase.<SustantivoFlexion>builder()
                .nombre("SUSTANTIVO")
                .criterio(SustantivoCriterioBuilder.crear()
                        .conCaso(Caso.NOMINATIVO)
                        .build())
                .extractor(ExtractorSustantivo.get())
                .build();

        PalabraFrase<NumeralFlexion> numeral = PalabraFrase.<NumeralFlexion>builder()
                .nombre("NUMERO")
                .criterio(NumeralCriterioBuilder.crear()
                        .conCaso(Caso.NOMINATIVO)
                        .cantidadEntre(1, 4)
                        .conDependencia(DependenciaBuilder.de(sustantivo)
                                .si(sust -> sust.getNumero() == Numero.SINGULAR,
                                        NumeralCriterioBuilder.crear().conNumero(Numero.SINGULAR).conCantidad(1).build())
                                .si(sust -> sust.getNumero() == Numero.DUAL,
                                        NumeralCriterioBuilder.crear().conNumero(Numero.DUAL).conCantidad(2).build())
                                .orElse(NumeralCriterioBuilder.crear().conNumero(Numero.PLURAL).conCantidad(3, 4).build())
                        )
                        // Dependencia de género: concordancia con el sustantivo
                        .conDependencia(DependenciaBuilder.de(sustantivo)
                                .si(sust -> sust.getSustantivoBase().getGenero() == Genero.MASCULINO,
                                        NumeralCriterioBuilder.crear().conGenero(Genero.MASCULINO).build())
                                .si(sust -> sust.getSustantivoBase().getGenero() == Genero.FEMENINO,
                                        NumeralCriterioBuilder.crear().conGenero(Genero.FEMENINO).build())
                                .orElse(NumeralCriterioBuilder.crear().conGenero(Genero.NEUTRO).build())
                        )
                        .build()
                )
                .generador(sustantivo, sust -> numeralService.getNumeral(sust))
                .extractor(ExtractorNumero.get())
                .build();

        agregarElemento(numeral);
        agregarElemento(sustantivo);
    }
}
