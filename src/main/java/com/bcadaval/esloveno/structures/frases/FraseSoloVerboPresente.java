package com.bcadaval.esloveno.structures.frases;

import com.bcadaval.esloveno.beans.base.PalabraFlexion;
import com.bcadaval.esloveno.beans.enums.Caso;
import com.bcadaval.esloveno.beans.enums.FormaVerbal;
import com.bcadaval.esloveno.beans.palabra.Pronombre;
import com.bcadaval.esloveno.beans.palabra.PronombreFlexion;
import com.bcadaval.esloveno.beans.palabra.VerboFlexion;
import com.bcadaval.esloveno.services.palabra.PronombreService;
import com.bcadaval.esloveno.structures.ElementoApoyo;
import com.bcadaval.esloveno.structures.EstructuraFrase;
import com.bcadaval.esloveno.structures.SlotPalabra;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;

/**
 * Estructura de frase: Solo un Verbo en presente
 *
 * Ejemplo: "yo corro" → "jaz tečem"
 *
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

    public FraseSoloVerboPresente() {
        super();
    }

    @PostConstruct
    public void configurarEstructura() {
        agregarApoyo(ElementoApoyo.builder()
            .nombre("PRONOMBRE")
            .generadorObjeto(frase -> {
                SlotPalabra slotVerbo = frase.getSlotPorNombre("VERBO");
                if (slotVerbo == null || !slotVerbo.estaAsignado()) return null;
                VerboFlexion verbo = (VerboFlexion) slotVerbo.getPalabraAsignada();
                return pronombreService.getPronombre(verbo);
            })
            .extractorDeEspanol(PalabraFlexion::getSignificado)   // ES_SL fila1: "yo"
            .extractorAEsloveno(PalabraFlexion::getFlexion)   // ES_SL fila2: "jaz"
            .extractorDeEsloveno(PalabraFlexion::getFlexion)  // SL_ES fila1: "jaz"
            .extractorAEspanol(PalabraFlexion::getSignificado)    // SL_ES fila2: "yo"
            .build());

        // Slot: Verbo en presente
        agregarSlot(SlotPalabra.builder()
            .nombre("VERBO")
            .matcher(p -> p instanceof VerboFlexion vf && vf.getFormaVerbal() == FormaVerbal.PRESENT)
            .extractorDeEspanol(PalabraFlexion::getSignificado)
            .extractorAEsloveno(PalabraFlexion::getAcentuado)
            .extractorDeEsloveno(PalabraFlexion::getFlexion)
            .extractorAEspanol(PalabraFlexion::getSignificado)
            .build());
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
