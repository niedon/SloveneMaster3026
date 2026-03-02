package com.bcadaval.esloveno.structures.frase.frases;

import com.bcadaval.esloveno.beans.enums.*;
import com.bcadaval.esloveno.beans.palabra.PronombreFlexion;
import com.bcadaval.esloveno.beans.palabra.VerboFlexion;
import com.bcadaval.esloveno.services.palabra.PronombreService;
import com.bcadaval.esloveno.structures.DificultadFrase;
import com.bcadaval.esloveno.structures.extractores.ExtractorPronombre;
import com.bcadaval.esloveno.structures.extractores.ExtractorVerbo;
import com.bcadaval.esloveno.structures.frase.Frase;
import com.bcadaval.esloveno.structures.frase.PalabraFrase;
import com.bcadaval.esloveno.structures.frase.criterio.PronombreCriterioBuilder;
import com.bcadaval.esloveno.structures.frase.criterio.VerboCriterioBuilder;
import com.bcadaval.esloveno.structures.frase.dependencia.DependenciaBuilder;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Frase: Pronombre + Verbo en presente.
 * <p>
 * Ejemplo: "yo corro" → "jaz tečem"
 * <p>
 * Huecos:
 * <ol>
 *   <li><strong>PRONOMBRE</strong> (apoyo): generado a partir del verbo para concordar en persona/número</li>
 *   <li><strong>VERBO</strong> (obligatorio): {@link VerboFlexion} con forma PRESENT y no negativo</li>
 * </ol>
 */
@Component
@DificultadFrase(NivelDificultad.PRINCIPIANTE)
public class FraseSoloVerboPresente extends Frase {

    @Autowired
    private PronombreService pronombreService;

    @Override
    public String getIdentificador() {
        return "SOLO_VERBO_PRESENTE";
    }

    @Override
    public String getNombreMostrar() {
        return "Verbos en presente";
    }

    @PostConstruct
    public void configurarEstructura() {
        PalabraFrase<VerboFlexion> verbo = PalabraFrase.<VerboFlexion>builder()
                .nombre("VERBO")
                .criterio(VerboCriterioBuilder.crear()
                        .conFormaVerbal(FormaVerbal.PRESENT)
                        .conNegativo(false)
                        .build())
                .extractor(ExtractorVerbo.get())
                .build();

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

        agregarElemento(pronombre);
        agregarElemento(verbo);
    }
}
