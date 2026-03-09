package com.bcadaval.esloveno.structures.frase.frases;

import com.bcadaval.esloveno.beans.enums.*;
import com.bcadaval.esloveno.beans.palabra.*;
import com.bcadaval.esloveno.services.palabra.NumeralService;
import com.bcadaval.esloveno.services.palabra.ParticulaService;
import com.bcadaval.esloveno.services.palabra.PronombreService;
import com.bcadaval.esloveno.services.palabra.verbo.VerbosService;
import com.bcadaval.esloveno.structures.DificultadFrase;
import com.bcadaval.esloveno.structures.extractores.*;
import com.bcadaval.esloveno.structures.frase.Frase;
import com.bcadaval.esloveno.structures.frase.PalabraFrase;
import com.bcadaval.esloveno.structures.frase.criterio.*;
import com.bcadaval.esloveno.structures.frase.dependencia.DependenciaBuilder;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Frase: Pronombre + "ne" + Verbo transitivo presente + Numeral genitivo + Sustantivo genitivo.
 * <p>
 * En esloveno, la negación de un verbo transitivo con complemento directo cambia
 * el caso del CD de acusativo a genitivo (genitivo de negación).
 * <p>
 * Excluye biti/imeti/hoteti que tienen formas negativas especiales.
 * <p>
 * Ejemplo: "jaz ne vidim ene knjige" (yo no veo un libro)
 * <p>
 * Huecos:
 * <ol>
 *   <li><strong>PRONOMBRE</strong> (opcional): pronombre personal concordante con el verbo</li>
 *   <li><strong>PARTICULA</strong> (apoyo): partícula "ne"</li>
 *   <li><strong>VERBO</strong> (opcional): verbo transitivo en presente, con generador fallback</li>
 *   <li><strong>NUMERO</strong> (opcional): numeral en genitivo concordante con sustantivo</li>
 *   <li><strong>SUSTANTIVO</strong> (obligatorio): sustantivo en genitivo</li>
 * </ol>
 */
@Component
@DificultadFrase(NivelDificultad.INTERMEDIO)
public class FraseNegacionConCD extends Frase {

    @Autowired
    private PronombreService pronombreService;

    @Autowired
    private ParticulaService particulaService;

    @Autowired
    private VerbosService verbosService;

    @Autowired
    private NumeralService numeralService;

    @Override
    public String getIdentificador() {
        return "NEGACION_CON_CD";
    }

    @Override
    public String getNombreMostrar() {
        return "Negación con CD (ne + verbo + sust. GEN)";
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

        // Verbo transitivo en presente (opcional, con generador)
        PalabraFrase<VerboFlexion> verbo = PalabraFrase.<VerboFlexion>builder()
                .nombre("VERBO")
                .criterio(VerboCriterioBuilder.crear()
                        .conFormaVerbal(FormaVerbal.PRESENT)
                        .conTransitividad(Transitividad.TRANSITIVO, Transitividad.AMBITRANSITIVO)
                        .conNegativo(false)
                        .conPrincipalExcepto("biti", "imeti", "hoteti")
                        .build())
                .generador(() -> verbosService.getVerboTransitivoPresenteAleatorio(Arrays.asList("biti", "imeti", "hoteti")))
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

        // Partícula "ne" (apoyo)
        PalabraFrase<ParticulaFlexion> particula = PalabraFrase.<ParticulaFlexion>builder()
                .nombre("PARTICULA_NE")
                .generador(() -> particulaService.getPorPrincipal("ne"))
                .extractor(ExtractorParticula.get())
                .build();

        // Numeral en genitivo (opcional, depende del sustantivo)
        PalabraFrase<NumeralFlexion> numeral = PalabraFrase.<NumeralFlexion>builder()
                .nombre("NUMERO")
                .criterio(NumeralCriterioBuilder.crear()
                        .conCaso(Caso.GENITIVO)
                        .conDependencia(DependenciaBuilder.de(sustantivo)
                                .si(sust -> sust.getNumero() == Numero.SINGULAR,
                                        NumeralCriterioBuilder.crear().conNumero(Numero.SINGULAR).conCantidad(1).build())
                                .si(sust -> sust.getNumero() == Numero.DUAL,
                                        NumeralCriterioBuilder.crear().conNumero(Numero.DUAL).conCantidad(2).build())
                                .orElse(NumeralCriterioBuilder.crear().conNumero(Numero.PLURAL).build())
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
        agregarElemento(particula);
        agregarElemento(verbo);
        agregarElemento(numeral);
        agregarElemento(sustantivo);
    }
}

