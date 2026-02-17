package com.bcadaval.esloveno.structures.frases;

import com.bcadaval.esloveno.beans.enums.CaracteristicaGramatical;
import com.bcadaval.esloveno.beans.enums.FormaVerbal;
import com.bcadaval.esloveno.beans.palabra.VerboFlexion;
import com.bcadaval.esloveno.structures.CriterioBusqueda;
import com.bcadaval.esloveno.structures.ElementoFrase;
import com.bcadaval.esloveno.structures.EstructuraFrase;
import com.bcadaval.esloveno.structures.ExcluirDeFrases;
import com.bcadaval.esloveno.structures.extractores.ExtraccionSlotEstandar;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.stereotype.Component;

/**
 * Estructura de frase: Verbo en Infinitivo
 * <p>
 * Ejemplo: "delati" (trabajar), "gledati" (mirar)
 * <p>
 * El infinitivo es la forma básica del verbo que se aprende primero.
 * En esloveno termina típicamente en "-ti" o "-či".
 * <p>
 * Usos importantes:
 * - Diccionarios y vocabulario básico
 * - Con verbos modales: moram delati (debo trabajar)
 * - En construcciones infinitivas: rad bi jedel (me gustaría comer)
 * <p>
 * Ideal para nivel A1 para aprender los verbos base.
 * <p>
 * Elementos:
 * 1. VERBO (slot): VerboFlexion con forma INFINITIVE
 */
@Component
@ExcluirDeFrases(razon = "Estructura excluida temporalmente")
public class FraseSoloVerboInfinitivo extends EstructuraFrase {

    @Getter
    private final String identificador = "SOLO_VERBO_INFINITIVO";
    @Getter
    private final String nombreMostrar = "Verbo (infinitivo)";

    public FraseSoloVerboInfinitivo() {
        super();
    }

    @PostConstruct
    public void configurarEstructura() {
        // Definir slot de verbo en infinitivo
        ElementoFrase<VerboFlexion> verbo = ElementoFrase.<VerboFlexion>builder()
                .nombre("VERBO")
                .criterio(CriterioBusqueda.de(VerboFlexion.class)
                        .con(CaracteristicaGramatical.FORMA_VERBAL, FormaVerbal.INFINITIVE)
                        .build())
                .extractor(ExtraccionSlotEstandar.get())
                .extractorDeEspanol(p ->
                        String.format("(%s) %s", p.getVerboBase().getAspecto().getEmoji(), p.getSignificado()))
                .extractorAEspanol(p ->
                        String.format("(%s) %s", p.getVerboBase().getAspecto().getEmoji(), p.getSignificado()))
                .build();

        // Agregar elemento (solo el verbo, sin apoyo)
        agregarElemento(verbo);
    }
}
