package com.bcadaval.esloveno.structures.frase.frases;

import com.bcadaval.esloveno.beans.enums.*;
import com.bcadaval.esloveno.beans.palabra.PronombreFlexion;
import com.bcadaval.esloveno.beans.palabra.VerboFlexion;
import com.bcadaval.esloveno.structures.DificultadFrase;
import com.bcadaval.esloveno.structures.extractores.ExtractorPronombre;
import com.bcadaval.esloveno.structures.extractores.ExtractorVerbo;
import com.bcadaval.esloveno.structures.frase.Frase;
import com.bcadaval.esloveno.structures.frase.PalabraFrase;
import com.bcadaval.esloveno.structures.frase.criterio.PronombreCriterioBuilder;
import com.bcadaval.esloveno.structures.frase.criterio.VerboCriterioBuilder;
import com.bcadaval.esloveno.structures.frase.dependencia.DependenciaBuilder;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
@DificultadFrase(NivelDificultad.INTERMEDIO_ALTO)
public class FraseSoloVerboPasado extends Frase {

    @Override
    public String getIdentificador() {
        return "SOLO_VERBO_PASADO";
    }

    @Override
    public String getNombreMostrar() {
        return "Pasado (Sujeto Pronombre)";
    }

    @PostConstruct
    public void configurarEstructura() {
        // 1. PARTICIPIO (Ancla): Define Género y Número aleatorios
        PalabraFrase<VerboFlexion> participio = PalabraFrase.<VerboFlexion>builder()
                .nombre("VERBO")
                .criterio(VerboCriterioBuilder.crear()
                        .conFormaVerbal(FormaVerbal.PARTICIPLE)
                        .build())
                .extractor(ExtractorVerbo.get())
                .build();

        // 2. PRONOMBRE (Depende del participio)
        PalabraFrase<PronombreFlexion> pronombre = PalabraFrase.<PronombreFlexion>builder()
                .nombre("PRONOMBRE")
                .criterio(PronombreCriterioBuilder.crear()
                        .conCaso(Caso.NOMINATIVO)
                        .conTipoPronombre(TipoPronombre.PERSONAL)
                        // DEPENDENCIA 1: NÚMERO (Separada)
                        .conDependencia(DependenciaBuilder.de(participio)
                                .si(v -> v.getNumero() == Numero.SINGULAR,
                                        PronombreCriterioBuilder.crear().conNumero(Numero.SINGULAR).build())
                                .si(v -> v.getNumero() == Numero.DUAL,
                                        PronombreCriterioBuilder.crear().conNumero(Numero.DUAL).build())
                                .orElse(PronombreCriterioBuilder.crear().conNumero(Numero.PLURAL).build())
                        )
                        // DEPENDENCIA 2: GÉNERO (Separada)
                        .conDependencia(DependenciaBuilder.de(participio)
                                .si(v -> v.getGenero() == Genero.MASCULINO,
                                        PronombreCriterioBuilder.crear().conGenero(Genero.MASCULINO).build())
                                .si(v -> v.getGenero() == Genero.FEMENINO,
                                        PronombreCriterioBuilder.crear().conGenero(Genero.FEMENINO).build())
                                .orElse(PronombreCriterioBuilder.crear().conGenero(Genero.NEUTRO).build())
                        )
                        .build())
                .extractor(ExtractorPronombre.get())
                .build();

        // 3. AUXILIAR (Depende del pronombre)
        PalabraFrase<VerboFlexion> auxiliar = PalabraFrase.<VerboFlexion>builder()
                .nombre("VERBO_AUXILIAR")
                .criterio(VerboCriterioBuilder.crear()
                        .conPrincipal("biti")
                        .conFormaVerbal(FormaVerbal.PRESENT)
                        .conNegativo(false)
                        // DEPENDENCIA 1: PERSONA
                        .conDependencia(DependenciaBuilder.de(pronombre)
                                .si(p -> p.getPersona() == Persona.PRIMERA,
                                        VerboCriterioBuilder.crear().conPersona(Persona.PRIMERA).build())
                                .si(p -> p.getPersona() == Persona.SEGUNDA,
                                        VerboCriterioBuilder.crear().conPersona(Persona.SEGUNDA).build())
                                .orElse(VerboCriterioBuilder.crear().conPersona(Persona.TERCERA).build())
                        )
                        // DEPENDENCIA 2: NÚMERO
                        .conDependencia(DependenciaBuilder.de(pronombre)
                                .si(p -> p.getNumero() == Numero.SINGULAR,
                                        VerboCriterioBuilder.crear().conNumero(Numero.SINGULAR).build())
                                .si(p -> p.getNumero() == Numero.DUAL,
                                        VerboCriterioBuilder.crear().conNumero(Numero.DUAL).build())
                                .orElse(VerboCriterioBuilder.crear().conNumero(Numero.PLURAL).build())
                        )
                        .build())
                .extractor(ExtractorVerbo.get())
                .extractorDeEspanol(v -> "\uD83D\uDD19")
                .extractorAEspanol(v -> "\uD83D\uDD19")
                .build();

        agregarElemento(pronombre);
        agregarElemento(auxiliar);
        agregarElemento(participio);
    }
}
