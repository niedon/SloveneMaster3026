package com.bcadaval.esloveno.structures.frase.frases;

import com.bcadaval.esloveno.beans.enums.*;
import com.bcadaval.esloveno.beans.palabra.PronombreFlexion;
import com.bcadaval.esloveno.beans.palabra.SustantivoFlexion;
import com.bcadaval.esloveno.beans.palabra.VerboFlexion;
import com.bcadaval.esloveno.services.palabra.PronombreService;
import com.bcadaval.esloveno.structures.DificultadFrase;
import com.bcadaval.esloveno.structures.extractores.ExtractorPronombre;
import com.bcadaval.esloveno.structures.extractores.ExtractorSustantivo;
import com.bcadaval.esloveno.structures.extractores.ExtractorVerbo;
import com.bcadaval.esloveno.structures.frase.Frase;
import com.bcadaval.esloveno.structures.frase.PalabraFrase;
import com.bcadaval.esloveno.structures.frase.criterio.PronombreCriterioBuilder;
import com.bcadaval.esloveno.structures.frase.criterio.SustantivoCriterioBuilder;
import com.bcadaval.esloveno.structures.frase.criterio.VerboCriterioBuilder;
import com.bcadaval.esloveno.structures.frase.dependencia.DependenciaBuilder;
import com.bcadaval.esloveno.services.palabra.sustantivo.SustantivoService;
import com.bcadaval.esloveno.services.palabra.verbo.VerbosService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@DificultadFrase(NivelDificultad.INTERMEDIO_ALTO)
public class FraseVerboCDPasado extends Frase {

    @Autowired
    private PronombreService pronombreService;
    @Autowired
    private VerbosService verbosService;
    @Autowired
    private SustantivoService sustantivoService;

    @Override
    public String getIdentificador() {
        return "VERBO_CD_PASADO";
    }

    @Override
    public String getNombreMostrar() {
        return "Pasado + CD (Sujeto Pronombre)";
    }

    @PostConstruct
    public void configurarEstructura() {
        // 1. PARTICIPIO: Transitivo
        PalabraFrase<VerboFlexion> participio = PalabraFrase.<VerboFlexion>builder()
                .nombre("VERBO")
                .criterio(VerboCriterioBuilder.crear()
                        .conFormaVerbal(FormaVerbal.PARTICIPLE)
                        .conTransitividad(Transitividad.TRANSITIVO) // Obligatorio para tener CD
                        .build())
                .extractor(ExtractorVerbo.get())
                .build();

        // 2. PRONOMBRE: Dependencia doble (Num, Gen)
        PalabraFrase<PronombreFlexion> pronombre = PalabraFrase.<PronombreFlexion>builder()
                .nombre("PRONOMBRE")
                .criterio(PronombreCriterioBuilder.crear()
                        .conCaso(Caso.NOMINATIVO)
                        .conTipoPronombre(TipoPronombre.PERSONAL)
                        // DEPENDENCIA 1: NÚMERO
                        .conDependencia(DependenciaBuilder.de(participio)
                                .si(v -> v.getNumero() == Numero.SINGULAR, PronombreCriterioBuilder.crear().conNumero(Numero.SINGULAR).build())
                                .si(v -> v.getNumero() == Numero.DUAL, PronombreCriterioBuilder.crear().conNumero(Numero.DUAL).build())
                                .orElse(PronombreCriterioBuilder.crear().conNumero(Numero.PLURAL).build())
                        )
                        // DEPENDENCIA 2: GÉNERO
                        .conDependencia(DependenciaBuilder.de(participio)
                                .si(v -> v.getGenero() == Genero.MASCULINO, PronombreCriterioBuilder.crear().conGenero(Genero.MASCULINO).build())
                                .si(v -> v.getGenero() == Genero.FEMENINO, PronombreCriterioBuilder.crear().conGenero(Genero.FEMENINO).build())
                                .orElse(PronombreCriterioBuilder.crear().conGenero(Genero.NEUTRO).build())
                        )
                        .build())
                .generador(participio, v -> pronombreService.getAnyPronombre(v.getNumero(), v.getGenero(), Caso.NOMINATIVO, TipoPronombre.PERSONAL))
                .extractor(ExtractorPronombre.get())
                .build();

        // 3. AUXILIAR
        PalabraFrase<VerboFlexion> auxiliar = PalabraFrase.<VerboFlexion>builder()
                .nombre("VERBO_AUXILIAR")
                .criterio(VerboCriterioBuilder.crear()
                        .conPrincipal("biti")
                        .conFormaVerbal(FormaVerbal.PRESENT)
                        .conNegativo(false)
                        // DEPENDENCIA 1: PERSONA
                        .conDependencia(DependenciaBuilder.de(pronombre) 
                                .si(p -> p.getPersona() == Persona.PRIMERA, VerboCriterioBuilder.crear().conPersona(Persona.PRIMERA).build())
                                .si(p -> p.getPersona() == Persona.SEGUNDA, VerboCriterioBuilder.crear().conPersona(Persona.SEGUNDA).build())
                                .orElse(VerboCriterioBuilder.crear().conPersona(Persona.TERCERA).build())
                        )
                        // DEPENDENCIA 2: NÚMERO
                        .conDependencia(DependenciaBuilder.de(pronombre) 
                                .si(p -> p.getNumero() == Numero.SINGULAR, VerboCriterioBuilder.crear().conNumero(Numero.SINGULAR).build())
                                .si(p -> p.getNumero() == Numero.DUAL, VerboCriterioBuilder.crear().conNumero(Numero.DUAL).build())
                                .orElse( VerboCriterioBuilder.crear().conNumero(Numero.PLURAL).build())
                        )
                        .build())
                .generador(pronombre, p -> verbosService.getVerboAuxiliar("biti", FormaVerbal.PRESENT, p.getPersona(), p.getNumero(), false))
                .extractor(ExtractorVerbo.get())
                .extractorDeEspanol(v -> "\uD83D\uDD19")
                .extractorAEspanol(v -> "\uD83D\uDD19")
                .build();

        // 4. CD: Sustantivo en Acusativo
        PalabraFrase<SustantivoFlexion> cd = PalabraFrase.<SustantivoFlexion>builder()
                .nombre("CD")
                .criterio(SustantivoCriterioBuilder.crear()
                        .conCaso(Caso.ACUSATIVO) // REQUISITO 1: Caso correcto
                        .build())
                .generador(participio, v -> sustantivoService.getAnySustantivo(Caso.ACUSATIVO, null, null))
                .extractor(ExtractorSustantivo.get())
                .build();

        agregarElemento(pronombre);
        agregarElemento(auxiliar);
        agregarElemento(participio);
        agregarElemento(cd);
    }
}
