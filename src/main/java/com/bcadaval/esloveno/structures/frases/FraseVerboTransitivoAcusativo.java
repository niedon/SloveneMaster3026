package com.bcadaval.esloveno.structures.frases;

import java.util.Set;

import com.bcadaval.esloveno.beans.palabra.NumeralFlexion;
import com.bcadaval.esloveno.beans.palabra.PronombreFlexion;
import com.bcadaval.esloveno.structures.CriterioBusqueda;
import com.bcadaval.esloveno.structures.ElementoFrase;
import com.bcadaval.esloveno.structures.extractores.ExtraccionApoyoEstandar;
import com.bcadaval.esloveno.structures.extractores.ExtraccionSlotEstandar;
import com.bcadaval.esloveno.structures.specifications.SustantivoFlexionSpecs;
import com.bcadaval.esloveno.structures.specifications.VerboFlexionSpecs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bcadaval.esloveno.beans.enums.Caso;
import com.bcadaval.esloveno.beans.enums.FormaVerbal;
import com.bcadaval.esloveno.beans.enums.Transitividad;
import com.bcadaval.esloveno.beans.palabra.SustantivoFlexion;
import com.bcadaval.esloveno.beans.palabra.VerboFlexion;
import com.bcadaval.esloveno.services.palabra.NumeralService;
import com.bcadaval.esloveno.services.palabra.PronombreService;
import com.bcadaval.esloveno.structures.EstructuraFrase;

import jakarta.annotation.PostConstruct;

/**
 * Estructura de frase: Pronombre + Verbo transitivo + Número + Sustantivo Acusativo
 * <p>
 * Ejemplo: "jaz vidim 1 knjigo" (yo veo 1 libro)
 * <p>
 * Elementos (en orden):
 * 1. PRONOMBRE (apoyo): generado a partir del verbo
 * 2. VERBO (slot): VerboFlexion con transitividad TRANSITIVO y forma PRESENT
 * 3. NUMERO (apoyo): numeral que concuerda con el sustantivo
 * 4. CD (slot): SustantivoFlexion con caso ACUSATIVO
 */
@Component
public class FraseVerboTransitivoAcusativo extends EstructuraFrase {

    public static final String IDENTIFICADOR = "VERBO_TRANSITIVO_ACUSATIVO";
    public static final String NOMBRE_MOSTRAR = "Verbo (tr) + Sustantivo (ACU)";

    @Autowired
    private PronombreService pronombreService;

    @Autowired
    private NumeralService numeralService;

    @Autowired
    private ExtraccionSlotEstandar extraccionSlotEstandar;

    @Autowired
    private ExtraccionApoyoEstandar extraccionApoyoEstandar;

    public FraseVerboTransitivoAcusativo() {
        super();
    }

    @PostConstruct
    public void configurarEstructura() {
        // Definir slot de verbo transitivo en presente
        ElementoFrase<VerboFlexion> verbo = ElementoFrase.<VerboFlexion>builder()
                .nombre("VERBO")
                .criterio(CriterioBusqueda.de(
                        VerboFlexion.class,
                        vf -> vf.getFormaVerbal() == FormaVerbal.PRESENT
                                && vf.getVerboBase() != null
                                && vf.getVerboBase().getTransitividad() == Transitividad.TRANSITIVO,
                        VerboFlexionSpecs.conFormaVerbalTransitividadYBase(FormaVerbal.PRESENT, Transitividad.TRANSITIVO)
                ))
                .extractor(extraccionSlotEstandar)
                .build();

        // Definir slot de sustantivo en acusativo
        ElementoFrase<SustantivoFlexion> cd = ElementoFrase.<SustantivoFlexion>builder()
                .nombre("CD")
                .criterio(CriterioBusqueda.de(
                        SustantivoFlexion.class,
                        sf -> sf.getCaso() == Caso.ACUSATIVO && sf.getSustantivoBase() != null,
                        SustantivoFlexionSpecs.conCasoYBase(Caso.ACUSATIVO)
                ))
                .extractor(extraccionSlotEstandar)
                .build();

        // Definir apoyo de pronombre (depende del verbo)
        ElementoFrase<PronombreFlexion> pronombre = ElementoFrase.<PronombreFlexion>builder()
                .nombre("PRONOMBRE")
                .generador(verbo, palabra -> {
                    VerboFlexion vf = (VerboFlexion) palabra;
                    return pronombreService.getPronombre(vf);
                })
                .extractor(extraccionApoyoEstandar)
                .build();

        // Definir apoyo de número (depende del CD)
        ElementoFrase<NumeralFlexion> numero = ElementoFrase.<NumeralFlexion>builder()
                .nombre("NUMERO")
                .generador(cd, palabra -> {
                    SustantivoFlexion sf = (SustantivoFlexion) palabra;
                    return numeralService.getNumeral(sf);
                })
                .extractor(extraccionApoyoEstandar)
                .build();

        // Agregar en orden de visualización
        agregarElemento(pronombre);
        agregarElemento(verbo);
        agregarElemento(numero);
        agregarElemento(cd);
    }

    @Override
    public String getIdentificador() {
        return IDENTIFICADOR;
    }

    @Override
    public String getNombreMostrar() {
        return NOMBRE_MOSTRAR;
    }

    @Override
    public Set<Caso> getCasosUsados() {
        return Set.of(Caso.ACUSATIVO);
    }

    @Override
    public Set<FormaVerbal> getFormasVerbalesUsadas() {
        return Set.of(FormaVerbal.PRESENT);
    }
}
