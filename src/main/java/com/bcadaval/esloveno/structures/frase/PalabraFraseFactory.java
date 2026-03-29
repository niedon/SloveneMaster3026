package com.bcadaval.esloveno.structures.frase;

import com.bcadaval.esloveno.beans.enums.*;
import com.bcadaval.esloveno.beans.palabra.*;
import com.bcadaval.esloveno.services.palabra.NumeralService;
import com.bcadaval.esloveno.services.palabra.ParticulaService;
import com.bcadaval.esloveno.services.palabra.PronombreService;
import com.bcadaval.esloveno.services.palabra.sustantivo.SustantivoService;
import com.bcadaval.esloveno.services.palabra.verbo.VerbosService;
import com.bcadaval.esloveno.structures.extractores.*;
import com.bcadaval.esloveno.structures.frase.criterio.NumeralCriterioBuilder;
import com.bcadaval.esloveno.structures.frase.criterio.PronombreCriterioBuilder;
import com.bcadaval.esloveno.structures.frase.criterio.SustantivoCriterioBuilder;
import com.bcadaval.esloveno.structures.frase.criterio.VerboCriterioBuilder;
import com.bcadaval.esloveno.structures.frase.dependencia.Dependencia;
import com.bcadaval.esloveno.structures.frase.dependencia.DependenciaBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class PalabraFraseFactory {

    @Autowired
    private PronombreService pronombreService;
    @Autowired
    private ParticulaService particulaService;
    @Autowired
    private VerbosService verbosService;
    @Autowired
    private NumeralService numeralService;
    @Autowired
    private SustantivoService sustantivoService;

    public PalabraFrase<SustantivoFlexion> crearSustantivoDependienteParticipio(String nombre, PalabraFrase<VerboFlexion> participio) {
        return PalabraFrase.<SustantivoFlexion>builder()
                .nombre(nombre)
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
    }

    public PalabraFrase<SustantivoFlexion> crearSustantivoOpcional(String nombre, Caso caso) {
        return PalabraFrase.<SustantivoFlexion>builder()
                .nombre(nombre)
                .criterio(SustantivoCriterioBuilder.crear()
                        .conCaso(caso)
                        .build())
                .extractor(ExtractorSustantivo.get())
                .generador(() -> sustantivoService.getAnySustantivo(caso))
                .build();
    }

    public PalabraFrase<SustantivoFlexion> crearSustantivoAncla(String nombre, Caso caso) {
        return PalabraFrase.<SustantivoFlexion>builder()
                .nombre(nombre)
                .criterio(SustantivoCriterioBuilder.crear()
                        .conCaso(caso)
                        .build())
                .extractor(ExtractorSustantivo.get())
                .build();
    }

    public PalabraFrase<NumeralFlexion> crearNumeralOpcional(String nombre, PalabraFrase<SustantivoFlexion> sustantivo, Caso caso) {
        return PalabraFrase.<NumeralFlexion>builder()
                .nombre(nombre)
                .criterio(NumeralCriterioBuilder.crear()
                        .conCaso(caso)
                        .conDependencia(DependenciaBuilder.de(sustantivo)
                                .si(sust -> sust.getNumero() == Numero.SINGULAR,
                                        NumeralCriterioBuilder.crear().conNumero(Numero.SINGULAR).conCantidad(1).build())
                                .si(sust -> sust.getNumero() == Numero.DUAL,
                                        NumeralCriterioBuilder.crear().conNumero(Numero.DUAL).conCantidad(2).build())
                                .orElse(NumeralCriterioBuilder.crear().conNumero(Numero.PLURAL).conCantidadMayorQue(2).build())
                        )
                        .conDependencia(DependenciaBuilder.de(sustantivo)
                                .si(sust -> sust.getSustantivoBase().getGenero() == Genero.MASCULINO,
                                        NumeralCriterioBuilder.crear().conGenero(Genero.MASCULINO, Genero.NULO).build())
                                .si(sust -> sust.getSustantivoBase().getGenero() == Genero.FEMENINO,
                                        NumeralCriterioBuilder.crear().conGenero(Genero.FEMENINO, Genero.NULO).build())
                                .orElse(NumeralCriterioBuilder.crear().conGenero(Genero.NEUTRO, Genero.NULO).build())
                        )
                        .build())
                .generador(sustantivo, numeralService::getNumeral)
                .extractor(ExtractorNumero.get())
                .build();
    }

    public PalabraFrase<NumeralFlexion> crearNumeralApoyo(String nombre, PalabraFrase<SustantivoFlexion> sustantivo) {
        return PalabraFrase.<NumeralFlexion>builder()
                .nombre(nombre)
                .generador(sustantivo, numeralService::getNumeral)
                .extractor(ExtractorNumero.get())
                .extractorDeEsloveno(x -> "")
                .build();
    }

    public PalabraFrase<ParticulaFlexion> crearParticulaNe(String nombre, PalabraFrase<VerboFlexion> verbo) {
        return PalabraFrase.<ParticulaFlexion>builder()
                .nombre(nombre)
                .generador(() -> particulaService.getPorPrincipal("ne"))
                .extractor(ExtractorParticula.get())
                .extractorAEsloveno(p -> Arrays.asList("biti", "imeti", "hoteti").contains(verbo.getPalabraAsignada().getPrincipal()) ? "" : p.getAcentuado())
                .extractorDeEsloveno(p -> Arrays.asList("biti", "imeti", "hoteti").contains(verbo.getPalabraAsignada().getPrincipal()) ? "" : p.getFlexion())
                .build();
    }

    public PalabraFrase<PronombreFlexion> crearPronombreParaVerboPresente(String nombre, PalabraFrase<VerboFlexion> verbo) {
        return PalabraFrase.<PronombreFlexion>builder()
                .nombre(nombre)
                .criterio(PronombreCriterioBuilder.crear()
                        .conCaso(Caso.NOMINATIVO)
                        .conClitico(false)
                        .conTipoPronombre(TipoPronombre.PERSONAL)
                        // DEPENDENCIA DE NÚMERO
                        .conDependencia(dependenciaPronombreVerboNumero(verbo))
                        // DEPENDENCIA DE PERSONA
                        .conDependencia(dependenciaPronombreVerboPersona(verbo))
                        .build())
                .generador(verbo, v -> pronombreService.getAnyPronombre(v))
                .extractor(ExtractorPronombre.get())
                .build();
    }

    public PalabraFrase<PronombreFlexion> crearPronombreParaVerboParticipio(String nombre, PalabraFrase<VerboFlexion> verbo) {
        return PalabraFrase.<PronombreFlexion>builder()
                .nombre(nombre)
                .criterio(PronombreCriterioBuilder.crear()
                        .conCaso(Caso.NOMINATIVO)
                        .conTipoPronombre(TipoPronombre.PERSONAL)
                        // DEPENDENCIA DE NÚMERO
                        .conDependencia(dependenciaPronombreVerboNumero(verbo))
                        // DEPENDENCIA DE GÉNERO
                        .conDependencia(dependenciaPronombreVerboGenero(verbo))
                        .build())
                .generador(verbo, v -> pronombreService.getAnyPronombre(v))
                .extractor(ExtractorPronombre.get())
                .build();
    }

    public PalabraFrase<VerboFlexion> crearVerboInfinitivoAncla() {
        return PalabraFrase.<VerboFlexion>builder()
                .nombre("VERBO")
                .criterio(VerboCriterioBuilder.crear()
                        .conFormaVerbal(FormaVerbal.INFINITIVE)
                        .build())
                .extractor(ExtractorVerbo.get())
                .build();
    }

    public PalabraFrase<VerboFlexion> crearVerboParticipioAncla(String nombre, Transitividad... transitividad) {
        VerboCriterioBuilder criterios = VerboCriterioBuilder.crear()
                .conFormaVerbal(FormaVerbal.PARTICIPLE);
        if(transitividad.length > 0){
            criterios = criterios.conTransitividad(transitividad);
        }

        return PalabraFrase.<VerboFlexion>builder()
                .nombre(nombre)
                .criterio(criterios.build())
                .extractor(ExtractorVerbo.get())
                .build();
    }

    public PalabraFrase<VerboFlexion> crearBitiAuxiliarPasadoParaPronombre(String nombre, PalabraFrase<PronombreFlexion> pronombre) {
        return PalabraFrase.<VerboFlexion>builder()
                .nombre(nombre)
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
    }

    public PalabraFrase<VerboFlexion> crearBitiAuxiliarPasadoParaSustantivo(String nombre, PalabraFrase<SustantivoFlexion> sustantivo) {
        return PalabraFrase.<VerboFlexion>builder()
                .nombre(nombre)
                .criterio(VerboCriterioBuilder.crear()
                        .conPrincipal("biti")
                        .conFormaVerbal(FormaVerbal.PRESENT)
                        .conPersona(Persona.TERCERA)
                        .conNegativo(false)
                        .conDependencia(DependenciaBuilder.de(sustantivo)
                                .si(s -> s.getNumero() == Numero.SINGULAR, VerboCriterioBuilder.crear().conNumero(Numero.SINGULAR).build())
                                .si(s -> s.getNumero() == Numero.DUAL, VerboCriterioBuilder.crear().conNumero(Numero.DUAL).build())
                                .orElse(VerboCriterioBuilder.crear().conNumero(Numero.PLURAL).build())
                        )
                        .build())
                .generador(sustantivo, s -> verbosService.getVerboAuxiliar("biti", FormaVerbal.PRESENT, Persona.TERCERA, s.getNumero(), false))
                .extractor(ExtractorVerbo.get())
                .extractorDeEspanol(v -> "\uD83D\uDD19")
                .extractorAEspanol(v -> "\uD83D\uDD19")
                .build();
    }

    public PalabraFrase<VerboFlexion> crearBitiAuxiliarFuturoParaPronombre(String nombre, PalabraFrase<PronombreFlexion> pronombre) {
        return PalabraFrase.<VerboFlexion>builder()
                .nombre(nombre)
                .criterio(VerboCriterioBuilder.crear()
                        .conPrincipal("biti")
                        .conFormaVerbal(FormaVerbal.FUTURE)
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
                .generador(pronombre, p -> verbosService.getVerboAuxiliar("biti", FormaVerbal.FUTURE, p.getPersona(), p.getNumero(), false))
                .extractor(ExtractorVerbo.get())
                .extractorDeEspanol(v -> "🔜")
                .extractorAEspanol(v -> "🔜")
                .build();
    }

    public PalabraFrase<VerboFlexion> crearBitiAuxiliarFuturoParaSustantivo(String nombre, PalabraFrase<SustantivoFlexion> sustantivo) {
        return PalabraFrase.<VerboFlexion>builder()
                .nombre(nombre)
                .criterio(VerboCriterioBuilder.crear()
                        .conPrincipal("biti")
                        .conFormaVerbal(FormaVerbal.FUTURE)
                        .conPersona(Persona.TERCERA)
                        .conNegativo(false)
                        .conDependencia(DependenciaBuilder.de(sustantivo)
                                .si(s -> s.getNumero() == Numero.SINGULAR, VerboCriterioBuilder.crear().conNumero(Numero.SINGULAR).build())
                                .si(s -> s.getNumero() == Numero.DUAL, VerboCriterioBuilder.crear().conNumero(Numero.DUAL).build())
                                .orElse(VerboCriterioBuilder.crear().conNumero(Numero.PLURAL).build())
                        )
                        .build())
                .generador(sustantivo, s -> verbosService.getVerboAuxiliar("biti", FormaVerbal.FUTURE, Persona.TERCERA, s.getNumero(), false))
                .extractor(ExtractorVerbo.get())
                .extractorDeEspanol(v -> "🔜")
                .extractorAEspanol(v -> "🔜")
                .build();
    }

    private Dependencia<VerboFlexion> dependenciaPronombreVerboNumero(PalabraFrase<VerboFlexion> verbo) {
        return DependenciaBuilder.de(verbo)
                .si(v -> v.getNumero() == Numero.SINGULAR,
                        PronombreCriterioBuilder.crear().conNumero(Numero.SINGULAR).build())
                .si(v -> v.getNumero() == Numero.DUAL,
                        PronombreCriterioBuilder.crear().conNumero(Numero.DUAL).build())
                .orElse(PronombreCriterioBuilder.crear().conNumero(Numero.PLURAL).build());
    }

    private Dependencia<VerboFlexion> dependenciaPronombreVerboPersona(PalabraFrase<VerboFlexion> verbo) {
        return DependenciaBuilder.de(verbo)
                .si(v -> v.getPersona() == Persona.PRIMERA,
                        PronombreCriterioBuilder.crear().conPersona(Persona.PRIMERA).build())
                .si(v -> v.getPersona() == Persona.SEGUNDA,
                        PronombreCriterioBuilder.crear().conPersona(Persona.SEGUNDA).build())
                .orElse(PronombreCriterioBuilder.crear().conPersona(Persona.TERCERA).build());
    }

    private Dependencia<VerboFlexion> dependenciaPronombreVerboGenero(PalabraFrase<VerboFlexion> verbo) {
        return DependenciaBuilder.de(verbo)
                .si(v -> v.getGenero() == Genero.MASCULINO,
                        PronombreCriterioBuilder.crear().conGenero(Genero.MASCULINO).build())
                .si(v -> v.getGenero() == Genero.FEMENINO,
                        PronombreCriterioBuilder.crear().conGenero(Genero.FEMENINO).build())
                .orElse(PronombreCriterioBuilder.crear().conGenero(Genero.NEUTRO).build());
    }
}
