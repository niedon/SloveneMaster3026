package com.bcadaval.esloveno.structures.frases;

import com.bcadaval.esloveno.beans.enums.Caso;
import com.bcadaval.esloveno.beans.enums.FormaVerbal;
import com.bcadaval.esloveno.beans.palabra.PronombreFlexion;
import com.bcadaval.esloveno.beans.palabra.VerboFlexion;
import com.bcadaval.esloveno.services.palabra.PronombreService;
import com.bcadaval.esloveno.structures.CriterioBusqueda;
import com.bcadaval.esloveno.structures.ElementoFrase;
import com.bcadaval.esloveno.structures.EstructuraFrase;
import com.bcadaval.esloveno.structures.extractores.ExtraccionApoyoEstandar;
import com.bcadaval.esloveno.structures.extractores.ExtraccionSlotEstandar;
import com.bcadaval.esloveno.structures.specifications.VerboFlexionSpecs;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;

/**
 * Estructura de frase: Solo un Verbo en presente
 * <p>
 * Ejemplo: "yo corro" → "jaz tečem"
 * <p>
 * Elementos (en orden):
 * 1. PRONOMBRE (apoyo): generado a partir del verbo
 * 2. VERBO (slot): VerboFlexion con forma PRESENT
 */
@Component
public class FraseSoloVerboPresente extends EstructuraFrase {

    public static final String IDENTIFICADOR = "SOLO_VERBO_PRESENTE";
    public static final String NOMBRE_MOSTRAR = "Verbo (presente)";

    @Autowired
    private PronombreService pronombreService;

    @Autowired
    private ExtraccionSlotEstandar extraccionSlotEstandar;

    @Autowired
    private ExtraccionApoyoEstandar extraccionApoyoEstandar;

    public FraseSoloVerboPresente() {
        super();
    }

    @PostConstruct
    public void configurarEstructura() {
        // Definir slot de verbo
        ElementoFrase<VerboFlexion> verbo = ElementoFrase.<VerboFlexion>builder()
                .nombre("VERBO")
                .criterio(CriterioBusqueda.de(
                        VerboFlexion.class,
                        vf -> vf.getFormaVerbal() == FormaVerbal.PRESENT && vf.getVerboBase() != null,
                        VerboFlexionSpecs.conFormaVerbalYBase(FormaVerbal.PRESENT)
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

        // Agregar en orden de visualización
        agregarElemento(pronombre);
        agregarElemento(verbo);
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
        return Collections.emptySet();
    }

    @Override
    public Set<FormaVerbal> getFormasVerbalesUsadas() {
        return Set.of(FormaVerbal.PRESENT);
    }
}
