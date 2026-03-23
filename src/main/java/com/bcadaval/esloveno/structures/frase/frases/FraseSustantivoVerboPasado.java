package com.bcadaval.esloveno.structures.frase.frases;

import com.bcadaval.esloveno.beans.enums.Caso;
import com.bcadaval.esloveno.beans.enums.FormaVerbal;
import com.bcadaval.esloveno.beans.enums.Genero;
import com.bcadaval.esloveno.beans.enums.NivelDificultad;
import com.bcadaval.esloveno.beans.enums.Numero;
import com.bcadaval.esloveno.beans.enums.Persona;
import com.bcadaval.esloveno.beans.palabra.NumeralFlexion;
import com.bcadaval.esloveno.beans.palabra.SustantivoFlexion;
import com.bcadaval.esloveno.beans.palabra.VerboFlexion;
import com.bcadaval.esloveno.services.palabra.NumeralService;
import com.bcadaval.esloveno.services.palabra.sustantivo.SustantivoService;
import com.bcadaval.esloveno.services.palabra.verbo.VerbosService;
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
public class FraseSustantivoVerboPasado extends Frase {

    @Autowired
    private NumeralService numeralService;
    @Autowired
    private SustantivoService sustantivoService;
    @Autowired
    private VerbosService verbosService;

    @Override
    public String getIdentificador() {
        return "SUSTANTIVO_VERBO_PASADO";
    }

    @Override
    public String getNombreMostrar() {
        return "Pasado (Sujeto Sustantivo)";
    }

    @PostConstruct
    public void configurarEstructura() {
        // 1. PARTICIPIO (Ancla): Define Género, Número
        PalabraFrase<VerboFlexion> participio = PalabraFrase.<VerboFlexion>builder()
                .nombre("VERBO")
                .criterio(VerboCriterioBuilder.crear()
                        .conFormaVerbal(FormaVerbal.PARTICIPLE)
                        .build())
                .extractor(ExtractorVerbo.get())
                .build();

        // 2. SUJETO: Depende de Participio
        PalabraFrase<SustantivoFlexion> sujeto = PalabraFrase.<SustantivoFlexion>builder()
                .nombre("SUJETO")
                .criterio(SustantivoCriterioBuilder.crear()
                        .conCaso(Caso.NOMINATIVO)
                        // Dep Gen
                        .conDependencia(DependenciaBuilder.de(participio)
                                .si(v -> v.getGenero() == Genero.MASCULINO, SustantivoCriterioBuilder.crear().conGenero(Genero.MASCULINO).build())
                                .si(v -> v.getGenero() == Genero.FEMENINO, SustantivoCriterioBuilder.crear().conGenero(Genero.FEMENINO).build())
                                .orElse(SustantivoCriterioBuilder.crear().conGenero(Genero.NEUTRO).build())
                        )
                        // Dep Num
                        .conDependencia(DependenciaBuilder.de(participio)
                                .si(v -> v.getNumero() == Numero.SINGULAR, SustantivoCriterioBuilder.crear().conNumero(Numero.SINGULAR).build())
                                .si(v -> v.getNumero() == Numero.DUAL, SustantivoCriterioBuilder.crear().conNumero(Numero.DUAL).build())
                                .orElse(SustantivoCriterioBuilder.crear().conNumero(Numero.PLURAL).build())
                        )
                        .build())
                .generador(participio, v -> sustantivoService.getAnySustantivo(Caso.NOMINATIVO, v.getGenero(), v.getNumero()))
                .extractor(ExtractorSustantivo.get())
                .build();

        // 3. NUMERAL
        PalabraFrase<NumeralFlexion> numeralSujeto = PalabraFrase.<NumeralFlexion>builder()
                .nombre("NUMERO_SUJETO")
                .generador(sujeto, sust -> numeralService.getNumeral(sust))
                .extractor(ExtractorNumero.get())
                .extractorDeEsloveno(x -> "")
                .build();

        // 4. AUXILIAR: Biti (Siempre 3ª Persona)
        PalabraFrase<VerboFlexion> auxiliar = PalabraFrase.<VerboFlexion>builder()
                .nombre("VERBO_AUXILIAR")
                .criterio(VerboCriterioBuilder.crear()
                        .conPrincipal("biti")
                        .conFormaVerbal(FormaVerbal.PRESENT)
                        .conPersona(Persona.TERCERA)
                        .conNegativo(false)
                        // DEPENDENCIA: Número concuerda con el Sujeto
                        .conDependencia(DependenciaBuilder.de(sujeto)
                                .si(s -> s.getNumero() == Numero.SINGULAR, VerboCriterioBuilder.crear().conNumero(Numero.SINGULAR).build())
                                .si(s -> s.getNumero() == Numero.DUAL, VerboCriterioBuilder.crear().conNumero(Numero.DUAL).build())
                                .orElse(VerboCriterioBuilder.crear().conNumero(Numero.PLURAL).build())
                        )
                        .build())
                .generador(sujeto, s -> verbosService.getVerboAuxiliar("biti", FormaVerbal.PRESENT, Persona.TERCERA, s.getNumero(), false))
                .extractor(ExtractorVerbo.get())
                .extractorDeEspanol(v -> "\uD83D\uDD19")
                .extractorAEspanol(v -> "\uD83D\uDD19")
                .build();

        agregarElemento(numeralSujeto);
        agregarElemento(sujeto);
        agregarElemento(auxiliar);
        agregarElemento(participio);
    }
}
