package com.bcadaval.esloveno.structures.frases;

import java.util.Set;

import com.bcadaval.esloveno.beans.base.PalabraFlexion;
import com.bcadaval.esloveno.beans.enums.FormaVerbal;
import com.bcadaval.esloveno.beans.enums.Numero;
import com.bcadaval.esloveno.services.palabra.NumeralService;
import com.bcadaval.esloveno.structures.ElementoApoyo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bcadaval.esloveno.beans.enums.Caso;
import com.bcadaval.esloveno.beans.palabra.SustantivoFlexion;
import com.bcadaval.esloveno.structures.EstructuraFrase;
import com.bcadaval.esloveno.structures.SlotPalabra;

import jakarta.annotation.PostConstruct;

/**
 * Estructura de frase: Solo un Sustantivo Nominativo
 *
 * Ejemplo: "El libro" → "Knjiga"
 *
 * Slots:
 * - SUJETO: SustantivoFlexion con caso NOMINATIVO
 */
@Component
public class FraseSoloSustantivoNominativo extends EstructuraFrase {

    public static final String IDENTIFICADOR = "SOLO_SUSTANTIVO_NOMINATIVO";
    public static final String NOMBRE_MOSTRAR = "Sustantivo (NOM)";

    @Autowired
    private NumeralService numeralService;

    public FraseSoloSustantivoNominativo() {
        super();
    }

    @PostConstruct
    public void configurarEstructura() {

        agregarApoyo(ElementoApoyo.builder()
                .nombre("NUMERO")
                .generadorObjeto(frase -> {
                    SlotPalabra slotCD = frase.getSlotPorNombre("SUSTANTIVO");
                    if (slotCD == null || !slotCD.estaAsignado()) return null;
                    SustantivoFlexion sf = (SustantivoFlexion) slotCD.getPalabraAsignada();
                    return numeralService.getNumeral(sf);
                })
                .extractorDeEspanol(PalabraFlexion::getSignificado)   // ES_SL fila1: "uno"
                .extractorAEsloveno(PalabraFlexion::getFlexion)       // ES_SL fila2: "en"
                .extractorDeEsloveno(PalabraFlexion::getFlexion)      // SL_ES fila1: "en"
                .extractorAEspanol(PalabraFlexion::getSignificado)    // SL_ES fila2: "uno"
                .build());
        // Slot: Sustantivo nominativo
        agregarSlot(SlotPalabra.builder()
            .nombre("SUSTANTIVO")
            .matcher(p -> p instanceof SustantivoFlexion sf &&
                         sf.getCaso() == Caso.NOMINATIVO &&
                         sf.getSustantivoBase() != null)
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
        return Set.of(Caso.NOMINATIVO);
    }

    @Override
    public Set<FormaVerbal> getFormasVerbalesUsadas() {
        return Set.of();
    }
}

