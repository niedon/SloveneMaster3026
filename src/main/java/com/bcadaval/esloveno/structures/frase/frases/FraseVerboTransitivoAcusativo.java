package com.bcadaval.esloveno.structures.frase.frases;

import com.bcadaval.esloveno.beans.enums.*;
import com.bcadaval.esloveno.beans.palabra.NumeralFlexion;
import com.bcadaval.esloveno.beans.palabra.PronombreFlexion;
import com.bcadaval.esloveno.beans.palabra.SustantivoFlexion;
import com.bcadaval.esloveno.beans.palabra.VerboFlexion;
import com.bcadaval.esloveno.services.palabra.NumeralService;
import com.bcadaval.esloveno.services.palabra.verbo.VerbosService;
import com.bcadaval.esloveno.structures.DificultadFrase;
import com.bcadaval.esloveno.structures.extractores.ExtractorNumero;
import com.bcadaval.esloveno.structures.extractores.ExtractorVerbo;
import com.bcadaval.esloveno.structures.frase.Frase;
import com.bcadaval.esloveno.structures.frase.PalabraFrase;
import com.bcadaval.esloveno.structures.frase.criterio.NumeralCriterioBuilder;
import com.bcadaval.esloveno.structures.frase.criterio.VerboCriterioBuilder;
import com.bcadaval.esloveno.structures.frase.dependencia.DependenciaBuilder;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Frase: Pronombre + Verbo transitivo en presente + Número + Sustantivo en Acusativo.
 * <p>
 * Ejemplo: "jaz vidim 1 knjigo" (yo veo 1 libro)
 * <p>
 * Huecos:
 * <ol>
 *   <li><strong>PRONOMBRE</strong> (apoyo): generado a partir del verbo</li>
 *   <li><strong>VERBO</strong> (opcional): {@link VerboFlexion} transitivo en presente, con generador fallback</li>
 *   <li><strong>NUMERO</strong> (apoyo): numeral que concuerda con el sustantivo</li>
 *   <li><strong>CD</strong> (obligatorio): {@link SustantivoFlexion} con caso ACUSATIVO</li>
 * </ol>
 */
@Component
@DificultadFrase(NivelDificultad.ELEMENTAL)
public class FraseVerboTransitivoAcusativo extends Frase {

    @Autowired
    private NumeralService numeralService;

    @Autowired
    private VerbosService verbosService;

    @Override
    public String getIdentificador() {
        return "VERBO_TRANSITIVO_ACUSATIVO";
    }

    @Override
    public String getNombreMostrar() {
        return "Verbo (tr) + Sustantivo (ACU)";
    }

    @PostConstruct
    public void configurarEstructura() {
        PalabraFrase<SustantivoFlexion> cd =  palabraFraseFactory.crearSustantivoAncla("CD", Caso.ACUSATIVO);

        PalabraFrase<VerboFlexion> verbo = PalabraFrase.<VerboFlexion>builder()
                .nombre("VERBO")
                .criterio(VerboCriterioBuilder.crear()
                        .conFormaVerbal(FormaVerbal.PRESENT)
                        .conTransitividad(Transitividad.TRANSITIVO, Transitividad.AMBITRANSITIVO)
                        .conNegativo(false)
                        .build())
                .generador(() -> verbosService.getVerboTransitivoPresenteAleatorio())
                .extractor(ExtractorVerbo.get())
                .build();

        PalabraFrase<PronombreFlexion> pronombre = palabraFraseFactory.crearPronombreParaVerboPresente("PRONOMBRE", verbo);

        PalabraFrase<NumeralFlexion> numero = PalabraFrase.<NumeralFlexion>builder()
                .nombre("NUMERO")
                .criterio(NumeralCriterioBuilder.crear()
                        .conDependencia(DependenciaBuilder.de(cd)
                                .si(sust -> sust.getNumero() == Numero.SINGULAR,
                                        NumeralCriterioBuilder.crear().conNumero(Numero.SINGULAR).conCantidad(1).build())
                                .si(sust -> sust.getNumero() == Numero.DUAL,
                                        NumeralCriterioBuilder.crear().conNumero(Numero.DUAL).conCantidad(2).build())
                                .orElse(NumeralCriterioBuilder.crear().conNumero(Numero.PLURAL).conCantidad(3, 4).build())
                        )
                        .conDependencia(DependenciaBuilder.de(cd)
                                .si(sust -> sust.getSustantivoBase().getGenero() == Genero.MASCULINO &&
                                                sust.getNumero() == Numero.SINGULAR &&
                                                sust.getSustantivoBase().getAnimacidad() == Animacidad.ANIMADO,
                                        NumeralCriterioBuilder.crear().conCaso(Caso.GENITIVO).build())
                                .orElse(NumeralCriterioBuilder.crear().conCaso(Caso.ACUSATIVO).build()))
                        .conDependencia(DependenciaBuilder.de(cd)
                                .si(sust -> sust.getSustantivoBase().getGenero() == Genero.MASCULINO,
                                        NumeralCriterioBuilder.crear().conGenero(Genero.MASCULINO).build())
                                .si(sust -> sust.getSustantivoBase().getGenero() == Genero.FEMENINO,
                                        NumeralCriterioBuilder.crear().conGenero(Genero.FEMENINO).build())
                                .orElse(NumeralCriterioBuilder.crear().conGenero(Genero.NEUTRO).build())
                        )
                        .build())
                .generador(cd, numeralService::getNumeral)
                .extractor(ExtractorNumero.get())
                .build();

        agregarElemento(pronombre);
        agregarElemento(verbo);
        agregarElemento(numero);
        agregarElemento(cd);
    }
}
