package com.bcadaval.esloveno.structures.frase.frases;

import com.bcadaval.esloveno.beans.enums.Caso;
import com.bcadaval.esloveno.beans.enums.FormaVerbal;
import com.bcadaval.esloveno.beans.enums.Genero;
import com.bcadaval.esloveno.beans.enums.NivelDificultad;
import com.bcadaval.esloveno.beans.enums.Numero;
import com.bcadaval.esloveno.beans.enums.Persona;
import com.bcadaval.esloveno.beans.enums.Transitividad;
import com.bcadaval.esloveno.beans.palabra.NumeralFlexion;
import com.bcadaval.esloveno.beans.palabra.SustantivoFlexion;
import com.bcadaval.esloveno.beans.palabra.VerboFlexion;
import com.bcadaval.esloveno.services.palabra.NumeralService;
import com.bcadaval.esloveno.structures.DificultadFrase;
import com.bcadaval.esloveno.structures.extractores.ExtractorNumero;
import com.bcadaval.esloveno.structures.extractores.ExtractorSustantivo;
import com.bcadaval.esloveno.structures.extractores.ExtractorVerbo;
import com.bcadaval.esloveno.structures.frase.Frase;
import com.bcadaval.esloveno.structures.frase.PalabraFrase;
import com.bcadaval.esloveno.structures.frase.criterio.SustantivoCriterioBuilder;
import com.bcadaval.esloveno.structures.frase.criterio.VerboCriterioBuilder;
import com.bcadaval.esloveno.structures.frase.dependencia.DependenciaBuilder;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@DificultadFrase(NivelDificultad.INTERMEDIO_ALTO)
public class FraseSustantivoVerboCDPasado extends Frase {

    @Autowired
    private NumeralService numeralService;

    @Override
    public String getIdentificador() {
        return "SUSTANTIVO_VERBO_CD_PASADO";
    }

    @Override
    public String getNombreMostrar() {
        return "Pasado + CD (Sujeto Sustantivo)";
    }

    @PostConstruct
    public void configurarEstructura() {
        // 1. SUJETO
        PalabraFrase<SustantivoFlexion> sujeto = PalabraFrase.<SustantivoFlexion>builder()
                .nombre("SUJETO")
                .criterio(SustantivoCriterioBuilder.crear().conCaso(Caso.NOMINATIVO).build())
                .extractor(ExtractorSustantivo.get())
                .build();

        PalabraFrase<NumeralFlexion> numeralSujeto = PalabraFrase.<NumeralFlexion>builder()
                .nombre("NUMERO_SUJETO")
                .generador(sujeto, sust -> numeralService.getNumeral(sust))
                .extractor(ExtractorNumero.get())
                .extractorDeEsloveno(x -> "")
                .build();

        // 2. AUXILIAR (3ª Persona)
        PalabraFrase<VerboFlexion> auxiliar = PalabraFrase.<VerboFlexion>builder()
                .nombre("VERBO_AUXILIAR")
                .criterio(VerboCriterioBuilder.crear()
                        .conPrincipal("biti")
                        .conFormaVerbal(FormaVerbal.PRESENT)
                        .conPersona(Persona.TERCERA)
                        .conNegativo(false)
                        .conDependencia(DependenciaBuilder.de(sujeto)
                                .si(s -> s.getNumero() == Numero.SINGULAR, VerboCriterioBuilder.crear().conNumero(Numero.SINGULAR).build())
                                .si(s -> s.getNumero() == Numero.DUAL, VerboCriterioBuilder.crear().conNumero(Numero.DUAL).build())
                                .orElse(VerboCriterioBuilder.crear().conNumero(Numero.PLURAL).build())
                        )
                        .build())
                .extractor(ExtractorVerbo.get())
                .extractorDeEspanol(v -> "\uD83D\uDD19")
                .extractorAEspanol(v -> "\uD83D\uDD19")
                .build();

        // 3. PARTICIPIO (Transitivo)
        PalabraFrase<VerboFlexion> participio = PalabraFrase.<VerboFlexion>builder()
                .nombre("VERBO")
                .criterio(VerboCriterioBuilder.crear()
                        .conFormaVerbal(FormaVerbal.PARTICIPLE)
                        .conTransitividad(Transitividad.TRANSITIVO) // IMP: Transitivo
                        // Dep Número
                        .conDependencia(DependenciaBuilder.de(sujeto)
                                .si(s -> s.getNumero() == Numero.SINGULAR, VerboCriterioBuilder.crear().conNumero(Numero.SINGULAR).build())
                                .si(s -> s.getNumero() == Numero.DUAL, VerboCriterioBuilder.crear().conNumero(Numero.DUAL).build())
                                .orElse(VerboCriterioBuilder.crear().conNumero(Numero.PLURAL).build())
                        )
                        // Dep Género
                        .conDependencia(DependenciaBuilder.de(sujeto)
                                .si(s -> s.getPalabraBase().getGenero() == Genero.MASCULINO, VerboCriterioBuilder.crear().conGenero(Genero.MASCULINO).build())
                                .si(s -> s.getPalabraBase().getGenero() == Genero.FEMENINO, VerboCriterioBuilder.crear().conGenero(Genero.FEMENINO).build())
                                .orElse(VerboCriterioBuilder.crear().conGenero(Genero.NEUTRO).build())
                        )
                        .build())
                .extractor(ExtractorVerbo.get())
                .build();

        // 4. CD
        PalabraFrase<SustantivoFlexion> cd = PalabraFrase.<SustantivoFlexion>builder()
                .nombre("CD")
                .criterio(SustantivoCriterioBuilder.crear()
                        .conCaso(Caso.ACUSATIVO)
                        .build())
                .extractor(ExtractorSustantivo.get())
                .build();

        agregarElemento(numeralSujeto);
        agregarElemento(sujeto);
        agregarElemento(auxiliar);
        agregarElemento(participio);
        agregarElemento(cd);
    }
}

