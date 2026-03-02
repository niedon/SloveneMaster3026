package com.bcadaval.esloveno.structures.frase.frases;

import com.bcadaval.esloveno.beans.enums.*;
import com.bcadaval.esloveno.beans.palabra.*;
import com.bcadaval.esloveno.services.palabra.NumeralService;
import com.bcadaval.esloveno.services.palabra.PronombreService;
import com.bcadaval.esloveno.structures.DificultadFrase;
import com.bcadaval.esloveno.structures.extractores.*;
import com.bcadaval.esloveno.structures.frase.Frase;
import com.bcadaval.esloveno.structures.frase.PalabraFrase;
import com.bcadaval.esloveno.structures.frase.criterio.*;
import com.bcadaval.esloveno.structures.frase.dependencia.DependenciaBuilder;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Frase: Pronombre + Verbo especial negativo (biti/imeti/hoteti) + Numeral genitivo + Sustantivo genitivo.
 * <p>
 * Versión para los verbos con formas negativas especiales (nisem, nimam, nočem).
 * No usa la partícula "ne" separada.
 * <p>
 * Ejemplo: "jaz nimam ene knjige" (yo no tengo un libro)
 * <p>
 * Huecos:
 * <ol>
 *   <li><strong>PRONOMBRE</strong> (opcional): pronombre personal concordante con el verbo</li>
 *   <li><strong>VERBO</strong> (obligatorio): biti/imeti/hoteti en presente, forma negativa</li>
 *   <li><strong>NUMERO</strong> (opcional): numeral en genitivo concordante con sustantivo</li>
 *   <li><strong>SUSTANTIVO</strong> (obligatorio): sustantivo en genitivo</li>
 * </ol>
 */
@Component
@DificultadFrase(NivelDificultad.INTERMEDIO)
public class FraseNegacionConCDEspecial extends Frase {

    @Autowired
    private PronombreService pronombreService;

    @Autowired
    private NumeralService numeralService;

    @Override
    public String getIdentificador() {
        return "NEGACION_CON_CD_ESPECIAL";
    }

    @Override
    public String getNombreMostrar() {
        return "Negación con CD especial (biti/imeti/hoteti + sust. GEN)";
    }

    @PostConstruct
    public void configurarEstructura() {
        // Sustantivo en genitivo - elemento nuclear (obligatorio)
        PalabraFrase<SustantivoFlexion> sustantivo = PalabraFrase.<SustantivoFlexion>builder()
                .nombre("SUSTANTIVO")
                .criterio(SustantivoCriterioBuilder.crear()
                        .conCaso(Caso.GENITIVO)
                        .build())
                .extractor(ExtractorSustantivo.get())
                .build();

        // Verbo especial negativo (obligatorio)
        PalabraFrase<VerboFlexion> verbo = PalabraFrase.<VerboFlexion>builder()
                .nombre("VERBO")
                .criterio(VerboCriterioBuilder.crear()
                        .conFormaVerbal(FormaVerbal.PRESENT)
                        .conNegativo(true)
                        .conPrincipal(/*"biti", */"imeti", "hoteti")
                        .build())
                .extractor(ExtractorVerbo.get())
                .build();

        // Pronombre (opcional, depende del verbo)
        PalabraFrase<PronombreFlexion> pronombre = PalabraFrase.<PronombreFlexion>builder()
                .nombre("PRONOMBRE")
                .criterio(PronombreCriterioBuilder.crear()
                        .conCaso(Caso.NOMINATIVO)
                        .conClitico(false, null)
                        .conTipoPronombre(TipoPronombre.PERSONAL)
                        .conDependencia(DependenciaBuilder.de(verbo)
                                .si(v -> v.getPersona() == Persona.PRIMERA && v.getNumero() == Numero.SINGULAR,
                                        PronombreCriterioBuilder.crear().conPersona(Persona.PRIMERA).conNumero(Numero.SINGULAR).build())
                                .si(v -> v.getPersona() == Persona.PRIMERA && v.getNumero() == Numero.DUAL,
                                        PronombreCriterioBuilder.crear().conPersona(Persona.PRIMERA).conNumero(Numero.DUAL).build())
                                .si(v -> v.getPersona() == Persona.PRIMERA && v.getNumero() == Numero.PLURAL,
                                        PronombreCriterioBuilder.crear().conPersona(Persona.PRIMERA).conNumero(Numero.PLURAL).build())
                                .si(v -> v.getPersona() == Persona.SEGUNDA && v.getNumero() == Numero.SINGULAR,
                                        PronombreCriterioBuilder.crear().conPersona(Persona.SEGUNDA).conNumero(Numero.SINGULAR).build())
                                .si(v -> v.getPersona() == Persona.SEGUNDA && v.getNumero() == Numero.DUAL,
                                        PronombreCriterioBuilder.crear().conPersona(Persona.SEGUNDA).conNumero(Numero.DUAL).build())
                                .si(v -> v.getPersona() == Persona.SEGUNDA && v.getNumero() == Numero.PLURAL,
                                        PronombreCriterioBuilder.crear().conPersona(Persona.SEGUNDA).conNumero(Numero.PLURAL).build())
                                .si(v -> v.getPersona() == Persona.TERCERA && v.getNumero() == Numero.SINGULAR,
                                        PronombreCriterioBuilder.crear().conPersona(Persona.TERCERA).conNumero(Numero.SINGULAR).build())
                                .si(v -> v.getPersona() == Persona.TERCERA && v.getNumero() == Numero.DUAL,
                                        PronombreCriterioBuilder.crear().conPersona(Persona.TERCERA).conNumero(Numero.DUAL).build())
                                .orElse(PronombreCriterioBuilder.crear().conPersona(Persona.TERCERA).conNumero(Numero.PLURAL).build())
                        )
                        .build())
                .generador(verbo, v -> pronombreService.getPronombre(v))
                .extractor(ExtractorPronombre.get())
                .build();

        // Numeral en genitivo (opcional, depende del sustantivo)
        PalabraFrase<NumeralFlexion> numeral = PalabraFrase.<NumeralFlexion>builder()
                .nombre("NUMERO")
                .criterio(NumeralCriterioBuilder.crear()
                        .conCaso(Caso.GENITIVO)
                        .cantidadEntre(1, 4)
                        .conDependencia(DependenciaBuilder.de(sustantivo)
                                .si(sust -> sust.getNumero() == Numero.SINGULAR,
                                        NumeralCriterioBuilder.crear().conNumero(Numero.SINGULAR).conCantidad(1).build())
                                .si(sust -> sust.getNumero() == Numero.DUAL,
                                        NumeralCriterioBuilder.crear().conNumero(Numero.DUAL).conCantidad(2).build())
                                .orElse(NumeralCriterioBuilder.crear().conNumero(Numero.PLURAL).conCantidad(3, 4).build())
                        )
                        .conDependencia(DependenciaBuilder.de(sustantivo)
                                .si(sust -> sust.getSustantivoBase().getGenero() == Genero.MASCULINO,
                                        NumeralCriterioBuilder.crear().conGenero(Genero.MASCULINO).build())
                                .si(sust -> sust.getSustantivoBase().getGenero() == Genero.FEMENINO,
                                        NumeralCriterioBuilder.crear().conGenero(Genero.FEMENINO).build())
                                .orElse(NumeralCriterioBuilder.crear().conGenero(Genero.NEUTRO).build())
                        )
                        .build())
                .generador(sustantivo, sust -> numeralService.getNumeral(sust))
                .extractor(ExtractorNumero.get())
                .build();

        agregarElemento(pronombre);
        agregarElemento(verbo);
        agregarElemento(numeral);
        agregarElemento(sustantivo);
    }
}

