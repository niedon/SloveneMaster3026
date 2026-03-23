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
        // 1. SUJETO (Ancla): Sustantivo en Nominativo
        PalabraFrase<SustantivoFlexion> sujeto = PalabraFrase.<SustantivoFlexion>builder()
                .nombre("SUJETO")
                .criterio(SustantivoCriterioBuilder.crear()
                        .conCaso(Caso.NOMINATIVO)
                        .build())
                .extractor(ExtractorSustantivo.get())
                .build();


        PalabraFrase<NumeralFlexion> numeralSujeto = PalabraFrase.<NumeralFlexion>builder()
                .nombre("NUMERO_SUJETO")
                .generador(sujeto, sust -> numeralService.getNumeral(sust))
                .extractor(ExtractorNumero.get())
                .extractorDeEsloveno(x -> "")
                .build();

        // 2. AUXILIAR: Biti (Siempre 3ª Persona)
        PalabraFrase<VerboFlexion> auxiliar = PalabraFrase.<VerboFlexion>builder()
                .nombre("VERBO_AUXILIAR")
                .criterio(VerboCriterioBuilder.crear()
                        .conPrincipal("biti")
                        .conFormaVerbal(FormaVerbal.PRESENT)
                        .conPersona(Persona.TERCERA) // REQUISITO 2: Siempre tercera persona
                        .conNegativo(false)
                        // DEPENDENCIA: Número concuerda con el Sujeto
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

        // 3. PARTICIPIO: Concuerda en Género y Número con Sujeto
        PalabraFrase<VerboFlexion> participio = PalabraFrase.<VerboFlexion>builder()
                .nombre("VERBO")
                .criterio(VerboCriterioBuilder.crear()
                        .conFormaVerbal(FormaVerbal.PARTICIPLE)
                        // DEPENDENCIA 1: NÚMERO
                        .conDependencia(DependenciaBuilder.de(sujeto)
                                .si(s -> s.getNumero() == Numero.SINGULAR, VerboCriterioBuilder.crear().conNumero(Numero.SINGULAR).build())
                                .si(s -> s.getNumero() == Numero.DUAL, VerboCriterioBuilder.crear().conNumero(Numero.DUAL).build())
                                .orElse(VerboCriterioBuilder.crear().conNumero(Numero.PLURAL).build())
                        )
                        // DEPENDENCIA 2: GÉNERO (En sustantivos, el género viene de la palabra base)
                        .conDependencia(DependenciaBuilder.de(sujeto)
                                .si(s -> s.getPalabraBase().getGenero() == Genero.MASCULINO, 
                                        VerboCriterioBuilder.crear().conGenero(Genero.MASCULINO).build())
                                .si(s -> s.getPalabraBase().getGenero() == Genero.FEMENINO, 
                                        VerboCriterioBuilder.crear().conGenero(Genero.FEMENINO).build())
                                .orElse(VerboCriterioBuilder.crear().conGenero(Genero.NEUTRO).build())
                        )
                        .build())
                .extractor(ExtractorVerbo.get())
                .build();

        agregarElemento(numeralSujeto);
        agregarElemento(sujeto);
        agregarElemento(auxiliar);
        agregarElemento(participio);
    }
}

