package com.bcadaval.esloveno.structures.frases;

import java.util.Set;

import com.bcadaval.esloveno.beans.base.PalabraFlexion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bcadaval.esloveno.beans.enums.Caso;
import com.bcadaval.esloveno.beans.enums.FormaVerbal;
import com.bcadaval.esloveno.beans.enums.Transitividad;
import com.bcadaval.esloveno.beans.palabra.SustantivoFlexion;
import com.bcadaval.esloveno.beans.palabra.VerboFlexion;
import com.bcadaval.esloveno.services.palabra.NumeralService;
import com.bcadaval.esloveno.services.palabra.PronombreService;
import com.bcadaval.esloveno.structures.ElementoApoyo;
import com.bcadaval.esloveno.structures.EstructuraFrase;
import com.bcadaval.esloveno.structures.SlotPalabra;

import jakarta.annotation.PostConstruct;

/**
 * Estructura de frase: Pronombre + Verbo transitivo + Número + Sustantivo Acusativo
 *
 * Ejemplo: "jaz vidim 1 knjigo" (yo veo 1 libro)
 *
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

    public FraseVerboTransitivoAcusativo() {
        super();
    }

    @PostConstruct
    public void configurarEstructura() {
        // 1. PRONOMBRE (apoyo) - depende del verbo, accede por nombre "VERBO"
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

        // 2. VERBO (slot principal) - solo verbos en presente
        agregarSlot(SlotPalabra.builder()
            .nombre("VERBO")
            .matcher(p -> p instanceof VerboFlexion vf &&
                         vf.getFormaVerbal() == FormaVerbal.PRESENT &&
                         vf.getVerboBase() != null &&
                         vf.getVerboBase().getTransitividad() == Transitividad.TRANSITIVO)
            .extractorDeEspanol(PalabraFlexion::getSignificado)
            .extractorAEsloveno(PalabraFlexion::getAcentuado)
            .extractorDeEsloveno(PalabraFlexion::getFlexion)
            .extractorAEspanol(PalabraFlexion::getSignificado)
            .build());

        // 3. NUMERO (apoyo - numeral que concuerda con el sustantivo)
        agregarApoyo(ElementoApoyo.builder()
            .nombre("NUMERO")
            .generadorObjeto(frase -> {
                SlotPalabra slotCD = frase.getSlotPorNombre("CD");
                if (slotCD == null || !slotCD.estaAsignado()) return null;
                SustantivoFlexion sf = (SustantivoFlexion) slotCD.getPalabraAsignada();
                return numeralService.getNumeral(sf);
            })
            .extractorDeEspanol(PalabraFlexion::getSignificado)   // ES_SL fila1: "uno"
            .extractorAEsloveno(PalabraFlexion::getFlexion)       // ES_SL fila2: "en"
            .extractorDeEsloveno(PalabraFlexion::getFlexion)      // SL_ES fila1: "en"
            .extractorAEspanol(PalabraFlexion::getSignificado)    // SL_ES fila2: "uno"
            .build());

        // 4. SUSTANTIVO ACUSATIVO (slot principal)
        agregarSlot(SlotPalabra.builder()
            .nombre("CD")
            .matcher(p -> p instanceof SustantivoFlexion sf &&
                         sf.getCaso() == Caso.ACUSATIVO &&
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
        return Set.of(Caso.ACUSATIVO);
    }

    @Override
    public Set<FormaVerbal> getFormasVerbalesUsadas() {
        return Set.of(FormaVerbal.PRESENT);
    }
}
