package com.bcadaval.esloveno.structures.frase.frases;

import com.bcadaval.esloveno.beans.enums.*;
import com.bcadaval.esloveno.beans.palabra.ParticulaFlexion;
import com.bcadaval.esloveno.beans.palabra.PronombreFlexion;
import com.bcadaval.esloveno.beans.palabra.VerboFlexion;
import com.bcadaval.esloveno.services.palabra.ParticulaService;
import com.bcadaval.esloveno.services.palabra.PronombreService;
import com.bcadaval.esloveno.structures.DificultadFrase;
import com.bcadaval.esloveno.structures.extractores.ExtractorParticula;
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
 * Frase: Pronombre + Partícula "ne" + Verbo conjugado en presente (negación estándar).
 * <p>
 * Excluye los verbos biti, imeti y hoteti, que tienen formas negativas especiales.
 * <p>
 * Ejemplo: "jaz ne tečem" (yo no corro)
 * <p>
 * Huecos:
 * <ol>
 *   <li><strong>PRONOMBRE</strong> (opcional): pronombre personal nominativo concordante con el verbo</li>
 *   <li><strong>PARTICULA</strong> (apoyo): partícula "ne"</li>
 *   <li><strong>VERBO</strong> (obligatorio): verbo en presente, no negativo, excluyendo biti/imeti/hoteti</li>
 * </ol>
 */
@Component
@DificultadFrase(NivelDificultad.PRINCIPIANTE)
public class FraseNegacionVerbo extends Frase {

    @Autowired
    private PronombreService pronombreService;

    @Autowired
    private ParticulaService particulaService;

    @Override
    public String getIdentificador() {
        return "NEGACION_VERBO";
    }

    @Override
    public String getNombreMostrar() {
        return "Negación verbo (ne + verbo)";
    }

    @PostConstruct
    public void configurarEstructura() {
        PalabraFrase<VerboFlexion> verbo = PalabraFrase.<VerboFlexion>builder()
                .nombre("VERBO")
                .criterio(VerboCriterioBuilder.crear()
                        .conFormaVerbal(FormaVerbal.PRESENT)
                        .conNegativo(false)
                        .conPrincipalExcepto("biti", "imeti", "hoteti")
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
                                .si(v -> v.getPersona() == Persona.PRIMERA,
                                        PronombreCriterioBuilder.crear().conPersona(Persona.PRIMERA).build())
                                .si(v -> v.getPersona() == Persona.SEGUNDA,
                                        PronombreCriterioBuilder.crear().conPersona(Persona.SEGUNDA).build())
                                .orElse(PronombreCriterioBuilder.crear().conPersona(Persona.TERCERA).build())
                        )
                        .conDependencia(DependenciaBuilder.de(verbo)
                                .si(v -> v.getNumero() == Numero.SINGULAR,
                                        PronombreCriterioBuilder.crear().conNumero(Numero.SINGULAR).build())
                                .si(v -> v.getNumero() == Numero.DUAL,
                                        PronombreCriterioBuilder.crear().conNumero(Numero.DUAL).build())
                                .orElse(PronombreCriterioBuilder.crear().conNumero(Numero.PLURAL).build())
                        )
                        .build())
                .generador(verbo, v -> pronombreService.getPronombre(v))
                .extractor(ExtractorPronombre.get())
                .build();

        PalabraFrase<ParticulaFlexion> particula = PalabraFrase.<ParticulaFlexion>builder()
                .nombre("PARTICULA_NE")
                .generador(() -> particulaService.getPorPrincipal("ne"))
                .extractor(ExtractorParticula.get())
                .build();

        agregarElemento(pronombre);
        agregarElemento(particula);
        agregarElemento(verbo);
    }
}

