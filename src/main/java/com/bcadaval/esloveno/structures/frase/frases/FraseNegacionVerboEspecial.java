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
 * Frase: Pronombre + Verbo especial en forma negativa (biti/imeti/hoteti).
 * <p>
 * Los verbos biti, imeti y hoteti tienen formas negativas especiales en esloveno
 * (ej: nisem, nimam, nočem) por lo que no usan la partícula "ne" separada.
 * <p>
 * Ejemplo: "jaz nisem" (yo no soy), "jaz nimam" (yo no tengo)
 * <p>
 * Huecos:
 * <ol>
 *   <li><strong>PRONOMBRE</strong> (opcional): pronombre personal nominativo concordante con el verbo</li>
 *   <li><strong>VERBO</strong> (obligatorio): verbo biti/imeti/hoteti en presente, forma negativa</li>
 * </ol>
 */
@Component
@DificultadFrase(NivelDificultad.PRINCIPIANTE)
public class FraseNegacionVerboEspecial extends Frase {

    @Autowired
    private PronombreService pronombreService;

    @Autowired
    private ParticulaService particulaService;

    @Override
    public String getIdentificador() {
        return "NEGACION_VERBO_ESPECIAL";
    }

    @Override
    public String getNombreMostrar() {
        return "Negación verbo especial (biti/imeti/hoteti)";
    }

    @PostConstruct
    public void configurarEstructura() {
        PalabraFrase<VerboFlexion> verbo = PalabraFrase.<VerboFlexion>builder()
                .nombre("VERBO")
                .criterio(VerboCriterioBuilder.crear()
                        .conFormaVerbal(FormaVerbal.PRESENT)
                        .conNegativo(true)
                        .conPrincipal("biti", "imeti", "hoteti")
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
                .extractorAEsloveno(p -> "")
                .extractorDeEsloveno(p -> "")
                .build();

        agregarElemento(pronombre);
        agregarElemento(particula);
        agregarElemento(verbo);
    }
}

